
# CloudNest – Distributed File Management Platform

A modern cloud-ready file storage and sharing system built with a microservices architecture, secure authentication, a Next.js frontend, and fully automated DevOps pipeline. CloudNest enables seamless file uploads, previews, public sharing, and background processing—optimized to run reliably on low-memory servers.


## 🚀 Features

#### Frontend

- Modern and responsive Next.js App Router UI
- File uploads with real-time progress
- mage/document preview support
- Grid & list views with sorting and filtering
- Secure public link sharing
- Email-based password reset (JavaMailSender)
- Signed URL uploads directly to S3 storage

#### Backend
- Distributed microservices (Auth, File, Worker)
- JWT authentication + NGINX auth_request
- Redis-backed message queue for background jobs
- Thumbnail generation pipeline
- Optimized Spring Boot containers
- S3-compatible storage abstraction

#### DevOps
- Fully automated CI/CD via GitHub Actions
- Zero-downtime deployments
- Docker health checks & automatic rollouts
- NGINX API Gateway + HTTPS (Let’s Encrypt)
- Monitoring with Dozzle + UptimeRobot
- Secure reverse proxy routing
- Production-ready Docker Compose orchestration

## 🧩 Tech Stack

**Client:** Next.js 14 (App Router), TypeScript, SWR, TailwindCSS

**Server:** Java 17 (Spring Boot), Spring Security + JWT, Postgresql(Neon DB), Spring MVC / JPA, JavaMailSender (password reset), Redis (Queue), S3-compatible storage (MinIO / AWS S3)

**DevOps:** Docker + Docker Compose, GitHub Actions CI/CD, NGINX reverse proxy, Let’s Encrypt SSL
Linux (Ubuntu 24.04)
 
## 📁 Project Structure

```
cloudnest/
│
├── auth-service/             # Authentication microservice
├── file-service/             # File upload, metadata, signed URLs
├── worker-service/           # Background queue processor (Redis)
├── ui-service/               # Next.js frontend
├── nginx/                    # API gateway + SSL + routing
├── docker-compose.yml        # Production orchestrator
└── .github/workflows/        # CI/CD pipelines
```

## 🔧 Installation

1. Clone repo

```bash
  git clone https://github.com/<your-user>/cloudnest.git
  cd cloudnest
```

2. Start services

```bash
  docker-compose up --build
```

3. Frontend Dev Mode


```bash
  cd ui-service
  pnpm install
  pnpm run dev
```
## 🔐 Environment Variables

To run this project, you will need to add the following environment variables to your .env file

`DB_URL` `DB_USERNAME` `DB_PASSWORD` 

`MAIL_USER` `MAIL_PASSWORD`

`JWT_SECRET`

`MINIO_URL` `PUBLIC_FILE_BASE_URL` `MINIO_ACCESS_KEY` `MINIO_SECRET_KEY` `MINIO_BUCKET` `MINIO_REGION`

`REDIS_URL`

`FRONTEND_ORIGIN`

`BASE_API_URL`

`SERVER_NAME`



## 🔒 Security Considerations
- HTTPS everywhere (Let’s Encrypt)
- NGINX auth_request for protected APIs
- Segregated microservices
- Signed URL uploads prevent direct backend load
- Password reset tokens are short-lived and stored securely
## 🤝 Contributing

Contributions are always welcome!

Please create a PR with clear commit messages or open an issue for discussions.


## 📄 License

[MIT](https://choosealicense.com/licenses/mit/)

