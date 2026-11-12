
package com.example.orders.dto;

import com.example.orders.model.Order;

import java.math.BigDecimal;

public class OrderResponse {

    private final Long id;
    private final String customerName;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal totalPrice;
    private final String status;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.customerName = order.getCustomerName();
        this.productName = order.getProductName();
        this.quantity = order.getQuantity();
        this.unitPrice = order.getUnitPrice();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus().name();
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }
}
