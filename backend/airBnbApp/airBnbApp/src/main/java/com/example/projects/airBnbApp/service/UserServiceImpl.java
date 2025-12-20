package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.example.projects.airBnbApp.dto.UserDto;
import com.example.projects.airBnbApp.entity.User;
import com.example.projects.airBnbApp.exception.ResourceNotFoundException;
import com.example.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.example.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found with id: "+id));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        log.info("Updating profile for user id: {}", getCurrentUser().getId());
        User user = getCurrentUser();
        if(profileUpdateRequestDto.getName() != null) user.setName(profileUpdateRequestDto.getName());
        if(profileUpdateRequestDto.getGender() != null) user.setGender(profileUpdateRequestDto.getGender());
        if (profileUpdateRequestDto.getDateOfBirth() != null) user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        userRepository.save(user);
    }

    @Override
    public UserDto getMyProfile() {
        log.info("Retrieving profile for user id: {}", getCurrentUser().getId());
        return modelMapper.map(getCurrentUser(), UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByEmail(username).orElse(null);
    }
}
