<script setup>
import { ref, onMounted, watch, computed } from "vue";
import Chart from "chart.js/auto";

import { getAptDeal } from "@/api/apt.js";
import ReviewModal from "./ReviewModal.vue";
import axios from "@/util/axios-common.js";
const baseUrl = import.meta.env.VITE_VUE_API_URL;

const props = defineProps({
  apt: {
    type: Object,
    required: true,
  },
  showBackButton: {
    type: Boolean,
  },
});

const aptSeq = props.apt.aptSeq;

const emit = defineEmits(["hide-list"]);

const clickHide = () => {
  emit("hide-list", true);
};

// 모달 상태 관리
const showReviewModal = ref(false);

// 리뷰 모달 열기
const openReviewModal = () => {
  showReviewModal.value = true;
};

// 리뷰 모달 닫기
const closeReviewModal = () => {
  showReviewModal.value = false;
};

// 이미지 모달
const activeImage = ref(null); // 현재 활성화된 이미지 URL

const openModal = (imageUrl) => {
  activeImage.value = imageUrl; // 클릭한 이미지 URL로 설정
};

const closeModal = () => {
  activeImage.value = null; // 모달 닫기
};

// 닉네임 설정
const user = ref(
  localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : null
);

const userId = computed(() => user.value?.id || null); // 유저 ID
const userNickname = computed(() => user.value?.nickname || ""); // 유저 닉네임

const dealList = ref([]); // 거래 내역 리스트
const selectedApt = ref(aptSeq); // 선택된 아파트 ID
const AptNm = ref(""); // 아파트 이름
const editingReview = ref(null);
const isEditing = ref(false);
const nearbyFacilities = ref([]); // 주변 편의시설 리스트

// Kakao Places 서비스 객체
let placesService = null;

// 컴포넌트가 마운트될 때, 최초로 거래 내역을 불러오기
onMounted(async () => {
  fetchDealList(selectedApt.value);

  fetchReviews();
  await initializeKakaoPlaces();
  fetchNearbyFacilities();
  renderChart(dealList.value);
});

// aptSeq가 변경될 때마다 거래 내역을 다시 불러오기
watch(
  () => aptSeq,
  (newAptSeq) => {
    selectedApt.value = newAptSeq; // 새 아파트 ID로 갱신
    fetchDealList(newAptSeq); // 새로운 아파트에 맞는 거래 내역을 가져오기
    fetchNearbyFacilities();
    fetchReviews();
  }
);

const fetchDealList = async (aptSeq) => {
  getAptDeal(
    aptSeq,
    ({ data }) => {
      if (data && data.length > 0) {
        dealList.value = data;
        console.log("Fetched AptDeal:", dealList.value);

        const firstHouseInfo = data[0]?.houseInfos?.aptNm;
        if (firstHouseInfo) {
          AptNm.value = firstHouseInfo;
        } else {
          console.warn("aptNm not found in houseInfos of first data item.");
          AptNm.value = "Unknown Apartment";
        }

        // 거래 내역 데이터를 성공적으로 불러온 후 그래프를 렌더링
        renderChart(dealList.value);
      } else {
        console.warn("No deal data found.");
        dealList.value = [];
        AptNm.value = "No Data";
      }
    },
    (error) => {
      console.error("Error fetching AptDeal:", error);
      dealList.value = [];
      AptNm.value = "Error Fetching Data";
    }
  );
};

// Kakao Places 객체 초기화
// const initializeKakaoPlaces = () => {
//     if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
//         console.log("Kakao Maps API 로드 완료");
//         placesService = new kakao.maps.services.Places();
//     } else {
//         console.error("Kakao Maps API가 로드되지 않았습니다. 스크립트를 확인하세요.");
//     }
// };
const initializeKakaoPlaces = () => {
  return new Promise((resolve, reject) => {
    // Kakao Maps API가 로드되었는지 확인
    if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
      console.log("Kakao Maps API 로드 완료");
      placesService = new kakao.maps.services.Places();
      resolve(placesService); // Places 객체 반환
    } else {
      // 일정 시간 후 다시 확인
      const checkInterval = setInterval(() => {
        if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
          console.log("Kakao Maps API 로드 완료 (재확인)");
          placesService = new kakao.maps.services.Places();
          clearInterval(checkInterval);
          resolve(placesService);
        }
      }, 100); // 100ms 간격으로 확인

      // 타임아웃 설정
      setTimeout(() => {
        if (!placesService) {
          clearInterval(checkInterval);
          console.error(
            "Kakao Maps API가 로드되지 않았습니다. 스크립트를 확인하세요."
          );
          reject(new Error("Kakao Maps API 로드 실패"));
        }
      }, 5000); // 5초 내에 로드되지 않으면 실패 처리
    }
  });
};

