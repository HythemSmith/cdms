package com.example.cdms.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cdms.service.ShopifySyncService;

import jakarta.validation.constraints.Min;

@RestController 
@RequestMapping("/sync")
public class SyncController {
    private final ShopifySyncService shopifySyncService;

    public SyncController(ShopifySyncService syncService) {
        this.shopifySyncService = syncService;
    }

    @PostMapping ("/shopify")
    public String triggerShopifySync(@RequestParam  @Min(1) long tenant,
                                     @RequestParam(required = false) Long worker) {
        shopifySyncService.syncData(tenant, worker);
        return "Đã kích hoạt đồng bộ thành công!";
    }
    
}
