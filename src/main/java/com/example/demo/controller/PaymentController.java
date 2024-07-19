package com.example.demo.controller;

import com.example.demo.model.dto.Payment.*;
import com.example.demo.service.StripeService;
import com.stripe.model.StripeObject;
import com.sun.xml.bind.v2.TODO;
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

    // Fetch product config
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.getConfig());
    }

    // Create customer
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

    // For test only
    @PostMapping("/login-customer/{customer_id}")
    public ResponseEntity<?> loginCustomer(@PathVariable("customer_id") String customerId, HttpServletResponse response) {
        Cookie cookie = new Cookie("customer", customerId);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.CREATED).body("Customer login.");
    }

    // Creates a payment intent for one-time payments.
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPayment(@CookieValue(name = "customer", required = false) String customerId, @Valid @RequestBody CreatePaymentRequest createPaymentRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.createPayment(createPaymentRequest, customerId));
    }

    // Creates a payment intent for one-time payments.
    @PostMapping("/create-subscription")
    public ResponseEntity<?> createSubscription(@CookieValue(name = "customer", required = false) String customerId, @Valid @RequestBody CreateSubscriptionRequest CreateSubscriptionRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.createSubscription(CreateSubscriptionRequest, customerId));
    }

    // Cancel subscription
    @PostMapping("/cancel-subscription")
    public ResponseEntity<?> cancelSubscription(@Valid @RequestBody CancelSubscriptionRequest cancelSubscriptionRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.cancelSubscription(cancelSubscriptionRequest));
    }


    // Get the current customer's subscriptions
    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscription(@CookieValue(name = "customer", required = false) String customerId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.getSubscription(customerId));
    }

    // Creates a refund for a payment.
    @PostMapping("/create-refund")
    public ResponseEntity<?> createRefund(@Valid @RequestBody PaymentRefundRequest paymentRefundRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeService.createRefund(paymentRefundRequest));
    }

    /*
     * Payment Methods Management
     *
     *
     */

    // Add a payment method for the customer.
    // TODO
    @PostMapping("/add-payment-method")
    public ResponseEntity<?> addPaymentMethod(@CookieValue(name = "customer", required = false) String customerId, @Valid @RequestBody AddPaymentMethodRequest addPaymentMethodRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.addPaymentMethod(customerId, addPaymentMethodRequest));
    }

    // Remove a payment method for the customer.
    @PostMapping("/remove-payment-method")
    public ResponseEntity<?> removePaymentMethod(@Valid @RequestBody RemovePaymentMethodRequest removePaymentMethodRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.removePaymentMethod(removePaymentMethodRequest));
    }

    // List all payment methods of the customer.
    @GetMapping("/list-payment-methods")
    public ResponseEntity<?> listPaymentMethods(@CookieValue(name = "customer", required = false) String customerId) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.listPaymentMethods(customerId));
    }

    /*
     * Invoice Management
     *
     *
     */

    // Create an invoice for the customer.
    // Send through email to customer
    @PostMapping("/create-invoice")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody CreateInvoiceRequest createInvoiceRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.createInvoice(createInvoiceRequest));
    }

    // List all invoices for the customer.
    @GetMapping("/list-invoices")
    public ResponseEntity<?> listInvoices(@CookieValue(name = "customer", required = false) String customerId) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.listInvoices(customerId));
    }

    // Get detailed information of a specific invoice.
    @GetMapping("/get-invoice")
    public ResponseEntity<?> getInvoice(GetInvoiveRequest getInvoiveRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.getInvoice(getInvoiveRequest));
    }

    /*
     * Discount and Coupon Management
     *
     *
     */

    // Apply a discount coupon to a payment.
    // Return a Stripe checkout session url
    @PostMapping("/apply-coupon")
    public ResponseEntity<?> applyCoupon(@Valid @RequestBody ApplyCouponRequest applyCouponRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.applyCoupon(applyCouponRequest));
    }

    // Remove an applied discount coupon.
    @PostMapping("/remove-coupon")
    public ResponseEntity<?> removeCoupon(@Valid @RequestBody RemoveCouponRequest removeCouponRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.removeCoupon(removeCouponRequest));
    }

    //List available discount coupons.
    @GetMapping("/list-coupons")
    public ResponseEntity<?> listCoupons() {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.listCoupons());
    }

    /*
     * Analytics and Reporting
     *
     *
     */

    // Get the transaction history of the customer.
    @GetMapping("/transaction-history")
    public ResponseEntity<?> getTransactionHistory(@CookieValue(name = "customer", required = false) String customerId) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.getTransactionHistory(customerId));
    }

    // Get a monthly summary of payments.
    @PostMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(@CookieValue(name = "customer", required = false) String customerId, MonthlySummaryRequest monthlySummaryRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.getMonthlySummary(customerId, monthlySummaryRequest));
    }

    /*
     * Security and Fraud Detection
     * TODO
     *
     */

    // Validate a payment to detect fraudulent transactions.
//    @PostMapping("/validate-payment")

    // Report a fraudulent transaction.
//    @PostMapping("/report-fraud")
}
