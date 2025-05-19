<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import apiClient from "@/util/axios-common.js";

// 사용자 입력 데이터
const nickname = ref(""); // 닉네임 입력 값
const email = ref(""); // 이메일 입력 값
const password = ref(""); // 비밀번호 입력 값
const verificationCode = ref(""); // 인증코드 입력 값
const message = ref(""); // 사용자 메시지 (성공/실패)

// Vue Router 인스턴스 생성
const router = useRouter();
const isLoading = ref(false); // 로딩 상태 관리

// 인증코드 전송
const sendVerificationCode = async () => {
  isLoading.value = true; // 로딩 시작
  try {
    const response = await apiClient.post(
      "/user/send-verification-code",
      null,
      {
        params: {
          email: email.value, // 이메일을 쿼리 매개변수로 전달
        },
      }
    );
    message.value = "인증코드가 이메일로 전송되었습니다.";
    console.log(response.data);
  } catch (error) {
    message.value =
      "인증코드 전송 실패: " +
      (error.response?.data?.error || "알 수 없는 오류");
    console.error(error.response || error);
  } finally {
    isLoading.value = false; // 로딩 종료
  }
};

// 인증코드 확인
const verifyCode = async () => {
  try {
    const response = await apiClient.post("/user/verify-code", null, {
      params: {
        email: email.value,
        code: verificationCode.value,
      },
    });
    message.value = "이메일 인증에 성공했습니다.";
    console.log(response.data);
  } catch (error) {
    message.value =
      "이메일 인증 실패: " + (error.response?.data?.error || "알 수 없는 오류");
    console.error(error.response || error);
  }
};

// 회원가입
const register = async () => {
  try {
    const response = await apiClient.post("/user/register", {
      email: email.value,
      nickname: nickname.value,
      password: password.value,
    });
    message.value = "회원가입 성공!";
    console.log(response.data);
    router.push({ name: "email-login" });
  } catch (error) {
    message.value =
      "회원가입 실패: " + (error.response?.data?.error || "알 수 없는 오류");
    console.error(error.response || error);
  }
};
</script>

<template>
  <div v-if="isLoading" class="loading-overlay">
    <div class="spinner"></div>
    <p>인증번호를 전송 중입니다. 잠시만 기다려주세요...</p>
  </div>
  <main class="signup-container">
    <div class="signup-box">
      <h2>회원가입</h2>

      <!-- 아이디 입력 -->
      <label for="nickname">닉네임</label>
      <input
        id="nickname"
        v-model="nickname"
        type="text"
        placeholder="입력 4자 이상 입력하세요"
        required
      />

      <!-- 비밀번호 입력 -->
      <label for="password">비밀번호</label>
      <input
        id="password"
        v-model="password"
        type="password"
        placeholder="영문, 숫자, 특수문자 포함 8자 이상"
        required
      />

      <!-- 이메일 입력 -->
      <label for="email">이메일</label>
      <div class="email-container">
        <input
          id="email"
          v-model="email"
          type="email"
          placeholder="이메일을 입력하세요"
          required
        />
        <button class="verify-btn" @click="sendVerificationCode">인증</button>
      </div>

      <!-- 인증코드 입력 -->
      <div class="verification-code-container">
        <input
          v-model="verificationCode"
          type="text"
          placeholder="인증번호 입력"
        />
        <button class="verify-btn" @click="verifyCode">확인</button>
      </div>

      <!-- 회원가입 버튼 -->
      <button class="signup-btn" @click="register">APT에 합류하기 →</button>

      <!-- 메시지 출력 -->
      <p>{{ message }}</p>
    </div>
  </main>
</template>

<style scoped>
.signup-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.signup-box {
  width: 400px;
  background: white;
  padding: 30px;
  border-radius: 8px;
  border: 1px solid #ddd;
  text-align: center;
}

.signup-box h2 {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--eerie-black);
  margin-bottom: 1rem;
}

.signup-box label {
  display: block;
  text-align: left;
  font-size: 1rem;
  margin-bottom: 8px;
  color: var(--eerie-black);
}

.signup-box input {
  width: 100%;
  height: 40px;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 10px;
}

.email-container {
  display: flex;
  align-items: center; /* 입력 필드와 버튼을 수직으로 정렬 */
  gap: 10px;
}

.email-container input {
  flex: 1; /* 입력 필드가 공간을 채우도록 설정 */
}

.email-container .verify-btn {
  padding: 0 15px; /* 상하 여백 제거 후 좌우 여백 유지 */
  height: 40px; /* 입력 필드와 동일한 높이 */
  line-height: 40px; /* 버튼 높이와 동일하게 설정하여 텍스트를 수직 가운데 정렬 */
  text-align: center; /* 텍스트를 가로 가운데 정렬 */
  background-color: var(--indian-red);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 20px;
}
.email-container .verify-btn:hover {
  background-color: #d85e5e;
}

.signup-box .signup-btn {
  width: 100%;
  padding: 12px;
  background-color: var(--eerie-black);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
}

.signup-box .signup-btn:hover {
  background-color: var(--eerie-grey);
}

.signup-box p {
  margin-top: 15px;
  font-size: 14px;
  color: #555;
}

.verification-code-container {
  display: flex;
  align-items: center; /* 입력 필드와 버튼을 수직으로 정렬 */
  gap: 10px;
  margin-bottom: 20px; /* 다른 입력 필드와 간격 유지 */
}

.verification-code-container input {
  flex: 1; /* 입력 필드가 공간을 채우도록 설정 */
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 10px;
  box-sizing: border-box;
}

.verification-code-container .verify-btn {
  padding: 0 15px; /* 상하 여백 제거 후 좌우 여백 유지 */
  height: 40px; /* 입력 필드와 동일한 높이 */
  line-height: 40px; /* 텍스트 수직 가운데 정렬 */
  text-align: center; /* 텍스트 가로 가운데 정렬 */
  background-color: var(--jade);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 20px;
}
.verification-code-container .verify-btn:hover {
  background-color: #37a56b;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 1000;
}

.spinner {
  margin-bottom: 15px;
  width: 50px;
  height: 50px;
  border: 5px solid rgba(0, 0, 0, 0.1);
  border-top-color: #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-overlay p {
  font-size: 1.2rem;
  color: #555;
}
</style>
