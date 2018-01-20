/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import kotlinx.cinterop.*
import gtk3.*

class Application(id: String) {
    val app = gtk_application_new(id, G_APPLICATION_FLAGS_NONE)!!

    fun onActivate(callback: CPointer<CFunction<(CPointer<GtkApplication>?, gpointer?) -> Unit>>) {
        g_signal_connect(app, "activate", callback)
    }

    fun run(args: Array<String>): Int {
        val status = memScoped {
            g_application_run(app.reinterpret(),
                    args.size, args.map { it.cstr.getPointer(memScope) }.toCValues())
        }
        g_object_unref(app)
        return status
    }
}

abstract class Widget {
    abstract val widgetPtr: CPointer<GtkWidget>
}

abstract class Container: Widget() {
    fun add(widget: Widget) = gtk_container_add(widgetPtr.reinterpret(), widget.widgetPtr)
}

class Window(app: CValuesRef<GtkApplication>): Container() {
    override val widgetPtr = gtk_application_window_new(app)!!
    private val window get() = widgetPtr.reinterpret<GtkWindow>()

    var title: String
        get() = ""
        set(value) { gtk_window_set_title(window, value) }

    fun setDefaultSize(width: Int, height: Int) = gtk_window_set_default_size(window, width, height)

    fun showAll() = gtk_widget_show_all(widgetPtr)
}

class ButtonBox(orientation: GtkOrientation): Container() {
    override val widgetPtr = gtk_button_box_new(orientation)!!
}

fun signalHandler(sender: CPointer<*>?, data: COpaquePointer?) {
    val button = StableObjPtr.fromValue(data!!).get() as Button
    button.clicked()
}

typealias SignalHandler = () -> Unit
class Signal {
    private var handlers = emptyList<SignalHandler>()

    operator fun plusAssign(handler: SignalHandler) { handlers += handler }
    operator fun minusAssign(handler: SignalHandler) { handlers -= handler }

    operator fun invoke() {
        for (handler in handlers) {
            try {
                handler()
            } catch (e: Throwable) {
            }
        }
    }
}

class Button(label: String): Widget() {
    override val widgetPtr = gtk_button_new_with_label(label)!!

    init {
        g_signal_connect(widgetPtr, "clicked", staticCFunction(::signalHandler), StableObjPtr.create(this).value)
    }

    val clicked = Signal()
}

fun CPointer<GtkApplication>.window(builder: Window.() -> Unit) {
    Window(reinterpret()).apply(builder).showAll()
}
fun Container.buttonBox(builder: ButtonBox.() -> Unit) = add(ButtonBox(GtkOrientation.GTK_ORIENTATION_HORIZONTAL).apply(builder))
fun ButtonBox.button(label: String, builder: Button.() -> Unit) = add(Button(label).apply(builder))

fun gtkVictorMain(args: Array<String>): Int {
    val app = Application("org.gtk.example")!!
    app.onActivate(staticCFunction { app, _ ->
        app!!.window {
            title = "Kotlin"
            setDefaultSize(200, 200)

            buttonBox {
                button("Hello World!") {
                    clicked += {
                        println("Hello Kotlin!")
                        gtk_widget_destroy(this@window.widgetPtr)
                    }
                }
            }
        }
    })
    return app.run(args)
}

fun main(args: Array<String>) {
    gtkVictorMain(args)
}
