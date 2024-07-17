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
   Modify the .env file with your STRIPE_PUBLIC_KEY, STRIPE_SECRET_KEY and STRIPE_WEBHOOK_SECRET.

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

3. To use webhook, please install Stripe CLI and run 
   ```sh
   stripe listen --forward-to localhost:8101/api/webhook
   ```