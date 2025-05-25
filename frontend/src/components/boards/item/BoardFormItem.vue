<script setup>
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { registArticle, getModifyArticle, modifyArticle } from "@/api/board";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

const router = useRouter();
const route = useRoute();

const props = defineProps({ type: String });

const isUseId = ref(false);
const isLoading = ref(false); // 로딩 상태 관리

const article = ref({
  articleNo: 0,
  subject: "",
  content: "",
  userId: userStore.getUser.id,
  userName: userStore.getUser?.nickname || "",
  hit: 0,
  registerTime: "",
});

if (props.type === "modify") {
  let { articleno } = route.params;
  console.log(articleno + "번글 얻어와서 수정할거야");
  getModifyArticle(
    articleno,
    ({ data }) => {
      article.value = data;
      isUseId.value = true;
    },
    (error) => {
      console.log(error);
    }
  );
  isUseId.value = true;
}

const subjectErrMsg = ref("");
const contentErrMsg = ref("");
watch(
  () => article.value.subject,
  (value) => {
    let len = value.length;
    if (len == 0 || len > 30) {
      subjectErrMsg.value = "제목을 확인해 주세요!!!";
    } else subjectErrMsg.value = "";
  },
  { immediate: true }
);
watch(
  () => article.value.content,
  (value) => {
    let len = value.length;
    if (len == 0 || len > 500) {
      contentErrMsg.value = "내용을 확인해 주세요!!!";
    } else contentErrMsg.value = "";
  },
  { immediate: true }
);

function onSubmit() {
  // 로딩 중이면 중복 요청 방지
  if (isLoading.value) return;

  if (subjectErrMsg.value) {
    alert(subjectErrMsg.value);
    return;
  }
  if (contentErrMsg.value) {
    alert(contentErrMsg.value);
    return;
  }

  // 로딩 시작
  isLoading.value = true;

  // 이메일 데이터 준비
  const emailData = {
    userName: article.value.userName,
    subject: article.value.subject,
    content: article.value.content,
  };

  // 글 등록/수정 작업
  const articlePromise =
    props.type === "regist" ? writeArticle() : updateArticle();

  articlePromise
    .then(() => {
      // 이메일 전송
      return sendEmail(emailData);
    })
    .catch((error) => {
      console.error("작업 중 오류 발생:", error);
    })
    .finally(() => {
      isLoading.value = false; // 로딩 종료
    });
}

function writeArticle() {
  console.log("글등록하자!!", article.value);
  return new Promise((resolve, reject) => {
    registArticle(
      article.value,
      (response) => {
        let msg = "글등록 처리시 문제 발생했습니다.";
        if (response.status == 201) msg = "글등록이 완료되었습니다.";
        alert(msg);
        moveList();
        resolve();
      },
      (error) => {
        console.log(error);
        reject(error);
      }
    );
  });
}

function updateArticle() {
  console.log(article.value.articleNo + "번글 수정하자!!", article.value);
  return new Promise((resolve, reject) => {
    modifyArticle(
      article.value,
      (response) => {
        let msg = "글수정 처리시 문제 발생했습니다.";
        if (response.status == 200) msg = "글정보 수정이 완료되었습니다.";
        alert(msg);
        moveList();
        resolve();
      },
      (error) => {
        console.log(error);
        reject(error);
      }
    );
  });
}

function sendEmail(emailData) {
  return new Promise((resolve, reject) => {
    fetch(`${import.meta.env.VITE_VUE_API_URL}/board`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(emailData),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.text();
      })
      .then((data) => {
        console.log("Email sent successfully:", data);
        alert("이메일이 성공적으로 전송되었습니다.");
        resolve();
      })
      .catch((error) => {
        console.error("Error sending email", error);
        alert("이메일 전송 중 문제가 발생했습니다.");
        reject(error);
      });
  });
}

function moveList() {
  router.push({ name: "article-list" });
}
</script>

<template>
  <div v-if="isLoading" class="loading-overlay">
    <div class="spinner"></div>
    <p>이메일을 전송 중입니다. 잠시만 기다려주세요...</p>
  </div>
  <form v-else @submit.prevent="onSubmit">
    <div class="mb-3">
      <label for="userid" class="form-label">작성자 이름 : </label>
      <input
        type="text"
        class="form-control"
        :value="article.userName"
        disabled
      />
    </div>
    <div class="mb-3">
      <label for="subject" class="form-label">제목 : </label>
      <input
        type="text"
        class="form-control"
        v-model="article.subject"
        placeholder="제목을 입력하세요."
      />
    </div>
    <div class="mb-3">
      <label for="content" class="form-label">내용 : </label>
      <textarea
        class="form-control"
        v-model="article.content"
        rows="10"
        placeholder="이메일: &#10;&#10;문의 내용:"
      ></textarea>
    </div>
    <div class="col-auto text-center">
      <button
        type="submit"
        class="btn btn-outline-primary mb-3"
        v-if="type === 'regist'"
      >
        글작성
      </button>
      <button type="submit" class="btn btn-outline-success mb-3" v-else>
        글수정
      </button>
      <button
        type="button"
        class="btn btn-outline-danger mb-3 ms-1"
        @click="moveList"
      >
        목록으로이동...
      </button>
    </div>
  </form>
</template>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 1000;
}

.spinner {
  margin-bottom: 15px;
  width: 50px;
  height: 50px;
  border: 5px solid rgba(0, 0, 0, 0.1);
  border-top-color: #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-overlay p {
  font-size: 1.2rem;
  color: #555;
}
</style>
