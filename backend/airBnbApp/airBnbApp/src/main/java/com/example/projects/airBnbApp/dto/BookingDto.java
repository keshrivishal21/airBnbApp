package com.example.projects.airBnbApp.dto;

import com.example.projects.airBnbApp.entity.Guest;
import com.example.projects.airBnbApp.entity.Hotel;
import com.example.projects.airBnbApp.entity.Room;
import com.example.projects.airBnbApp.entity.User;
import com.example.projects.airBnbApp.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {
    private Long id;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private BigDecimal amount;
}
