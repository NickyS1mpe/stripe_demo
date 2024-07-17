package com.example.demo.model.dto.Payment;

import lombok.Data;

@Data
public class CreateInvoiceRequest {

    private String currency;

    private String description;

    private String customerId;

    private String priceId;
}
