package com.example.projects.airBnbApp.repository;

import com.example.projects.airBnbApp.entity.Guest;
import com.example.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest,Long> {
    List<Guest> findByUser(User user);
}
