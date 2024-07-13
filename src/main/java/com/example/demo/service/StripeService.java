package com.example.demo.service;

import com.example.demo.model.dto.Payment.*;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Autowired
    private Dotenv dotenv;

    private static Gson gson = new Gson();


    public String getConfig() {
        Map<String, Object> responseData = new HashMap<>();
        try {
            responseData.put(
                    "publishableKey",
                    dotenv.get("STRIPE_PUBLISHABLE_KEY")
            );

            PriceListParams params = PriceListParams
                    .builder()
                    .addLookupKey("sample_basic")
                    .addLookupKey("sample_premium")
                    .addLookupKey("sample_year")
                    .build();
            PriceCollection prices = Price.list(params);
            responseData.put("prices", prices.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gson.toJson(responseData);
    }

    public Map<?, ?> createCustomer(CreateCustomerRequest createCustomerRequest) {
        CustomerCreateParams customerCreateParams = CustomerCreateParams
                .builder()
                .setEmail(createCustomerRequest.getEmail())
                .build();

        Customer customer;
        try {
            customer = Customer.create(customerCreateParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("customer", customer.getId());

        return responseData;
    }

    public String createSubscription(CreateSubscriptionRequest createSubscriptionRequest, String customerId) {
        String priceId = createSubscriptionRequest.getPriceId();

        SubscriptionCreateParams subscriptionCreateParams = SubscriptionCreateParams
                .builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams
                        .Item.builder()
                        .setPrice(priceId)
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addAllExpand(Arrays.asList("latest_invoice.payment_intent"))
                .setPaymentSettings(SubscriptionCreateParams
                        .PaymentSettings
                        .builder()
                        .setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION)
                        .build())
                .build();
        Subscription subscription;
        try {
            subscription = Subscription.create(subscriptionCreateParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("subscriptionId", subscription.getId());
        responseData.put("clientSecret", subscription.getLatestInvoiceObject().getPaymentIntentObject().getClientSecret());
        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }

    public String cancelSubscription(CancelSubscriptionRequest cancelSubscriptionRequest) {
        String subscriptionId = cancelSubscriptionRequest.getSubscriptionId();
        Subscription subscription;
        try {
            subscription = Subscription.retrieve(subscriptionId);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Subscription deletedSubscription;
        try {
            deletedSubscription = subscription.cancel();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("subscription", deletedSubscription);
        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }

    public String getSubscription(String customerId) {
        SubscriptionListParams params = SubscriptionListParams
                .builder()
                .setStatus(SubscriptionListParams.Status.ALL)
                .setCustomer(customerId)
                .addAllExpand(Arrays.asList("data.default_payment_method"))
                .build();

        SubscriptionCollection subscriptionCollection = null;
        try {
            subscriptionCollection = Subscription.list(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("subscriptions", subscriptionCollection);
        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }

    public String createRefund(PaymentRefundRequest paymentRefundRequest) {
        RefundCreateParams params = RefundCreateParams
                .builder()
                .setPaymentIntent
                        (paymentRefundRequest.getPaymentIntentId())
                .build();

        Refund refund = null;
        try {
            refund = Refund.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("refund", refund.getStatus());
        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }

    public String createPayment(CreatePaymentRequest createPaymentRequest, String customerId) {

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(createPaymentRequest.getPrice())
                        .setCurrency(createPaymentRequest.getCurrency())
                        .setCustomer(customerId)
                        .setReceiptEmail(createPaymentRequest.getEmail())
                        .setPaymentMethod(createPaymentRequest.getPaymentMethod())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();
        PaymentIntent paymentIntent = null;
        try {
            paymentIntent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("clientSecret", paymentIntent.getClientSecret());
        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }
}
