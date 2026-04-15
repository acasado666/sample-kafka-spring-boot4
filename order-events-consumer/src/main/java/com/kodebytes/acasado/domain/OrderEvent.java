package com.kodebytes.acasado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Entity
@Getter
@Setter
public class OrderEvent {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long orderId;

        @Enumerated(EnumType.STRING)
        @NotNull
        OrderEventType eventType;

        @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL})
        Phone phone;

        @Column(nullable = false, updatable = false)
        private Instant createdAt;

        @Column(nullable = false)
        private Instant updatedAt;

        @PrePersist
        protected void onCreate() {
                createdAt = Instant.now();
                updatedAt = Instant.now();
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = Instant.now();
        }

        public OrderEvent() {
        }

        public OrderEvent(Long orderId, OrderEventType eventType, Phone phone) {
                this.orderId = orderId;
                this.eventType = eventType;
                this.phone = phone;
        }

        @Override
        public String toString() {
                return "OrderEvent{" +
                        "orderId=" + orderId +
                        ", eventType=" + eventType +
                        ", phone=" + phone +
                        ", createdAt=" + createdAt +
                        ", updatedAt=" + updatedAt +
                        '}';
        }
}
