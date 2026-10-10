#include "ProductCtrl.h"
#include <json/json.h>

void ProductCtrl::getProducts(const drogon::HttpRequestPtr &req,
                              std::function<void(const drogon::HttpResponsePtr &)> &&callback) {
    if (req->method() == drogon::Options) {
        auto resp = drogon::HttpResponse::newHttpResponse();
        resp->setStatusCode(drogon::k204NoContent);
        callback(resp);
        return;
    }

    Json::Value ret;
    ret["success"] = true;
    Json::Value products(Json::arrayValue);

    Json::Value p1;
    p1["id"] = 1;
    p1["title"] = "Noise Cancelling Wireless Headphones";
    p1["category"] = "Electronics";
    p1["price"] = 3499.00;
    p1["discountPrice"] = 2799.00;
    p1["sellerName"] = "Namitha Electronics Hub";
    p1["rating"] = 4.8;
    p1["reviewCount"] = 24;
    p1["imageUrl"] = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600";
    products.append(p1);

    ret["products"] = products;
    ret["count"] = 1;

    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}

void ProductCtrl::getProductById(const drogon::HttpRequestPtr &req,
                                 std::function<void(const drogon::HttpResponsePtr &)> &&callback,
                                 int id) {
    Json::Value ret;
    ret["success"] = true;
    Json::Value p;
    p["id"] = id;
    p["title"] = "Noise Cancelling Wireless Headphones";
    p["category"] = "Electronics";
    p["price"] = 3499.00;
    p["discountPrice"] = 2799.00;
    p["sellerName"] = "Namitha Electronics Hub";
    p["rating"] = 4.8;
    p["stock"] = 45;
    p["imageUrl"] = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600";
    p["description"] = "Premium over-ear wireless headphones with active noise cancellation.";
    ret["product"] = p;

    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}

void ProductCtrl::createProduct(const drogon::HttpRequestPtr &req,
                                std::function<void(const drogon::HttpResponsePtr &)> &&callback) {
    Json::Value ret;
    ret["success"] = true;
    ret["message"] = "Product created successfully in NAMITHAMART";
    auto resp = drogon::HttpResponse::newHttpJsonResponse(ret);
    callback(resp);
}