// 주변 편의시설 데이터 가져오기
const fetchNearbyFacilities = async () => {
  if (!placesService) {
    console.error("Kakao Places 서비스가 초기화되지 않았습니다.");
    return;
  }

  // 유효한 위도와 경도 확인
  if (!props.apt.latitude || !props.apt.longitude) {
    console.warn("유효한 위도와 경도 정보가 없습니다.");
    nearbyFacilities.value = [];
    return;
  }

  const aptLatitude = props.apt.latitude; // 아파트 위도
  const aptLongitude = props.apt.longitude; // 아파트 경도
  const location = new kakao.maps.LatLng(aptLatitude, aptLongitude);
  const categories = ["BK9", "MT1", "PM9", "OL7", "CE7", "CS2"];

  try {
    // 카테고리 검색을 병렬로 처리
    const results = await Promise.all(
      categories.map(
        (category) =>
          new Promise((resolve) => {
            placesService.categorySearch(
              category,
              (data, status) => {
                if (status === kakao.maps.services.Status.OK) {
                  // 거리 필터링: 500m 이내 시설만 추가
                  const filteredData = data.filter((place) => {
                    const distance = calculateDistance(
                      aptLatitude,
                      aptLongitude,
                      parseFloat(place.y), // 시설 위도
                      parseFloat(place.x) // 시설 경도
                    );
                    return distance <= 500;
                  });
                  resolve(filteredData);
                } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
                  console.warn(`카테고리 ${category}에 대한 결과가 없습니다.`);
                  resolve([]); // 빈 배열 반환
                } else {
                  console.error(
                    `카테고리 ${category} 검색 중 오류 발생: ${status}`
                  );
                  resolve([]); // 빈 배열 반환
                }
              },
              {
                location,
                radius: 500, // 500m 반경
              }
            );
          })
      )
    );

    // 검색 결과 병합
    nearbyFacilities.value = results.flat();
    //console.log("검색된 편의시설:", nearbyFacilities.value);
  } catch (error) {
    console.error("주변 편의시설 검색 중 오류 발생:", error);
  }
};

