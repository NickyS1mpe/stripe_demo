package com.example.demo.model.dto.Payment;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentRefundRequest implements Serializable {
    private String paymentIntentId;
}
