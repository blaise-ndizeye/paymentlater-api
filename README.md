# PaymentLater API 🚀

> A simulated payment API designed to replicate real-world payment systems (e.g., IremboPay, Stripe, or mobile money APIs)

[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

## 📖 Overview

**PaymentLater** is a developer-friendly mock payment API that eliminates the barriers developers face when integrating payment systems into their projects. Built with **Kotlin and Spring Boot**, it provides comprehensive RESTful endpoints that simulate real-world payment provider behavior without the overhead of legal documentation, sandbox registration, or compliance requirements.

Perfect for prototyping, testing, learning, and building demos without the complexity of real payment integrations.

## 🎯 Motivation

Most developers face common barriers when building payment-enabled applications:

- **Complex registration processes** requiring business documentation
- **Lengthy approval workflows** with government-issued documents
- **Complicated sandbox environments** with usage limitations
- **Legal terms and compliance requirements** for simple prototypes

**PaymentLater** solves these problems by providing:
- ✅ **Instant access** - No paperwork or registration required
- ✅ **Simple integration** - Standard RESTful API design
- ✅ **Real-world simulation** - Webhooks, wallets, and transaction flows
- ✅ **Developer-focused** - Built for learning and rapid prototyping

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔐 **Dual Authentication** | JWT tokens for admins, API keys for merchants |
| 💳 **Payment Processing** | Complete payment lifecycle simulation |
| 🔄 **Webhook Support** | Real-time event notifications |
| 💰 **Multi-Currency** | Support for multiple currency types |
| 📊 **Analytics Dashboard** | Merchant and system analytics |
| ♻️ **Refund Management** | Full refund processing workflow |
| 🔍 **Transaction History** | Comprehensive transaction tracking |
| 📧 **Email Notifications** | Automated email alerts and confirmations |
| 📚 **Interactive API Docs** | Swagger UI for easy testing |
| 🛡️ **Security Features** | BCrypt hashing, JWT tokens, API key authentication |

## 🏗️ Architecture

### Tech Stack
- **Backend**: Kotlin + Spring Boot 3.5.3
- **Database**: MongoDB with auto-indexing
- **Security**: Spring Security with JWT & API Key authentication
- **Documentation**: OpenAPI 3 with Swagger UI
- **Email**: Spring Mail with SMTP integration
- **Testing**: JUnit 5, MockK, Spring Boot Test
- **Containerization**: Docker with multi-stage builds
- **Deployment**: Docker Compose with production configurations
- **Images**: Eclipse Temurin OpenJDK 21 on Alpine Linux

### Project Structure
```
src/main/kotlin/com/blaise/paymentlater/
├── config/                 # Application configurations
├── controller/             # REST API controllers
├── domain/                # Domain models and entities
│   ├── enum/              # Enumerations
│   ├── exception/         # Custom exceptions
│   ├── extension/         # Kotlin extensions
│   └── model/            # Data models
├── dto/                   # Data Transfer Objects
│   ├── request/           # Request DTOs
│   └── response/          # Response DTOs
├── repository/            # MongoDB repositories
├── security/              # Security configurations & filters
│   ├── admin/             # Admin authentication
│   ├── merchant/          # Merchant authentication
│   └── shared/            # Shared security components
└── service/               # Business logic services
    └── v1/                # API version 1 services
```

## 🐳 Docker & Security Features

### 🔒 Production-Ready Deployment

| Feature | Development | Production | High-Security |
|---------|------------|-------------|---------------|
| **Multi-stage builds** | ✅ | ✅ | ✅ |
| **Optimized images** | ✅ Alpine JRE | ✅ Alpine JRE | ✅ Alpine JRE |
| **Unit tests in build** | ✅ | ✅ | ✅ |
| **Non-root user** | ✅ | ✅ | ✅ |
| **Environment variables** | ✅ | ✅ Docker Secrets | ✅ Docker Secrets |
| **Health checks** | ✅ | ✅ | ✅ |
| **Resource limits** | ⚠️ | ✅ | ✅ |
| **Read-only filesystem** | ❌ | ❌ | ✅ |
| **Security hardening** | ❌ | ⚠️ | ✅ |
| **Network isolation** | ✅ | ✅ | ✅ Enhanced |
| **Intrusion detection** | ❌ | ❌ | ✅ Fail2ban |

### 🔍 Container Optimization

- **🏃 Fast builds**: Multi-stage caching reduces build time by ~60%
- **💻 Small images**: Final image ~283MB (vs 1GB+ typical Spring Boot)
- **🛡️ Security**: Non-root execution, capability dropping, secrets management
- **📊 Monitoring**: Built-in health checks, metrics, and logging
- **🔄 CI/CD Ready**: Optimized for automated deployments

### 🛠️ Development Tools Included

```bash
# MongoDB Admin Interface (development)
docker-compose --profile tools up -d
# Access: http://localhost:8081

# Security monitoring (production)
docker-compose -f docker-compose.prod.yml --profile security-tools up -d
```

## 🚀 Quick Start

### Prerequisites
- **Docker & Docker Compose** (Recommended) OR
- **Java 21+** + **MongoDB 4.4+** (Manual setup)
- **SMTP Email Server** (Gmail recommended)

### 🐳 Docker Setup (Recommended)

> **The fastest way to get started!** Complete with MongoDB, security hardening, and development tools.

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/PaymentLaterAPI.git
   cd PaymentLaterAPI
   ```

2. **Set up environment variables**
   ```bash
   # Copy template and customize
   cp .env.example .env
   # Edit .env with your actual values (required!)
   ```

3. **Start the application**
   ```bash
   # Development mode (with MongoDB admin UI)
   docker-compose --profile tools up -d
   
   # Production mode  
   docker-compose -f docker-compose.prod.yml up -d
   
   # High-security production
   docker-compose -f docker-compose.prod.yml -f docker-compose.security.yml up -d
   ```

4. **Access the services**
   - **API Base URL**: `http://localhost:1010`
   - **Swagger UI**: `http://localhost:1010/swagger-ui.html`
   - **API Documentation**: `http://localhost:1010/api-docs`
   - **MongoDB Admin** (dev only): `http://localhost:8081`
   - **Health Check**: `http://localhost:1010/actuator/health`

### ⚙️ Manual Setup (Alternative)

1. **Clone and setup environment**
   ```bash
   git clone https://github.com/yourusername/PaymentLaterAPI.git
   cd PaymentLaterAPI
   cp .env.example .env  # Edit with your values
   ```

2. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

### 📚 Complete Docker Guide

For comprehensive Docker usage, security configurations, and production deployment, see:
**[📖 Docker Cheatsheet](DOCKER_CHEATSHEET.md)**

## 📚 API Documentation

### Authentication

#### Admin Authentication (JWT)
```bash
# Register Admin
POST /api/v1/admin/auth/register
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@example.com",
  "password": "securepassword"
}

# Login
POST /api/v1/admin/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "securepassword"
}
```

#### Merchant Authentication (API Key)
```bash
# Register Merchant
POST /api/v1/merchant/auth/register
Content-Type: application/json

{
  "name": "My Business",
  "email": "merchant@example.com",
  "webhookUrl": "https://mybusiness.com/webhook"
}

# Use API Key in requests
GET /api/v1/merchant/auth/me
X-API-KEY: your-64-character-api-key
```

### Payment Processing

#### Create Payment Intent
```bash
POST /api/v1/payments
X-API-KEY: your-api-key
Content-Type: application/json

{
  "items": [
    {
      "name": "Electricity & Water",
      "description": "Monthly bills",
      "unitAmount": 20,
      "quantity": 2
    }
  ],
  "currency": "EUR",
  "metadata": {
    "referenceId": "123XXX",
    "userId": "123",
    "phone": "949784925244606",
    "email": "customer@example.com",
    "description": "Paying for bills"
  }
}
```

#### Confirm Payment Intent
```bash
POST /api/v1/payments/{paymentIntentId}/confirm
X-API-KEY: your-api-key
Content-Type: application/json

{
  "paymentMethod": "MOBILE_MONEY",
  "metadata": {
    "customerEmail": "alice@example.com",
    "customerPhone": "+250788123456",
    "customerName": "Alice Johnson",
    "referenceId": "REF-1001",
    "description": "Payment for order #1024",
    "failureReason": null,
    "refundReason": null,
    "gatewayResponseCode": "00",
    "extra": {
      "network": "MTN Rwanda",
      "transactionId": "MTN-TXN-78901",
      "paymentSession": "SESSION-ABC123"
    }
  }
}
```

### Webhook Configuration
```bash
POST /api/v1/merchant/auth/set-webhook
X-API-KEY: your-api-key
Content-Type: application/json

{
  "webhookUrl": "https://mybusiness.com/webhook1"
}
```

## 🔌 Integration Examples

### Kotlin/Android
```kotlin
// Add to your build.gradle.kts
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}

// API Client
interface PaymentLaterApiService {
    @POST("payments")
    suspend fun createPaymentIntent(
        @Header("X-API-KEY") apiKey: String,
        @Body request: PaymentIntentRequest
    ): PaymentIntentResponse
}

// Usage
class PaymentRepository {
    private val api = // ... retrofit instance
    
    suspend fun processPayment(amount: Long): PaymentIntentResponse {
        val metadata = mapOf<String, Any>(...)
        val items = listOf<Map<String, Any>>(...)
        
        return api.createPaymentIntent(
            apiKey = "your-api-key",
            request = PaymentIntentRequest(
                items = items,
                metadata = metadata,
                currency = "RWF"
            )
        )
    }
}
```

### JavaScript/React
```javascript
// Payment service
class PaymentLaterService {
    constructor(apiKey) {
        this.apiKey = apiKey;
        this.baseUrl = 'http://localhost:1010/api/v1';
    }
    
    async createPaymentIntent(paymentData) {
        const response = await fetch(`${this.baseUrl}/payments/`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-KEY': this.apiKey
            },
            body: JSON.stringify(paymentData)
        });
        return response.json();
    }
}

// React component
function CheckoutForm() {
    const paymentService = new PaymentLaterService('your-api-key');
    
    const handlePayment = async (amount) => {
        const metadata = {...}
        const items = [...]
        
        const intent = await paymentService.createPaymentIntent({
            items: items,
            metadata: metadata,
            currency: 'RWF'
        });
        
        // Handle payment confirmation
        console.log('Payment Intent:', intent);
    };
}
```

## 🎯 Use Cases

### 📱 **Mobile App Development**
- Prototype e-commerce checkout flows
- Test payment integration in development
- Demo payment features to stakeholders

### 🎓 **Educational Projects**
- University assignments requiring payment features
- Bootcamp projects demonstrating full-stack skills
- Teaching payment system architecture

### 🏆 **Hackathons & MVPs**
- Quick payment integration for hackathon projects
- MVP development without payment provider delays
- Proof of concept demonstrations

### 🧪 **Testing & Development**
- Frontend payment flow testing
- API integration testing
- Webhook handling development

## 📊 Monitoring & Analytics

### Health Check
```bash
GET /actuator/health
```

### System Analytics (Admin)
```bash
GET /api/v1/admin/analytics/system/{windowHours}
Authorization: Bearer jwt-token
```

### Merchant Analytics
```bash
GET /api/v1/merchant/analytics/overview
X-API-KEY: your-api-key
```

## 🛡️ Security Features

### 🔐 Application Security
- **JWT Authentication** with 15-hour access tokens and 30-day refresh tokens
- **API Key Authentication** with SHA-256 hashing
- **BCrypt Password Hashing** for admin accounts
- **Role-Based Access Control** (RBAC)
- **Request Rate Limiting** (planned)
- **HTTPS Support** (configure with SSL certificates)

### 🐳 Container Security (Docker)
- **🚫 Non-root execution**: All containers run as unprivileged users
- **🔒 Docker Secrets**: Production credentials never stored in images
- **📝 Read-only filesystems**: Immutable container environments (high-security mode)
- **🚪 Capability dropping**: Removes dangerous Linux capabilities
- **🔥 Resource limits**: DoS protection through memory/CPU constraints
- **🌐 Network isolation**: Secure bridge networking with custom subnets
- **📊 Health monitoring**: Automated health checks and restart policies
- **🔍 Intrusion detection**: Optional Fail2ban integration for attack prevention

### 🔍 Security Monitoring
```bash
# Check security compliance
docker inspect payment-later-api-prod | jq '.HostConfig | {SecurityOpt, ReadonlyRootfs, CapDrop}'

# Monitor failed attempts
docker-compose logs | grep -i "failed\|error\|unauthorized"

# Resource usage monitoring
docker stats payment-later-api-prod
```

## 🧪 Testing

### Local Testing
```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

### Docker Testing
```bash
# Tests are automatically run during Docker build
docker build -t payment-later-api:test .

# Run tests in existing container
docker-compose exec payment-later-api ./gradlew test

# Integration testing with full stack
docker-compose up -d
curl http://localhost:1010/actuator/health
```

## 🤝 Contributing

We welcome contributions from developers who want to:

- 🔧 **Add new features** or endpoints
- 📱 **Build SDKs** for other platforms (JavaScript, Flutter, Python)
- 📝 **Improve documentation** and examples
- 🧪 **Write tests** and improve code quality
- 🐛 **Fix bugs** and performance issues
- 💡 **Suggest new use cases** and improvements

### Contribution Guidelines

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b fea/amazing-feature`)
3. **Follow coding standards** (Kotlin conventions, comprehensive documentation)
4. **Add tests** for new functionality
5. **Update documentation** as needed
6. **Commit changes** (`git commit -m 'fea(amazing-feature): Add amazing feature'`)
7. **Push to branch** (`git push origin fea/amazing-feature`)
8. **Open a Pull Request**

