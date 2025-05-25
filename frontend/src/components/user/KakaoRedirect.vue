<template></template>

<script>
import axios from "axios";
import { useRouter } from "vue-router";
import { useNavBar } from "@/stores/navbar";
import { useUserStore } from "@/stores/user";
import apiClient from "@/util/axios-common.js";

export default {
  setup() {
    const router = useRouter();
    const navBar = useNavBar();
    const userStore = useUserStore();

    function toMysqlDatetime(isoString) {
      const date = new Date(isoString);
      return date.toISOString().slice(0, 19).replace("T", " ");
    }

    const syncLocalStorageToDB = async () => {
      const localData =
        JSON.parse(localStorage.getItem("recentApartments")) || [];
      const localData2 =
        JSON.parse(localStorage.getItem("recentNeighborhoods")) || [];

      try {
        const payload = localData.map((item) => ({
          houseInfoId: item.houseInfoId,
          bookmarked: item.bookmarked,
          time: toMysqlDatetime(item.time), // 시간 변환
          userId: userStore.getUser?.id, // 로그인된 유저 ID 추가
        }));
        const payload2 = localData2.map((item1) => ({
          dongCd: item1.dongCd,
          bookmarked: item1.bookmarked,
          time: toMysqlDatetime(item1.time), // 시간 변환
          userId: userStore.getUser?.id, // 로그인된 유저 ID 추가
        }));

        console.log("전송할 아파트 payload:", payload);
        console.log("전송할 동네 payload:", payload2);

        // 병렬 요청 처리
        await Promise.all([
          apiClient.post("/interest/sync-apartments", payload),
          apiClient.post("/interest/sync-neighborhoods", payload2),
        ]);

        console.log("로컬 스토리지 데이터를 DB에 동기화했습니다.");
        localStorage.removeItem("recentApartments");
        localStorage.removeItem("recentNeighborhoods");
      } catch (error) {
        console.error(
          "로컬 스토리지 동기화 실패:",
          error.response?.data || error.message
        );
      }
    };

    const sendAuthCodeToBackend = async (code) => {
      try {
        const response = await axios.post(
          "${import.meta.env.VITE_VUE_API_URL}/oauth/auth",
          { code },
          { withCredentials: true }
        );

        const userInfo = response.data.user; // Assuming user info is included in response

        // Save user info in localStorage
        localStorage.setItem("user", JSON.stringify(userInfo));

        // Update user information in the store
        userStore.setUser(userInfo);
        localStorage.setItem("user", JSON.stringify(userInfo));
        await syncLocalStorageToDB();

        // Update the menu state to reflect the logged-in status
        navBar.changeMenuState(true);

        // Navigate to the main page
        router.push({ name: "main" });
      } catch (error) {
        console.error(
          "Failed to authenticate:",
          error.response?.data || error.message
        );
        alert("로그인에 실패했습니다. 다시 시도해주세요."); // User-friendly error message
      }
    };

    // Check the authorization code on component mount
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get("code");

    if (code) {
      console.log("Authorization Code:", code);
      sendAuthCodeToBackend(code);
    } else {
      console.error("Authorization code missing");
      alert("인증 코드가 없습니다. 로그인 페이지로 돌아갑니다.");
      router.push({ name: "user-login" }); // Redirect to login page
    }
  },
};
</script>
