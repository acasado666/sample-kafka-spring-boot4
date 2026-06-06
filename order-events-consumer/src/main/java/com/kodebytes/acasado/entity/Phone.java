package com.kodebytes.acasado.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "phone")
public class Phone {

    @Id
    @NotNull
    @Column(name = "phone_id")
    private Integer phoneId;

    @NotBlank
    @Column(name = "phone_name", nullable = false)
    private String phoneName;

    @NotBlank
    @Column(name = "phone_model", nullable = false)
    private String phoneModel;

    @NotBlank
    @Column(name = "phone_manufacturer", nullable = false)
    private String phoneManufacturer;

    @NotNull
    @Column(name = "phone_price", nullable = false)
    private Long phonePrice;

    @OneToOne
    @JoinColumn(name = "order_event_id")
    private OrderEvent orderEvent;

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

    public Integer getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(Integer phoneId) {
        this.phoneId = phoneId;
    }

    public String getPhoneName() {
        return phoneName;
    }

    public void setPhoneName(String phoneName) {
        this.phoneName = phoneName;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getPhoneManufacturer() {
        return phoneManufacturer;
    }

    public void setPhoneManufacturer(String phoneManufacturer) {
        this.phoneManufacturer = phoneManufacturer;
    }

    public Long getPhonePrice() {
        return phonePrice;
    }

    public void setPhonePrice(Long phonePrice) {
        this.phonePrice = phonePrice;
    }

    public OrderEvent getOrderEvent() {
        return orderEvent;
    }

    public void setOrderEvent(OrderEvent orderEvent) {
        this.orderEvent = orderEvent;
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
