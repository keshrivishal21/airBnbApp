package com.example.projects.airBnbApp.dto;

import com.example.projects.airBnbApp.entity.HotelContactInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomPriceResponseDto {
    private Long id;
    private String type;
    private String[] photos;
    private String[] amenities;
    private Double price;
}
