# PaymentLaterAPI Docker Compose Cheatsheet

This guide provides comprehensive commands for running your PaymentLaterAPI in both development and production modes using Docker Compose.

## 🚀 Quick Start

### Development Mode
```bash
# Start all services in development mode
docker-compose up -d

# Start with logs visible
docker-compose up

# Start specific service
docker-compose up -d mongodb payment-later-api
```

### Production Mode
```bash
# Setup secrets first, then:
docker-compose -f docker-compose.prod.yml up -d

# With nginx reverse proxy
docker-compose -f docker-compose.prod.yml --profile nginx up -d
```

---

## 📋 Environment Setup

### Development (.env file - REQUIRED)
**First, create your .env file:**
```bash
# Copy the template and customize
cp .env.example .env

# Edit with your actual values
nano .env  # or your preferred editor
```

**Required environment variables:**
```bash
# Database Configuration
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=your-secure-password
MONGO_DATABASE=payment_later_dev

# JWT Secret (Generate: openssl rand -base64 64)
JWT_SECRET_BASE64=your-base64-jwt-secret

# Mail Configuration (Gmail App Password recommended)
MAIL_SERVER_EMAIL=your-email@gmail.com
MAIL_SERVER_PASSWORD=your-gmail-app-password

# Development Tools
MONGO_EXPRESS_USERNAME=admin
MONGO_EXPRESS_PASSWORD=express-password
SPRING_PROFILES_ACTIVE=dev
```

### Production (Docker Secrets)
```bash
# Create production secrets
echo "admin" | docker secret create mongo_root_username -
echo "your-secure-password" | docker secret create mongo_root_password -
echo "your-mongodb-password" | docker secret create mongodb_password -
echo "your-base64-jwt-secret" | docker secret create jwt_secret -
echo "your-email@gmail.com" | docker secret create mail_server_email -
echo "your-mail-password" | docker secret create mail_server_password -
```

---

## 🛠 Development Commands

### Starting Services
```bash
# Start all services
docker-compose up -d

# Start with rebuild
docker-compose up -d --build

# Start specific services
docker-compose up -d mongodb
docker-compose up -d payment-later-api

# Start with MongoDB admin UI
docker-compose --profile tools up -d
```

### Viewing Logs
```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f payment-later-api
docker-compose logs -f mongodb

# View last 100 lines
docker-compose logs --tail=100 payment-later-api
```

### Managing Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Restart a service
docker-compose restart payment-later-api

# Scale application (if needed)
docker-compose up -d --scale payment-later-api=3
```

### Database Management
```bash
# Access MongoDB shell
docker-compose exec mongodb mongosh -u admin -p admin123 --authenticationDatabase admin

# Access Mongo Express (Web UI)
# Navigate to: http://localhost:8081
# Username: admin, Password: admin123
```

### Development Debugging
```bash
# Execute commands in running container
docker-compose exec payment-later-api sh

# Check application health
curl http://localhost:1010/actuator/health

# View API documentation
curl http://localhost:1010/api-docs
```

---

## 🏭 Production Commands

### Setting Up Production Environment
```bash
# 1. Create production directory structure
sudo mkdir -p /opt/payment-later/data/mongodb
sudo mkdir -p /opt/payment-later/logs
sudo mkdir -p /opt/payment-later/config

# 2. Create Docker secrets
echo "admin" | docker secret create mongo_root_username -
echo "$(openssl rand -base64 32)" | docker secret create mongo_root_password -
echo "$(openssl rand -base64 32)" | docker secret create mongodb_password -
echo "$(openssl rand -base64 64)" | docker secret create jwt_secret -
echo "production-email@company.com" | docker secret create mail_server_email -
echo "production-mail-password" | docker secret create mail_server_password -
```

### Starting Production Services
```bash
# Start production environment
docker-compose -f docker-compose.prod.yml up -d

# Start with nginx reverse proxy
docker-compose -f docker-compose.prod.yml --profile nginx up -d

# Start with enhanced security
docker-compose -f docker-compose.prod.yml -f docker-compose.security.yml up -d

# Start with rebuild
docker-compose -f docker-compose.prod.yml up -d --build
```

### Production Monitoring
```bash
# View production logs
docker-compose -f docker-compose.prod.yml logs -f

# Check service health
docker-compose -f docker-compose.prod.yml ps

# Monitor resource usage
docker stats payment-later-api-prod payment-later-mongodb-prod

# Check application metrics
curl http://localhost:1010/actuator/metrics
```

### Production Maintenance
```bash
# Stop production services
docker-compose -f docker-compose.prod.yml down

# Update application (zero-downtime)
docker-compose -f docker-compose.prod.yml up -d --no-deps payment-later-api

# Database backup
docker-compose -f docker-compose.prod.yml exec mongodb \
  mongodump --authenticationDatabase admin -u admin -p "$(docker secret inspect mongo_root_password --format '{{.Spec.Data}}')" \
  --out /data/backup/$(date +%Y%m%d_%H%M%S)

# View disk usage
docker system df
```

---

## 🔒 Enhanced Security Configuration

### High-Security Production Deployment
```bash
# Deploy with maximum security hardening
docker-compose -f docker-compose.prod.yml -f docker-compose.security.yml up -d

# Deploy with security monitoring
docker-compose -f docker-compose.prod.yml -f docker-compose.security.yml --profile security-tools up -d

