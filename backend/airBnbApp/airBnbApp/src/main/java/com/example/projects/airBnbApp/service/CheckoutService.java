package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
