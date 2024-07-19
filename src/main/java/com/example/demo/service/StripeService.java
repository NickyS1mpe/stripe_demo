package com.example.demo.service;

import com.example.demo.common.ErrorCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.dto.Payment.*;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            responseData.put("publishableKey", dotenv.get("STRIPE_PUBLISHABLE_KEY"));

            PriceListParams params = PriceListParams.builder().addLookupKey("sample_basic").addLookupKey("sample_premium").addLookupKey("sample_year").build();
            PriceCollection prices = Price.list(params);
            responseData.put("prices", prices.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gson.toJson(responseData);
    }

    public Map<?, ?> createCustomer(CreateCustomerRequest createCustomerRequest) {
        CustomerCreateParams customerCreateParams = CustomerCreateParams.builder().setEmail(createCustomerRequest.getEmail()).build();

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
        if (customerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Customer has not login.");
        }
        String priceId = createSubscriptionRequest.getPriceId();

        SubscriptionCreateParams subscriptionCreateParams = SubscriptionCreateParams.builder().setCustomer(customerId).addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build()).setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE).addAllExpand(Arrays.asList("latest_invoice.payment_intent")).setPaymentSettings(SubscriptionCreateParams.PaymentSettings.builder().setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION).build()).build();
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
        if (customerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Customer has not login.");
        }
        SubscriptionListParams params = SubscriptionListParams.builder().setStatus(SubscriptionListParams.Status.ALL).setCustomer(customerId).addAllExpand(Arrays.asList("data.default_payment_method")).build();

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
        RefundCreateParams params = RefundCreateParams.builder().setPaymentIntent(paymentRefundRequest.getPaymentIntentId()).build();

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
        if (customerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Customer has not login.");
        }
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder().setAmount(createPaymentRequest.getPrice()).setCurrency(createPaymentRequest.getCurrency()).setCustomer(customerId).setReceiptEmail(createPaymentRequest.getEmail()).setPaymentMethod(createPaymentRequest.getPaymentMethod()).setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()).build();
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

    public String listPaymentMethods(String customerId) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Customer has not login.");
        }
        Customer resource = null;
        try {
            resource = Customer.retrieve(customerId);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        CustomerListPaymentMethodsParams params = CustomerListPaymentMethodsParams.builder().setLimit(3L).build();

        PaymentMethodCollection paymentMethods = null;
        try {
            paymentMethods = resource.listPaymentMethods(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        return StripeObject.PRETTY_PRINT_GSON.toJson(paymentMethods);
    }

    public String removePaymentMethod(RemovePaymentMethodRequest removePaymentMethodRequest) {
        PaymentMethod resource = null;
        PaymentMethod paymentMethod = null;
        try {
            resource = PaymentMethod.retrieve(removePaymentMethodRequest.getPaymentId());
            PaymentMethodDetachParams params = PaymentMethodDetachParams.builder().build();
            paymentMethod = resource.detach(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        return StripeObject.PRETTY_PRINT_GSON.toJson(paymentMethod);
    }

    public String listInvoices(String customerId) {
        if (customerId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Customer has not login.");
        }
        InvoiceListParams params = InvoiceListParams.builder().setCustomer(customerId).setLimit(3L).build();

        InvoiceCollection invoices = null;
        try {
            invoices = Invoice.list(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        return StripeObject.PRETTY_PRINT_GSON.toJson(invoices);
    }

    public String getInvoice(GetInvoiveRequest getInvoiveRequest) {
        Invoice invoice = null;
        try {
            invoice = Invoice.retrieve(getInvoiveRequest.getInvoiceId());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        return StripeObject.PRETTY_PRINT_GSON.toJson(invoice);
    }


    public String listCoupons() {
        CouponListParams params = CouponListParams.builder().setLimit(3L).build();
        CouponCollection coupons = null;
        try {
            coupons = Coupon.list(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        return StripeObject.PRETTY_PRINT_GSON.toJson(coupons);
    }

    public String removeCoupon(RemoveCouponRequest removeCouponRequest) {
        Coupon resource = null;
        Coupon coupon = null;
        try {
            resource = Coupon.retrieve(removeCouponRequest.getCouponId());
            coupon = resource.delete();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        return StripeObject.PRETTY_PRINT_GSON.toJson(coupon);
    }

    public String getTransactionHistory(String customerId) {
        Customer resource = null;
        CustomerBalanceTransactionCollection customerBalanceTransactions =
                null;
        try {
            resource = Customer.retrieve(customerId);
            CustomerBalanceTransactionsParams params =
                    CustomerBalanceTransactionsParams.builder().setLimit(3L).build();
            customerBalanceTransactions = resource.balanceTransactions(params);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        return StripeObject.PRETTY_PRINT_GSON.toJson(customerBalanceTransactions);
    }

    public String applyCoupon(ApplyCouponRequest applyCouponRequest) {

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(applyCouponRequest.getPriceId())
                                        .setQuantity(1L)
                                        .build())
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .addDiscount(
                                SessionCreateParams.Discount.builder()
                                        .setCoupon(applyCouponRequest.getCouponId())
                                        .build())
                        .setSuccessUrl("https://example.com/success")
                        .setCancelUrl("https://example.com/cancel")
                        .build();

        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("sessionUrl", session.getUrl());

        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }

    public String createInvoice(CreateInvoiceRequest createInvoiceRequest) {
        String customerId = createInvoiceRequest.getCustomerId();
        Invoice responseInvoice = null;
        try {
            // Create an Invoice
            InvoiceCreateParams invoiceParams =
                    InvoiceCreateParams
                            .builder()
                            .setCustomer(customerId)
                            .setCurrency(createInvoiceRequest.getCurrency())
                            .setDescription(createInvoiceRequest.getDescription())
                            .setCollectionMethod(InvoiceCreateParams.CollectionMethod.SEND_INVOICE)
                            .setDaysUntilDue(30L)
                            .build();

            Invoice invoice = Invoice.create(invoiceParams);

            InvoiceItemCreateParams invoiceItemParams =
                    InvoiceItemCreateParams.builder()
                            .setCustomer(customerId)
                            .setPrice(createInvoiceRequest.getPriceId())
                            .setInvoice(invoice.getId())
                            .build();

            try {
                InvoiceItem.create(invoiceItemParams);
            } catch (StripeException e) {
                throw new RuntimeException(e);
            }

            // Send an Invoice
            InvoiceSendInvoiceParams params = InvoiceSendInvoiceParams.builder().build();
            responseInvoice = invoice.sendInvoice(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("invoiceId", responseInvoice.getId());
        responseData.put("invoiceStatus", responseInvoice.getStatus());

        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }

    public String getMonthlySummary(String customerId, MonthlySummaryRequest monthlySummaryRequest) {
        long spendSummary;
        InvoiceCollection invoices;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate startLocalDate = LocalDate.parse(monthlySummaryRequest.getStartTime(), formatter);
            LocalDate endLocalDate = LocalDate.parse(monthlySummaryRequest.getEndTime(), formatter);
            long startTimestamp = startLocalDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long endTimestamp = endLocalDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            InvoiceListParams params = InvoiceListParams.builder()
                    .setCustomer(customerId)
                    .setCreated(InvoiceListParams.Created.builder()
                            .setGte(startTimestamp)
                            .setLte(endTimestamp)
                            .build())
                    .build();

            invoices = Invoice.list(params);

            spendSummary = invoices.getData().stream()
                    .mapToLong(Invoice::getTotal)
                    .sum();

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("totalSpend", spendSummary);
        responseData.put("currency", invoices.getData().isEmpty() ? "NULL" : invoices.getData().get(0).getCurrency());
        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }

    public String addPaymentMethod(String customerId, AddPaymentMethodRequest addPaymentMethodRequest) {
        PaymentMethod paymentMethod = null;
        try {
            PaymentMethod resource = PaymentMethod.retrieve(addPaymentMethodRequest.getPaymentId());
            PaymentMethodAttachParams params =
                    PaymentMethodAttachParams.builder().setCustomer(customerId).build();
            paymentMethod = resource.attach(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("PaymentId", paymentMethod.getId());
        return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
    }
}
