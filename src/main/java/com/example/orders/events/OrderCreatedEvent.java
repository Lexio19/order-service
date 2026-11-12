
package com.example.orders.events;

public class OrderCreatedEvent {

    private String eventId;
    private Long orderId;
    private String productName;
    private Integer quantity;
    private String occurredAt;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(
            String eventId,
            Long orderId,
            String productName,
            Integer quantity,
            String occurredAt) {

        this.eventId = eventId;
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }
}