/**
 * 두 지점 사이의 거리 계산 (Haversine Formula)
 * @param {number} lat1 - 첫 번째 지점의 위도
 * @param {number} lon1 - 첫 번째 지점의 경도
 * @param {number} lat2 - 두 번째 지점의 위도
 * @param {number} lon2 - 두 번째 지점의 경도
 * @returns {number} - 두 지점 사이의 거리 (미터 단위)
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3; // 지구의 반지름 (미터)
  const φ1 = (lat1 * Math.PI) / 180; // 첫 번째 지점의 위도를 라디안으로 변환
  const φ2 = (lat2 * Math.PI) / 180; // 두 번째 지점의 위도를 라디안으로 변환
  const Δφ = ((lat2 - lat1) * Math.PI) / 180; // 위도의 차이
  const Δλ = ((lon2 - lon1) * Math.PI) / 180; // 경도의 차이

  const a =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c; // 두 지점 사이의 거리 (미터 단위)
}

const reviewList = ref([]); // 리뷰 리스트

const fetchReviews = async () => {
  try {
    const response = await axios.get(`/review/${selectedApt.value}`);
    reviewList.value = response.data;
    console.log("Fetched Reviews:", reviewList.value);
  } catch (error) {
    console.error("리뷰 데이터를 가져오는 중 오류 발생:", error);
    reviewList.value = []; // 에러 시 빈 배열로 초기화
  }
};

// 평균 평점 계산
const averageRating = computed(() => {
  if (!reviewList.value.length) return "평점 없음";
  const total = reviewList.value.reduce(
    (sum, review) => sum + review.rating,
    0
  );
  return (total / reviewList.value.length).toFixed(1); // 소수점 1자리까지
});

// 평점을 별 이미지로 변환
const getStarImage = (star, rating) => {
  let path;

  if (star <= Math.floor(rating)) {
    // 채워진 별
    path = new URL("@/components/icon/starFilled.png", import.meta.url).href;
  } else if (star - 1 < rating && rating < star) {
    // 반쪽 별
    path = new URL("@/components/icon/starHalf.png", import.meta.url).href;
  } else {
    // 빈 별
    path = new URL("@/components/icon/starEmpty.png", import.meta.url).href;
  }

  return path;
};

const generateStars = (rating) => {
  return Array.from({ length: 5 }, (_, index) =>
    getStarImage(index + 1, rating)
  );
};

const openEditReviewModal = (review) => {
  editingReview.value = review;
  isEditing.value = true;
  showReviewModal.value = true;
};

// Handle delete
const deleteReview = async (reviewId) => {
  if (!confirm("Are you sure you want to delete this review?")) return;

  try {
    await axios.delete(`/review/delete/${reviewId}`, {
      params: { userId: userId.value, isAdmin: false },
    });
    alert("Review deleted successfully!");
    fetchReviews(); // Refresh the review list
  } catch (error) {
    console.error("Error deleting review:", error);
    alert("Failed to delete review.");
  }
};

// Handle review save (add/edit)
const handleReviewSaved = () => {
  fetchReviews(); // Refresh the reviews after saving
  showReviewModal.value = false;
  editingReview.value = null;
  isEditing.value = false;
};

// 테이블 표시 데이터 제어
const visibleCount = ref(10); // 처음 표시할 데이터 수

// 표시할 거래 데이터 계산
const visibleDeals = computed(() =>
  dealList.value.slice(0, visibleCount.value)
);

// "더보기" 버튼 클릭 시 데이터 추가 로드
const loadMore = () => {
  visibleCount.value += 10; // 추가로 10개의 데이터 로드
};

function formatDealAmount(amount) {
  if (!amount) return ""; // 값이 없을 경우 빈 문자열 반환
  const numericAmount = parseInt(amount.replace(/,/g, "")); // 숫자로 변환
  const billion = Math.floor(numericAmount / 10000); // 억 단위 계산
  const tenThousand = numericAmount % 10000; // 만 단위 계산

  return `${billion > 0 ? `${billion}억` : ""}${
    billion > 0 && tenThousand > 0 ? "\n" : ""
  }${tenThousand > 0 ? `${tenThousand}만 원` : ""}`;
}
function formatDealDate(year, month, day) {
  // 날짜 포맷 변경
  return `${year}년 ${month}월 ${day}일`;
}

/////////////////////// 그래프////////////////////////////////
// 데이터 처리 함수
function processData(dealData) {
  const groupedData = {};

  dealData.forEach((deal) => {
    const dateKey = `${deal.dealYear}-${deal.dealMonth}-${deal.dealDay}`;
    const dealAmount = parseInt(deal.dealAmount.replace(/,/g, ""), 10);

    if (!groupedData[dateKey]) {
      groupedData[dateKey] = {
        count: 0,
        totalAmount: 0,
      };
    }

    groupedData[dateKey].count += 1;
    groupedData[dateKey].totalAmount += dealAmount;
  });

  // 날짜별로 정렬 후 상위 15개만 가져오기
  const sortedKeys = Object.keys(groupedData).sort(
    (a, b) => new Date(b) - new Date(a)
  ); // 최신 거래 순 정렬
  const recentKeys = sortedKeys.slice(0, 15).reverse(); // 최신 15개를 가져와 순서 뒤집기 (오름차순)

  const dealCounts = recentKeys.map((date) => groupedData[date].count);
  const avgPrices = recentKeys.map(
    (date) => groupedData[date].totalAmount / groupedData[date].count
  );

  return { labels: recentKeys, dealCounts, avgPrices };
}

let chartInstance = null;

function renderChart(dealData) {
  if (chartInstance) {
    chartInstance.destroy(); // 기존 차트 제거
  }

  const { labels, dealCounts, avgPrices } = processData(dealData);

  const ctx = document.getElementById("dealChart").getContext("2d");
  chartInstance = new Chart(ctx, {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          label: "거래 수",
          data: dealCounts,
          backgroundColor: "rgba(54, 162, 235, 0.5)", // 파란색 반투명
          borderColor: "rgba(54, 162, 235, 1)", // 파란색
          borderWidth: 1,
          yAxisID: "y-axis-dealCount",
          barPercentage: 0.6, // 막대 너비 조정
        },
        {
          label: "평균 거래가",
          data: avgPrices,
          type: "line",
          borderColor: "rgba(75, 192, 192, 1)", // 초록색
          backgroundColor: "rgba(75, 192, 192, 0.2)", // 초록색 반투명
          borderWidth: 2,
          pointRadius: 3, // 데이터 포인트 크기
          pointBackgroundColor: "rgba(75, 192, 192, 1)", // 데이터 포인트 색상
          yAxisID: "y-axis-avgPrice",
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false, // 반응형 높이 조정
      plugins: {
        legend: {
          position: "top", // 범례 위치
          labels: {
            font: {
              size: 14,
              family: "Pretendard",
            },
            color: "#333", // 범례 글씨 색상
          },
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              let value = context.raw;
              if (context.dataset.label === "평균 거래가") {
                value = value.toLocaleString() + " 원"; // 천단위 콤마 추가
              }
              return `${context.dataset.label}: ${value}`;
            },
          },
          backgroundColor: "rgba(0, 0, 0, 0.8)", // 툴팁 배경색
          titleFont: { size: 14, family: "Pretendard", weight: "bold" },
          bodyFont: { size: 12, family: "Pretendard" },
          padding: 10,
        },
      },
      scales: {
        "y-axis-dealCount": {
          type: "linear",
          position: "left",
          grid: {
            color: "rgba(200, 200, 200, 0.3)", // 연한 그리드선
          },
          title: {
            display: true,
            text: "거래 수",
            font: { size: 14, family: "Pretendard", weight: "bold" },
            color: "#333",
          },
          ticks: {
            font: { size: 12 },
            color: "#333",
            stepSize: 1, // 눈금 간격을 1로 설정
            beginAtZero: true, // 0에서 시작
          },
          suggestedMax: Math.max(...dealCounts) + 1, // 최대값보다 2 높게 설정
        },
        "y-axis-avgPrice": {
          type: "linear",
          position: "right",
          grid: {
            drawOnChartArea: false, // 오른쪽 눈금선 비활성화
          },
          title: {
            display: true,
            text: "평균 거래가 (천원)",
            font: { size: 14, family: "Pretendard", weight: "bold" },
            color: "#333",
          },
          ticks: {
            font: { size: 12 },
            color: "#333",
            callback: (value) => value.toLocaleString(), // 천단위 콤마
          },
          suggestedMax: Math.max(...avgPrices) + 1,
        },
        x: {
          grid: {
            color: "rgba(200, 200, 200, 0.3)", // 연한 그리드선
          },
          ticks: {
            font: { size: 12 },
            color: "#333",
          },
        },
      },
    },
  });
}

const categoryList = [
  { code: "BK9", name: "은행" },
  { code: "MT1", name: "마트" },
  { code: "PM9", name: "약국" },
  { code: "OL7", name: "주유소" },
  { code: "CE7", name: "카페" },
  { code: "CS2", name: "편의점" },
];

// 현재 선택된 카테고리
const selectedCategory = ref("BK9"); // 초기값: 편의점

// 선택된 카테고리에 맞는 시설 필터링
const filteredFacilities = computed(() => {
  return nearbyFacilities.value.filter(
    (facility) => facility.category_group_code === selectedCategory.value
  );
});

// 카테고리 변경 함수
const filterByCategory = (categoryCode) => {
  selectedCategory.value = categoryCode;
};

function formatReviewTime(timestamp) {
  if (!timestamp) return "Invalid date";

  const date = new Date(timestamp);

  // 날짜와 시간 추출
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0"); // 월은 0부터 시작하므로 +1
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  // 형식: YYYY-MM-DD HH:mm
  return `${year}-${month}-${day} ${hours}:${minutes}`;
}
</script>

<template>
  <div class="deal-list-view">
    <div class="list-header">
      <button v-if="showBackButton" @click="clickHide" class="back-button">
        ← 뒤로
      </button>
      <h2>
        {{ AptNm }}
        <span v-if="reviewList.length" class="average-rating">
          ({{ averageRating }}
          <span class="star" style="color: var(--jonquil)">★</span>)
        </span>
      </h2>

      <button v-if="user" @click="openReviewModal" class="review-button">
        리뷰 작성
      </button>
    </div>

    <div class="chart-container">
      <canvas id="dealChart"></canvas>
    </div>
    <p v-if="dealList.length" class="deal-count">
      총 거래 내역: {{ dealList.length }}건
    </p>
    <div class="table-container">
      <table class="deal-table">
        <thead>
          <tr>
            <th>거래 금액</th>
            <th>계약 날짜</th>
            <th>전용 면적 (m²)</th>
            <th>층수</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in visibleDeals" :key="item.dealId">
            <td>{{ formatDealAmount(item.dealAmount) }}</td>
            <td>
              {{ formatDealDate(item.dealYear, item.dealMonth, item.dealDay) }}
            </td>
            <td>{{ item.excluUseAr }}</td>
            <td>{{ item.floor }}층</td>
          </tr>
        </tbody>
      </table>
      <button
        v-if="visibleDeals.length < dealList.length"
        @click="loadMore"
        class="load-more-button"
      >
        더보기
      </button>
    </div>

    <div class="facility-section">
      <div class="facility-header">
        <h2>주변 편의시설 (500m 이내)</h2>
      </div>

      <!-- 카테고리 버튼 -->
      <div class="category-buttons">
        <button
          v-for="category in categoryList"
          :key="category.code"
          @click="filterByCategory(category.code)"
          :class="{ active: selectedCategory === category.code }"
          class="category-button"
        >
          {{ category.name }}
        </button>
      </div>

      <!-- 시설 목록 -->
      <div
        v-if="filteredFacilities.length > 0"
        class="facility-table-container"
      >
        <table class="facility-table">
          <thead>
            <tr>
              <th>거리</th>
              <th>시설 이름</th>
              <th>전화번호</th>
              <th>주소</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="facility in filteredFacilities" :key="facility.id">
              <td class="fw-bold">{{ facility.distance }}m</td>
              <td>{{ facility.place_name }}</td>
              <td>{{ facility.phone || "없음" }}</td>
              <td style="display: flex; justify-content: space-between">
                {{ facility.road_address_name || facility.address_name }}
                <a
                  :href="facility.place_url"
                  target="_blank"
                  class="button-link"
                  >→</a
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 시설 없음 메시지 -->
      <div v-else class="no-facility">
        <p>선택된 카테고리에 해당하는 시설이 없습니다.</p>
      </div>
    </div>

    <div class="review-section">
      <div class="review-title">
        <h2>리뷰 목록</h2>
      </div>
      <div v-if="reviewList.length">
        <div
          v-for="review in reviewList"
          :key="review.reviewId"
          class="review-card"
        >
          <div class="review-header">
            <h4>{{ review.user.nickname }}</h4>
            <div class="review-stars">
              <img
                v-for="(star, index) in generateStars(review.rating)"
                :key="index"
                :src="star"
                class="star-icon"
              />
            </div>
          </div>
          <div class="review-content">
            <p class="review-text">{{ review.content }}</p>
            <div v-if="review.imageUrl" class="review-image">
              <img
                :src="`${baseUrl}${review.imageUrl}`"
                alt="리뷰 이미지"
                @click="openModal(`${baseUrl}${review.imageUrl}`)"
                class="review-thumbnail"
              />
            </div>

            <!-- 모달 -->
            <div v-if="activeImage" class="modal" @click.self="closeModal">
              <img :src="activeImage" alt="모달 이미지" class="modal-image" />
            </div>
          </div>

          <div class="review-footer">
            <div v-if="userId === review.user.id" class="footer-buttons">
              <button @click="openEditReviewModal(review)" class="edit-button">
                수정
              </button>
              <button
                @click="deleteReview(review.reviewId)"
                class="delete-button"
              >
                삭제
              </button>
            </div>
            <p class="footer-content">{{ formatReviewTime(review.time) }}</p>
          </div>
        </div>
      </div>
      <div v-else class="no-reviews">
        리뷰가 없습니다. 첫 번째 리뷰를 작성해보세요!
      </div>
    </div>

    <ReviewModal
      v-if="showReviewModal"
      :apt-seq="selectedApt"
      :apt-name="AptNm"
      :user-nickname="userNickname"
      :user-id="userId"
      :editing-review="editingReview"
      :is-editing="isEditing"
      @review-saved="handleReviewSaved"
      @close="showReviewModal = false"
    />
  </div>
</template>

<style scoped>
/* 전체 페이지 */
.deal-list-view {
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #333;
  padding: 10px;
  width: 90%;
  position: sticky;
  top: 0;
  min-width: 600px;
  max-width: 800px;
}

