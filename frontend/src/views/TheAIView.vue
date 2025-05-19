<script setup>
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
</script>

<template>
  <div class="ai-view">
    <div class="all-container">
      <img class="rocket-img" src="@/img/Saly-43.png" alt="rocket">
      <div class="container">
        <div style="margin-bottom: 50px; text-align: center;">
          <!-- Title Section -->
          <h1 class="fw-bold">나만의 AI를 구독하세요</h1>
          <p class="text-muted ">
            동네별 아파트를 찾고, 최신 부동산 기사를 스크랩해보세요.
          </p>
        </div>

        <!-- Pricing Plans -->
        <div class="row justify-content-center g-4">
          <!-- Free Plan -->
          <div class="col-md-4">
            <div class="card border-secondary h-100">
              <div class="card-body">
                <h5 class="card-title text-secondary">Free</h5>
                <h2 class="card-price fw-bold mb-4">$0</h2>
                <ul class="list-unstyled text-start">
                  <li>✔ AI 기본 기능</li>
                  <li>✔ 챗봇 대화 생성하기</li>
                </ul>
              </div>
              <button class="btn btn-outline-secondary w-90 mt-1">Get Started</button>
            </div>
          </div>

          <!-- Personal Plan -->
          <div class="col-md-4">
            <div class="card border-primary h-100">
              <div class="card-body">
                <h5 class="card-title text-primary">Personal</h5>
                <h2 class="card-price fw-bold mb-4">$0.99</h2>
                <ul class="list-unstyled text-start">
                  <li>✔ AI 기본 기능</li>
                  <li>✔ 챗봇 대화 생성하기</li>
                  <li>✔ 아파트 검색하기</li>
                </ul>
              </div>
              <router-link 
                :to="{ path: '/aichat' }" 
                class="btn btn-primary w-90 mt-1"
              >
                Get Started
              </router-link>
            </div>
          </div>

          <!-- Organization Plan -->
          <div class="col-md-4">
            <div class="card border-warning h-100">
              <div class="card-body">
                <h5 class="card-title text-warning">Organization</h5>
                <h2 class="card-price fw-bold mb-4">$4.99</h2>
                <ul class="list-unstyled text-start">
                  <li>✔ AI 기본 기능</li>
                  <li>✔ 챗봇 대화 생성하기</li>
                  <li>✔ 아파트 검색하기</li>
                  <li>✔ 최신 기사 스크랩하기</li>
                </ul>
              </div>
              <button class="btn btn-outline-warning w-90 mt-1">Get Started</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
  </div>
</template>

<style scoped>
h1 {
  font-size: 2rem;
  color: var(--eerie-black);
}
.rocket-img {
  position: absolute;
  width: 500px;
  height: auto;
  left: 0; 
  top: 50%; /* 수직 가운데 정렬 */
  transform: translateY(-50%); /* 가운데 정렬 보정 */
  z-index: -1; /* 콘텐츠 뒤로 배치 */
}
.ai-view {
  padding: auto 0;
  width: 100%;
  margin: 0;
  padding: 0;
  height: 600px;
}
.all-container{
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto; /* 중앙 정렬 */
  z-index: 2; /* 이미지보다 위에 표시 */
  position: relative; /* 부모 요소 기준으로 위치 설정 */
}
.container {
  max-width: 1200px; /* 콘텐츠 최대 너비 제한 */
  width: 100%; /* 화면 너비에 맞춤 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto; /* 중앙 정렬 */
  z-index: 2;
}

.card {
  width: 300px;
  border-radius: 10px;
  padding: 1.5rem;
}
.card-price {
  font-size: 1.8rem;
  color: var(--eerie-black);
}

</style>
