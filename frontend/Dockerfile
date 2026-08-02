# === 阶段1: 构建 Vue (context = frontend/) ===
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# === 阶段2: Nginx 托管 ===
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# 模板机制: 启动时用环境变量 BACKEND_HOST 替换 nginx 配置 (支持 Railway/Render 任意后端地址)
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