/* 헤더 스타일 */
.list-header {
  position: sticky; /* 컨테이너 내에서 고정 */
  top: 0; /* .map-list의 최상단에 고정 */
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0; /* 여백 제거 */
  padding: 10px;
  width: 100%;
  height: 62px;
  background-color: white; /* 배경색 */
  border-bottom: 1px solid #ddd;
  z-index: 10; /* 스크롤되는 다른 요소 위로 올림 */
}

.list-header h2 {
  font-size: 1.5rem;
  color: #222;
  flex: 1;
  text-align: center;
  margin: 0;
}

p {
  margin-bottom: 0px;
  padding-left: 10px;
}
.back-button {
  font-size: 1rem;
  color: var(--eerie-grey);
  background: none;
  border: none;
  cursor: pointer;
}

.back-button:hover {
  color: #0056b3;
}

.review-button {
  background-color: #4f9cf9;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 15px;
  cursor: pointer;
}

.review-button:hover {
  background-color: #4388dd;
}

/* 테이블 스타일 */
.table-container {
  margin-top: 20px;
  overflow-x: auto;
  border-radius: 18px;
}

.deal-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
  font-size: 1rem;
  background-color: white;
}

.deal-table thead {
  background-color: #c5d6ed7d;
  border-bottom: 1px solid var(--alice-blue);
}

