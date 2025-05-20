FROM node:22-alpine
WORKDIR /app
COPY . .
RUN npm install && npm run build && npm install -g serve
EXPOSE 3000
CMD ["serve", "-s", "dist"]
