
package com.example.orders.repository;

import com.example.orders.model.OrderStatus;
import com.example.orders.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    // Buscar pedidos de un cliente sin distinguir mayúsculas
    List<Order> findByCustomerNameIgnoreCase(String customerName);

    // Buscar pedidos por estado
    List<Order> findByStatus(OrderStatus status);

    // Buscar pedidos cuyo nombre de producto contenga un texto
    List<Order> findByProductNameContainingIgnoreCase(String productName);

    @Query("SELECT o FROM Order o WHERE o.quantity >= :minimumQuantity")
    List<Order> findOrdersWithMinimumQuantity(
            @Param("minimumQuantity") Integer minimumQuantity
    );
}
