cmake_minimum_required(VERSION 3.20)
project(NAMITHAMART CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find Drogon and SQLite3 packages
find_package(Drogon CONFIG REQUIRED)
find_package(SQLite3 REQUIRED)

# Include directories
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/controllers)

# Source files
set(SOURCES
    main.cpp
    controllers/AuthCtrl.cc
    controllers/ProductCtrl.cc
    controllers/OrderCtrl.cc
)

add_executable(namithamart_server ${SOURCES})

target_link_libraries(namithamart_server
    PRIVATE
    Drogon::Drogon
    SQLite::SQLite3
)
