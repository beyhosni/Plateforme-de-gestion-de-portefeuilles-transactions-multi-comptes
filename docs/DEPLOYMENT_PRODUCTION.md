# Guide de Déploiement Production

## Vue d'ensemble

Ce guide vous accompagne pour déployer la plateforme Wallet & Transaction sur AWS avec Kubernetes (EKS).

## Architecture de Production

```
Internet
    ↓
Load Balancer (Gateway Service)
    ↓
EKS Cluster (Microservices)
    ↓
├── RDS PostgreSQL (4 databases)
├── DocumentDB (MongoDB compatible)
└── Amazon MQ (RabbitMQ)
```

## Prérequis

- Compte AWS avec permissions appropriées
- AWS CLI configuré
- kubectl installé
- Terraform >= 1.6.0
- Docker (pour construire les images)
- Accès à un registry Docker (ECR, Docker Hub, etc.)

## Étape 1 : Préparer les Images Docker

### 1.1 Créer un ECR Repository

```bash
aws ecr create-repository --repository-name wallet-platform/account-service
aws ecr create-repository --repository-name wallet-platform/wallet-service
aws ecr create-repository --repository-name wallet-platform/transaction-service
aws ecr create-repository --repository-name wallet-platform/categorization-service
aws ecr create-repository --repository-name wallet-platform/notification-service
aws ecr create-repository --repository-name wallet-platform/gateway-service
aws ecr create-repository --repository-name wallet-platform/frontend
```

### 1.2 Construire et Pousser les Images

```bash
# Login à ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Construire et pousser chaque service
cd backend/account-service
docker build -t <account-id>.dkr.ecr.us-east-1.amazonaws.com/wallet-platform/account-service:latest .
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/wallet-platform/account-service:latest

# Répéter pour chaque service...
```

## Étape 2 : Déployer l'Infrastructure avec Terraform

### 2.1 Initialiser Terraform

```bash
cd infrastructure/terraform
terraform init
```

### 2.2 Créer le fichier terraform.tfvars

```hcl
aws_region = "us-east-1"
project_name = "wallet-platform"

# Sécurisez ces valeurs!
db_password = "votre-mot-de-passe-securise"
docdb_password = "votre-mot-de-passe-securise"
rabbitmq_password = "votre-mot-de-passe-securise"
```

### 2.3 Déployer l'Infrastructure

```bash
# Prévisualiser les changements
terraform plan

# Appliquer
terraform apply
```

Cette commande crée :
- VPC avec sous-réseaux publics et privés
- Cluster EKS avec node groups
- RDS PostgreSQL
- DocumentDB (MongoDB compatible)
- Amazon MQ (RabbitMQ)
- Security groups et networking

### 2.4 Récupérer les Informations

```bash
terraform output
```

Notez les endpoints pour la configuration Kubernetes.

## Étape 3 : Configurer kubectl

```bash
aws eks update-kubeconfig --region us-east-1 --name wallet-transaction-platform-cluster
```

Vérifier la connexion :

```bash
kubectl get nodes
```

## Étape 4 : Déployer les Services Kubernetes

### 4.1 Créer le Namespace

```bash
kubectl apply -f ../kubernetes/namespace.yaml
```

### 4.2 Créer les Secrets

Modifier `secrets.yaml` avec vos valeurs :

```bash
kubectl apply -f ../kubernetes/secrets.yaml
```

### 4.3 Déployer RabbitMQ

```bash
kubectl apply -f ../kubernetes/rabbitmq/
```

### 4.4 Déployer les Microservices

Modifier les images dans les fichiers YAML pour pointer vers votre ECR :

```bash
kubectl apply -f ../kubernetes/services/
```

### 4.5 Vérifier les Déploiements

```bash
kubectl get pods -n fintech-platform
kubectl get services -n fintech-platform
```

## Étape 5 : Configuration des Bases de Données

### 5.1 Créer les Bases de Données PostgreSQL

Connectez-vous à RDS et créez les bases :

```sql
CREATE DATABASE account_db;
CREATE DATABASE wallet_db;
CREATE DATABASE transaction_db;
CREATE DATABASE notification_db;
```

