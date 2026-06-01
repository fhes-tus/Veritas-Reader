cmake_minimum_required(VERSION 3.22.1)
project("piper_jni")

add_library(piper_jni SHARED piper_jni.cpp)

find_library(log-lib log)

target_link_libraries(piper_jni ${log-lib})
