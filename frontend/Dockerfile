# ============================================================
# Stage 1: Build (Node.js + Angular)
# ============================================================
FROM node:22-alpine AS builder

WORKDIR /build

ARG API_URL=http://api:8080
ENV API_URL=${API_URL}

# Copia arquivos de dependência primeiro (cache de layer)
COPY package.json package-lock.json ./
RUN npm ci

# Copia o restante do código e builda para produção com SSR
COPY . .
RUN npm run build

# ============================================================
# Stage 2: Runtime (Node.js slim)
# ============================================================
FROM node:22-alpine AS runtime

WORKDIR /app

# Copia apenas o que é necessário para rodar o SSR
COPY --from=builder /build/dist dist
COPY --from=builder /build/package.json package.json
COPY --from=builder /build/node_modules node_modules

# Porta exposta pelo servidor Express/SSR
EXPOSE 3000

ENV PORT=3000
ENV API_URL=http://api:8080

CMD ["node", "dist/device-checker-front/server/server.mjs"]
