package com.example.kafka.springbootkafka.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics = "test01", groupId = "testgroup")
    public void consumeMessage(String message){
        System.out.println("Consumer message => " + message);
    }
}