.deal-table th,
.deal-table td {
  padding: 12px 16px;
}

.deal-table th {
  font-weight: bold;
  color: #555;
}

.deal-table tbody tr:nth-child(odd) {
  background-color: #bfcde134;
}

.deal-table tbody tr:nth-child(even) {
  background-color: #dde4ee34;
}

.deal-table tbody tr:hover {
  background-color: #bfcde156;
}

.load-more-button {
  display: block;
  width: 100%;
  /* 버튼이 테이블 너비에 맞게 */
  margin: 0 auto;
  /* 중앙 정렬 */
  padding: 12px 0;
  /* 위아래 여백 */
  font-size: 1rem;
  background-color: #e4eaf17d;
  /* 약간의 불투명한 배경 */
  color: rgb(121, 119, 119);
  /* 글씨는 검정색 */
  border: none;
  /* 테두리 제거 */
  border-radius: 0;
  /* 행처럼 보이게 둥근 모서리 제거 */
  text-align: center;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.load-more-button:hover {
  background-color: #f0f0f0;
  /* 연한 회색 배경 */
  color: black;
  /* 글씨 유지 */
}

/* 차트 컨테이너 */
.chart-container {
  min-width: 600px;
  width: 100%;
  height: 400px;
  /* 차트 높이 고정 */
  margin: 20px auto;
  padding: 10px;
  border: 1px solid #ddd;
  /* 테두리 추가 */
  border-radius: 8px;
  /* 둥근 모서리 */
  background-color: #ffffff;
  /* 배경 연한 회색 */
}

canvas {
  width: 100%;
  height: auto;
}

/* 주변 편의시설 */
.facility-section {
  max-width: 800px;
}

.facility-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 0; /* 여백 제거 */
  padding: 10px;
  width: 100%;
  background-color: white;
  border-bottom: 1px solid #ddd;
  margin-bottom: 30px;
}

