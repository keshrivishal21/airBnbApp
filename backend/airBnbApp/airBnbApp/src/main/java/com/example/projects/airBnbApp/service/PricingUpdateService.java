package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.HotelDto;
import com.example.projects.airBnbApp.entity.Hotel;
import com.example.projects.airBnbApp.entity.HotelMinPrice;
import com.example.projects.airBnbApp.entity.Inventory;
import com.example.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.example.projects.airBnbApp.repository.HotelRepository;
import com.example.projects.airBnbApp.repository.InventoryRepository;
import com.example.projects.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PricingUpdateService {

    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void updatePrice(){
        int page = 0;
        int size = 100;

        log.info("Starting pricing update for all hotels");
        while(true){
            Page<Hotel>hotelPage = hotelRepository.findAll(PageRequest.of(page,size));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrice);
            page++;
        }
    }

    public void updateHotelPrice(Hotel hotel){
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
        updateInventoryPrice(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate,BigDecimal>dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date,price)->{
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        hotelMinPriceRepository.saveAll(hotelPrices);
    }


    public void updateInventoryPrice(List<Inventory> inventoryList){
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }
}
