#pragma once
#include <drogon/HttpController.h>

class AuthCtrl : public drogon::HttpController<AuthCtrl> {
public:
    METHOD_LIST_BEGIN
    ADD_METHOD_TO(AuthCtrl::login, "/api/auth/login", drogon::Post, drogon::Options);
    ADD_METHOD_TO(AuthCtrl::registerUser, "/api/auth/register", drogon::Post, drogon::Options);
    ADD_METHOD_TO(AuthCtrl::me, "/api/auth/me", drogon::Get, drogon::Options);
    METHOD_LIST_END

    void login(const drogon::HttpRequestPtr &req,
               std::function<void(const drogon::HttpResponsePtr &)> &&callback);

    void registerUser(const drogon::HttpRequestPtr &req,
                     std::function<void(const drogon::HttpResponsePtr &)> &&callback);

    void me(const drogon::HttpRequestPtr &req,
            std::function<void(const drogon::HttpResponsePtr &)> &&callback);
};
