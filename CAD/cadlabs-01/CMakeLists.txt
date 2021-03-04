cmake_minimum_required(VERSION 3.0)
project(cadlabs-01)

set(CMAKE_CXX_STANDARD 14)

set (CMAKE_CXX_FLAGS "-pthread")

set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -ftree-vectorize -fopt-info-vec-optimized")

message ("-- Compilation flags: ${CMAKE_CXX_FLAGS}")

include_directories(include)
add_subdirectory (src)

add_executable(array_add src/array_add_main.cpp)
add_executable(array_add_par src/array_add_par_main.cpp)

##############
# Google Tests
##############

find_package(GTest)

if(GTest_FOUND)
    enable_testing()

    add_subdirectory (test)
    include_directories(test)

    add_executable(cadlabs_tests ${CAD_SOURCES} ${CAD_TEST_SOURCES})
    include_directories(${GTEST_INCLUDE_DIRS})
    target_link_libraries(cadlabs_tests ${GTEST_BOTH_LIBRARIES})

endif(GTest_FOUND)