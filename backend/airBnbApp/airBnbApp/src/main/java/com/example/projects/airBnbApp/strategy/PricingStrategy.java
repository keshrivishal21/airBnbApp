package com.example.projects.airBnbApp.strategy;

import com.example.projects.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
