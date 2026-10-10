#include "OrderCtrl.h"
#include <json/json.h>

void OrderCtrl::checkout(const drogon::HttpRequestPtr &req,
                         std::function<void(const drogon::HttpResponsePtr &)> &&callback) {
    Json::Value ret;
    ret["success"] = true;
    ret["message"] = "Order placed successfully on NAMITHAMART!";
    ret["orderNumber"] = "NM-CPP-99482";
    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}

void OrderCtrl::getOrders(const drogon::HttpRequestPtr &req,
                          std::function<void(const drogon::HttpResponsePtr &)> &&callback) {
    Json::Value ret;
    ret["success"] = true;
    Json::Value orders(Json::arrayValue);
    ret["orders"] = orders;
    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}
