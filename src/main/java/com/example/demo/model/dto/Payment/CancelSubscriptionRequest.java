package com.example.demo.model.dto.Payment;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelSubscriptionRequest implements Serializable {

    private String subscriptionId;
}
