<template>
    <div>
        <div class="faq-item" v-for="(faq, index) in faqs" :key="index">
            <div class="faq-question" @click="toggleAnswer(index)">
                <strong>{{ faq.question }}</strong>
                <span :class="{ 'expanded': activeIndex === index }">+</span>
            </div>
            <div v-show="activeIndex === index" class="faq-answer">
                <div v-html="faq.answer"></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'

const faqs = [
    {
        question: 'APT 서비스는 어떤 기능을 제공하나요?',
        answer: `
        <div>
            <h3>AI</h3>
            <ul>
                <li><strong>사용자 대화 기반 추천:</strong> 대화를 분석하여 맞춤 동네 및 아파트 추천.</li>
                <li><strong>동네 추천:</strong> 관련 키워드 및 요구사항에 따라 적합한 동네를 추천.</li>
                <li><strong>아파트 추천:</strong> 사용자의 요청에 따라 최적의 아파트 데이터를 제공합니다.</li>
            </ul>
            <h3>APT</h3>
            <ul>
                <li><strong>지역 기반 검색:</strong> 특정 지역의 아파트 정보 및 실거래가 제공.</li>
                <li><strong>키워드 검색:</strong> 아파트 이름 또는 동 이름으로 빠른 검색이 가능합니다.</li>
            </ul>
        </div>
        `
    },
    {
        question: '1:1 문의는 어떻게 작성하나요?',
        answer: `
        <ul>
            <li>'문의하기' 페이지에서 제목과 상세 내용을 작성 후 제출합니다.</li>
            <li>파일 첨부가 필요한 경우 업로드 후 문의를 등록할 수 있습니다.</li>
            <li>등록된 문의는 '내 문의' 페이지에서 확인 가능합니다.</li>
        </ul>
        `
    },
    {
        question: '관심 아파트 및 동네 관리는 어떻게 하나요?',
        answer: `
        <ul>
            <li>최근 열람한 아파트와 동네는 자동으로 기록됩니다.</li>
            <li>즐겨찾기를 통해 관심 있는 아파트와 동네를 저장할 수 있습니다.</li>
            <li>저장된 데이터는 계정에 동기화되어 어디서든 확인 가능합니다.</li>
        </ul>
        `
    }
]

const activeIndex = ref(null)

const toggleAnswer = (index) => {
    activeIndex.value = activeIndex.value === index ? null : index
}
</script>

<style scoped>
.faq-item {
    margin-bottom: 20px;
    padding: 10px;
    border-bottom: 1px solid #ddd;
}

.faq-question {
    font-size: 18px;
    font-weight: 400;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    text-align: left;
}

.faq-question:hover {
    color: #007bff;
}

.faq-answer {
    margin-top: 10px;
    padding-left: 20px;
    font-size: 16px;
    line-height: 1.6;
    text-align: left;
}

.faq-answer ul {
    padding-left: 20px;
}

.faq-answer li {
    margin-bottom: 8px;
}

.faq-question .expanded {
    transform: rotate(45deg);
    transition: transform 0.3s ease;
}
</style>
