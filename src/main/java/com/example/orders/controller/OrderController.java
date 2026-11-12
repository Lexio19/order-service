
package com.example.orders.controller;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Crear pedido
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new OrderResponse(order));
    }

    // Consultar todos los pedidos
    @GetMapping
    public List<OrderResponse> findAll() {

        return orderService.findAll()
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    // Consultar un pedido
    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {

        return new OrderResponse(orderService.findById(id));
    }

    // Cancelar un pedido
    @PatchMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {

        return new OrderResponse(orderService.cancelOrder(id));
    }

    @GetMapping("/search")
    public List<OrderResponse> searchByCustomer(
            @RequestParam String customerName) {

        return orderService.findByCustomerName(customerName)
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }
}
