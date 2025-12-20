package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.*;
import com.example.projects.airBnbApp.entity.Hotel;
import com.example.projects.airBnbApp.entity.Room;
import com.example.projects.airBnbApp.entity.User;
import com.example.projects.airBnbApp.exception.ResourceNotFoundException;
import com.example.projects.airBnbApp.exception.UnauthorizedException;
import com.example.projects.airBnbApp.repository.BookingRepository;
import com.example.projects.airBnbApp.repository.HotelRepository;
import com.example.projects.airBnbApp.repository.InventoryRepository;
import com.example.projects.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("Created new hotel with id: {}", hotel.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Retrieving hotel with id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: " + id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + id);
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with id: {}", id);
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(existingHotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + id);
        }
        modelMapper.map(hotelDto, existingHotel);
        existingHotel = hotelRepository.save(existingHotel);
        existingHotel.setId(id);
        log.info("Updated hotel with id: {}", id);
        return modelMapper.map(existingHotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        log.info("Deleting hotel with id: {}", id);
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(existingHotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + id);
        }

        for(Room room : existingHotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.delete(room);
        }
        hotelRepository.delete(existingHotel);
        log.info("Deleted hotel with id: {}", id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating hotel with id: {}", hotelId);
        Hotel existingHotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(existingHotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + hotelId);
        }
        existingHotel.setActive(true);
        for(Room room : existingHotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId,HotelInfoRequestDto hotelInfoRequestDto){
        log.info("Retrieving hotel info with id: {}", hotelId);
        Hotel existingHotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        long daysCount = ChronoUnit.DAYS.between(hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate())+1;

        List<RoomPriceDto> roomPriceDtoList = inventoryRepository.findRoomAveragePrice(hotelId,
                hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate(),
                hotelInfoRequestDto.getRoomsCount(), daysCount);

        List<RoomPriceResponseDto> rooms = roomPriceDtoList.stream()
                .map(roomPriceDto -> {
                    RoomPriceResponseDto roomPriceResponseDto = modelMapper.map(roomPriceDto.getRoom(),
                            RoomPriceResponseDto.class);
                    roomPriceResponseDto.setPrice(roomPriceDto.getPrice());
                    return roomPriceResponseDto;
                })
                .toList();

        return new HotelInfoDto(modelMapper.map(existingHotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        log.info("Retrieving all hotels");
        User user = getCurrentUser();
        List<Hotel>hotels =  hotelRepository.findByOwner(user);
        return hotels.stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .toList();
    }

}
