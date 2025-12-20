package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.example.projects.airBnbApp.dto.UserDto;
import com.example.projects.airBnbApp.entity.User;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
