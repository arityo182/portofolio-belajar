# ============================================
# FRONTEND — React 19 + Vite 8 + Nginx
# Multi-stage: Node build → Nginx serve
# ============================================

# Stage 1: Build dengan Node
FROM node:24-alpine AS builder
WORKDIR /app

# Salin package.json & lock dulu (cache layer)
COPY package.json package-lock.json ./
RUN npm install

# Salin source code
COPY . .

# Build static files
RUN npm run build

# ============================================
# Stage 2: Serve dengan Nginx
FROM nginx:alpine

# Hapus default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config (dengan proxy /api ke backend)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy hasil build dari stage 1
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
