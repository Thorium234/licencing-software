# Thorium - Licensing-as-a-Service Platform
## Software Requirements Specification (SRS)

---

## 1. Introduction

### 1.1 Purpose

This document defines the Software Requirements Specification for Thorium, a licensing-as-a-service (LaaS) platform that enables software developers to protect their desktop and web applications through hardware-based node-locked licensing.

### 1.2 Scope

Thorium provides:
- A Java SDK for desktop application integration (node-locked licensing)
- A RESTful API for web application integration
- An Admin Dashboard for developers to manage products and licenses
- Hardware fingerprinting using CPU and Motherboard identifiers
- 16-character license key generation with embedded expiry and cryptographic signatures
- Feature locking with watermark overlay for unregistered software
- Split payment integration via Stripe, Flutterwave, and M-Pesa

### 1.3 Definitions, Acronyms, and Abbreviations

| Term | Definition |
|------|------------|
| HWID | Hardware ID - unique identifier derived from CPU and Motherboard serial numbers |
| LaaS | Licensing-as-a-Service - platform model where Thorium provides licensing for third-party apps |
| Developer | Third-party software developer who integrates Thorium into their application |
| Customer | End-user who purchases a license from the Developer |
| Node-Locked | License bound to specific hardware machine |
| License Key | 16-character Base32/Base36 encoded token containing expiry and signature |
| SDK | Software Development Kit - JAR library for desktop app integration |
| JWT | JSON Web Token for authentication |
| API Key | Developer authentication key for API access |

---

## 2. Functional Requirements

### 2.1 Core Features

#### 2.1.1 Hardware Fingerprinting
- **FR-001**: The system SHALL generate a unique Hardware ID (HWID) by combining CPU ID and Motherboard Serial Number
- **FR-002**: The HWID SHALL be generated using SHA-256 hashing
- **FR-003**: The SDK SHALL use OSHI library to extract hardware information
- **FR-004**: The system SHALL handle missing hardware information gracefully

#### 2.1.2 License Key Generation
- **FR-005**: The Admin App SHALL generate 16-character license keys
- **FR-006**: The license key SHALL embed expiry date (Year/Month) in encoded format
- **FR-007**: The license key SHALL embed a feature mask indicating license tier
- **FR-008**: The license key SHALL include a cryptographic signature to prevent tampering
- **FR-009**: The license key SHALL use Base32 encoding (excluding confusing characters O, 0, I, 1)
- **FR-010**: The Admin SHALL specify the HWID when generating a license key

#### 2.1.3 License Validation
- **FR-011**: The SDK SHALL validate license keys locally without internet connection
- **FR-012**: The SDK SHALL verify the cryptographic signature before accepting a key
- **FR-013**: The SDK SHALL check expiry date against system clock
- **FR-014**: The SDK SHALL verify HWID matches the current machine
- **FR-015**: The API SHALL provide online validation endpoint for web applications
- **FR-016**: The system SHALL detect clock rollback attacks via last_run timestamp file

#### 2.1.4 Feature Locking
- **FR-017**: The system SHALL disable printing when license is invalid or expired
- **FR-018**: The system SHALL overlay "UNREGISTERED - THORIUM DEMO" watermark on printed documents
- **FR-019**: The watermark SHALL have configurable opacity (default 0.3)
- **FR-020**: The watermark SHALL be rotated and centered on the page

#### 2.1.5 Developer Management
- **FR-021**: Developers SHALL be able to register and create accounts
- **FR-022**: Developers SHALL be able to create and manage products
- **FR-023**: Developers SHALL be able to generate API keys for their products
- **FR-024**: Developers SHALL be able to view license analytics

#### 2.1.6 Payment Integration
- **FR-025**: The system SHALL support Stripe payment gateway
- **FR-026**: The system SHALL support Flutterwave payment gateway
- **FR-027**: The system SHALL support M-Pesa mobile money
- **FR-028**: Developers SHALL connect their own bank accounts
- **FR-029**: Payments SHALL bypass Thorium and go directly to developers (split payments)
- **FR-030**: The system SHALL generate license keys automatically upon payment via webhook

### 2.2 User Interactions and Flows

#### 2.2.1 Developer Registration Flow
```
1. Developer visits Admin Dashboard
2. Developer clicks "Register"
3. Developer enters name, email, password
4. System creates Developer account
5. Developer receives API credentials
```

