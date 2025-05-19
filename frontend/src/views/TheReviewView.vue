<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";

const reviews = ref([]); // 리뷰 데이터
const visibleReviews = ref([]); // 현재 보여지는 리뷰 리스트
let currentIndex = 0; // 캐러셀 현재 인덱스

// Fetch high-rating reviews from API
const fetchHighRatingReviews = async () => {
  try {
    const response = await axios.get("http://localhost:8080/review/high-rating"); // API 호출
    reviews.value = response.data;
    updateVisibleReviews();
  } catch (error) {
    console.error("Failed to fetch high-rating reviews:", error);
  }
};

// Update visible reviews for the carousel
const updateVisibleReviews = () => {
  const total = reviews.value.length;

  if (total < 3) {
    // 리뷰가 3개 미만이면 모든 리뷰를 표시
    visibleReviews.value = [...reviews.value];
  } else {
    // 리뷰가 3개 이상이면 캐러셀 동작
    visibleReviews.value = [
      reviews.value[currentIndex % total],
      reviews.value[(currentIndex + 1) % total],
      reviews.value[(currentIndex + 2) % total],
    ];
  }
};

const next = () => {
  if (reviews.value.length >= 3) {
    currentIndex = (currentIndex + 1) % reviews.value.length; // 인덱스 증가
    updateVisibleReviews();
  }
};

// Move to the previous review
const prev = () => {
  if (reviews.value.length >= 3) {
    currentIndex = (currentIndex - 1 + reviews.value.length) % reviews.value.length; // 인덱스 감소
    updateVisibleReviews();
  }
};

// Fetch reviews on component mount
onMounted(() => {
  fetchHighRatingReviews();
});

const getStarImage = (star, rating) => {
  let path;

  if (star <= Math.floor(rating)) {
    // 채워진 별
    path = new URL('@/components/icon/starFilled.png', import.meta.url).href;
  } else if (star - 1 < rating && rating < star) {
    // 반쪽 별
    path = new URL('@/components/icon/starHalf.png', import.meta.url).href;
  } else {
    // 빈 별
    path = new URL('@/components/icon/starEmpty.png', import.meta.url).href;
  }

  //console.log("Image Path:", path);
  return path;
};

</script>


<template>
  <div class="review-container">
    <div style="margin-bottom: 35px; text-align: center;">
      <h1 class="fw-bold">평점 높은 아파트 리뷰</h1>
      <p class="text-muted">
        실사용자가 남긴 아파트별 리뷰를 확인해보세요.
      </p>
    </div>
    <div class="review-body">
      <!-- 리뷰 캐러셀 -->
      <div v-if="visibleReviews.length" class="review-carousel">
        <button
          v-if="reviews.length >= 3"
          @click="prev"
          class="carousel-button left"
        >
          ◀
        </button>

        <!-- 리뷰 카드 -->
        <div
          class="review-card"
          v-for="review in visibleReviews"
          :key="review.reviewId"
        >
          <div v-if="review.imageUrl" class="review-image">
            <img
              :src="`http://localhost:8080${review.imageUrl}`"
              alt="리뷰 이미지"
            />
          </div>
          <div class="review-content">
            <p class="content">{{ review.content }}</p>
          </div>
          
          <div class="rating">
            <span v-for="star in 5" :key="star" class="star">
              <img
                :src="getStarImage(star, review.rating)"
                alt="star"
                class="star-icon"
              />
            </span>
          </div>
          <div class="review-footer">
            <div class="user-info">
              <span class="user-nickname">{{ review.user.nickname }} / {{  review.houseInfos.aptNm }}</span>
            </div>
            <p class="review-time">
              {{ new Date(review.time).toLocaleDateString() }}
            </p>
          </div>
        </div>

        <button
          v-if="reviews.length >= 3"
          @click="next"
          class="carousel-button right"
        >
          ▶
        </button>
      </div>
      <p v-else class="no-reviews">등록된 리뷰가 없습니다.</p>
    </div>
    <img class="footer-img" src="@/img/Saly-16.png" alt="img" />
  </div>
</template>



<style scoped>
/* 전체 컨테이너 */
.review-container {
  display: flex;
  flex-direction: column;
  padding: 2rem;
  text-align: center;
  height: 700px;
  position: relative;
}

.review-body {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.footer-img {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 550px;
}

/* 제목 */
h1 {
  font-size: 2rem;
  color: var(--eerie-black);
}

/* 캐러셀 */
.review-carousel {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  gap: 1rem;
  padding: 1rem;
}

/* 캐러셀 버튼 */
.carousel-button {
  background: transparent;
  border: none;
  font-size: 2rem;
  color: var(--alabaster);
  cursor: pointer;
  z-index: 3;
}

.carousel-button:hover {
  color: var(--powder-blue);
}

.carousel-button.left {
  position: absolute;
  left: -40px;
}

.carousel-button.right {
  position: absolute;
  right: -40px;
}

/* 리뷰 카드 */
.review-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: left;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1rem;
  height: 300px;
  width: 300px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: left;
}

/* 리뷰 내용 */
.review-content {
  margin-bottom: 1rem;
  height: 150px;
  padding: 5px;
  overflow: auto;
}

/* 전체 스크롤바 영역 */
.review-content::-webkit-scrollbar {
  width: 8px;
  /* 세로 스크롤바 너비 */
  height: 8px;
  /* 가로 스크롤바 높이 */
}

/* 스크롤바 트랙 */
.review-content::-webkit-scrollbar-track {
  border-radius: 10px;
  /* 둥근 모서리 */
}

/* 스크롤바 드래그 가능한 영역 */
.review-content::-webkit-scrollbar-thumb {
  background: #f1f1f1;
  /* 드래그 영역 색상 */
  border-radius: 10px;
  /* 둥근 모서리 */
}

/* 스크롤바 드래그 영역에 호버 효과 */
.review-content::-webkit-scrollbar-thumb:hover {
  background: #d8d8d8;
  /* 호버 시 색상 */
}

.content {
  font-size: 1rem;
  color: #555;
  margin-bottom: 0.5rem;
}

/* 별점 */
.rating {
  display: flex;
  gap: 0.2rem;
}

.star-icon {
  width: 20px;
  height: 20px;
}

/* 리뷰 푸터 */
.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-nickname {
  font-size: 0.9rem;
  color: #555;
  font-weight: bold;
}

.review-time {
  font-size: 0.8rem;
  color: #aaa;
  margin: 0;
}

/* 리뷰가 없을 때 메시지 */
.no-reviews {
  font-size: 1.2rem;
  color: #666;
}
.review-image {
  margin-bottom: 1rem;
  text-align: center;
  width: 100%;
  max-height: 130px;
}
.review-image img{
  width: 100%;
  height: 100%;
  text-align: center;
  border-radius: 8px; /* 둥근 모서리 */
  object-fit: cover; /* 비율 유지하며 잘리도록 */
}
</style>
