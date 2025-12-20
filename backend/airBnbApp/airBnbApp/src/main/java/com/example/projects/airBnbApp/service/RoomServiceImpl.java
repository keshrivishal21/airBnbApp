package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.RoomDto;
import com.example.projects.airBnbApp.entity.Hotel;
import com.example.projects.airBnbApp.entity.Room;
import com.example.projects.airBnbApp.entity.User;
import com.example.projects.airBnbApp.exception.ResourceNotFoundException;
import com.example.projects.airBnbApp.exception.UnauthorizedException;
import com.example.projects.airBnbApp.repository.HotelRepository;
import com.example.projects.airBnbApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId,RoomDto roomDto) {
        log.info("Creating new room in hotel with id: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + hotelId);
        }
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }

        log.info("Created new room with id: {} in hotel with id: {}", room.getId(), hotelId);
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Retrieving all rooms in hotel with id: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + hotelId);
        }
        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .toList();
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Retrieving room with id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: " + roomId));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting room with id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: " + roomId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnauthorizedException("This user does not delete the room with id: " + roomId);
        }
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
        log.info("Deleted room with id: {}", roomId);

    }

    @Override
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + hotelId);
        }
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: " + roomId));
        modelMapper.map(roomDto, existingRoom);
        existingRoom.setId(roomId);
        log.info("Updated room with id: {}", roomId);
        existingRoom = roomRepository.save(existingRoom);
        return modelMapper.map(existingRoom, RoomDto.class);
    }
}
