<script setup>
import { useNavBar } from "@/stores/navbar";
import { useUserStore } from "@/stores/user";
import { storeToRefs } from "pinia";
import { useRouter } from "vue-router"; // Vue Router 임포트

const { changeMenuState } = useNavBar();
const store = useUserStore();
const { getUser: user } = storeToRefs(store);
const { userLogin } = store;

// Vue Router 인스턴스 가져오기
const router = useRouter();

const login = () => {
    userLogin();
    changeMenuState();
    console.log("login successful !!!");
    // 로그인 완료 후 메인 페이지로 이동 (예시)
    router.push("/main");
};

const navigateToKakao = () => {
    router.push("/user/login/kakao"); // 카카오 로그인 폼으로 이동
};

const navigateToEmail = () => {
    router.push("/user/login/email"); // LoginForm.vue 경로
};

const navigateToRegister = () => {
    router.push("/user/join"); // 회원가입 페이지로 이동
};
</script>


<template>
    <main class="login-container">
        <div class="login-box">
            <div>
                <h1>LOGIN</h1>
                <!-- 기존 카카오 로그인 버튼 -->
                <button class="login-btn kakao-btn" @click="navigateToKakao">카카오로 로그인하기</button>
                
                <!-- 이메일 로그인 버튼 -->
                <button class="login-btn email-btn" @click="navigateToEmail">
                    이메일로 로그인하기
                </button>
                
                <!-- 회원가입 링크 -->
                <p class="register-link">
                    아직 회원이 아니라면? 
                    <a href="#" @click.prevent="navigateToRegister">회원가입</a>
                </p>
            </div>
        </div>
        <!-- <div class="dog-img"><img src="@/img/Saly-18.png" alt="" style="width: 550px"></div> -->
    </main>
</template>


<style scoped>
.login-container {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    position: relative;
    width: 100%;
    height: 100%;
    margin: 0;
    padding: 0;
}
.dog-img {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 100px;
    height: 500px;
    z-index: 1;
}
.login-box {
    display: flex;
    align-items: center;
    text-align: center;
    padding: 2rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    background-color: #fff;
    width: 400px;
    height: 400px;
    z-index: 2;
}
.login-box h1 {
    font-size: 2rem;
    font-weight: 900;
    color: var(--eerie-black);
    margin-bottom: 1rem;
}
.login-btn {
    width: 200px;
    padding: 0.8rem;
    font-size: 1rem;
    margin: 0.5rem 0;
    border: none;
    border-radius: 15px;
    cursor: pointer;
}
.kakao-btn {
    background-color: #fee500;
    color: var(--eerie-black);
}
.kakao-btn:hover {
    background-color: #f7de04;
}
.email-btn {
    background-color: #69aaf8;
    color: #fff;
}
.email-btn:hover {
    background-color: #64a2ec;
}
.register-link {
    margin-top: 1rem;
    font-size: 0.9rem;
}
.rocket {
    position: absolute;
    bottom: 5%;
    right: 5%;
    width: 150px;
}
</style>