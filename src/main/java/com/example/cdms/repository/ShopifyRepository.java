package com.example.cdms.repository;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.cdms.dto.ShopifyProductDto;

@Component 
public class ShopifyRepository {
    
    private final RestClient client;

    public ShopifyRepository(RestClient.Builder clientBuilder) {
        this.client = clientBuilder.baseUrl("http://localhost:8080").build();
    }

    public List<ShopifyProductDto> fetchProducts() {
        ShopifyResponse response = client.get()
                                         .uri("/mock/shopify/products")
                                         .retrieve()
                                         .body(ShopifyResponse.class);

        return  response.data().products().nodes();
    }

}

record ShopifyResponse(Data data) {}
record Data(Products products) {}
record Products(List<ShopifyProductDto> nodes) {}
