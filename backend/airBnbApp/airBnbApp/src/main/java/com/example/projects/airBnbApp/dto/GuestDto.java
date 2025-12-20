package com.example.projects.airBnbApp.dto;

import com.example.projects.airBnbApp.entity.User;
import com.example.projects.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private String name;
    private Gender gender;
    private Integer age;

}
