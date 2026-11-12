
package com.example.orders.service;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.exception.InvalidOrderStateException;
import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.messaging.OrderProducer;
import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import com.example.orders.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public OrderService(
            OrderRepository orderRepository,
            OrderProducer orderProducer) {

        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        Order order = new Order(
                request.getCustomerName(),
                request.getProductName(),
                request.getQuantity(),
                request.getUnitPrice()
        );

        Order savedOrder = orderRepository.save(order);

        orderProducer.sendOrderCreated(savedOrder);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {

        return orderRepository.findAll();
    }

    @Transactional
    public Order cancelOrder(Long id) {

        Order order = findById(id);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                    "Only pending orders can be cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerName(String customerName) {

        return orderRepository.findByCustomerNameIgnoreCase(
                customerName
        );
    }
}
