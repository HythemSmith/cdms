package com.example.cdms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mock/error")
public class MockErrorController {

    @GetMapping("/shopify/invalid-variant")
    public Map<String, Object> invalidVariant() {
        Map<String, Object> product = Map.of(
                "id", "gid://shopify/Product/invalid",
                "title", "Product with no variant",
                "variants", Map.of("nodes", List.of())
        );
        return Map.of("data", Map.of("products", Map.of("nodes", List.of(product))));
    }

    @GetMapping("/shopify/null-fields")
    public Map<String, Object> nullFields() {
        Map<String, Object> variant = Map.of("sku", null, "price", null, "inventory_quantity", null);
        Map<String, Object> product = Map.of(
                "id", "gid://shopify/Product/nulls",
                "title", null,
                "variants", Map.of("nodes", List.of(variant))
        );
        return Map.of("data", Map.of("products", Map.of("nodes", List.of(product))));
    }

    @PostMapping("/vietful/invalid")
    public String pushInvalidVietful(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("sku") || !body.containsKey("partnerSKU")) {
            throw new IllegalArgumentException("Missing required fields: sku, partnerSKU");
        }
        return "ok";
    }
}
