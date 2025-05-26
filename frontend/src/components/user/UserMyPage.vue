<script setup>
import { storeToRefs } from "pinia";
import { useUserStore } from "@/stores/user";
import apiClient from "@/util/axios-common.js";
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import UserInterest from "./UserInterest.vue";
const baseUrl = import.meta.env.VITE_VUE_API_URL / api;

const userStore = useUserStore();
const { getUser } = storeToRefs(userStore);
const user = getUser.value;
const router = useRouter();

// State for reviews
const userReviews = ref([]);
const visibleReviews = ref([]); // 현재 보여지는 리뷰 리스트
let currentIndex = 0; // 캐러셀 현재 인덱스
const profileImage = ref(
  user?.profileImage || "http://localhost:5173/src/img/default.jpg"
); // 기본 이미지 또는 유저 이미지
const isRegular = ref(false); // loginType이 REGULAR인지 여부
const bookmarkedCount = ref(0);
const inquiryCount = ref(0);

const reviewStats = computed(() => {
  const reviewCount = userReviews.value.length;
  const averageRating =
    reviewCount > 0
      ? (
          userReviews.value.reduce((sum, review) => sum + review.rating, 0) /
          reviewCount
        ).toFixed(2)
      : "0.00";
  return { reviewCount, averageRating };
});

// Fetch bookmarked count
const fetchBookmarkedCount = async () => {
  if (!user) return;
  try {
    const response = await apiClient.get(`/interest/bookmarked-apartments`, {
      params: { userId: user.id },
    });
    bookmarkedCount.value = response.data.length;
  } catch (error) {
    console.error("Failed to fetch bookmarked count:", error);
  }
};

// Fetch inquiry count
const fetchBoardCount = async () => {
  if (!user) return;
  try {
    const response = await apiClient.get(`/board/user/${user.id}`);
    inquiryCount.value = response.data.length;
  } catch (error) {
    console.error("Failed to fetch inquiry count:", error);
  }
};

// Fetch reviews by user ID
const fetchUserReviews = async () => {
  if (!user) return;
  try {
    const response = await apiClient.get(`/review/user/${user.id}`);
    userReviews.value = response.data;
    updateVisibleReviews();
  } catch (error) {
    console.error("Failed to fetch user reviews:", error);
  }
};

const next = () => {
  if (userReviews.value.length >= 3) {
    currentIndex = (currentIndex + 1) % userReviews.value.length; // 인덱스 증가
    updateVisibleReviews();
  }
};

// Move to the previous review
const prev = () => {
  if (userReviews.value.length >= 3) {
    currentIndex =
      (currentIndex - 1 + userReviews.value.length) % userReviews.value.length; // 인덱스 감소
    updateVisibleReviews();
  }
};

// Update visible reviews for the carousel
const updateVisibleReviews = () => {
  const total = userReviews.value.length;

  if (total < 3) {
    // 리뷰가 3개 미만이면 모든 리뷰를 표시
    visibleReviews.value = [...userReviews.value];
  } else {
    // 리뷰가 3개 이상이면 캐러셀 동작
    visibleReviews.value = [
      userReviews.value[currentIndex % total],
      userReviews.value[(currentIndex + 1) % total],
      userReviews.value[(currentIndex + 2) % total],
    ];
  }
};

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

  //console.log("Image Path:", path);
  return path;
};

const fetchUserProfile = async () => {
  if (!user) return;
  try {
    const response = await apiClient.get(`/user/${user.id}`);
    console.log(response.data);

    isRegular.value = response.data.loginType === "REGULAR";

    // loginType에 따라 프로필 이미지 설정
    if (isRegular.value) {
      profileImage.value = response.data.profileImage
        ? `${import.meta.env.VITE_VUE_API_URL}/api/${
            response.data.profileImage
          }`
        : "http://localhost:5173/src/img/default.jpg"; // 기본 이미지 경로
    } else {
      // 소셜 로그인일 경우 profileImage를 그대로 사용
      profileImage.value =
        response.data.profileImage ||
        "http://localhost:5173/src/img/default.jpg"; // 기본 경로
    }
  } catch (error) {
    console.error("Failed to fetch user profile:", error);
  }
};

const handleProfileImageUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  try {
    const response = await apiClient.post(
      `/user/profile-image/${user.id}`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );

    // 서버에서 반환된 파일 이름 사용
    console.log(response.data);

    const fileName = response.data.profileImage;
    if (!fileName) throw new Error("File name is undefined or empty");

    profileImage.value = `${import.meta.env.VITE_VUE_API_URL}/api/${fileName}`;
    console.log("Updated profile image URL:", profileImage.value);
  } catch (error) {
    console.error("Failed to upload profile image:", error);
  }
};

