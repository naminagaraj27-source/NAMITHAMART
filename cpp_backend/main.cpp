/**
 * NAMITHAMART — Multi-Seller Online Shopping Platform
 * C++20 Drogon Framework Server Entrypoint
 */

#include <drogon/drogon.h>
#include <iostream>

int main() {
    std::cout << "==========================================================" << std::endl;
    std::cout << "               NAMITHAMART E-COMMERCE SERVER             " << std::endl;
    std::cout << "                 C++20 Drogon Framework                  " << std::endl;
    std::cout << "==========================================================" << std::endl;

    // Load Drogon configuration or configure listeners
    drogon::app()
        .addListener("0.0.0.0", 8080)
        .setDocumentRoot("./public")
        .loadConfigFile("./config.json");

    std::cout << "[Server] NAMITHAMART listening on http://localhost:8080" << std::endl;

    drogon::app().run();
    return 0;
}
