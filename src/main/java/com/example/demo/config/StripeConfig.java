package com.example.demo.config;

import com.stripe.Stripe;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Bean
    public Dotenv dotenv() {
        return Dotenv.load();
    }

    @Bean
    public StripeConfig configureStripe(Dotenv dotenv) {
        Stripe.apiKey = dotenv.get("STRIPE_SECRET_KEY");
        return new StripeConfig();
    }
}
