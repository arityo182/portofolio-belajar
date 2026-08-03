# 🧪 Cara Run LocalStack (S3 Simulation)

> Simulasi AWS S3 service di lokal tanpa akun AWS asli. Berguna untuk development fitur upload X-ray.

---

## Apa Itu LocalStack?

[LocalStack](https://localstack.cloud/) adalah tool yang menjalankan services AWS secara lokal:
- **S3** (object storage) — untuk upload X-ray images
- **SQS** (message queue) — untuk antrian async
- **Lambda** (serverless) — untuk processing pipeline
- Dan lain-lain

**Kelebihan:**
- Gratis (basic tier)
- Tidak perlu akun AWS
- Tidak perlu internet
- Data tetap lokal

**Kekurangan:**
- Tidak semua AWS service tersedia
- Tidak 100% compatible dengan AWS asli
- Butuh Docker

---

## Prasyarat

- Docker Desktop / Docker Engine + Compose
- AWS CLI (opsional, untuk testing)

---

## Langkah-langkah

### 1. Jalankan LocalStack

```bash
cd portofolio-belajar

# Uncomment docker-compose.localstack.yml terlebih dahulu
# Hapus semua baris # di awal

docker compose -f config/localstack/docker-compose.localstack.yml up -d
```

### 2. Cek Status

```bash
docker compose -f config/localstack/docker-compose.localstack.yml ps
```

```
NAME                    STATUS              PORTS
medika-localstack       Up (healthy)        0.0.0.0:4566->4566/tcp
```

### 3. Install AWS CLI (opsional)

```bash
# Ubuntu/Debian
sudo apt install awscli -y

# macOS
brew install awscli

# Windows
# Download dari https://aws.amazon.com/cli/
```

### 4. Buat S3 Bucket

```bash
# Tanpa AWS CLI — pakai curl
curl -X POST http://localhost:4566/medika-xray \
  -H "Content-Type: application/xml" \
  -d '<CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><LocationConstraint>ap-southeast-1</LocationConstraint></CreateBucketConfiguration>'

# Atau pakai AWS CLI
aws --endpoint-url=http://localhost:4566 s3 mb s3://medika-xray
```

### 5. Test Upload

```bash
# Upload file
echo "test xray" > test-xray.txt
aws --endpoint-url=http://localhost:4566 s3 cp test-xray.txt s3://medika-xray/uploads/test-xray.txt

# List files
aws --endpoint-url=http://localhost:4566 s3 ls s3://medika-xray/

# Download file
aws --endpoint-url=http://localhost:4566 s3 cp s3://medika-xray/uploads/test-xray.txt ./downloaded.txt

# Hapus file
aws --endpoint-url=http://localhost:4566 s3 rm s3://medika-xray/uploads/test-xray.txt
```

---

## Konfigurasi Spring Boot

### application-localstack.properties

```properties
# Aktifkan dengan: spring.profiles.active=localstack

# S3 Configuration (LocalStack)
app.s3.endpoint-url=http://localhost:4566
app.s3.bucket=medika-xray
app.s3.region=ap-southeast-1
cloud.aws.region.static=ap-southeast-1
cloud.aws.credentials.access-key=test
cloud.aws.credentials.secret-key=test
```

### Cara Aktifkan Profile

```bash
# Via environment variable
export SPRING_PROFILES_ACTIVE=localstack

# Via docker-compose.yml
environment:
  SPRING_PROFILES_ACTIVE: localstack
```

### Code Java (S3 Client)

```java
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.net.URI;

// LocalStack S3 client
S3Client s3 = S3Client.builder()
    .endpointOverride(URI.create("http://localhost:4566"))
    .region(Region.of("ap-southeast-1"))
    .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create("test", "test")))
    .forcePathStyle(true)
    .build();

// Upload X-ray
s3.putObject(PutObjectRequest.builder()
    .bucket("medika-xray")
    .key("uploads/xray-001.jpg")
    .build(),
    RequestBody.fromFile(new File("xray-image.jpg")));
```

---

## Menggunakan LocalStack dengan Docker Compose Utama

### Tambahkan ke docker-compose.yml

```yaml
services:
  # ... service lainnya ...

  localstack:
    image: localstack/localstack:latest
    container_name: medika-localstack
    restart: unless-stopped
    ports:
      - "4566:4566"
    environment:
      SERVICES: s3
      DEFAULT_REGION: ap-southeast-1
    volumes:
      - localstack_data:/var/lib/localstack
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:4566/_localstack/health"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  localstack_data:
```

### Update Backend Service

```yaml
  backend:
    environment:
      APP_S3_ENDPOINT_URL: http://localstack:4566
      APP_S3_BUCKET: medika-xray
    depends_on:
      localstack:
        condition: service_healthy
```

---

## Use Cases untuk Hospital App

### 1. Upload X-ray Images

```
User → Frontend → POST /api/screening/upload → Backend → S3 (LocalStack)
                                                           ↓
                                                    ML Service (predict)
```

### 2. Store Radiology Images

```
Dokter → Admin Panel → Upload gambar radiologi → S3 (LocalStack)
                                                   ↓
                                              URL disimpan di DB
```

### 3. Backup Database

```
Cron job → pg_dump → Upload ke S3 (LocalStack)
```

---

## Useful Commands

```bash
# List semua buckets
aws --endpoint-url=http://localhost:4566 s3 ls

# List files di bucket
aws --endpoint-url=http://localhost:4566 s3 ls s3://medika-xray/ --recursive

# Copy file dari local ke S3
aws --endpoint-url=http://localhost:4566 s3 cp ./file.txt s3://medika-xray/path/

# Copy file dari S3 ke local
aws --endpoint-url=http://localhost:4566 s3 cp s3://medika-xray/path/file.txt ./

# Hapus file
aws --endpoint-url=http://localhost:4566 s3 rm s3://medika-xray/path/file.txt

# Hapus semua file
aws --endpoint-url=http://localhost:4566 s3 rm s3://medika-xray/ --recursive

# Bucket info
aws --endpoint-url=http://localhost:4566 s3api head-bucket --bucket medika-xray

# Health check
curl http://localhost:4566/_localstack/health
```

---

## Troubleshooting

### LocalStack tidak start
```bash
# Cek logs
docker compose -f config/localstack/docker-compose.localstack.yml logs

# Pastikan port 4566 tidak dipakai
lsof -i :4566
```

### Upload gagal
```bash
# Cek bucket sudah ada
aws --endpoint-url=http://localhost:4566 s3 ls

# Buat bucket jika belum ada
aws --endpoint-url=http://localhost:4566 s3 mb s3://medika-xray
```

### Connection refused
```bash
# Pastikan LocalStack running
docker compose ps

# Cek health
curl http://localhost:4566/_localstack/health

# Jika pakai Docker Compose utama, gunakan hostname "localstack" bukan "localhost"
# S3 endpoint: http://localstack:4566
```

### Spring Boot tidak detect S3
```bash
# Pastikan profile aktif
# Cek: SPRING_PROFILES_ACTIVE=localstack

# Pastikan dependency ada di build.gradle
# implementation 'software.amazon.awssdk:s3:2.x.x'
```

---

## Perbedaan LocalStack vs AWS S3 Asli

| Aspek | LocalStack | AWS S3 |
|---|---|---|
| Biaya | Gratis | ~$0.023/GB/bulan |
| Kecepatan | Lambat (local) | Cepat (CDN global) |
| Durability | Tidak guaranteed | 99.999999999% |
| Ketersediaan | Local saja | Global |
| Authentication | Test/test | IAM roles |
| Bucket creation | Langsung | Butuh permission |

> **Tips:** Gunakan LocalStack untuk development, AWS S3 untuk production.

---

## Referensi

- [LocalStack Docs](https://docs.localstack.cloud/)
- [AWS S3 API Reference](https://docs.aws.amazon.com/AmazonS3/latest/API/)
- [LocalStack GitHub](https://github.com/localstack/localstack)