// Fetch reviews on mount
onMounted(() => {
  fetchUserReviews();
  fetchBookmarkedCount();
  fetchBoardCount();
  fetchUserProfile();
});

// 유저 정보 수정 상태 관리
const isEditing = ref(false);
const updatedNickname = ref("");
const currentPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");

// 사용자 정보 수정 API 호출
const updateUserInfo = async () => {
  if (!user) return;
  if (newPassword.value !== confirmPassword.value) {
    alert("새 비밀번호가 일치하지 않습니다.");
    return;
  }

  try {
    const response = await apiClient.put(`/user/${user.id}/update`, {
      nickname: updatedNickname.value || user.nickname,
      currentPassword: currentPassword.value,
      password: newPassword.value,
      confirmPassword: confirmPassword.value,
    });

    alert(response.data.message);

    // Pinia 상태 업데이트: 닉네임만 변경
    userStore.setUser({ ...user, nickname: updatedNickname.value });
    await fetchUser();

    closeModal(); // 모달 닫기
  } catch (error) {
    console.error("Failed to update user information:", error);
    alert("사용자 정보를 업데이트하는 데 실패했습니다.");
  }
};

const fetchUser = async () => {
  if (!user.value) return; // 사용자 정보가 없으면 요청하지 않음
  try {
    const response = await apiClient.get(`/user/${user.value.id}`);
    userStore.setUser(response.data); // 서버에서 가져온 사용자 정보를 Pinia 상태에 저장
    console.log("Fetched user data:", response.data);
  } catch (error) {
    console.error("Failed to fetch user information:", error);
    alert("사용자 정보를 가져오는 데 실패했습니다.");
  }
};

const goToAIPage = () => {
  router.push({ name: "aichat" });
};

// 모달 상태
const isModalOpen = ref(false);

// 모달 열기
const openModal = () => {
  isModalOpen.value = true;
};

// 모달 닫기
const closeModal = () => {
  isModalOpen.value = false;
};
</script>

<template>
  <div class="mypage-container">
    <!-- User Info -->
    <!-- 내 정보 -->
    <section class="user-info-section">
      <div class="user-info-container">
        <!-- 프로필 이미지 -->
        <div class="profile-image-container">
          <img :src="profileImage" alt="Profile Image" class="profile-image" />
          <div v-if="isRegular">
            <input
              type="file"
              id="profile-upload"
              class="profile-upload"
              @change="handleProfileImageUpload"
            />
            <label for="profile-upload" class="upload-button"
              >프로필 수정</label
            >
          </div>
        </div>

        <!-- 내 정보 -->
        <div class="user-details">
          <h1>내 정보</h1>
          <div>
            <p><strong>닉네임:</strong> {{ getUser.nickname }}</p>
            <p v-if="isRegular"><strong>이메일:</strong> {{ user.email }}</p>
          </div>
          <!-- 정보 수정 버튼 -->
          <div class="edit-section">
            <button @click="openModal" class="edit-button">정보 수정</button>
            <button @click="goToAIPage" class="edit-button">AI Page</button>
          </div>
        </div>

        <!-- 내 활동 통계 -->
        <div class="user-stats">
          <div class="stats">
            <p>리뷰 개수: {{ reviewStats.reviewCount }}</p>
            <p>리뷰 평균 별점: {{ reviewStats.averageRating }}</p>
            <p>찜한 아파트 개수: {{ bookmarkedCount }}</p>
            <p>문의 내역 개수: {{ inquiryCount }}</p>
          </div>
        </div>
      </div>
    </section>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>정보 수정</h2>
        <label>
          <strong>새 닉네임:</strong>
          <input
            type="text"
            v-model="updatedNickname"
            placeholder="새 닉네임"
          />
        </label>
        <label>
          <strong>현재 비밀번호:</strong>
          <input
            type="password"
            v-model="currentPassword"
            placeholder="현재 비밀번호"
          />
        </label>
        <label>
          <strong>새 비밀번호:</strong>
          <input
            type="password"
            v-model="newPassword"
            placeholder="새 비밀번호"
          />
        </label>
        <label>
          <strong>새 비밀번호 확인:</strong>
          <input
            type="password"
            v-model="confirmPassword"
            placeholder="새 비밀번호 확인"
          />
        </label>
        <div class="button-group">
          <button @click="updateUserInfo" class="save-button">저장</button>
          <button @click="closeModal" class="cancel-button">취소</button>
        </div>
      </div>
    </div>

    <!-- User Reviews -->
    <section class="user-reviews-section">
      <div class="user-review-header">
        <h1>내가 작성한 리뷰</h1>
      </div>
      <div class="review-body">
        <!-- 리뷰 캐러셀 -->
        <div v-if="visibleReviews.length" class="review-carousel">
          <button
            v-if="userReviews.length >= 3"
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
              <img :src="`${baseUrl}${review.imageUrl}`" alt="리뷰 이미지" />
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
                <span class="user-nickname">{{ review.houseInfos.aptNm }}</span>
                <!-- <span class="user-nickname">{{ review.user.nickname }}</span> -->
              </div>

              <p class="review-time">
                {{ new Date(review.time).toLocaleDateString() }}
              </p>
            </div>
          </div>

          <button
            v-if="userReviews.length >= 3"
            @click="next"
            class="carousel-button right"
          >
            ▶
          </button>
        </div>
        <p v-else class="no-reviews">등록된 리뷰가 없습니다.</p>
      </div>
    </section>
    <section class="user-interest-section">
      <UserInterest />
    </section>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6); /* 배경 투명도 조정 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #ffffff; /* 모달 배경 흰색 */
  padding: 30px;
  border-radius: 15px;
  width: 400px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 그림자 추가 */
  text-align: center;
  position: relative;
}

