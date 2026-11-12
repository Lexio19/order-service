
package com.example.orders.service;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.exception.OrderNotFoundException;
import com.example.orders.messaging.OrderProducer;
import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import com.example.orders.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProducer orderProducer;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {

        request = new CreateOrderRequest();

        request.setCustomerName("Alex");
        request.setProductName("Keyboard");
        request.setQuantity(2);
        request.setUnitPrice(new BigDecimal("49.90"));
    }

    @Test
    void shouldCreateOrderSuccessfully() {

        Order savedOrder = new Order(
                "Alex",
                "Keyboard",
                2,
                new BigDecimal("49.90")
        );

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        Order result = orderService.createOrder(request);

        assertNotNull(result);

        assertEquals(OrderStatus.PENDING, result.getStatus());

        assertEquals(
                new BigDecimal("99.80"),
                result.getTotalPrice()
        );

        ArgumentCaptor<Order> captor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        assertEquals(
                "Alex",
                captor.getValue().getCustomerName()
        );

        verify(orderProducer).sendOrderCreated(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.findById(99L)
        );
    }

    @Test
    void shouldCancelPendingOrder() {

        Order order = new Order(
                "Alex",
                "Keyboard",
                2,
                new BigDecimal("49.90")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        Order result = orderService.cancelOrder(1L);

        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }
}
