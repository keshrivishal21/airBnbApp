package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.BookingDto;
import com.example.projects.airBnbApp.dto.BookingRequest;
import com.example.projects.airBnbApp.dto.GuestDto;
import com.example.projects.airBnbApp.dto.HotelReportDto;
import com.example.projects.airBnbApp.entity.enums.BookingStatus;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingDto initiateBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<Long> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingStatus getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    List<BookingDto> getMyBookings();


    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);
}
