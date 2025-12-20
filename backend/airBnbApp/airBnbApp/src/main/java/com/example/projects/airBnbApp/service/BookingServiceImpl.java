package com.example.projects.airBnbApp.service;

import com.example.projects.airBnbApp.dto.BookingDto;
import com.example.projects.airBnbApp.dto.BookingRequest;
import com.example.projects.airBnbApp.dto.GuestDto;
import com.example.projects.airBnbApp.dto.HotelReportDto;
import com.example.projects.airBnbApp.entity.*;
import com.example.projects.airBnbApp.entity.enums.BookingStatus;
import com.example.projects.airBnbApp.exception.ResourceNotFoundException;
import com.example.projects.airBnbApp.exception.UnauthorizedException;
import com.example.projects.airBnbApp.repository.*;
import com.example.projects.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initiateBooking(BookingRequest bookingRequest) {
        log.info("Initiating booking for hotel id: {}, room id: {} from {} to {} for {} rooms",
                bookingRequest.getHotelId(),
                bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: " + bookingRequest.getHotelId()));
        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: " + bookingRequest.getRoomId()));
        List<Inventory> inventories = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount());
        Long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        if(inventories.size() != daysCount){
            throw new IllegalStateException("Room not available for the selected dates");
        }

        inventoryRepository.initBooking(room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventories);
        BigDecimal totalAmount = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        User user = new User();
        user.setId(1L);

        // TODO: Calculate amount based on inventory prices


        Booking booking = Booking.builder()
                .hotel(hotel)
                .room(room)
                .user(user)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .roomsCount(bookingRequest.getRoomsCount())
                .bookingStatus(BookingStatus.RESERVED)
                .amount(totalAmount)
                .build();
        booking = bookingRepository.save(booking);
        log.info("Booking initiated with id: {} for hotel id: {}, room id: {}",
                booking.getId(),
                bookingRequest.getHotelId(),
                bookingRequest.getRoomId());
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<Long> guestIdList) {
        log.info("Adding guests for booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: "+bookingId));
        User user = getCurrentUser();

        if (!user.equals(booking.getUser())) {
            throw new UnauthorizedException("Booking does not belong to this user with id: "+user.getId());
        }

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for (Long guestId: guestIdList) {
            Guest guest = guestRepository.findById(guestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: "+guestId));
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to the current user with id: " + user.getId());
        }

        if(hasBookingExpired(booking)) {
            throw new IllegalStateException("Cannot add guests. Booking has expired.");
        }
        String sessionUrl = checkoutService.getCheckoutSession(booking, frontendUrl+"payments/success", frontendUrl+"payments/failure");
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
                            new ResourceNotFoundException("Booking not found for session ID: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to the current user with id: " + user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());
        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        log.info("Cancelled booking with id: {}", bookingId);
    }

    @Override
    public BookingStatus getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to the current user with id: " + user.getId());
        }
        return booking.getBookingStatus();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        log.info("Retrieving all bookings for hotel id: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own the hotel with id: " + hotelId);
        }
        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .toList();
    }

    @Override
    public List<BookingDto> getMyBookings() {
        log.info("Retrieving bookings for current user");
        User user = getCurrentUser();
        List<Booking> bookings = bookingRepository.findByUser(user);
        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .toList();
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not " +
                "found with ID: "+hotelId));
        User user = getCurrentUser();

        log.info("Generating report for hotel with ID: {}", hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: "+hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }


}
