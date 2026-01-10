# V I B E L E T

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-3.0-38B2AC?style=for-the-badge&logo=tailwind-css)

---

## Transmission Initiated

**Vibelet** is a full-stack social media application. It delivers a strictly chronological content feed, robust privacy controls, and a bidirectional friendship system, all built on a strict Spring Boot architecture.

Built with **Java 17** and **Spring Boot 3.4**, this project serves as a comprehensive demonstration of monolithic architecture integrity, complex relationship management, and secure data handling.

---

## Technical Architecture

Vibelet implements a monolithic architecture adhering to **MVC (Model-View-Controller)** principles, safeguarded by automated architecture tests.

### Tech Stack
* **Core:** Java 17, Spring Boot 3.4.13
* **Data Access:** Spring Data JPA (Hibernate), JDBC Template (Analytics), PostgreSQL (Production), H2 (Test)
* **Security:** Spring Security 6, BCrypt, Custom UserDetailsService
* **Frontend:** Server-side rendering with **Thymeleaf**, styled via **TailwindCSS** (CDN)
* **Quality Assurance:** JUnit 5, Mockito, ArchUnit, Selenium (E2E)
* **DevOps:** Docker, Docker Compose, Gradle

### Key Implementations

#### 1. Complex Relationship Management (`FriendshipService`)
The platform moves beyond simple "followers" to a bidirectional friendship graph.
* **State Machine:** Manages transitions between `PENDING`, `ACCEPTED`, and `REJECTED`.
* **Logic:** Prevents self-friending, duplicate requests, and unauthorized handling of friendship entities.
* **Querying:** Custom JPQL queries resolve the "Squad" (friends list) regardless of who initiated the request.

#### 2. Granular Content Privacy
Content visibility is strictly enforced at the Service layer via the `PrivacyStatus` enum:
* **PUBLIC:** Visible to all authenticated users.
* **FRIENDS_ONLY:** Visible only if a confirmed `ACCEPTED` friendship exists.
* **PRIVATE:** Visible only to the author.

#### 3. Data Sovereignty (Import/Export)
Implemented via `DataExportController`, ensuring full portability of user data:
* **JSON Export:** Serializes user profile, vibes, and friendship connections.
* **Data Restoration:** Transactional import mechanism that rebuilds the social graph and content history while handling duplicates.

#### 4. Architectural Integrity
**ArchUnit** is integrated to enforce strict modularity rules automatically:
* Controllers are prohibited from accessing Repositories directly.
* Service layer boundaries are strictly maintained.
* Circular dependencies are blocked at the build level.

---

## Getting Started

### Prerequisites
* Java 17+
* Docker & Docker Compose (optional, for Database)

### Run via Docker (Recommended)
The project includes a `docker-compose.yml` for instant PostgreSQL integration.

```bash
# 1. Build the application
./gradlew build

# 2. Launch App and Database
docker-compose up --build
```

Access the app at: `http://localhost:8080`

### Run Locally (Dev Mode)
Defaults to H2 in-memory database or local Postgres depending on profile settings.

```bash
./gradlew bootRun
```

---

## Testing Strategy

The project maintains code quality through a multi-layered testing strategy:

* **Unit Tests:** Business logic verification using Mockito (e.g., `FriendshipServiceTest`, `VibeServiceTest`).
* **Integration Tests:** `@DataJpaTest` for repository layer and `@WebMvcTest` for secure endpoints.
* **Architecture Tests:** `ArchitectureTest.java` ensures structural compliance.
* **E2E Tests:** Selenium WebDriver (`LoginE2ETest`) verifies critical user paths in a headless Chrome environment.

---

## Project Structure

```txt
src/main/java/com/example/vibelet
├── config       # MVC and Resource handlers
├── controller   # REST and Web Controllers
├── dto          # Data Transfer Objects
├── exception    # Global Exception Handling
├── model        # JPA Entities (User, Vibe, Friendship, Comment)
├── repository   # JPA Repositories & JDBC Analytics
├── security     # Spring Security Configuration
└── service      # Business Logic (Transaction boundaries)
```

---

**Author:** Damian Mitros