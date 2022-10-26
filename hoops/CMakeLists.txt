# log:  put hoops into external folder using Env-Variable: SOURCE_RMC_MOBILE_COMMON

#
cmake_minimum_required(VERSION 3.4.1)

# Show verbose
set(CMAKE_VERBOSE_MAKEFILE ON)


#
# Config Enviroment
#
project(hoopsfacade)

#
# Prerequisite detect
# as RMS required, we need to set a environment value to refer the common folder
#
#if(NOT DEFINED ENV{SOURCE_RMC_MOBILE_COMMON})
#    # SOURCE_RMC_MOBILE_COMMON
#    message(FATAL_ERROR "not defined environment variable:SOURCE_RMC_MOBILE_COMMON")
#endif()

# ref env_variable
set(EXTERNAL_PATH $ENV{SOURCE_ANDROID_3RD_LIBS}/hoops)
message("EXTERNAL_PATH: ${EXTERNAL_PATH}")

#
set(LOCAL_PATH ${CMAKE_CURRENT_LIST_DIR}/src/main/cpp)
message("LOCAL_PATH: ${LOCAL_PATH}")

set(HOOPS_LIB ${EXTERNAL_PATH}/libs/${ANDROID_ABI})
message("HOOPS_LIB:  ${HOOPS_LIB}")

# Specifies a path to native header files.
include_directories(
        ${EXTERNAL_PATH}/include
        ${EXTERNAL_PATH}/shared)

# Specifies Other .cpp files that need to be compiled
aux_source_directory(${LOCAL_PATH} SRC_LIST)
aux_source_directory(${EXTERNAL_PATH}/shared SHARED_SRC_LIST)

# Merge other src files into SRC_LIST
list(APPEND SRC_LIST ${SHARED_SRC_LIST})


#
#  BEGIN
#
add_library(${PROJECT_NAME} SHARED ${SRC_LIST})


# system libs
find_library(log-lib log)
find_library(android-lib android)

#
# hoops need
#
add_library(hps_core SHARED IMPORTED)
set_target_properties(
        hps_core PROPERTIES IMPORTED_LOCATION
        ${HOOPS_LIB}/libhps_core.so)

add_library(hps_sprk SHARED IMPORTED)
set_target_properties(hps_sprk PROPERTIES IMPORTED_LOCATION
        ${HOOPS_LIB}/libhps_sprk.so)

add_library(hps_sprk_ops SHARED IMPORTED)
set_target_properties(hps_sprk_ops PROPERTIES IMPORTED_LOCATION
        ${HOOPS_LIB}/libhps_sprk_ops.so)

add_library(gnustl_shared SHARED IMPORTED)
set_target_properties(gnustl_shared PROPERTIES IMPORTED_LOCATION
        ${HOOPS_LIB}/libgnustl_shared.so)

add_library(hps_sprk_exchange SHARED IMPORTED)
set_target_properties(hps_sprk_exchange PROPERTIES IMPORTED_LOCATION
        ${HOOPS_LIB}/libhps_sprk_exchange.so)

add_library(A3DLIBS SHARED IMPORTED)
set_target_properties(A3DLIBS PROPERTIES IMPORTED_LOCATION
        ${HOOPS_LIB}/libA3DLIBS.so)


set(CMAKE_SHARED_LINKER_FLAGS "-v")

#
target_link_libraries( # Specifies the target library.
        ${PROJECT_NAME}

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${android-lib}
        # hoops needed
        hps_core
        hps_sprk
        hps_sprk_ops
        gnustl_shared
        hps_sprk_exchange
        A3DLIBS)

