package com.example.demo.model.dto.Payment;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateSubscriptionRequest implements Serializable {

    private String priceId;
}
