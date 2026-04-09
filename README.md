# Find Home Nepal
**Helping people to find homes quickly and easily**

---

## Vision / Why It Matters
Finding rental properties in Nepal is scattered, slow, and confusing.

**Find Home Nepal** simplifies this by offering a single, intuitive platform where users can **explore, list, and connect with property owners directly**, saving time and frustration.

> "A home shouldn't be hard to find."

---

## Key Features

- **Property Search & Filtering** –  
  Users can filter homes, flats, and rooms based on location, price, amenities, and personal preferences.

- **Real-Time Chat System** –  s
  Users and property owners can communicate instantly through an integrated messaging system.

- **SMS Notifications for OTP & Password Reset** –  
  Users receive secure one-time passwords (OTP) for account verification and can reset their password via SMS.

- **Admin Dashboard & Analytics** –  
  Admins can visualize property listings, user activity, and platform usage through intuitive dashboards.

- **Verified Property Owners** –  
  Only authenticated owners can list properties, ensuring trustworthy listings.

- **Secure & Resilient Platform** –  
  Business logic is handled with retries and rate-limiting for reliability, using Spring Retry, Resilience4j, and Bucket4j.

- **File Upload & Storage Management** –  
  Supports local storage and AWS S3.

---

## Technologies Used
Presented in a cognitive order: core → supporting → advanced features.

- **Backend & Core:** Java 17, Spring Boot, Spring Data JPA/Hibernate
- **Database:** PostgreSQL
- **Caching:** Redis
- **Messaging System:** WebSocket protocol
- **Resilience & Retry:** Resilience4j + Spring Retry, Bucket4j
- **Monitoring:** Spring Actuator
- **API Documentation:** Swagger/OpenAPI
- **Testing:** JUnit, Mockito
- **Containerization & Deployment:** Docker, Docker Compose, AWS EC2

---

## Setup

```bash
# Clone the repository
git clone <repo-link>
cd find-home-nepal

# Start services using Docker Compose
docker-compose up -d

# Application runs at http://localhost:8080 by default
```

##  Live Demo

 **Live Application:** https://www.findhomenepal.com/

Experience the platform in action:
-  Search and filter properties in real-time
-  Chat directly with property owners
-  Secure authentication with OTP verification
