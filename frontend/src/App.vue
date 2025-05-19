<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import TheHeaderNav from '@/views/TheHeaderNav.vue';

const isSticky = ref(false); // sticky 상태
const isScrollable = ref(false); // 현재 페이지에서 스크롤 가능 여부
const route = useRoute(); // 현재 라우트 정보

// 스크롤 이벤트 처리
const handleScroll = () => {
  if (isScrollable.value) {
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    isSticky.value = scrollTop > 0; // 스크롤이 발생하면 sticky 활성화
  }
};

// 라우트 변경 시 스크롤 가능 여부 업데이트
const updateScrollable = () => {
  isScrollable.value = route.meta.scrollable || false; // meta.scrollable 기반 설정
  if (!isScrollable.value) {
    isSticky.value = false; // 스크롤 불가능한 경우 sticky 비활성화
  }
};

// 컴포넌트 마운트 시 이벤트 등록
onMounted(() => {
  updateScrollable(); // 초기 스크롤 가능 여부 확인
  window.addEventListener('scroll', handleScroll); // 스크롤 이벤트 등록
});

// 라우트 변경 감지
watch(() => route.meta.scrollable, updateScrollable);

// 컴포넌트 언마운트 시 이벤트 해제
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<template>
  <div>
    <!-- Header -->
    <header :class="{ 'sticky-header': isSticky }" class="default-header">
      <TheHeaderNav />
    </header>

    <!-- Main Content -->
    <main :class="{ 'scrollable-content': isScrollable }">
      <router-view />
    </main>
  </div>
</template>

<style scoped>

/* 헤더 스타일 */
.default-header {
  position: relative;
  width: 100%;
  background-color: white;
  z-index: 1000;
  transition: all 0.3s ease; /* 부드러운 전환 효과 */
}

/* Sticky 상태일 때 적용되는 스타일 */
.sticky-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

/* 스크롤 가능한 컨텐츠 */
.scrollable-content {
  flex: 1; /* 메인 컨텐츠가 전체 높이를 차지 */
  overflow-y: auto; /* 세로 스크롤 활성화 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE, Edge */
}
.scrollable-content::-webkit-scrollbar {
  display: none; /* Chrome, Safari */
}

/* 기본적으로 스크롤 비활성화 */
main {
  overflow: hidden; /* 스크롤 비활성화 */
}
</style>
