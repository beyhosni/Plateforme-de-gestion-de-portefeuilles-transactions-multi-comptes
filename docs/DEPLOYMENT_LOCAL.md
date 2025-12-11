# Guide de Déploiement Local

## Prérequis

- Java 23
- Maven 3.9+
- Node.js 20+
- Docker & Docker Compose
- Git

## Étapes de Déploiement

### 1. Cloner le Projet

```bash
git clone <repository-url>
cd Plateforme-de-gestion-de-portefeuilles-transactions-multi-comptes
```

### 2. Lancer avec Docker Compose

La méthode la plus simple :

```bash
cd infrastructure/docker
docker-compose up -d
```

Cette commande lance :
- 4 bases PostgreSQL (ports 5432-5435)
- 1 base MongoDB (port 27017)
- RabbitMQ (ports 5672, 15672)
- 6 microservices (ports 8080-8085)
- Frontend React (port 3000)

### 3. Construction Manuelle (Optionnel)

Si vous souhaitez construire les services individuellement :

#### Backend

```bash
# Construction de la bibliothèque partagée
cd backend/shared-library
mvn clean install

# Construction de chaque service
cd ../account-service
mvn clean package

cd ../wallet-service
mvn clean package

# ... répéter pour chaque service
```

#### Frontend

```bash
cd frontend
npm install
npm run build
```

### 4. Vérification des Services

#### Services Backend

- **Account Service** : http://localhost:8081/actuator/health
- **Wallet Service** : http://localhost:8082/actuator/health
- **Transaction Service** : http://localhost:8083/actuator/health
- **Categorization Service** : http://localhost:8084/actuator/health
- **Notification Service** : http://localhost:8085/actuator/health
- **Gateway Service** : http://localhost:8080/actuator/health

#### RabbitMQ Management

- URL : http://localhost:15672
- Username : `guest`
- Password : `guest`

#### Frontend

- URL : http://localhost:3000

### 5. Tester l'Application

#### 1. Créer un compte utilisateur

```bash
curl -X POST http://localhost:8080/api/accounts/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### 2. Se connecter

```bash
curl -X POST http://localhost:8080/api/accounts/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Vous recevrez un JWT token.

#### 3. Créer un wallet

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <votre-token>" \
  -d '{
    "userId": 1,
    "name": "Mon Wallet Principal",
    "currency": "USD",
    "initialBalance": 1000.00,
    "walletType": "CHECKING"
  }'
```

#### 4. Créer une transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <votre-token>" \
  -d '{
    "sourceWalletId": 1,
    "amount": 100.00,
    "currency": "USD",
    "transactionType": "WITHDRAWAL",
    "description": "Achat Amazon"
  }'
```

### 6. Arrêter les Services

```bash
docker-compose down

# Pour supprimer aussi les volumes (données)
docker-compose down -v
```

## Dépannage

### Port déjà utilisé

Si un port est déjà utilisé, vous pouvez :
- Arrêter le processus utilisant le port
- Modifier le port dans `docker-compose.yml`

### Problèmes de mémoire avec Maven

Augmentez la mémoire Java :

```bash
export MAVEN_OPTS="-Xmx2048m"
mvn clean package
```

### RabbitMQ ne démarre pas

Vérifiez les logs :

```bash
docker-compose logs rabbitmq
```

### Services ne peuvent pas se connecter à RabbitMQ

Assurez-vous que RabbitMQ est complètement démarré avant les autres services:

```bash
docker-compose up -d rabbitmq
# Attendre 30 secondes
docker-compose up -d
```

## Logs et Monitoring

Voir les logs d'un service spécifique :

```bash
docker-compose logs -f account-service
docker-compose logs -f transaction-service
```

Voir tous les logs :

```bash
docker-compose logs -f
```

## Variables d'Environnement

Vous pouvez personnaliser les variables dans `docker-compose.yml` :

- `DB_HOST` : Hôte de la base de données
- `RABBITMQ_HOST` : Hôte RabbitMQ
- `JWT_SECRET` : Secret pour JWT (changez-le en production!)
- `WALLET_SERVICE_URL` : URL du wallet service

## Prochaines Étapes

Une fois le système local fonctionnel, consultez `DEPLOYMENT_PRODUCTION.md` pour le déploiement en production sur Kubernetes.