### Development Setup

```bash
# Clone your fork
git clone https://github.com/yourusername/PaymentLaterAPI.git

# Add upstream remote
git remote add upstream https://github.com/original/PaymentLaterAPI.git

# Create development branch
git checkout -b develop

# Install dependencies and run
./gradlew bootRun
```

## 📋 Roadmap

### ✅ **Completed**
- [x] Core REST API implementation
- [x] JWT and API Key authentication
- [x] Payment intent and confirmation flow
- [x] Webhook system
- [x] Email notifications
- [x] MongoDB integration with auto-indexing
- [x] Comprehensive documentation
- [x] Swagger UI integration
- [x] **Docker containerization** with multi-stage builds
- [x] **Production-ready deployment** configurations
- [x] **Security hardening** with container security features
- [x] **Environment management** with secrets and variables
- [x] **Development tooling** (MongoDB Express, health checks)
- [x] **Comprehensive Docker documentation** and cheatsheets

### 🚧 **In Progress**
- [ ] Kotlin SDK
- [ ] Enhanced analytics and reporting
- [ ] Rate limiting and throttling
- [ ] Comprehensive testing suite

### 📅 **Planned**
- [ ] **SDK Development**
  - [ ] Kotlin/Android SDK
  - [ ] JavaScript/TypeScript SDK
  - [ ] Flutter/Dart SDK
  - [ ] Python SDK