#### 2.2.2 Product Creation Flow
```
1. Developer logs into Admin Dashboard
2. Developer clicks "New Product"
3. Developer enters product name, description, version
4. System creates Product and generates Product ID
5. Developer receives SDK download link
```

#### 2.2.3 License Generation Flow (Offline)
```
1. Customer installs Developer's software
2. Software displays HWID to Customer
3. Customer sends HWID to Developer (via email/payment)
4. Developer enters HWID in Admin Dashboard
5. Developer selects expiry date and features
6. Admin generates 16-char license key
7. Developer sends license key to Customer
8. Customer enters license key in software
9. Software validates and activates license
```

#### 2.2.4 License Validation Flow (Desktop)
```
1. Desktop App starts
2. SDK reads local license file
3. SDK decodes license key
4. SDK verifies cryptographic signature
5. SDK checks expiry date
6. SDK verifies HWID matches current machine
7. SDK checks for clock rollback
8. If valid: Unlock all features
9. If invalid: Lock premium features, show watermark
```

#### 2.2.5 Online Validation Flow (Web)
```
1. User accesses Developer's web app
2. Web app sends license key to Thorium API
3. API validates key against database
4. API returns validation status
5. Web app enables/disables features accordingly
```

### 2.3 Data Flow and Processing

#### 2.3.1 Key Processing Modules

**HardwareFingerprint Module**
- Input: None (reads hardware directly)
- Output: SHA-256 hash string (HWID)
- Responsibility: Extract CPU ID, Motherboard Serial, Hash and return HWID

**LicenseTokenEngine Module**
- Input: HWID, Expiry Date, Feature Mask, Private Key
- Output: 16-character license key
- Responsibility: Encode data into license key format with signature

**LicenseParser Module**
- Input: 16-character license key, Private Key
- Output: Decoded HWID, Expiry Date, Feature Mask, Validation Status
- Responsibility: Decode license key and verify cryptographic signature

**LicenseManager Module**
- Input: License key, Current HWID
- Output: Boolean (valid/invalid), Feature mask
- Responsibility: Coordinate validation, handle lockout, manage license file

**WatermarkService Module**
- Input: GraphicsContext, Opacity level
- Output: Modified GraphicsContext with watermark overlay
- Responsibility: Draw watermark on printed documents

### 2.4 Edge Cases

| ID | Scenario | Expected Behavior |
|----|----------|-------------------|
| EC-001 | Missing CPU ID | Use Motherboard Serial only |
| EC-002 | Missing Motherboard Serial | Use CPU ID only |
| EC-003 | Both CPU and Motherboard missing | Use MAC Address as fallback |
| EC-004 | Expired license | Lock features, show expiry message |
| EC-005 | Tampered license key | Reject key, do not activate |
| EC-006 | Clock rollback detected | Lock app, require online validation |
| EC-007 | Invalid HWID (machine changed) | Reject key, show "machine mismatch" error |
| EC-008 | Corrupted license file | Treat as no license, lock features |
| EC-009 | Network timeout (online validation) | Fall back to offline validation |
| EC-010 | Empty license key input | Show validation error, do not proceed |

---

## 3. Non-Functional Requirements

### 3.1 Performance

- **NFR-001**: License validation SHALL complete in less than 100ms
- **NFR-002**: Hardware fingerprint generation SHALL complete in less than 500ms
- **NFR-003**: API response time SHALL be under 200ms for 95th percentile
- **NFR-004**: The system SHALL support 10,000 concurrent license validations

### 3.2 Scalability

- **NFR-005**: The architecture SHALL support horizontal scaling
- **NFR-006**: Database SHALL handle millions of license records
- **NFR-007**: Cache layer SHALL reduce database load for repeated validations

### 3.3 Security

- **NFR-008**: All API communications SHALL use HTTPS
- **NFR-009**: Passwords SHALL be hashed using bcrypt with salt
- **NFR-010**: License keys SHALL be signed using RSA-4096 or ECDSA
- **NFR-011**: API keys SHALL be stored encrypted in the database
- **NFR-012**: JWT tokens SHALL have 15-minute expiry
- **NFR-013**: Refresh tokens SHALL be rotated after each use
- **NFR-014**: Rate limiting SHALL prevent brute force attacks (100 requests/minute)
- **NFR-015**: Audit logs SHALL capture all administrative actions

