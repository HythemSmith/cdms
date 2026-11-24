package com.example.cdms.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/mock/shopify")
public class MockShopifyController {
    
    @GetMapping("/products")
    public Map<String, Object> getProducts() {

        Map<String, Object> variant1 = Map.of(
                "sku", "SKU-001",
                "price", "199000",
                "inventory_quantity", 50);

        Map<String, Object> product1 = Map.of( 
                "id", "gid://shopify/Product/1001",
                "title", "Ao thun nam",
                "variants", Map.of("nodes", List.of(variant1)));

        return Map.of(
                "data", Map.of(
                        "products", Map.of(
                                "nodes", List.of(product1) 
                        )));
    }

}
