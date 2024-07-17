package com.example.demo.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    @Autowired
    private Dotenv dotenv;

    public void webhookHandling(String payload, String sigHeader) {
        String endpointSecret = dotenv.get("STRIPE_WEBHOOK_SECRET");
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException(e);
        }

        switch (event.getType()) {
            case "invoice.payment_succeeded":

                System.out.println("Payment succeeded for invoice: " + event.getType());

                break;
            case "invoice.paid":

                System.out.println("Paid invoice succeed: " + event.getType());

                break;
            case "invoice.payment_failed":

                System.out.println("Payment failed for invoice: " + event.getType());

                break;
            case "payment_intent.succeeded":

                System.out.println("Created payment intent succeed: " + event.getType());

                break;
            case "customer.subscription.created":

                System.out.println("Created subscription succeed: " + event.getType());

                break;
            case "customer.subscription.deleted":

                System.out.println("Deleted subscription succeed: " + event.getType());

                break;
            case "charge.captured":

                System.out.println("Charge captured: " + event.getType());

                break;
            default:

                System.out.println("Unhandled event: " + event.getType());

                break;
        }
    }
}