### 5.2 Configurer les Variables d'Environnement

Mettez à jour les deployments avec les endpoints réels :

```bash
kubectl set env deployment/account-service \
  DB_HOST=<rds-endpoint> \
  -n fintech-platform
```

## Étape 6 : Exposer l'Application

Le Gateway Service est exposé via un LoadBalancer AWS.

Récupérer l'URL :

```bash
kubectl get service gateway-service -n fintech-platform
```

Le `EXTERNAL-IP` sera votre point d'entrée.

## Étape 7 : Configuration DNS (Optionnel)

Créer un enregistrement DNS pointant vers le LoadBalancer :

```
api.votre-domaine.com -> <EXTERNAL-IP du LoadBalancer>
```

## Étape 8 : SSL/TLS (Optionnel)

### Utiliser AWS Certificate Manager

1. Créer un certificat SSL pour votre domaine
2. Annoter le service pour utiliser le certificat :

```yaml
metadata:
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-ssl-cert: arn:aws:acm:region:account-id:certificate/certificate-id
    service.beta.kubernetes.io/aws-load-balancer-backend-protocol: http
```

## Monitoring et Logs

### CloudWatch Logs

Les logs EKS sont automatiquement envoyés à CloudWatch.

### Accéder aux logs

```bash
kubectl logs -f deployment/account-service -n fintech-platform
kubectl logs -f deployment/transaction-service -n fintech-platform
```

### Métriques

Installer Prometheus et Grafana :

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
```

## Scaling

### Horizontal Pod Autoscaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: account-service-hpa
  namespace: fintech-platform
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: account-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## Backup et Disaster Recovery

### Automated Backups

- **RDS** : Rétention de 7 jours configurée automatiquement
- **DocumentDB** : Snapshots quotidiens
- **Volumes K8s** : Utiliser Velero pour les backups

## Maintenance

### Mise à jour des Services

```bash
# Construire nouvelle image
docker build -t <ecr-repo>/account-service:v2 .
docker push <ecr-repo>/account-service:v2

# Mettre à jour le deployment
kubectl set image deployment/account-service \
  account-service=<ecr-repo>/account-service:v2 \
  -n fintech-platform

# Vérifier le rollout
kubectl rollout status deployment/account-service -n fintech-platform
```

### Rollback

```bash
kubectl rollout undo deployment/account-service -n fintech-platform
```

## Costs Estimation (mensuel)

- EKS Cluster : ~$73
- EC2 Instances (t3.medium x3) : ~$100
- RDS db.t3.micro : ~$15
- DocumentDB db.t3.medium : ~$70
- Amazon MQ t3.micro : ~$25
- NAT Gateway : ~$32
- Load Balancer : ~$20
- **Total estimé : ~$335/mois**

## Sécurité

### Best Practices

1. **Réseau**
   - Services dans sous-réseaux privés
   - Exposition via LoadBalancer uniquement

2. **Secrets**
   - Utiliser AWS Secrets Manager au lieu de Kubernetes Secrets
   - Rotation automatique des mots de passe

3. **IAM**
   - Utiliser IRSA (IAM Roles for Service Accounts)
   - Principe du moindre privilège

4. **Encryption**
   - TLS pour tout le trafic
   - Encryption at rest pour RDS et EBS

## Support et Troubleshooting

### Vérifier la santé des pods

```bash
kubectl describe pod <pod-name> -n fintech-platform
kubectl logs <pod-name> -n fintech-platform
```

### Se connecter à un pod

```bash
kubectl exec -it <pod-name> -n fintech-platform -- /bin/sh
```

### Tester la connectivité

```bash
kubectl run -it --rm debug --image=busybox --restart=Never -n fintech-platform -- sh
# À l'intérieur du pod
wget -O- http://account-service:8081/actuator/health
```

## Cleanup

Pour supprimer toute l'infrastructure :

```bash
# Supprimer les ressources Kubernetes
kubectl delete namespace fintech-platform

# Supprimer l'infrastructure Terraform
cd infrastructure/terraform
terraform destroy
```

**⚠️ ATTENTION: Cette action supprime toutes les données de manière irréversible!**