.facility-header h2 {
  font-size: 1.5rem;
  color: #222;
  flex: 1;
  text-align: center;
  margin-top: 50px;
}

.category-buttons {
  display: flex;
  flex-direction: row;
  text-align: center;
  width: 100%;
}
.category-button {
  flex: 1;
  width: 100%;
  background-color: white;
  border: 1px solid #c2d1c97d;
  padding: 5px 10px;
  border-radius: 15px 15px 0 0;
  overflow: hidden;
}
.category-button.active {
  background-color: #d9ebe17d;
  border-bottom: none;
}

.facility-table-container {
  display: flex;
  justify-content: center;
  width: 100%;
  border: 1px solid #c2d1c97d;
  border-top: none;
  border-radius: 0 0 15px 15px;
}
.facility-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
  font-size: 1rem;
  background-color: white;
  border-top: none;
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.facility-table thead {
  background-color: #d9ebe17d;
}

.facility-table th,
.facility-table td {
  padding: 12px 16px;
}
.facility-table td a {
  color: #333;
}
.facility-table th {
  font-weight: bold;
  color: #555;
}

.facility-table tbody tr:nth-child(odd) {
  background-color: #f5faf87d;
}

.facility-table tbody tr:hover {
  background-color: #d9ebe15e;
}

.no-facility {
  text-align: center;
  padding: 50px 0px;
  border: 1px solid #ddd;
  border-top: none;
  background-color: #d9ebe17d;
}

