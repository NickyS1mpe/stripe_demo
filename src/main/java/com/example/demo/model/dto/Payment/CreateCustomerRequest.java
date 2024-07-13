package com.example.demo.model.dto.Payment;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateCustomerRequest implements Serializable {
    private String email;

}