# View security-hardened containers
docker-compose -f docker-compose.prod.yml -f docker-compose.security.yml ps
```

### Security Features Enabled
- ✅ **No new privileges**: Prevents privilege escalation
- ✅ **Read-only filesystems**: Immutable container filesystems
- ✅ **Capability dropping**: Removes unnecessary Linux capabilities
- ✅ **Resource limits**: DoS protection through memory/CPU limits
- ✅ **Enhanced MongoDB auth**: SCRAM-SHA-256 with increased iterations
- ✅ **JVM security**: Security policies and hardened flags
- ✅ **Network isolation**: Secure bridge networking
- ✅ **Fail2ban integration**: Optional intrusion prevention

### Security Monitoring
```bash
# Check security compliance
docker inspect payment-later-api-prod | jq '.[] | {SecurityOpt: .HostConfig.SecurityOpt, ReadonlyRootfs: .HostConfig.ReadonlyRootfs}'

# Monitor failed login attempts (with Fail2ban)
docker-compose -f docker-compose.prod.yml -f docker-compose.security.yml logs fail2ban

# Audit container capabilities
docker inspect payment-later-api-prod | jq '.[] | .HostConfig | {CapAdd: .CapAdd, CapDrop: .CapDrop}'
```

---

## 🔍 Debugging & Troubleshooting

### Common Issues
```bash
# Check if ports are available
netstat -tulpn | grep :1010
netstat -tulpn | grep :27017

# View Docker system information
docker system info
docker system df

# Check container resource usage
docker stats

# View container processes
docker-compose top
```

### Container Inspection
```bash
# Inspect running container
docker inspect payment-later-api-dev

# View container environment
docker-compose exec payment-later-api env

# Check container file system
docker-compose exec payment-later-api ls -la /app
```

### Network Troubleshooting
```bash
# List Docker networks
docker network ls

# Inspect network
docker network inspect paymentlaterapi_payment-later-network

# Test connectivity between containers
docker-compose exec payment-later-api ping mongodb
```

---

## 🧪 Testing

### Running Unit Tests in Docker
```bash
# Run tests during build (automatic in Dockerfile)
docker build -t payment-later-api:test --target build .

# Run tests in existing container
docker-compose exec payment-later-api ./gradlew test

# Run specific test
docker-compose exec payment-later-api ./gradlew test --tests "ClassName.methodName"
```
---

## 📊 Monitoring & Health Checks

### Health Check Endpoints
```bash
# Application health
curl http://localhost:1010/actuator/health

# Detailed health info
curl http://localhost:1010/actuator/health/detailed

# Application info
curl http://localhost:1010/actuator/info

# Metrics
curl http://localhost:1010/actuator/metrics
```

### Log Management
```bash
# Configure log rotation (production)
docker-compose -f docker-compose.prod.yml config

# View aggregated logs
docker-compose logs --since="1h" --until="30m"

# Export logs
docker-compose logs --no-color > application.log
```

---

## 🔐 Security Best Practices

### Production Security Checklist
- ✅ Use Docker secrets for sensitive data
- ✅ Run containers as non-root user
- ✅ Bind services to localhost only
- ✅ Use specific image tags (not `latest`)
- ✅ Regular security updates
- ✅ Implement proper logging
- ✅ Use reverse proxy (nginx)
- ✅ Enable container resource limits

### Security Commands
```bash
# Scan image for vulnerabilities
docker scout cves payment-later-api:latest

# Check for secrets in logs
docker-compose logs | grep -i "password\|secret\|key"

# Update base images
docker-compose pull
docker-compose up -d --build
```

---

## 🚀 Performance Optimization

### Performance Tuning
```bash
# Enable Docker BuildKit for faster builds
export DOCKER_BUILDKIT=1

# Use multi-stage build caching
docker build --target build -t payment-later-api:build-cache .
docker build --cache-from payment-later-api:build-cache .

# Optimize JVM settings (already configured in prod)
# -XX:+UseContainerSupport
# -XX:MaxRAMPercentage=80.0
```

### Resource Management
```bash
# Check resource usage
docker-compose -f docker-compose.prod.yml exec payment-later-api \
  cat /proc/meminfo

# Monitor JVM memory
docker-compose exec payment-later-api \
  jcmd 1 VM.info
```

---

## 📦 Cleanup

### Development Cleanup
```bash
# Stop and remove everything
docker-compose down -v --remove-orphans

# Remove unused images
docker image prune -a

# Complete cleanup
docker system prune -a --volumes
```

### Production Cleanup (Careful!)
```bash
# Graceful shutdown
docker-compose -f docker-compose.prod.yml down

# Remove old images (keep data volumes)
docker image prune -a

# Full cleanup (⚠️ DESTROYS ALL DATA)
docker-compose -f docker-compose.prod.yml down -v
docker system prune -a --volumes
```

---

## 🔗 Useful URLs

- **Application**: http://localhost:1010
- **API Documentation**: http://localhost:1010/swagger-ui.html
- **API Docs JSON**: http://localhost:1010/api-docs
- **Health Check**: http://localhost:1010/actuator/health
- **MongoDB Express**: http://localhost:8081 (dev only)
- **Application Metrics**: http://localhost:1010/actuator/metrics

## 🆘 Emergency Commands

```bash
# Force restart everything
docker-compose down --remove-orphans && docker-compose up -d --build

# Emergency database backup
docker-compose exec mongodb mongodump --archive=backup.gz --gzip

# View critical errors only
docker-compose logs | grep -i error

# Check if application is responding
curl -I http://localhost:1010/actuator/health
```

---

**Note**: Always test these commands in a development environment before running them in production!