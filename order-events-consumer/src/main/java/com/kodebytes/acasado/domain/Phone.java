package com.kodebytes.acasado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Phone {

    @Id
    @NotNull
    private Long phoneId;

    @NotBlank
    private String phoneName;

    @NotBlank
    private String phoneModel;

    @NotBlank
    private String phoneManufacturer;

    @NotNull
    private Long phonePrice;

    @OneToOne
    @JoinColumn(name = "order_event_id")
    private OrderEvent orderEvent;

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

    public Phone() {
    }

    public Phone(Long phoneId, String phoneName, String phoneModel, Long phonePrice, String phoneManufacturer) {
        this.phoneId = phoneId;
        this.phoneName = phoneName;
        this.phoneModel = phoneModel;
        this.phonePrice = phonePrice;
        this.phoneManufacturer = phoneManufacturer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Phone phone = (Phone) o;
        return Objects.equals(phoneId, phone.phoneId)
                && Objects.equals(phoneName, phone.phoneName)
                && Objects.equals(phoneModel, phone.phoneModel)
                && Objects.equals(phonePrice, phone.phonePrice)
                && Objects.equals(phoneManufacturer, phone.phoneManufacturer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneId, phoneName, phoneModel, phonePrice, phoneManufacturer);
    }

    @Override
    public String toString() {
        return "Phone{" +
                "phoneId=" + phoneId +
                ", phoneName='" + phoneName + '\'' +
                ", phoneModel='" + phoneModel + '\'' +
                ", phonePrice=" + phonePrice +
                ", phoneManufacturer='" + phoneManufacturer + '\'' +
                '}';
    }
}
