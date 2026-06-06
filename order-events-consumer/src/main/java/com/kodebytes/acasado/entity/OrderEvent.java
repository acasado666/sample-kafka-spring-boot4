package com.kodebytes.acasado.entity;


import com.kodebytes.acasado.domain.OrderEventType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Table(name = "order_event")
public class OrderEvent {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "order_event_id")
        private Integer orderEventId;
        
        @NotNull
        @Enumerated(EnumType.STRING)
        @Column(name = "event_type", nullable = false)
        OrderEventType eventType;

        @OneToOne(mappedBy = "orderEvent", cascade = {CascadeType.ALL})
        Phone phone;

        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
                LocalDateTime now = LocalDateTime.now();
                createdAt = now;
                updatedAt = now;
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = LocalDateTime.now();
        }

        public OrderEvent() {
        }

        public OrderEvent(Integer orderEventId, OrderEventType eventType, Phone phone) {
                this.orderEventId = orderEventId;
                this.eventType = eventType;
                this.phone = phone;
        }

        public Integer getOrderEventId() {
                return orderEventId;
        }

        public void setOrderEventId(Integer orderEventId) {
                this.orderEventId = orderEventId;
        }

        public OrderEventType getEventType() {
                return eventType;
        }

        public void setEventType(OrderEventType eventType) {
                this.eventType = eventType;
        }

        public Phone getPhone() {
                    return phone;
        }

        public void setPhone(Phone phone) {
                this.phone = phone;
                if (phone != null && phone.getOrderEvent() != this) {
                        phone.setOrderEvent(this);
                }
        }

        public LocalDateTime getCreatedAt() {
                return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
                return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
                this.updatedAt = updatedAt;
        }

        @Override
        public String toString() {
                return "OrderEvent{" +
                        "orderEventId=" + orderEventId +
                        ", eventType=" + eventType +
                        ", phone=" + phone +
                        ", createdAt=" + createdAt +
                        ", updatedAt=" + updatedAt +
                        '}';
        }
}
