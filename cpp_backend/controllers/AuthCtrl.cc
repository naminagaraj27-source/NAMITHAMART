#include "AuthCtrl.h"
#include <json/json.h>

void AuthCtrl::login(const drogon::HttpRequestPtr &req,
                     std::function<void(const drogon::HttpResponsePtr &)> &&callback) {
    if (req->method() == drogon::Options) {
        auto resp = drogon::HttpResponse::newHttpResponse();
        resp->setStatusCode(drogon::k204NoContent);
        callback(resp);
        return;
    }

    auto json = req->getJsonObject();
    Json::Value ret;

    if (!json || !json->isMember("email") || !json->isMember("password")) {
        ret["success"] = false;
        ret["message"] = "Email and password are required";
        auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
        resp->setStatusCode(drogon::k400BadRequest);
        callback(resp);
        return;
    }

    std::string email = (*json)["email"].asString();
    std::string password = (*json)["password"].asString();

    // Default mock response or database query
    ret["success"] = true;
    ret["message"] = "Login successful. Welcome to NAMITHAMART!";
    ret["token"] = "NM-TOKEN-CPP-" + email;
    
    Json::Value user;
    user["name"] = email.find("admin") != std::string::npos ? "NAMITHAMART Admin" : "Namitha Customer";
    user["email"] = email;
    user["role"] = email.find("admin") != std::string::npos ? "admin" : (email.find("seller") != std::string::npos ? "seller" : "buyer");
    ret["user"] = user;

    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}

void AuthCtrl::registerUser(const drogon::HttpRequestPtr &req,
                           std::function<void(const drogon::HttpResponsePtr &)> &&callback) {
    auto json = req->getJsonObject();
    Json::Value ret;
    ret["success"] = true;
    ret["message"] = "Registered successfully on NAMITHAMART!";
    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}

void AuthCtrl::me(const drogon::HttpRequestPtr &req,
                 std::function<void(const drogon::HttpResponsePtr &)> &&callback) {
    Json::Value ret;
    ret["success"] = true;
    Json::Value user;
    user["id"] = 1;
    user["name"] = "NAMITHAMART User";
    user["role"] = "buyer";
    ret["user"] = user;
    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}
