<script setup>
import { ref, onMounted, computed } from "vue";
import axios from "@/util/axios-common.js";
import FindRouteView from "./FindRouteView.vue";

// Props 정의
const props = defineProps({
  apt: {
    type: Object,
    required: true,
  },
});

const aptItem = ref(props.apt);

const emit = defineEmits(["key-select"]);

const showFindRoute = ref(false);
const destination = ref({});

const user = ref(
  localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : null
);

// 북마크 상태 관리 (기본값은 false로 설정)
const bookmarked = ref(false);

const fetchBookmarkStatus = async () => {
  if (!user.value || !aptItem.value.aptSeq) return; // 로그인하지 않았거나 aptSeq가 없으면 중단
  try {
    const response = await axios.get("/interest/bookmark", {
      params: {
        houseInfoId: aptItem.value.aptSeq,
        userId: user.value.id,
      },
    });

    // 서버에서 데이터가 존재하면 bookmarked 값 업데이트
    if (response.data && response.data.bookmarked !== undefined) {
      bookmarked.value = response.data.bookmarked;
    }
  } catch (error) {
    console.error("북마크 상태를 불러오는데 실패했습니다:", error);
  }
};

// 북마크 토글
const toggleBookmark = async () => {
  bookmarked.value = !bookmarked.value;

  const interestData = {
    houseInfoId: aptItem.value.aptSeq,
    bookmarked: bookmarked.value, // 토글된 상태를 명확히 전달
    time: formatDate(new Date()), // MySQL에서 허용하는 형식으로 변환
  };

  try {
    if (user.value) {
      // 유저가 존재하면 DB 업데이트
      await axios.post("/interest/view-apartment", {
        userId: user.value.id,
        ...interestData,
      });
      console.log(
        bookmarked.value
          ? "북마크가 추가되었습니다."
          : "북마크가 취소되었습니다."
      );
    } else {
      // 로컬 스토리지 업데이트
      const localData =
        JSON.parse(localStorage.getItem("recentApartments")) || [];
      const existingIndex = localData.findIndex(
        (item) => item.houseInfoId === aptItem.value.aptSeq
      );
      if (existingIndex > -1) {
        localData[existingIndex].bookmarked = bookmarked.value;
      } else {
        localData.push(interestData);
      }
      localStorage.setItem("recentApartments", JSON.stringify(localData));
      console.log(bookmarked.value ? "로컬 북마크 추가" : "로컬 북마크 취소");
    }
  } catch (error) {
    console.error("북마크 상태 업데이트 중 오류가 발생했습니다:", error);
  }
};

// 길찾기 모달 열기 함수
const openFindRoute = () => {
  destination.value = {
    lat: aptItem.value.latitude,
    lng: aptItem.value.longitude,
    name: aptItem.value.aptNm,
  };
  console.log(
    `목적지 설정: 위도(${destination.value.lat}), 경도(${destination.value.lng}), 이름(${destination.value.name})`
  );
  showFindRoute.value = true;
};
const closeModal = () => {
  showFindRoute.value = false;
};

