# 前端 Dockerfile - 多阶段构建
FROM node:20-alpine AS builder
WORKDIR /app

# 复制 package.json
COPY package*.json ./

# 配置淘宝镜像并安装依赖
RUN npm config set registry https://registry.npmmirror.com && npm install

# 复制源码并构建
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 复制构建产物
COPY --from=builder /app/dist .

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
