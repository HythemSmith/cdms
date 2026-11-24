package com.example.cdms.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.cdms.dto.VietfulProductEvent;
import com.example.cdms.repository.TenantProductRepository;

@Service 
public class VietfulConsumer {
    private final TenantProductRepository tenantProductRepository;

    public VietfulConsumer(TenantProductRepository tenantProductRepository) {
        this.tenantProductRepository = tenantProductRepository;
    }


    @KafkaListener(topics="vietful-events", groupId = "cdms-worker")
    public void consume(VietfulProductEvent event) {
        System.out.println("Đã nhận từ Kafka: " + event.sku() + " | Số lượng: " + event.qty());
        tenantProductRepository.upsertFromVietful(1L, event);
        System.out.println("Đã lưu vào DB SKU: " + event.sku());
    }
}
