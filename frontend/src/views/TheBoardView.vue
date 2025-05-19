<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const activeTab = ref('article-list'); // 기본 탭 설정
const router = useRouter();
const route = useRoute();

// 경로에 따라 activeTab을 설정
onMounted(() => {
    activeTab.value = route.name; // 현재 경로 이름을 기준으로 활성 탭 설정
});

// 탭 변경 시 라우터 이동
const changeTab = (tab) => {
    activeTab.value = tab;
    router.push({ name: tab });
};

// 라우트 변경 감지하여 activeTab 업데이트
watch(route, (newRoute) => {
    activeTab.value = newRoute.name; // 라우트 변경 시 activeTab 업데이트
});
</script>

<template>
    <div class="container text-center mt-3">
        <div class="board-view">
            <h1 class="qna-title">문의하기</h1>

            <div class="qna-tabs">
                <button class="tab" :class="{ active: activeTab === 'faq' }" @click="changeTab('faq')">
                    자주 묻는 질문
                </button>
                <button class="tab" :class="{ active: activeTab === 'article-list' }" @click="changeTab('article-list')" activated>
                    1:1 문의
                </button>
                <button class="tab" :class="{ active: activeTab === 'notice' }" @click="changeTab('notice')">
                    공지사항
                </button>
            </div>
            <RouterView />
        </div>
    </div>
</template>

<style scoped>
.container {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
}
.board-view {
    flex: 1;
}
/* 제목 */
.qna-title {
    font-size: 3rem;
    font-weight: bold;
    color: #333;
    margin-bottom: 30px;
    text-align: center;
}

/* 탭 스타일 */
.qna-tabs {
    display: flex;
    justify-content: center;
    margin-bottom: 30px;
}

.tab {
    padding: 20px 40px;
    font-size: 1.5rem;
    font-weight: bold;
    border: 1px solid #ddd;
    background-color: #fff;
    cursor: pointer;
    color: #333;
    transition: all 0.3s ease;
}

.tab.active {
    background-color: black;
    color: #fff;
}

.tab:hover {
    background-color: black;
    color: #fff;
}

/* 설명 텍스트 */
.qna-description {
    font-size: 18px;
    color: #555;
    text-align: center;
    margin-bottom: 30px;
}

.qna-description .highlight {
    font-weight: bold;
    color: var(--figma-blue-hover);
}
</style>