const formatDate = (date) => {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const hours = String(d.getHours()).padStart(2, "0");
  const minutes = String(d.getMinutes()).padStart(2, "0");
  const seconds = String(d.getSeconds()).padStart(2, "0");

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

function formatDealAmount(amount) {
  if (!amount) return ""; // 값이 없을 경우 빈 문자열 반환
  const numericAmount = parseInt(amount.replace(/,/g, "")); // 숫자로 변환
  const billion = Math.floor(numericAmount / 10000); // 억 단위 계산
  const tenThousand = numericAmount % 10000; // 만 단위 계산

  return `${billion > 0 ? `${billion}억` : ""}${billion > 0 && tenThousand > 0 ? "\n" : ""
    }${tenThousand > 0 ? `${tenThousand}만 원` : ""}`;
}

function getBackgroundColorClass(amount) {
  const numericAmount = parseInt(amount.replace(/,/g, "")); // 숫자로 변환
  if (numericAmount >= 500000) return "x-high-price"; // 50억 이상
  else if (numericAmount >= 200000) return "very-high-price"; // 20억 이상
  else if (numericAmount >= 100000) return "high-price"; // 10억 이상
  else if (numericAmount >= 50000) return "medium-price"; // 5억 이상
  return "low-price"; // 그 이하
}

// 리뷰 관련 로직
const reviewList = ref([]);
const fetchReviews = async () => {
  try {
    const response = await axios.get(`/review/${aptItem.value.aptSeq}`);
    reviewList.value = response.data;
    console.log("Fetched Reviews:", reviewList.value);
  } catch (error) {
    console.error("리뷰 데이터를 가져오는 중 오류 발생:", error);
    reviewList.value = [];
  }
};

const averageRating = computed(() => {
  if (!reviewList.value.length) return "평점 없음";
  const total = reviewList.value.reduce(
    (sum, review) => sum + review.rating,
    0
  );
  return (total / reviewList.value.length).toFixed(1);
});

// 카드 클릭 이벤트 처리
const onSelect = async () => {
  emit("key-select", aptItem.value);

  if (!aptItem.value) {
    console.error("Apartment is missing!");
    return;
  }

  const interestData = {
    houseInfoId: aptItem.value.aptSeq,
    bookmarked: bookmarked.value, // 명확히 설정된 bookmarked 값 전달
    time: formatDate(new Date()), // MySQL에서 허용하는 형식으로 변환
  };

  try {
    if (user.value) {
      await axios.post("/interest/view-apartment", {
        userId: user.value.id,
        ...interestData,
      });
    } else {
      const localData =
        JSON.parse(localStorage.getItem("recentApartments")) || [];
      const existingIndex = localData.findIndex(
        (item) => item.houseInfoId === aptItem.value.aptSeq
      );
      if (existingIndex > -1) {
        localData[existingIndex] = interestData;
      } else {
        localData.push(interestData);
      }
      localStorage.setItem("recentApartments", JSON.stringify(localData));
    }
  } catch (error) {
    console.error("DB 저장 중 오류가 발생했습니다:", error);
  }
};

// 컴포넌트 마운트 시 북마크 상태와 리뷰 데이터 불러오기
onMounted(() => {
  fetchBookmarkStatus();
  fetchReviews();
});
</script>

<template>
  <div class="apartment-container">
    <div class="apartment-card">
      <div class="apartment-info" @click="onSelect">
        <div class="important-info" :class="getBackgroundColorClass(aptItem.recentDealAmount)">
          <span class="amount">{{
            formatDealAmount(aptItem.recentDealAmount)
          }}</span>
          <span>{{ aptItem.exclusiveArea }}㎡</span>
        </div>
        <div class="detail-info">
          <div class="detail-header">
            <h2 class="scrolling-text">
              {{ aptItem.aptNm }}
              <span v-if="averageRating !== '평점 없음'" class="average-rating">
                ({{ averageRating }}
                <span class="star" style="color: var(--jonquil)">★</span>)
              </span>
              <span v-else class="average-rating">
                (  -  
                <span class="star" style="color: #ddd">★</span>)
              </span>
            </h2>
          </div>
          <p>{{ aptItem.umdNm }} {{ aptItem.roadNm }}</p>
          <p>{{ aptItem.buildYear }}년 건축</p>
          <p class="text-muted" style="font-size: 1rem">
            최근 거래: {{ aptItem.recentDealDate }}
          </p>
        </div>
      </div>
      <div class="service-buttons">
        <div class="bookmark-button" @click.stop="toggleBookmark">
          <img v-if="bookmarked" src="@/components/icon/heartFilled.png" alt="Bookmark"
            style="width: 25px; height: 25px" />
          <img v-if="!bookmarked" src="@/components/icon/heartEmpty.png" alt="Bookmark"
            style="width: 24px; height: 25px" />
        </div>

        <!-- 길찾기 버튼 -->
        <button @click="openFindRoute" class="directions-button">길찾기</button>
      </div>
    </div>
    <FindRouteView v-if="showFindRoute" :destination="destination" @close="showFindRoute = false"
      @click-outside="closeModal" />
  </div>
</template>

<style scoped>
img {
  width: 140px;
  height: 140px;
}

.apartment-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
  width: 100%;
  height: 180px;
  border-top: 1px solid #ddd;
  padding: 16px;
  margin: 0;
}

.apartment-container:hover {
  background-color: #f9f9f9;
}

.apartment-card {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  position: relative;
  width: fit-content;
}

.apartment-info {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.apartment-info h2 {
  font-size: 1.5rem;
  margin-bottom: 8px;
  font-weight: bold;
  color: var(--eerie-black);
}

.apartment-info p {
  font-size: 1.1rem;
  margin: 0;
  color: var(--eerie-black);
}

.important-info {
  color: white;
  border-radius: 50%;
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  text-align: center;
  margin-right: 25px;
}

.important-info.x-high-price {
  background-color: #03110bdc;
}

.important-info.very-high-price {
  background-color: #113120dc;
}

.important-info.high-price {
  background-color: #174936d7;
}

.important-info.medium-price {
  background-color: rgba(97, 150, 125, 0.76);
}

.important-info.low-price {
  background-color: #99c288da;
}

.important-info .amount {
  font-weight: 800;
  font-size: 1.3rem;
  width: 120px;
  white-space: pre-line;
}

.detail-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.detail-header {
  display: flex;
  flex-direction: row;
  gap: 20px;
  width: 200px;
  overflow-x: auto;
  white-space: nowrap;
  scrollbar-width: none;
}

.detail-header .scrolling-text {
  display: inline-block;
  /* animation: marquee 10s linear infinite; */
}

.detail-header.no-animation .scrolling-text {
  position: static;
  animation: none;
  /* 애니메이션 비활성화 */
}

@keyframes marquee {
  0% {
    transform: translateX(100%);
  }

  100% {
    transform: translateX(-100%);
  }
}

.service-buttons {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;
}

.bookmark-button img {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.bookmark-button img:hover {
  transform: scale(1.2);
}

.directions-button {
  background-color: #414d4791;
  color: white;
  width: 50px;
  border: none;
  border-radius: 8px;
  padding: 6px 8px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.directions-button:hover {
  background-color: #414d47c8;
  transform: scale(1.02);
}

.directions-button:active {
  background-color: #414d47c8;
  transform: scale(0.95);
}

.average-rating {
  color: #333;
  font-size: 1.2rem;
  font-weight: 300;
}
</style>
