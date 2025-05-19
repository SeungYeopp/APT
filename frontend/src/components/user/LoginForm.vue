<script setup>
import { useNavBar } from "@/stores/navbar";
import { useUserStore } from "@/stores/user";
import { storeToRefs } from "pinia";
import { useRouter } from "vue-router";
import { ref } from "vue";
import apiClient from "@/util/axios-common.js"; // Axios 인스턴스 가져오기

const email = ref("");
const password = ref("");
const loginMessage = ref("");
const router = useRouter();
const { changeMenuState } = useNavBar();
const userStore = useUserStore();
const navBar = useNavBar();

function toMysqlDatetime(isoString) {
  const date = new Date(isoString);
  return date.toISOString().slice(0, 19).replace("T", " ");
}

const syncLocalStorageToDB = async () => {
  const localApartmentsData = JSON.parse(localStorage.getItem("recentApartments")) || [];
  const localNeighborhoodsData = JSON.parse(localStorage.getItem("recentNeighborhoods")) || [];

  try {
    // 페이로드 생성
    const apartmentsPayload = localApartmentsData.map((item) => ({
      houseInfoId: item.houseInfoId,
      bookmarked: item.bookmarked,
      time: toMysqlDatetime(item.time),
      userId: userStore.getUser?.id,
    }));

    const neighborhoodsPayload = localNeighborhoodsData.map((item1) => ({
      dongCd: item1.dongCd,
      bookmarked: item1.bookmarked,
      time: toMysqlDatetime(item1.time),
      userId: userStore.getUser?.id,
    }));

    console.log("전송할 아파트 payload:", apartmentsPayload);
    console.log("전송할 동네 payload:", neighborhoodsPayload);

    // 병렬 요청 처리
    await Promise.all([
      apiClient.post("/interest/sync-apartments", apartmentsPayload),
      apiClient.post("/interest/sync-neighborhoods", neighborhoodsPayload),
    ]);

    console.log("로컬 스토리지 데이터를 DB에 성공적으로 동기화했습니다.");
    localStorage.removeItem("recentApartments");
    localStorage.removeItem("recentNeighborhoods");
  } catch (error) {
    console.error("로컬 스토리지 동기화 실패:", error.response?.data || error.message);
  }
};




const login = async () => {
  try {
    const response = await apiClient.post(
      "/user/login",
      { email: email.value, password: password.value },
      { withCredentials: true }
    );
    const userData = response.data;

    // 상태와 localStorage를 동시에 업데이트
    userStore.setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));

    // 로컬 스토리지를 DB로 동기화
    await syncLocalStorageToDB();

    // 로그인이 성공했으므로 메뉴 상태도 업데이트
    navBar.changeMenuState(true);

    // 메인 페이지로 이동
    router.push({ name: "main" });
  } catch (error) {
    console.error("로그인 실패:", error);
    loginMessage.value = "로그인 실패: 이메일 또는 비밀번호를 확인하세요.";
  }
};
</script>

<template>
  <main class="login-container">
    <div class="login-box">
      <h2>LOGIN</h2>

      <!-- 로그인 폼 -->
      <form @submit.prevent="login">
        <label for="email">이메일</label>
        <input id="email" v-model="email" type="email" placeholder="이메일" required />

        <label for="password">비밀번호</label>
        <input id="password" v-model="password" type="password" placeholder="비밀번호" required />

        <!-- 로그인 버튼 -->
        <button type="submit" class="login-btn">로그인 →</button>
      </form>

      <p>{{ loginMessage }}</p>
    </div>
  </main>
</template>



<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.login-box {
  width: 400px;
  background: white;
  padding: 30px;
  border-radius: 8px;
  border: 1px solid #ddd;
  text-align: center;
}

.login-box h2 {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--eerie-black);
  margin-bottom: 1rem;
}

.login-box label {
  display: block;
  text-align: left;
  font-size: 1rem;
  margin-bottom: 8px;
  color: var(--eerie-black);
}

.login-box input {
  width: 100%;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 10px;
  box-sizing: border-box;
}

.login-box .login-btn {
  width: 100%;
  padding: 12px;
  margin-top:20px;
  background-color: var(--eerie-black);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
}

.login-box .login-btn:hover {
  background-color: var(--eerie-grey);
}

.login-box p {
  margin-top: 15px;
  font-size: 14px;
  color: #555;
}
</style>
