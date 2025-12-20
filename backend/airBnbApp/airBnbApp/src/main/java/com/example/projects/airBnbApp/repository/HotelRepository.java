package com.example.projects.airBnbApp.repository;

import com.example.projects.airBnbApp.entity.Hotel;
import com.example.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel,Long> {
    List<Hotel> findByOwner(User user);
}
