<script setup>
import { ref } from "vue";
import HomeListItem from "@/components/home/HomeListItem.vue";

defineProps({
    apartments: {
        type: Array,
        required: true,
    },
});

const emit = defineEmits(["key-select"]);

function clickApt(apt) {
    emit("key-select", apt);
}

</script>

<template>
    <div v-if="apartments?.length" class="apartment-list">
        <div class="apartment-count-container">
            <p class="apartment-count">총 아파트: {{ apartments.length }}건</p>
        </div>
        <HomeListItem v-for="apt in apartments" :key="apt.aptSeq" :apt="apt" @key-select="clickApt" />
    </div>
    <div v-else class="empty-state">
        <p>No apartments available.</p>
    </div>
</template>

<style scoped>
.apartment-count-container {
    width: 100%;
    text-align: center;
    margin-bottom: 10px; /* 아파트 목록과 간격 */
    padding-top: 10px; /* 줄과 텍스트 사이 간격 */
    border-top: 1px solid #ddd; /* 윗줄 스타일 */
}

.apartment-count {
    font-size: 1.2rem;
    color: #333;
    font-weight: 500;
    margin: 0;
}
.apartment-list {
    width: 100%;
    height: 100vh;
    background-color: white;
    text-align: center;
    display: flex;
    flex-direction: column;
    /* 세로로 정렬 */
    align-items: center;
    /* 가로 중앙 정렬 */
    justify-content: flex-start;
    /* 위쪽에 붙이기 */
    margin: 0;
    /* 여백 제거 */
    padding: 0;
    /* 여백 제거 */
    gap: 0;
    /* 아이템 간 간격 제거 */
}

.empty-state {
    width: 100%;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    border-top: 1px solid #ddd;
    background-color: #f9f9f9ad;
    /* 비어있는 상태 스타일 추가 */
}
</style>