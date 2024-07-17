package com.example.demo.model.dto.Payment;

import lombok.Data;

@Data
public class ApplyCouponRequest {

    private String couponId;

    private String priceId;
}
