cmake_minimum_required(VERSION 3.8)
project(fastchunk LANGUAGES C CXX)

find_package(JNI REQUIRED)

add_library(fastchunk SHARED fastchunk.cpp jni_fastchunk.cpp)
target_include_directories(fastchunk PRIVATE ${JNI_INCLUDE_DIRS} ${CMAKE_CURRENT_SOURCE_DIR})
set_target_properties(fastchunk PROPERTIES PREFIX "" )
