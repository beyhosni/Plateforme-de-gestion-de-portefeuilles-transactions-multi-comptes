# Wallet & Transaction Platform

Plateforme FinTech complète de gestion de portefeuilles et transactions multi-comptes, basée sur une architecture microservices événementielle.

## 🏗️ Architecture

- **6 Microservices** : account, wallet, transaction, categorization, notification, gateway
- **Event-Driven** : RabbitMQ pour communication asynchrone
- **Frontend React** : Interface moderne et responsive
- **Stack complet** : Java 23, Spring Boot 3.4, React 18, PostgreSQL, MongoDB, RabbitMQ

## 📁 Structure du Projet

```
wallet-transaction-platform/
├── backend/
│   ├── parent-pom/              # POM parent Maven
│   ├── shared-library/          # DTOs, Events, Config partagés
│   ├── account-service/         # Gestion utilisateurs & JWT
│   ├── wallet-service/          # Gestion portefeuilles
│   ├── transaction-service/     # Traitement transactions
│   ├── categorization-service/  # Catégorisation auto (MongoDB)
│   ├── notification-service/    # Notifications multi-canaux
│   └── gateway-service/         # API Gateway (Spring Cloud Gateway)
├── frontend/                    # Application React avec Vite
├── infrastructure/
│   ├── docker/                  # Docker Compose
│   ├── kubernetes/              # Manifests K8s
│   └── terraform/               # Infrastructure AWS (EKS, RDS, etc.)
└── docs/                        # Guides de déploiement
```

## 🚀 Démarrage Rapide

### Prérequis

- Java 23
- Maven 3.9+
- Node.js 20+
- Docker & Docker Compose

### Lancement Local

```bash
# Cloner le projet
git clone <repository-url>
cd Plateforme-de-gestion-de-portefeuilles-transactions-multi-comptes

# Lancer avec Docker Compose
cd infrastructure/docker
docker-compose up -d

# Accéder à l'application
# Frontend: http://localhost:3000
# Gateway API: http://localhost:8080
# RabbitMQ Management: http://localhost:15672
```

## 🎯 Fonctionnalités

### Account Service (Port 8081)

- ✅ Inscription et authentification JWT
- ✅ Gestion des utilisateurs
- ✅ Spring Security

### Wallet Service (Port 8082)

- ✅ Création de portefeuilles multi-devises
- ✅ Gestion du solde avec optimistic locking
- ✅ Support de différents types de wallets (CHECKING, SAVINGS, INVESTMENT, BUSINESS)

### Transaction Service (Port 8083)

- ✅ Création et traitement de transactions
- ✅ Support de TRANSFER, DEPOSIT, WITHDRAWAL, PAYMENT
- ✅ Publication d'événements RabbitMQ (completed/failed)

### Categorization Service (Port 8084)

- ✅ Catégorisation automatique basée sur règles
- ✅ MongoDB pour stockage des règles
- ✅ Publication d'événements de catégorisation

### Notification Service (Port 8085)

- ✅ Écoute de tous les événements
- ✅ Support Email (configuré via SMTP)
- ✅ Extensible pour SMS et Push notifications

### Gateway Service (Port 8080)

- ✅ Point d'entrée unique
- ✅ Routing vers microservices
- ✅ Configuration CORS

### Frontend React

- ✅ Authentification (Login/Register)
- ✅ Dashboard avec statistiques
- ✅ Gestion de wallets
- ✅ Historique et création de transactions
- ✅ Design moderne avec gradients et animations

## 📡 Événements RabbitMQ

### Exchanges

- `transaction.events` (topic)
- `categorization.events` (topic)
- `notification.events` (topic)

### Événements

1. **transaction.completed**
   - Publié par : transaction-service
   - Consommé par : wallet-service, categorization-service

2. **transaction.failed**
   - Publié par : transaction-service
   - Consommé par : notification-service

3. **transaction.categorized**
   - Publié par : categorization-service
   - Consommé par : notification-service

