import React from 'react';
import {createRoot} from 'react-dom/client';
import App from './App';
import {Elements} from '@stripe/react-stripe-js';
import {loadStripe} from '@stripe/stripe-js';

fetch('api/v1/payment/config')
    .then((response) => response.json())
    .then((data) => {
        console.log(data)
        const stripePromise = loadStripe(data.publishableKey);

        const container = document.getElementById('root');
        const root = createRoot(container); // createRoot(container!) if you use TypeScript
        root.render(
            <React.StrictMode>
                <Elements stripe={stripePromise}>
                    <App/>
                </Elements>
            </React.StrictMode>
        );
    })
    .catch((error) => {
        console.error('Error:', error);
    });
