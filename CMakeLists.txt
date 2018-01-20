cmake_minimum_required(VERSION 3.8)

set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_LIST_DIR}/KotlinCMakeModule)

project(cmake_gtk_example Kotlin)

konanc_library_search(gtk gtk/gtk.h)
cinterop(
        NAME gtk3
        DEF_FILE gtk3.def
)

konanc_executable(
        NAME GtkExample
        SOURCES common src
        LIBRARIES gtk3
)

konanc_executable(
        NAME VictorExample
        SOURCES common victorExample
        LIBRARIES gtk3
)