### 3.4 Reliability

- **NFR-016**: System uptime SHALL be 99.9% availability
- **NFR-017**: License validation SHALL work offline (local mode)
- **NFR-018**: System SHALL handle network failures gracefully
- **NFR-019**: Data backup SHALL be performed daily

### 3.5 Usability

- **NFR-020**: License key entry SHALL be user-friendly (paste support)
- **NFR-021**: Error messages SHALL be clear and actionable
- **NFR-022**: Admin Dashboard SHALL be intuitive with minimal training

### 3.6 Compatibility

- **NFR-023**: SDK SHALL support Java 11+
- **NFR-024**: SDK SHALL support Windows, macOS, and Linux
- **NFR-025**: Admin App SHALL be built with JavaFX

---

## 4. System Architecture

### 4.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Thorium Platform                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐     ┌──────────────────┐                     │
│  │   React Dashboard │     │   Spring Boot API  │                   │
│  │  (Developer UI)   │◄────►│   (REST Services)  │                  │
│  └──────────────────┘     └─────────┬─────────┘                   │
│                                      │                               │
│  ┌──────────────────────────────────┼──────────────────────────┐   │
│  │                    Spring Boot Services                      │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │   │
│  │  │  License    │  │   Auth      │  │  Payment    │           │   │
│  │  │  Service    │  │  Service    │  │  Service    │           │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘           │   │
│  │         │                │                │                   │   │
│  │  ┌──────┴────────────────┴────────────────┴─────────────┐   │   │
│  │  │                   Data Layer                         │   │   │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐    │   │   │
│  │  │  │PostgreSQL│  │  Redis   │  │    S3/Storage    │    │   │   │
│  │  │  └──────────┘  └──────────┘  └──────────────────┘    │   │   │
│  │  └──────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    Developer SDKs                             │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │  │
│  │  │   Java SDK  │  │  .NET SDK   │  │ Python SDK  │           │  │
│  │  │  (Desktop)  │  │   (Future)  │  │   (Future)  │           │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘           │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Developers                                │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Admin Dashboard (React)                       │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐    │
│  │ Dashboard │  │  Products │  │ Licenses  │  │ Settings  │    │
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘    │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP/REST
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot API Gateway                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    API Endpoints                           │  │
│  │  POST /auth/*  │ GET/POST /products/*  │ GET/POST /licenses│  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
         ▼                    ▼                    ▼
┌─────────────────┐  ┌───────────────┐  ┌─────────────────┐
│ Auth Service    │  │License Service│  │Payment Service  │
│ - JWT           │  │ - Generate    │  │ - Stripe         │
│ - OAuth         │  │ - Validate    │  │ - Flutterwave    │
│ - API Keys      │  │ - Revoke      │  │ - M-Pesa         │
└─────────────────┘  └───────────────┘  └─────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌─────────────────┐  ┌───────────────┐  ┌─────────────────┐
│  PostgreSQL     │  │     Redis     │  │     Webhook     │
│  (Users,Products│  │ (Cache,Ratelimit)  │   Handler       │
│   Licenses)     │  │               │  └─────────────────┘
└─────────────────┘  └───────────────┘
```

### 4.3 Database Schema (ERD)

```
┌────────────────┐       ┌────────────────┐       ┌────────────────┐
│   developers  │       │    products    │       │     users     │
├────────────────┤       ├────────────────┤       ├────────────────┤
│ id (PK)        │◄──────│ developer_id  │       │ id (PK)        │
│ name           │       │ id (PK)        │       │ developer_id  │
│ email          │       │ name           │       │ name           │
│ password_hash │       │ description    │       │ email          │
│ api_key        │       │ version        │       │ password_hash  │
│ stripe_account │       │ created_at     │       │ role           │
│ mpesa_account │       └────────────────┘       │ created_at     │
│ flutter_account│              │                └────────────────┘
│ created_at     │              │ 1:N                     │
└────────────────┘              │                         │
         │                     │                         │
         │1:N                  │                         │
         ▼                     ▼                         ▼
┌────────────────┐    ┌────────────────┐    ┌────────────────┐
│ subscriptions  │    │    licenses    │    │  api_keys     │
├────────────────┤    ├────────────────┤    ├────────────────┤
│ id (PK)        │    │ id (PK)       │    │ id (PK)       │
│ developer_id   │    │ product_id    │    │ developer_id  │
│ plan_id        │    │ license_key   │    │ key           │
│ starts_at      │    │ hwid          │    │ product_id    │
│ ends_at        │    │ status        │    │ expires_at    │
│ status         │    │ expires_at    │    │ created_at    │
│ created_at     │    │ feature_mask  │    └────────────────┘
└────────────────┘    │ created_at    │           │
         │           │ activated_at  │           │
         │           └───────────────┘           │
         │                   │                    │
         │                   │1:N                 │
         ▼                   ▼                    ▼
┌────────────────┐    ┌────────────────┐    ┌────────────────┐
│     plans      │    │    machines    │    │  audit_logs   │
├────────────────┤    ├────────────────┤    ├────────────────┤
│ id (PK)        │    │ id (PK)       │    │ id (PK)       │
│ name           │    │ license_id    │    │ developer_id  │
│ price          │    │ hwid          │    │ action        │
│ duration_months│   │ name          │    │ details       │
│ features       │    │ activated_at  │    │ ip_address    │
└────────────────┘    └────────────────┘    │ created_at    │
                               │           └────────────────┘
                               │1:N
                               ▼
                        ┌────────────────┐
                        │  activations   │
                        ├────────────────┤
                        │ id (PK)        │
                        │ machine_id     │
                        │ ip_address     │
                        │ activated_at   │
                        │ deactivated_at │
                        └────────────────┘
```

---

## 5. API Specification

### 5.1 Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/auth/register | Developer registration |
| POST | /api/v1/auth/login | Developer login |
| POST | /api/v1/auth/refresh | Refresh JWT token |
| POST | /api/v1/auth/logout | Logout and invalidate token |

### 5.2 Product Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/products | List all products |
| POST | /api/v1/products | Create new product |
| GET | /api/v1/products/{id} | Get product details |
| PUT | /api/v1/products/{id} | Update product |
| DELETE | /api/v1/products/{id} | Delete product |

### 5.3 License Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/licenses | List all licenses |
| POST | /api/v1/licenses | Generate new license |
| GET | /api/v1/licenses/{id} | Get license details |
| POST | /api/v1/licenses/validate | Validate license (online) |
| POST | /api/v1/licenses/revoke | Revoke license |
| PUT | /api/v1/licenses/{id} | Update license |

### 5.4 Analytics Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/analytics/overview | Dashboard overview |
| GET | /api/v1/analytics/licenses | License statistics |
| GET | /api/v1/analytics/revenue | Revenue data |

### 5.5 Payment Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/payments/webhook/stripe | Stripe webhook handler |
| POST | /api/v1/payments/webhook/flutterwave | Flutterwave webhook |
| POST | /api/v1/payments/webhook/mpesa | M-Pesa webhook handler |
| GET | /api/v1/payments/accounts | Get connected accounts |
| POST | /api/v1/payments/connect | Connect payment account |

---

## 6. Security Architecture

### 6.1 Authentication Flow

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐
│ Developer│ ──►│  Login  │ ──►│ Validate│ ──►│ JWT     │
│         │    │ Request │    │ Creds   │    │ Token   │
└─────────┘     └─────────┘     └─────────┘     └─────────┘
                                                    │
                                                    ▼
                                          ┌─────────────────┐
                                          │ Access Protected│
                                          │    Resources    │
                                          └─────────────────┘
```

### 6.2 License Validation Flow

```
┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐
│  Desktop   │    │    SDK     │    │   Local    │    │  Features  │
│    App     │───►│  Validates │───►│   Check    │───►│  Unlocked  │
│            │    │   Key      │    │  Signature │    │  orLocked  │
└────────────┘    └────────────┘    └────────────┘    └────────────┘
```

### 6.3 Security Measures

- **JWT Implementation**: 15-minute access tokens, 7-day refresh tokens
- **Password Hashing**: bcrypt with 12 rounds
- **License Signing**: RSA-4096 private key, ECDSA alternative
- **API Rate Limiting**: 100 requests/minute per API key
- **Input Validation**: All inputs sanitized and validated
- **SQL Injection Prevention**: Parameterized queries via JPA
- **XSS Prevention**: Output encoding in React frontend
- **CSRF Protection**: CSRF tokens for state-changing operations

---

## 7. Acceptance Criteria

### 7.1 Functional Acceptance

| ID | Criterion | Test Method |
|----|-----------|--------------|
| AC-001 | HWID is unique per machine | Generate HWID on 3 different machines, verify uniqueness |
| AC-002 | License key encodes expiry correctly | Generate key with known expiry, decode and verify |
| AC-003 | Invalid signatures are rejected | Modify one character in key, verify rejection |
| AC-004 | Expired licenses lock features | Set system clock past expiry, verify lockout |
| AC-005 | Watermark appears on prints | Print document with invalid license, verify watermark |
| AC-006 | Clock rollback is detected | Manually change system time backward, verify detection |
| AC-007 | API validates web licenses | Send valid/invalid keys to API, verify responses |
| AC-008 | Developer can create products | Create product via dashboard, verify in database |
| AC-009 | License generation works | Generate license via admin, verify key format |
| AC-010 | Webhook generates license | Send payment webhook, verify license creation |

### 7.2 Non-Functional Acceptance

| ID | Criterion | Test Method |
|----|-----------|--------------|
| AC-011 | Validation completes < 100ms | Time 1000 validations, calculate average |
| AC-012 | HWID generation < 500ms | Time 100 generations, calculate average |
| AC-013 | API responds < 200ms | Load test with 1000 concurrent requests |
| AC-014 | Offline validation works | Disconnect network, verify local validation |
| AC-015 | Security headers present | Check HTTP responses for security headers |

---

## 8. Appendix

### 8.1 License Key Format

```
┌────────────────────────────────────────────────────┐
│               16-Character License Key             │
├──────────┬─────────┬───────┬───────────────────────┤
│   HWID   │ Expiry  │Feature│    Signature          │
│  Block   │ Block  │ Mask  │      Block            │
│ (4 chars)│(4 chars)│(1 char)│     (7 chars)        │
└──────────┴─────────┴───────┴───────────────────────┘
```

- **HWID Block (4 chars)**: First 4 characters of machine HWID
- **Expiry Block (4 chars)**: Year (2 digits) + Month (2 digits), Base32 encoded
- **Feature Mask (1 char)**: License tier (1-9)
- **Signature Block (7 chars)**: RSA signature of above, Base32 encoded

### 8.2 Technology Stack

| Component | Technology |
|-----------|------------|
| Desktop SDK | Java 11+, OSHI, JavaFX |
| Admin App | Java 17, JavaFX |
| Backend API | Java 21, Spring Boot 3 |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Frontend | React 18, TypeScript, Tailwind CSS |
| Authentication | JWT, Spring Security |
| Payments | Stripe, Flutterwave, M-Pesa |
| Hosting | Docker, Ubuntu, Hetzner/DigitalOcean |

### 8.3 File Structure

```
thorium/
├── thorium-sdk/              # Java SDK for desktop apps
│   ├── src/main/java/
│   │   └── com/thorium/sdk/
│   │       ├── hardware/
│   │       │   └── HardwareFingerprint.java
│   │       ├── license/
│   │       │   ├── LicenseTokenEngine.java
│   │       │   ├── LicenseParser.java
│   │       │   ├── LicenseManager.java
│   │       │   └── LicenseValidator.java
│   │       ├── watermark/
│   │       │   └── WatermarkService.java
│   │       └── ThoriumSDK.java
│   └── pom.xml
│
├── thorium-admin/           # Admin Desktop App
│   ├── src/main/java/
│   │   └── com/thorium/admin/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── model/
│   │       └── view/
│   └── pom.xml
│
├── thorium-api/             # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/thorium/api/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── model/
│   │       └── config/
│   └── pom.xml
│
├── thorium-dashboard/       # React Frontend
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── hooks/
│   └── package.json
│
└── docs/
    ├── SRS.md
    ├── API.md
    └── DATABASE.md
```

---

## 9. Revision History

| Version | Date | Author | Description |
|---------|------|--------|-------------|
| 1.0.0 | 2026-01-15 | Thorium Team | Initial SRS document |

---

*End of Document*