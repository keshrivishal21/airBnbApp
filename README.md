# 🏨 AirBnb Clone - Hotel Booking Application

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.4-green?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Stripe-Payments-purple?style=for-the-badge&logo=stripe&logoColor=white" alt="Stripe"/>
</p>

A production-ready **AirBnb-style hotel booking platform** built with Spring Boot 3.5, featuring advanced dynamic pricing strategies, secure JWT authentication, Stripe payment integration, and comprehensive hotel management capabilities.

---

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Dynamic Pricing System](#-dynamic-pricing-system)
- [Authentication & Authorization](#-authentication--authorization)
- [Payment Integration](#-payment-integration)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 🏠 Hotel Management (Admin)
- Create, update, and delete hotels
- Manage hotel details including photos, amenities, and contact information
- Activate/deactivate hotels
- View all bookings and generate reports
- Manage room inventory with dynamic pricing

### 🛏️ Room Management
- Create and manage different room types
- Set base prices, capacity, and amenities
- Photo gallery support for each room
- Real-time inventory tracking

### 📅 Booking System
- Multi-step booking flow (Init → Add Guests → Payment → Confirmation)
- Real-time availability checking
- Guest management with user profiles
- Booking status tracking (Reserved, Guests Added, Payment Pending, Confirmed, Cancelled, Expired)

### 💰 Dynamic Pricing Engine
Implements the **Decorator Pattern** for flexible price calculation:
- **Base Pricing**: Room's base price
- **Surge Pricing**: Multiplier based on demand
- **Occupancy Pricing**: 20% increase when occupancy > 80%
- **Urgency Pricing**: Last-minute booking adjustments
- **Holiday Pricing**: 25% markup during holidays

### 🔐 Security
- JWT-based authentication with refresh tokens
- Role-based access control (GUEST, HOTEL_MANAGER)
- Secure password encryption with BCrypt
- HTTP-only cookies for refresh tokens

### 💳 Payment Processing
- Stripe Checkout integration
- Webhook handling for payment confirmation
- Secure payment session management

### 📖 API Documentation
- OpenAPI 3.0 (Swagger UI) integration
- Interactive API testing interface

---

## 🛠 Technology Stack

| Category | Technology |
|----------|------------|
| **Backend Framework** | Spring Boot 3.5.4 |
| **Language** | Java 21 |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security + JWT (jjwt 0.12.6) |
| **Payment** | Stripe Java SDK 28.2.0 |
| **Documentation** | SpringDoc OpenAPI 2.8.14 |
| **Object Mapping** | ModelMapper 3.2.0 |
| **Build Tool** | Maven |
| **Utilities** | Lombok |

---

## 🏗 Architecture

The application follows a **layered architecture** with clean separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                      Controllers                             │
│  (AuthController, HotelController, BookingController, etc.) │
├─────────────────────────────────────────────────────────────┤
│                       Services                               │
│   (HotelService, BookingService, InventoryService, etc.)    │
├─────────────────────────────────────────────────────────────┤
│                   Strategy (Pricing)                         │
│  (BasePricing, SurgePricing, OccupancyPricing, etc.)        │
├─────────────────────────────────────────────────────────────┤
│                     Repositories                             │
│   (HotelRepository, BookingRepository, UserRepository)      │
├─────────────────────────────────────────────────────────────┤
│                      Entities                                │
│       (Hotel, Room, Booking, User, Inventory, Guest)        │
├─────────────────────────────────────────────────────────────┤
│                      PostgreSQL                              │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns Used
- **Decorator Pattern**: Dynamic pricing calculation
- **Strategy Pattern**: Pricing strategies
- **Builder Pattern**: Entity construction (Booking, Inventory)
- **DTO Pattern**: Data transfer between layers
- **Repository Pattern**: Data access abstraction

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **PostgreSQL** 14+
- **Maven** 3.8+
- **Stripe Account** (for payment testing)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/airbnb-clone.git
   cd airbnb-clone
   ```

2. **Configure PostgreSQL**
   ```sql
   CREATE DATABASE airBnb;
   ```

3. **Update application.properties**
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/airBnb
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   # JWT Configuration
   jwt.secretKey=your_secret_key_here
   
   # Stripe Configuration
   stripe.secret.key=sk_test_your_stripe_secret_key
   stripe.webhook.secret=whsec_your_webhook_secret
   
   # Frontend URL (for CORS)
   frontend.url=http://localhost:3000
   ```

4. **Build and Run**
   ```bash
   cd backend/airBnbApp/airBnbApp
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

5. **Access the Application**
   - API Base URL: `http://localhost:8080/api/v1`
   - Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

---

## 📖 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/auth/signup` | Register a new user | Public |
| POST | `/auth/login` | Login and get JWT token | Public |
| POST | `/auth/refresh` | Refresh access token | Public |

### Hotel Management (Admin)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/admin/hotels` | Create a new hotel | HOTEL_MANAGER |
| GET | `/admin/hotels` | Get all hotels owned by admin | HOTEL_MANAGER |
| GET | `/admin/hotels/{hotelId}` | Get hotel by ID | HOTEL_MANAGER |
| PUT | `/admin/hotels/{hotelId}` | Update hotel | HOTEL_MANAGER |
| DELETE | `/admin/hotels/{hotelId}` | Delete hotel | HOTEL_MANAGER |
| PATCH | `/admin/hotels/{hotelId}/activate` | Activate hotel | HOTEL_MANAGER |
| GET | `/admin/hotels/{hotelId}/bookings` | Get all bookings | HOTEL_MANAGER |
| GET | `/admin/hotels/{hotelId}/reports` | Generate booking report | HOTEL_MANAGER |

### Room Management (Admin)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/admin/hotels/{hotelId}/rooms` | Create a new room | HOTEL_MANAGER |
| GET | `/admin/hotels/{hotelId}/rooms` | Get all rooms in hotel | HOTEL_MANAGER |
| GET | `/admin/hotels/{hotelId}/rooms/{roomId}` | Get room by ID | HOTEL_MANAGER |
| PUT | `/admin/hotels/{hotelId}/rooms/{roomId}` | Update room | HOTEL_MANAGER |
| DELETE | `/admin/hotels/{hotelId}/rooms/{roomId}` | Delete room | HOTEL_MANAGER |

### Inventory Management (Admin)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/admin/inventory/rooms/{roomId}` | Get room inventory | HOTEL_MANAGER |
| PATCH | `/admin/inventory/rooms/{roomId}` | Update inventory | HOTEL_MANAGER |

### Hotel Browsing (Public/Guest)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/hotels/search` | Search available hotels | Public |
| GET | `/hotels/{hotelId}/info` | Get hotel information | Public |

### Booking Flow

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/bookings/init` | Initiate booking | Authenticated |
| POST | `/bookings/{bookingId}/addGuests` | Add guests to booking | Authenticated |
| POST | `/bookings/{bookingId}/payments` | Initiate payment | Authenticated |
| POST | `/bookings/{bookingId}/cancel` | Cancel booking | Authenticated |
| POST | `/bookings/{bookingId}/status` | Get booking status | Authenticated |

### User Profile & Guests

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/users/profile` | Get user profile | Authenticated |
| PATCH | `/users/profile` | Update profile | Authenticated |
| GET | `/users/myBookings` | Get user's bookings | Authenticated |
| GET | `/users/guests` | Get all guests | Authenticated |
| POST | `/users/guests` | Add a new guest | Authenticated |
| PUT | `/users/guests/{guestId}` | Update guest | Authenticated |
| DELETE | `/users/guests/{guestId}` | Delete guest | Authenticated |

### Webhooks

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/webhook/payment` | Stripe payment webhook | Stripe |

---

## 🗄 Database Schema

### Entity Relationship

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│     User     │       │    Hotel     │       │     Room     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id           │       │ id           │       │ id           │
│ email        │◄──────│ owner_id     │◄──────│ hotel_id     │
│ name         │       │ name         │       │ type         │
│ password     │       │ city         │       │ basePrice    │
│ roles        │       │ photos[]     │       │ photos[]     │
│ dateOfBirth  │       │ amenities[]  │       │ amenities[]  │
│ gender       │       │ contactInfo  │       │ totalCount   │
└──────────────┘       │ active       │       │ capacity     │
                       └──────────────┘       └──────────────┘
                              │                      │
                              ▼                      ▼
                       ┌──────────────┐       ┌──────────────┐
                       │   Booking    │       │  Inventory   │
                       ├──────────────┤       ├──────────────┤
                       │ id           │       │ id           │
                       │ hotel_id     │       │ hotel_id     │
                       │ room_id      │       │ room_id      │
                       │ user_id      │       │ date         │
                       │ checkInDate  │       │ bookedCount  │
                       │ checkOutDate │       │ reservedCount│
                       │ roomsCount   │       │ totalCount   │
                       │ amount       │       │ surgeFactor  │
                       │ bookingStatus│       │ price        │
                       │ guests (M2M) │       │ city         │
                       └──────────────┘       └──────────────┘
                              │
                              ▼
                       ┌──────────────┐
                       │    Guest     │
                       ├──────────────┤
                       │ id           │
                       │ user_id      │
                       │ name         │
                       │ gender       │
                       │ age          │
                       └──────────────┘
```

### Enums

- **BookingStatus**: `RESERVED`, `GUESTS_ADDED`, `PAYMENT_PENDING`, `CONFIRMED`, `CANCELLED`, `EXPIRED`
- **Role**: `GUEST`, `HOTEL_MANAGER`
- **Gender**: `MALE`, `FEMALE`, `OTHER`
- **PaymentStatus**: Payment tracking states

---

## 💰 Dynamic Pricing System

The application implements a sophisticated **Decorator-based dynamic pricing engine**:

```java
PricingStrategy pricingStrategy = new BasePricingStrategy();
pricingStrategy = new SurgePricingStrategy(pricingStrategy);
pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
pricingStrategy = new HolidayPricingStrategy(pricingStrategy);

BigDecimal finalPrice = pricingStrategy.calculatePrice(inventory);
```

### Pricing Strategies

| Strategy | Logic |
|----------|-------|
| **BasePricingStrategy** | Returns the room's base price |
| **SurgePricingStrategy** | Multiplies by inventory's surge factor |
| **OccupancyPricingStrategy** | +20% when occupancy exceeds 80% |
| **UrgencyPricingStrategy** | Adjustments for last-minute bookings |
| **HolidayPricingStrategy** | +25% during holiday periods |

---

## 🔐 Authentication & Authorization

### JWT Flow

```
┌─────────┐      ┌──────────┐      ┌─────────────┐
│  User   │──────│  Login   │──────│ JWT Service │
└─────────┘      └──────────┘      └─────────────┘
                      │                   │
                      ▼                   ▼
              ┌───────────────┐   ┌───────────────┐
              │ Access Token  │   │ Refresh Token │
              │ (Response)    │   │ (HTTP Cookie) │
              └───────────────┘   └───────────────┘
```

### Security Configuration

- **Public Endpoints**: `/auth/**`, `/hotels/**`, `/webhook/**`, Swagger UI
- **Authenticated**: `/bookings/**`, `/users/**`
- **HOTEL_MANAGER Role**: `/admin/**`

---

## 💳 Payment Integration

### Stripe Checkout Flow

1. **Initialize Payment** → Create Stripe Checkout Session
2. **Redirect to Stripe** → User completes payment
3. **Webhook Notification** → Stripe notifies backend
4. **Confirm Booking** → Update booking status to CONFIRMED

### Webhook Handling

```java
@PostMapping("/webhook/payment")
public ResponseEntity<Void> capturePayments(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String sigHeader
) {
    Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
    bookingService.capturePayment(event);
    return ResponseEntity.noContent().build();
}
```

---

## 📁 Project Structure

```
airBnbApp/
├── src/main/java/com/example/projects/airBnbApp/
│   ├── AirBnbAppApplication.java          # Main Application
│   ├── advice/                            # Global Exception & Response Handling
│   │   ├── ApiError.java
│   │   ├── ApiResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── GlobalResponseHandler.java
│   ├── config/                            # Configuration Classes
│   │   ├── CorsConfig.java
│   │   ├── MapperConfig.java
│   │   └── StripeConfig.java
│   ├── controller/                        # REST Controllers
│   │   ├── AuthController.java
│   │   ├── HotelController.java
│   │   ├── HotelBookingController.java
│   │   ├── HotelBrowseController.java
│   │   ├── InventoryController.java
│   │   ├── RoomAdminController.java
│   │   ├── UserController.java
│   │   └── WebhookController.java
│   ├── dto/                               # Data Transfer Objects
│   │   ├── BookingDto.java
│   │   ├── HotelDto.java
│   │   ├── RoomDto.java
│   │   ├── UserDto.java
│   │   └── ... (20+ DTOs)
│   ├── entity/                            # JPA Entities
│   │   ├── Booking.java
│   │   ├── Guest.java
│   │   ├── Hotel.java
│   │   ├── HotelContactInfo.java
│   │   ├── HotelMinPrice.java
│   │   ├── Inventory.java
│   │   ├── Room.java
│   │   ├── User.java
│   │   └── enums/
│   │       ├── BookingStatus.java
│   │       ├── Gender.java
│   │       ├── PaymentStatus.java
│   │       └── Role.java
│   ├── exception/                         # Custom Exceptions
│   │   ├── ResourceNotFoundException.java
│   │   └── UnauthorizedException.java
│   ├── repository/                        # Data Repositories
│   │   ├── BookingRepository.java
│   │   ├── GuestRepository.java
│   │   ├── HotelMinPriceRepository.java
│   │   ├── HotelRepository.java
│   │   ├── InventoryRepository.java
│   │   ├── RoomRepository.java
│   │   └── UserRepository.java
│   ├── security/                          # Security Configuration
│   │   ├── AuthService.java
│   │   ├── JWTAuthFilter.java
│   │   ├── JWTService.java
│   │   └── WebSecurityConfig.java
│   ├── service/                           # Business Logic
│   │   ├── BookingService.java
│   │   ├── BookingServiceImpl.java
│   │   ├── CheckoutService.java
│   │   ├── CheckoutServiceImpl.java
│   │   ├── GuestService.java
│   │   ├── GuestServiceImpl.java
│   │   ├── HotelService.java
│   │   ├── HotelServiceImpl.java
│   │   ├── InventoryService.java
│   │   ├── InventoryServiceImpl.java
│   │   ├── PricingUpdateService.java
│   │   ├── RoomService.java
│   │   ├── RoomServiceImpl.java
│   │   ├── UserService.java
│   │   └── UserServiceImpl.java
│   ├── strategy/                          # Pricing Strategies
│   │   ├── BasePricingStrategy.java
│   │   ├── HolidayPricingStrategy.java
│   │   ├── OccupancyPricingStrategy.java
│   │   ├── PricingService.java
│   │   ├── PricingStrategy.java
│   │   ├── SurgePricingStrategy.java
│   │   └── UrgencyPricingStrategy.java
│   └── util/                              # Utility Classes
│       └── AppUtils.java
├── src/main/resources/
│   └── application.properties
├── pom.xml
└── README.md
```

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Your Name**
- GitHub: [@keshrivishal21](https://github.com/keshrivishal21)
- LinkedIn: [Vishal Keshri](https://www.linkedin.com/in/vishal-keshri-14b288262/)

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Stripe API](https://stripe.com/docs/api)
- [PostgreSQL](https://www.postgresql.org/)
- [JWT.io](https://jwt.io/)

---

<p align="center">
  ⭐ Star this repository if you find it helpful!
</p>

