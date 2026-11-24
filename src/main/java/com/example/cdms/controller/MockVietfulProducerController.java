package com.example.cdms.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cdms.dto.VietfulProductEvent;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping("/mock/vietful")
public class MockVietfulProducerController {
    KafkaTemplate<String, Object> kafkaTemplate;

    public MockVietfulProducerController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/push")
    public String pushEvent(@Valid @RequestBody VietfulProductEvent event) {
        kafkaTemplate.send("vietful-events", event.sku(), event);
        return "da ban event";
    
    }
}
