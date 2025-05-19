<template>
    <div>
      <div class="notice-item" v-for="(notice, index) in notices" :key="index">
        <div class="notice-title" @click="toggleNotice(index)">
          <strong>{{ notice.title }}</strong>
          <span :class="{ 'expanded': activeIndex === index }">+</span>
        </div>
        <div v-show="activeIndex === index" class="notice-detail">
          <p>{{ notice.detail }}</p>
          <p><strong>날짜: </strong>{{ notice.date }}</p>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  
  const notices = [
    { 
      title: '[공지사항] APT 개인정보 처리방침 개정 안내', 
      detail: '개인정보 처리방침이 개정되었습니다. 자세한 사항은 링크를 통해 확인해주세요.', 
      date: '2023-11-14' 
    },
    { 
      title: '[공지사항] Internet Explorer 11 지원 종료 안내', 
      detail: 'Internet Explorer 11의 지원이 종료됩니다. 자세한 내용은 링크를 통해 확인해주세요.', 
      date: '2023-04-14' 
    },
    { 
      title: '[공지사항] APT ‘간편문의’ 서비스 종료 예정 (12월5일)', 
      detail: '12월 5일을 기점으로 간편문의 서비스가 종료됩니다. 자세한 내용은 링크를 통해 확인해주세요.',
      date: '2022-11-28'
    },
    { 
      title: '[공지사항] APT 개인정보 처리방침 개정 안내', 
      detail: '개인정보 처리방침이 개정되었습니다. 자세한 사항은 링크를 통해 확인해주세요.',
      date: '2022-11-17'
    },
    { 
      title: '[공지사항] 허위매물 신고자 보상 정책 변경', 
      detail: '허위매물 신고자에게 보상 정책이 변경되었습니다. 자세한 내용은 링크를 통해 확인해주세요.',
      date: '2022-05-26'
    }
  ]
  
  const activeIndex = ref(null) // Track the active notice index
  
  const toggleNotice = (index) => {
    activeIndex.value = activeIndex.value === index ? null : index
  }
  </script>
  
  <style scoped>
  .notice-item {
    margin-bottom: 20px;
    padding: 10px;
    border-bottom: 1px solid #ddd;
  }
  
  .notice-title {
    font-size: 18px;
    font-weight: 400;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
  }
  
  .notice-title:hover {
    color: #007bff;
  }
  
  .notice-detail {
    margin-top: 10px;
    padding-left: 20px;
    font-size: 16px;
  }
  
  .notice-detail p {
    margin: 0;
  }
  
  .notice-title .expanded {
    transform: rotate(45deg);
    transition: transform 0.3s ease;
  }
  </style>
  