<script setup>
import { ref, onMounted } from "vue";
import axios from "@/util/axios-common.js";

const props = defineProps({
  aptSeq: {
    type: String,
    required: true,
  },
  aptName: {
    type: String,
    required: true,
  },
  userNickname: {
    type: String,
    required: true,
  },
  userId: {
    type: String,
    required: true,
  },
  editingReview: Object,
  isEditing: Boolean,
});

const emit = defineEmits(["close", "review-saved"]);

const reviewContent = ref("");
const rating = ref(0.0); // 별점 값
const selectedImage = ref(null); // 업로드할 이미지 파일
const imagePreview = ref(null); // 이미지 미리보기 URL

const handleFileChange = (event) => {
  selectedImage.value = event.target.files[0];
  if (selectedImage.value) {
    const reader = new FileReader();
    reader.onload = (e) => {
      imagePreview.value = e.target.result; // 미리보기 URL 설정
    };
    reader.readAsDataURL(selectedImage.value);
  }
};

const saveReview = async () => {
  try {
    const formData = new FormData();
    formData.append("userId", props.userId); // FormData에 userId 추가
    formData.append("isAdmin", false); // 관리자 여부
    formData.append("content", reviewContent.value); // 리뷰 내용
    formData.append("rating", parseFloat(rating.value)); // 평점

    if (selectedImage.value) {
      formData.append("file", selectedImage.value); // 이미지 파일 추가
    }

    if (props.isEditing) {
      // 수정 로직
      await axios.put(
        `/review/update/${props.editingReview.reviewId}`,
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        }
      );
      alert("리뷰가 수정되었습니다.");
    } else {
      // 저장 로직 (새로운 리뷰 추가)
      formData.append("houseId", props.aptSeq); // 집 ID 추가
      await axios.post(
        `/review/add`,
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        }
      );
      alert("리뷰가 저장되었습니다.");
    }

    // 부모 컴포넌트에 이벤트 전달
    emit("review-saved");
  } catch (error) {
    console.error("리뷰 저장 중 오류 발생:", error);
    alert("리뷰 저장 실패!");
  }
};

onMounted(() => {
  if (props.isEditing && props.editingReview) {
    reviewContent.value = props.editingReview.content;
    rating.value = props.editingReview.rating;

    // 수정 모드일 때 이미지 URL을 미리보기로 설정
    if (props.editingReview.imageUrl) {
      imagePreview.value = props.editingReview.imageUrl;
    }
  }
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
  <div class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header"><h3><span>{{ aptName }}</span> 리뷰 작성</h3></div>
      <!-- <div class="modal-info">
        <span>작성자: {{ userNickname }}</span>
      </div> -->
      <!-- 별점 -->
      <div class="rating-container">
        <select v-model.number="rating">
          <option v-for="value in 11" :key="value" :value="((value - 1) * 0.5)">
            {{ ((value - 1) * 0.5).toFixed(1) }}
          </option>
        </select>
        <div class="rating">
          <span v-for="star in 5" :key="star" class="star">
            <img
              :src="getStarImage(star, rating)"
              alt="star"
              class="star-icon"
            />
          </span>
        </div>
      </div>

      <!-- 리뷰 내용 -->
      <textarea v-model="reviewContent" placeholder="리뷰를 작성하세요" rows="5"></textarea>

      <div class="modal-buttons">
        <div class="file-upload-wrapper">
          <label for="file" class="upload-button">이미지 업로드</label>
          <input id="file" type="file" @change="handleFileChange" />
        </div>
        <div class="modal-actions">
          <button @click="saveReview">저장</button>
          <button @click="$emit('close')">취소</button>
        </div>
      </div>
      

      <!-- 이미지 미리 보기 -->
      <div v-if="imagePreview" class="image-preview">
        <img :src="imagePreview" alt="이미지 미리보기" style="width: 100px; height: 100px;"/>
      </div>
    </div>
  </div>
</template>




<style scoped>
.image-preview {
  margin-bottom: 10px;
  text-align: center;
  width: 100%;
  max-height: 150px;
}

.image-preview img {
  width: 100%;
  height: 100%;
  text-align: center;
  border-radius: 8px;
  /* 둥근 모서리 */
  object-fit: cover;
  /* 비율 유지하며 잘리도록 */
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}

.modal-header{
  display: flex;
  justify-content: center;
  border-bottom: 1px solid #ddd;
  margin-bottom: 15px;
}

.modal-info {
  text-align: right;
}

textarea {
  width: 100%;
  padding: 10px;
  margin: 10px 0;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.rating-container {
  display: flex;
  flex-direction: row;
  gap: 20px;
  margin: 10px;
}

.star-icon {
  width: 20px;
  height: 20px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 숨겨진 파일 입력 */
input[type="file"] {
  display: none; /* 숨기기 */
}

/* 커스텀 버튼 스타일 */
.upload-button {
  display: inline-block;
  padding: 7px 10px;
  font-size: 0.8rem;
  color: white;
  background-color: var(--eerie-grey);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  transition: background-color 0.2s ease;
}

.upload-button:hover {
  background-color: var(--eerie-black); /* 호버 시 색상 */
}

.modal-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding: 10px 0px;
}

button {
  padding: 7px 10px;
  font-size: 0.8rem;
  color: white;
  background-color: var(--eerie-grey);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  text-align: center;
  transition: background-color 0.2s ease;
}

button:hover {
  background-color: var(--eerie-black);
}
</style>

