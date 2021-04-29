package com.example.kafka.springbootkafka.controller;

import com.example.kafka.springbootkafka.service.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
    @Autowired
    private KafkaProducer producer;

    @PostMapping
    public void sendMessage(@RequestParam("message") String message){
        this.producer.sendMessage(message);
    }
}
