package com.example.demo.controller;


import com.example.demo.service.WebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class WebhookController {

    @Autowired
    private WebhookService webhookService;

    /*
     * Webhook handling
     *
     *
     */

    @PostMapping("/webhook")
    public ResponseEntity<?> webhookHandling(@RequestBody String payload,
                                             @RequestHeader("Stripe-Signature") String sigHeader) {
        webhookService.webhookHandling(payload, sigHeader);
        return ResponseEntity.ok("Succeed");
    }
}