- [ ] **Additional Features**
  - [ ] Subscription billing simulation
  - [ ] Wallet Integration
  - [ ] Multi-tenant support
  - [ ] Payment method expansion
  - [ ] Advanced fraud detection simulation
- [ ] **DevOps & Deployment**
  - [x] ~~Docker containerization~~ ✅ **Completed**
  - [ ] Kubernetes deployment configs
  - [ ] CI/CD pipeline setup
  - [ ] Advanced monitoring and observability

## ⚠️ Important Disclaimers

- **🚫 Not for Production**: This is a mock service for testing and development only
- **💰 No Real Money**: All transactions are simulated - no actual funds are processed
- **🔒 Educational Security**: Security implementations are practical but not enterprise-grade
- **📜 No Legal Compliance**: Not intended for PCI-DSS, AML, or KYC compliance

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Boot Team** for the excellent framework
- **Kotlin Team** for the amazing language
- **MongoDB** for reliable data persistence
- **OpenAPI Initiative** for standardized API documentation
- **Contributors** who make this project better

## 📞 Support & Community

- **📧 Email**: [blaiseendizeye@gmail.com](mailto:blaiseendizeye@gmail.com)
- **🐛 Issues**: [GitHub Issues](https://github.com/blaise-ndizeye/paymentlater-api/issues)

---

**⭐ If you find PaymentLater API helpful, please star this repository to support the project!**

<div align="center">
  Made with ❤️ by developers, for developers
</div>