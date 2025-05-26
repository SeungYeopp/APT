import axios from "axios";

const apiClient = axios.create({
  baseURL: `${import.meta.env.VITE_VUE_API_URL}`, // 백엔드 서버 주소
  headers: {
    "Content-Type": "application/json",
    withCredentials: true,
  },
});

export default apiClient;
