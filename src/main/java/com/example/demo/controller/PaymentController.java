package com.example.demo.controller;

import com.example.demo.model.dto.Payment.*;
import com.example.demo.service.StripeService;
import com.stripe.model.StripeObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/payment")
@Slf4j
public class PaymentController {

    @Autowired
    StripeService stripeService;

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.getConfig());
    }

    @PostMapping("/create-customer")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CreateCustomerRequest createCustomerRequest, HttpServletResponse response) {
        Map<?, ?> responseData = stripeService.createCustomer(createCustomerRequest);
        Cookie cookie = new Cookie("customer", responseData.get("customer").toString());
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.CREATED).body(StripeObject.PRETTY_PRINT_GSON.toJson(responseData));
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPayment(@CookieValue(name = "customer", required = false) String customerId, @Valid @RequestBody CreatePaymentRequest createPaymentRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.createPayment(createPaymentRequest, customerId));
    }

    @PostMapping("/create-subscription")
    public ResponseEntity<?> createSubscription(@CookieValue(name = "customer", required = false) String customerId, @Valid @RequestBody CreateSubscriptionRequest CreateSubscriptionRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.createSubscription(CreateSubscriptionRequest, customerId));
    }

    @PostMapping("/cancel-subscription")
    public ResponseEntity<?> cancelSubscription(@Valid @RequestBody CancelSubscriptionRequest cancelSubscriptionRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.cancelSubscription(cancelSubscriptionRequest));
    }

    @PostMapping("/update-subscription")
    public ResponseEntity<?> updateSubscription(@Valid @RequestBody CancelSubscriptionRequest cancelSubscriptionRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.cancelSubscription(cancelSubscriptionRequest));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscription(@CookieValue(name = "customer", required = false) String customerId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.getSubscription(customerId));
    }

    @PostMapping("/create-refund")
    public ResponseEntity<?> createRefund(@Valid @RequestBody PaymentRefundRequest paymentRefundRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.createRefund(paymentRefundRequest));
    }
}
