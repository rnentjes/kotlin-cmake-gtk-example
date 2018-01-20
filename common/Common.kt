import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import gtk3.*

// Note that all callback parameters must be primitive types or nullable C pointers.
fun <F : CFunction<*>> g_signal_connect(obj: CPointer<*>, actionName: String,
                                        action: CPointer<F>, data: gpointer? = null, connect_flags: Int = 0) {
    g_signal_connect_data(obj.reinterpret(), actionName, action.reinterpret(),
            data = data, destroy_data = null, connect_flags = connect_flags)

}