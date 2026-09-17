package com.phonestore.dto;

import com.phonestore.model.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotBlank
    private String recipientName;
    @NotBlank
    private String recipientPhone;
    private String recipientEmail;
    @NotBlank
    private String shippingAddress;
    private String province;
    private String district;
    private String ward;
    private String note;
    private String couponCode;
    private Order.ShippingMethod shippingMethod;
    @NotNull
    private Order.PaymentMethod paymentMethod;
    @NotEmpty
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
