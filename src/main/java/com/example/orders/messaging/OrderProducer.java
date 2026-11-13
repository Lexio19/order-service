
package com.example.orders.messaging;
import com.example.orders.model.Order;
import com.example.orders.events.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderProducer {

    private static final String TOPIC = "orders.created";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendOrderCreated(Order order) {

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getId(),
                order.getProductName(),
                order.getQuantity(),
                Instant.now().toString()
        );

        try {

            String message = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TOPIC,
                    String.valueOf(order.getId()),
                    message
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Error serializing OrderCreated event",
                    exception
            );
        }
    }
}
