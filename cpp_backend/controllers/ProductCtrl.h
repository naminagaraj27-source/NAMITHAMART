#pragma once
#include <drogon/HttpController.h>

class ProductCtrl : public drogon::HttpController<ProductCtrl> {
public:
    METHOD_LIST_BEGIN
    ADD_METHOD_TO(ProductCtrl::getProducts, "/api/products", drogon::Get, drogon::Options);
    ADD_METHOD_TO(ProductCtrl::getProductById, "/api/products/{id}", drogon::Get, drogon::Options);
    ADD_METHOD_TO(ProductCtrl::createProduct, "/api/products", drogon::Post, drogon::Options);
    METHOD_LIST_END

    void getProducts(const drogon::HttpRequestPtr &req,
                     std::function<void(const drogon::HttpResponsePtr &)> &&callback);

    void getProductById(const drogon::HttpRequestPtr &req,
                        std::function<void(const drogon::HttpResponsePtr &)> &&callback,
                        int id);

    void createProduct(const drogon::HttpRequestPtr &req,
                       std::function<void(const drogon::HttpResponsePtr &)> &&callback);
};
