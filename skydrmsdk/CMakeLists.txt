# Show verbose
set(CMAKE_VERBOSE_MAKEFILE ON)

# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

#
# Config V
#
set(LOCAL_PATH .)
# for common
set(LOCAL_SRC_PATH $ENV{SOURCE_RMC_MOBILE_COMMON}/nxl)
set(NXL_COMMON $ENV{SOURCE_RMC_MOBILE_COMMON})
set(3RD_COMMON $ENV{SOURCE_ANDROID_3RD_LIBS})

message("LOCAL_SRC_PATH:${LOCAL_SRC_PATH}")
message("NXL_COMMON:${NXL_COMMON}")

add_definitions(-DANDROID_ENV_FIPS_MODE)

# Specifies a path to native header files.
include_directories(
        ${NXL_COMMON}
        ${NXL_COMMON}/boost_1_61_0
        ${NXL_COMMON}/crypto
        ${NXL_COMMON}/crypto/cryptlite
        ${NXL_COMMON}/nxl
        ${NXL_COMMON}/nxl/include
        ${3RD_COMMON}/openssl_fips/include
        ${3RD_COMMON}/openssl_fips/shared)

# Specifies Other .cpp files that need to be compiled
aux_source_directory(./src/main/cpp SRC_LIST)
aux_source_directory(${NXL_COMMON}/nxl/src NXL_SRC_LIST)

# Merge other src files into SRC_LIST
list(APPEND SRC_LIST ${NXL_SRC_LIST})

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.
add_library( # Specifies the name of the library.
        nxlformat

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SRC_LIST})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${3RD_COMMON}/openssl_fips/libs/${ANDROID_ABI}")
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        nxlformat

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        openssl_fips_wrapper)