.modal-content h2 {
  margin-bottom: 20px;
  font-size: 1.8rem;
  font-weight: bold;
  color: #333;
  text-align: center;
}

.modal-content label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1rem;
  margin: 10px 0;
  color: #555;
}

.modal-content input {
  flex: 1;
  margin-left: 10px;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.edit-section {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.save-button,
.cancel-button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.save-button {
  background: #4caf50;
  color: white;
}

.save-button:hover {
  background: #45a049;
}

.cancel-button {
  background: #f44336;
  color: white;
}

.cancel-button:hover {
  background: #d32f2f;
}

/* 전체 컨테이너 */
.mypage-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  height: 100%;
  padding: 20px;
  margin: 0px;
}

/* User Info */
.user-info-section {
  background: white;
  padding: 60px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.user-info-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
  width: 100%;
}

.user-info-section h1 {
  font-size: 2rem;
  font-weight: 800;
  color: var(--eerie-black);
  margin-bottom: 1rem;
}

.user-info-section p {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 10px;
  text-align: left;
}

.user-info-section p strong {
  color: #191716;
}

.user-info {
  display: flex;
  flex-direction: row;
}

.user-details {
  flex: 7;
  padding: 50px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: left;
  position: relative;
}

.user-details h1 {
  position: relative;
  top: 0;
}

.user-stats {
  padding: 40px;
  margin: 10px;
  margin-right: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: left;
  border: 1px solid #ddd;
  border-radius: 20px;
  background-color: white;
}

.user-interest-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ddd;
  min-height: fit-content;
  overflow-y: auto;
  margin-bottom: 20px;
}

/* User Reviews */
.user-reviews-section {
  background: white;
  padding: 60px;
  border-radius: 8px;
  border: 1px solid #ddd;
  min-height: min-content;
  max-height: 1000px;
  /* 최대 높이 설정 */
  overflow-y: auto;
  /* 스크롤 활성화 */
}

.user-review-header {
  width: 100%;
  padding-left: 25px;
}

.user-reviews-section h1 {
  font-size: 1.9rem;
  font-weight: 700;
  color: var(--eerie-black);
  margin-bottom: 1rem;
  text-align: left;
}

/* 캐러셀 */
.review-carousel {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  gap: 1rem;
  padding: 1rem;
  width: 100%;
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
  left: 0px;
}

.carousel-button.right {
  position: absolute;
  right: 0px;
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
  height: 150px; /* 고정 높이 설정 */
  padding: 5px;
  overflow-y: auto; /* 세로 스크롤 활성화 */
  overflow-x: hidden; /* 가로 스크롤 방지 */
  white-space: normal; /* 텍스트 줄바꿈 허용 */
  word-wrap: break-word; /* 긴 단어 줄바꿈 */
  overflow-wrap: break-word; /* 최신 브라우저 지원 */
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

.review-image img {
  width: 100%;
  height: 100%;
  text-align: center;
  border-radius: 8px;
  /* 둥근 모서리 */
  object-fit: cover;
  /* 비율 유지하며 잘리도록 */
}

.profile-image-container {
  display: flex;
  flex: 3;
  align-items: center;
  flex-direction: column;
  gap: 20px;
}

.profile-image {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ddd;
}

.profile-upload {
  display: none;
}

.upload-button {
  /* background-color: #191716d7; */
  color: black;
  padding: 8px 20px;
  border: none;
  border-radius: 10px;
  border: 1px solid #191716d7;
  cursor: pointer;
  font-size: 0.8rem;
}
.upload-button:hover {
  background-color: #19171606;
}

.edit-button {
  background-color: white;
  color: black;
  padding: 8px 20px;
  border: none;
  border-radius: 10px;
  border: 1px solid #191716d7;
  cursor: pointer;
  font-size: 0.8rem;
  margin-top: 30px;
}
.edit-button:hover {
  background-color: #19171606;
}
</style>
