package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.GuestDto;

import java.util.List;

public interface GuestService {
    void updateGuest(Long guestId, GuestDto guestDto);

    void deleteGuest(Long guestId);

    List<GuestDto> getAllGuests();

    GuestDto addNewGuest(GuestDto guestDto);
}
