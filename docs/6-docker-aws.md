# ☁️ Cara Run Docker + AWS (EC2 Deployment)

> Deploy RS Medika Sentosa ke AWS EC2 dengan Docker Compose.

---

## Overview

```
GitHub Actions (CI) → SSH → AWS EC2 → Docker Compose → Running App
```

---

## Prasyarat

- AWS Account (free tier: t3.micro, tapi t3.medium recommended)
- AWS CLI terinstall
- SSH key pair (`.pem` file)
- Domain (opsional — bisa pakai IP langsung)

---

## Langkah 1: Buat EC2 Instance

### Via AWS Console

1. Buka [EC2 Dashboard](https://console.aws.amazon.com/ec2/)
2. **Launch Instance**
   - Name: `medika-sentosa`
   - AMI: **Ubuntu 24.04 LTS**
   - Instance type: **t3.medium** (2 vCPU, 4 GB RAM)
   - Key pair: Create new → download `.pem` file
   - Security Group:
     - SSH (22) — from your IP
     - HTTP (80) — from anywhere
     - Custom TCP (8080) — from anywhere
     - Custom TCP (3000) — from your IP only (Grafana)
3. **Launch Instance**
4. Catat **Public IPv4** (misal `54.251.xxx.xxx`)

### Via AWS CLI

```bash
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.medium \
  --key-name medika-key \
  --security-group-ids sg-xxxxx \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=medika-sentosa}]'
```

---

## Langkah 2: Setup EC2 Instance

```bash
# SSH ke EC2
ssh -i medika-key.pem ubuntu@54.251.xxx.xxx

# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker ubuntu

# Install Docker Compose
sudo apt install -y docker-compose-plugin

# Logout & login lagi (agar docker group aktif)
exit
ssh -i medika-key.pem ubuntu@54.251.xxx.xxx

# Clone repository
cd /opt
sudo git clone https://github.com/arityo182/portofolio-belajar.git medika
sudo chown -R ubuntu:ubuntu medika

# Setup environment
cd medika
cp .env.example .env
```

Edit `.env`:

```bash
nano .env
```

```env
POSTGRES_PASSWORD=your_production_password_here
JWT_SECRET=your_jwt_secret_use_openssl_rand_base64_48
```

Generate JWT secret:

```bash
openssl rand -base64 48
```

---

## Langkah 3: Build & Run di EC2

```bash
cd /opt/medika

# Build & start
docker compose up -d --build

# Cek status
docker compose ps

# Lihat logs
docker compose logs -f backend
```

---

## Langkah 4: Setup GitHub Secrets (untuk CI/CD)

Buka repo GitHub → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

| Secret Name | Value |
|---|---|
| `POSTGRES_PASSWORD` | Password dari `.env` |
| `EC2_HOST` | Public IP EC2 (misal `54.251.xxx.xxx`) |
| `EC2_SSH_KEY` | Isi lengkap isi file `.pem` (private key) |
| `AWS_ACCESS_KEY_ID` | Dari IAM (jika pakai ECS) |
| `AWS_SECRET_ACCESS_KEY` | Dari IAM (jika pakai ECS) |
| `AWS_REGION` | `ap-southeast-1` |

### Cara copy isi SSH key:

```bash
# Di terminal local
cat medika-key.pem
# Copy seluruh output, paste ke GitHub Secret "EC2_SSH_KEY"
```

---

## Langkah 5: Aktifkan Deploy Workflow

Buka `.github/workflows/deploy-aws.yml` — uncomment seluruh isi file.

**Cara uncomment:**
1. Hapus semua baris yang diawali `# ` (spasi setelah `#`)
2. Atau replace `# ` dengan kosong

Setelah uncomment, workflow akan:
1. Jalankan semua test (backend, frontend, ML)
2. SSH ke EC2
3. `git pull` latest code
4. `docker compose down` → `build` → `up -d`
5. Health check

---

## Langkah 6: Deploy

```bash
# Push ke main branch
git push origin main

# GitHub Actions akan otomatis deploy ke EC2
# Cek di: https://github.com/arityo182/portofolio-belajar/actions
```

---

## Monitoring di Production

### Prometheus + Grafana

```bash
# Akses Grafana dari local (port forwarding)
ssh -i medika-key.pem -L 3000:localhost:3000 ubuntu@54.251.xxx.xxx

# Buka http://localhost:3000
# Login: admin / admin
```

### Logs

```bash
# Di EC2
docker compose logs -f backend
docker compose logs -f chatbot

# Health check
curl http://localhost:8080/api/health
```

---

## Estimasi Biaya AWS

| Service |规格 | Estimasi/Bulan |
|---|---|---|
| EC2 t3.medium | 2 vCPU, 4 GB RAM | ~$30 |
| EBS gp3 | 30 GB | ~$3 |
| Data transfer | ~10 GB | ~$1 |
| **Total** | | **~$34/bulan** |

> 💡 **Tips**: Pakai [AWS Free Tier](https://aws.amazon.com/free/) untuk 12 bulan pertama (t3.micro gratis).

---

## ECS Alternative (untuk scaling)

Jika butuh auto-scaling, uncomment section ECS di `deploy-aws.yml`:

```yaml
# ECS workflow:
# 1. Build Docker image
# 2. Push ke ECR (Elastic Container Registry)
# 3. Update ECS service → auto-deploy
```

**Setup ECS:**
1. Buat ECR repository
2. Buat ECS cluster + service
3. Set GitHub secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`
4. Uncomment ECS section di workflow

---

## Troubleshooting

### EC2 timeout saat SSH
```bash
# Cek security group — pastikan port 22 terbuka dari IP kamu
# Cek IP public EC2
```

### Docker build gagal di EC2
```bash
# EC2 free tier punya storage terbatas — cek disk space
df -h

# Cleanup unused images
docker system prune -a
```

### Backend tidak start
```bash
# Cek logs
docker compose logs backend

# Cek PostgreSQL connection
docker compose exec postgres pg_isready
```

### Health check gagal
```bash
# Pastikan backend sudah fully started (Spring Boot cold start ~30-60s)
watch docker compose ps
# Tunggu sampai "healthy"
```
