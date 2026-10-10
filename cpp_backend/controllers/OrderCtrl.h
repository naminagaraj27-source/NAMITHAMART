#pragma once
#include <drogon/HttpController.h>

class OrderCtrl : public drogon::HttpController<OrderCtrl> {
public:
    METHOD_LIST_BEGIN
    ADD_METHOD_TO(OrderCtrl::checkout, "/api/orders/checkout", drogon::Post, drogon::Options);
    ADD_METHOD_TO(OrderCtrl::getOrders, "/api/orders", drogon::Get, drogon::Options);
    METHOD_LIST_END

    void checkout(const drogon::HttpRequestPtr &req,
                  std::function<void(const drogon::HttpResponsePtr &)> &&callback);

    void getOrders(const drogon::HttpRequestPtr &req,
                   std::function<void(const drogon::HttpResponsePtr &)> &&callback);
};