## 🗄️ Bases de Données

- **PostgreSQL** (4 instances)
  - account_db (port 5432)
  - wallet_db (port 5433)
  - transaction_db (port 5434)
  - notification_db (port 5435)

- **MongoDB** (port 27017)
  - categorization_db (règles de catégorisation)

- **RabbitMQ** (ports 5672, 15672)
  - Message broker avec management UI

## 🔐 Sécurité

- JWT Authentication avec Spring Security
- Secrets gérés via variables d'environnement
- CORS configuré sur Gateway
- Password encoding avec BCrypt

## 📊 API Endpoints

### Account Service

```
POST /api/accounts/register    # Inscription
POST /api/accounts/login        # Connexion
GET  /api/accounts/{id}         # Détails utilisateur
```

### Wallet Service

```
POST /api/wallets               # Créer un wallet
GET  /api/wallets/user/{userId} # Wallets d'un utilisateur
GET  /api/wallets/{id}          # Détails wallet
```

### Transaction Service

```
POST /api/transactions                # Créer transaction
GET  /api/transactions/wallet/{id}    # Transactions d'un wallet
GET  /api/transactions/{id}           # Détails transaction
```

## 🐳 Docker

Chaque service possède un Dockerfile multi-stage optimisé :

1. **Build stage** : Maven build avec Java 23
2. **Runtime stage** : JRE 23 Alpine (image légère)

## ☸️ Kubernetes

Déploiement production sur EKS avec :

- Deployments avec replicas (2+)
- Services (ClusterIP + LoadBalancer pour Gateway)
- ConfigMaps et Secrets
- PersistentVolumeClaims
- Health checks (liveness/readiness probes)
- Resource limits

## 🏗️ Terraform

Infrastructure AWS complète :

- VPC avec sous-réseaux publics/privés
- EKS Cluster avec node groups
- RDS PostgreSQL multi-AZ
- DocumentDB (MongoDB compatible)
- Amazon MQ (RabbitMQ managed)
- Security Groups
- Auto-scaling

## 📖 Documentation

- [Guide de Déploiement Local](docs/DEPLOYMENT_LOCAL.md)
- [Guide de Déploiement Production](docs/DEPLOYMENT_PRODUCTION.md)

## 🧪 Test de l'Application

### 1. Créer un utilisateur

```bash
curl -X POST http://localhost:8080/api/accounts/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Se connecter

```bash
curl -X POST http://localhost:8080/api/accounts/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

### 3. Créer un wallet

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "userId": 1,
    "name": "Main Checking",
    "currency": "USD",
    "initialBalance": 1000.00,
    "walletType": "CHECKING"
  }'
```

### 4. Créer une transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "sourceWalletId": 1,
    "amount": 50.00,
    "currency": "USD",
    "transactionType": "WITHDRAWAL",
    "description": "Amazon purchase"
  }'
```

## 🔍 Monitoring

- **Actuator Endpoints** : `/actuator/health` sur chaque service
- **RabbitMQ Management** : http://localhost:15672 (guest/guest)
- **Logs** : `docker-compose logs -f <service-name>`

## 🛠️ Technologies

### Backend

- Java 23
- Spring Boot 3.4.0
- Spring Cloud Gateway 2024.0.0
- Spring Data JPA
- Spring Security
- Spring AMQP (RabbitMQ)
- PostgreSQL 16
- MongoDB 7
- JWT (jjwt 0.12.3)
- Lombok
- MapStruct

### Frontend

- React 18
- Vite 5
- React Router 6
- Axios
- CSS moderne (gradients, animations)

### Infrastructure

- Docker & Docker Compose
- Kubernetes 1.28+
- Terraform 1.6+
- AWS (EKS, RDS, DocumentDB, Amazon MQ)

## 📝 License

Ce projet est un exemple de démonstration.

## 👥 Auteur

Créé pour démontrer une architecture microservices complète avec event-driven design.