.button-link {
  display: inline-block;
  text-decoration: none;
  color: white;
  background-color: #c2d1c97d;
  padding: 1px 5px;
  width: 25px;
  height: 25px;
  font-size: 10;
  font-weight: 300;
  border-radius: 50%;
  text-align: right;
  transition: background-color 0.3s ease; /* 마우스 오버 애니메이션 */
}

.button-link:hover {
  background-color: #8aa1947d;
}

/* 리뷰! */
.average-rating {
  color: #333;
  font-weight: 200;
}

.review-image {
  flex: 2;
  margin-bottom: 1rem;
  text-align: center;
  width: 100%;
  max-height: 80px;
}
.review-image img {
  width: 100%;
  height: 100%;
  text-align: center;
  border-radius: 8px;
  object-fit: cover; /* 비율 유지하며 잘리도록 */
}

.review-section {
  margin-top: 20px;
}

.no-reviews {
  text-align: center;
  margin-bottom: 50px;
}

.review-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px 30px;
  margin-bottom: 10px;
  max-width: 800px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px 0px;
}

.review-header h4 {
  margin: 0;
}

.review-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 30px 0px;
  padding: 20px;
  width: 100%;
  border-bottom: 1px solid #ddd;
}

.review-title h2 {
  font-size: 1.5rem;
  color: #222;
  flex: 1;
  text-align: center;
  margin: 0;
}

.review-stars {
  display: flex;
  gap: 4px;
}

.star-icon {
  width: 20px;
  height: 20px;
}

.review-content {
  display: flex;
  flex-direction: row;
  overflow-y: auto; /* 세로 스크롤 활성화 */
  scrollbar-width: thin; /* Firefox 스크롤바 크기 */
  -ms-overflow-style: none; /* IE 스크롤 스타일 제거 */
}
.review-text {
  flex: 8;
  font-size: 1rem;
  color: #333;
  margin: 10px 0;
  max-height: 150px;
  word-wrap: break-word; /* 긴 단어를 줄바꿈 */
  line-height: 1.5; /* 텍스트 간격 조정 */
}

.review-content::-webkit-scrollbar {
  width: 4px; /* 스크롤바 너비 */
}

.review-content::-webkit-scrollbar-thumb {
  background: #edecec; /* 스크롤바 색상 */
  border-radius: 4px; /* 둥근 스크롤바 */
}

.review-content::-webkit-scrollbar-thumb:hover {
  background: #dedddd; /* 호버 시 색상 */
}

.review-footer {
  position: relative;
  display: flex;
  justify-content: space-between; /* 버튼은 왼쪽, 콘텐츠는 오른쪽 */
  align-items: center;
  font-size: 0.8rem;
  color: #666;
  padding: 10px 0px;
}

.review-footer .footer-buttons {
  display: flex;
  gap: 10px; /* 버튼 간 간격 */
}

.review-footer button {
  padding: 5px 10px;
  font-size: 0.8rem;
  cursor: pointer;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 15px;
}
.review-footer button:hover {
  background-color: #f5f5f5;
}

.review-footer .footer-content {
  position: absolute;
  right: 0;
}

/* 이미지 모달 배경 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3); /* 배경 어둡게 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 이미지만 표시 (배경 제거) */
.modal-image {
  width: auto;
  max-width: 40%; /* 화면 너비의 90%까지 */
  height: auto;
  max-height: 40%; /* 화면 높이의 90%까지 */
  border-radius: 0; /* 둥근 테두리 제거 (선택 사항) */
  box-shadow: none; /* 그림자 제거 */
}

/* 클릭 닫기 기능을 위한 투명 영역 */
.modal::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>
