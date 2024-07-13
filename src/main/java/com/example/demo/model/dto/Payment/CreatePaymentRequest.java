package com.example.demo.model.dto.Payment;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreatePaymentRequest implements Serializable {
    private Long price;

    private String currency;

    private String email;

    private String paymentMethod;

}
