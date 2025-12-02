# Store Locator API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=yunuskilicdev_stores&metric=alert_status)](https://sonarcloud.io/dashboard?id=yunuskilicdev_stores)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=yunuskilicdev_stores&metric=coverage)](https://sonarcloud.io/dashboard?id=yunuskilicdev_stores)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=yunuskilicdev_stores&metric=sqale_index)](https://sonarcloud.io/dashboard?id=yunuskilicdev_stores)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/yunuskilicdev/stores)

A production-ready Spring Boot REST API that finds the nearest stores to a given geographic location using the Haversine distance formula.

## Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Monitoring](#-monitoring)
- [Testing](#-testing)
- [Security](#-security)
- [CI/CD](#-cicd)
- [Performance](#-performance)

## Features

- Nearest Store Search - Find the 5 closest stores to any location
- Haversine Distance - Accurate geographic distance calculations
- Comprehensive Validation - Multi-layer validation with Jakarta Bean Validation
- High Performance - In-memory data with caching (< 50ms response time)
- Observability - Prometheus metrics, Grafana dashboards, health checks
- API Documentation - Interactive Swagger/OpenAPI UI
- Security - OWASP dependency scanning, SonarQube analysis
- 95% Test Coverage** - Unit, integration, and parameterized tests

## Potential Improvements

- Add authentication/authorization
- Implement rate limiting

##  Architecture

### Design Decisions

**In-Memory Storage**  
With ~600 stores in the Netherlands, all data is loaded into memory at startup for optimal performance. No database required.

If scaling beyond this, consider a spatial capabilities like Redis.
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request
       ▼
┌─────────────────────────────────┐
│     REST Controller Layer       │
│  • Request validation           │
│  • OpenAPI documentation        │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│        Service Layer            │
│  • Business logic               │
│  • Distance calculations        │
│  • Caching (@Cacheable)         │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│      Repository Layer           │
│  • JSON data loading            │
│  • In-memory storage            │
│  • Validation at startup        │
└─────────────────────────────────┘
```

### Key Components

- **HaversineDistanceCalculator** - Calculates geographic distances
- **JsonStoreRepository** - Loads and validates store data
- **StoreService** - Finds nearest stores with caching
- **StoreController** - REST endpoints

##  Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.8 |
| **Build Tool** | Maven 3.9+ |
| **Validation** | Jakarta Bean Validation |
| **API Docs** | SpringDoc OpenAPI 3 |
| **Monitoring** | Prometheus + Grafana |
| **Caching** | Spring Cache (Caffeine) |
| **Testing** | JUnit 5, REST Assured, AssertJ |
| **Code Quality** | SonarQube Cloud, PMD |
| **Security** | OWASP Dependency Check |
| **CI/CD** | GitHub Actions |
| **Container** | Docker |

##  Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker (optional, for monitoring)

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/yunuskilicdev/stores.git
cd stores
```

2. **Build the project**
```bash
./mvnw clean install
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

4. **Test the API**
```bash
curl "http://localhost:8080/api/v1/stores/nearest?latitude=52.3676&longitude=4.9041&limit=5"
```

### Docker Compose (with monitoring)

```bash
docker-compose up -d
```

This starts:
- **Store API** - http://localhost:8080
- **Prometheus** - http://localhost:9090
- **Grafana** - http://localhost:3000

## API Documentation

### Interactive API Docs
**Swagger UI**: http://localhost:8080/swagger-ui/index.html

### Main Endpoint

**Find Nearest Stores**
```http
GET /api/v1/stores/nearest?latitude={lat}&longitude={lon}&limit={n}
```

**Parameters:**
- `latitude` (required) - Latitude (-90 to 90)
- `longitude` (required) - Longitude (-180 to 180)
- `limit` (optional) - Number of stores to return (default: 5, max: 50)

**Example Request:**
```bash
curl "http://localhost:8080/api/v1/stores/nearest?latitude=52.3676&longitude=4.9041&limit=5"
```

**Example Response:**
```json
{
  "query": {
    "latitude": 52.3676,
    "longitude": 4.9041,
    "limit": 5
  },
  "results": [
    {
      "uuid": "0d5b5c6e-6b1f-4c7e-8e9f-1a2b3c4d5e6f",
      "city": "Amsterdam",
      "postalCode": "1012 AB",
      "street": "Kalverstraat 1",
      "distance": 1.234
    }
  ],
  "totalFound": 5
}
```

**Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid parameters
- `500 Internal Server Error` - Server error

## Monitoring

### Health Checks
```bash
curl http://localhost:8080/actuator/health
```

### Metrics
- **Prometheus**: http://localhost:9090
- **Grafana Dashboard**: http://localhost:3000/d/jumbo-store-api
  - Username: `admin`
  - Password: `admin`

### Available Metrics
- Request rate and duration
- JVM memory and GC
- Cache hit rate
- Store count

### Grafana Dashboard
Pre-configured dashboard showing:
-  Request rate (req/sec)
-  Response time (p50, p95, p99)
-  Success rate
-  Memory usage

## Testing

### Run All Tests
```bash
./mvnw test
```

### Test Coverage
```bash
./mvnw verify jacoco:report
# View: target/site/jacoco/index.html
```

## Security

### Dependency Scanning
- **OWASP Dependency Check** in CI/CD
- Fails build on CVSS >= 7
- Weekly scans for vulnerabilities

### Code Quality
- **SonarQube Cloud** integration
- Zero critical/blocker issues
- Technical debt < 5%

### Input Validation
- Jakarta Bean Validation
- Custom coordinate validators
- Fail-fast at startup

### Documentation

##  CI/CD

### GitHub Actions Workflow

```yaml
Build & Test → SonarQube Analysis → Security Scan → Docker Build → Deploy
```

### Pipeline Stages

1. **Build & Test**
   - Compile Java 21
   - Run unit & integration tests
   - Generate coverage report

2. **Code Quality** (SonarQube)
   - Static analysis
   - Coverage check (>80%)
   - Quality gate

3. **Security Scan**
   - OWASP dependency check
   - Vulnerability scan
   - Fail on high severity

4. **Docker Build**
   - Multi-stage build
   - Optimized image size
   - Push to registry
