package com.example.projects.airBnbApp.strategy;

import com.example.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isHoliday = true; // This would be determined by some holiday logic
        if(isHoliday) {
            price = price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;
    }
}
