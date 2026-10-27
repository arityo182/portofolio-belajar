# ============================================
# FRONTEND — React 19 + Vite 8 + Nginx (JavaScript)
# ============================================
# OFFLINE: FROM medika-frontend-deps (deps udah di-image)
# ============================================

FROM medika-frontend-deps:latest AS builder
WORKDIR /app
# package.json + node_modules SUDAH ADA
# npm packages SUDAH TERINSTALL

# Salin source code
COPY . .

# Build — zero download
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
