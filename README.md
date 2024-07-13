# Stripe Subscription Server

This is a Spring Boot backend server for Stripe subscription service.

## Environment

- Java 22
- Maven 
- Stripe account

## Setup

1. 
   ```sh
   cp -rf .env.sapmple .env
   ```
   Modify the .env file with your STRIPE_PUBLIC_KEY and STRIPE_SECRET_KEY

2. ```sh
   public String getConfig() {
            ...
            PriceListParams params = PriceListParams
                    .builder()
                    .addLookupKey("sample_basic")
                    .addLookupKey("sample_premium")
                    .addLookupKey("sample_year")
                    .build();
            PriceCollection prices = Price.list(params);
            ...
    }
   ```
    In StripeService.java, Please modify the LookupKey for looking up your Price Object in Stripe.