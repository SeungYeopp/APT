import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    port: 3000, // ✅ 포트 지정 (기본은 5173, 이걸 원하는 값으로 바꿔줌)
    proxy: {
      "/apt": {
        target: "http://localhost:8080",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/apt/, "/apt"),
      },
    },
  },
  define: {
    "import.meta.env.VITE_KAKAO_MAP_SERVICE_KEY": JSON.stringify("0ef59022ef9db30bfeab7c47c898ee95"),
    "import.meta.env.VITE_VUE_API_URL": JSON.stringify("https://kgapt.store"),
    "import.meta.env.VITE_REDIRECT_URI": JSON.stringify("https://kgapt.store/oauth/redirect"),
  },
});
