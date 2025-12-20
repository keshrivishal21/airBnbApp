package com.example.projects.airBnbApp.repository;

import com.example.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<Object> findByEmail(String email);
}
