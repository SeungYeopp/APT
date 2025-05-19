<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { detailArticle, deleteArticle } from "@/api/board";
import { useUserStore } from "@/stores/user";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 현재 로그인한 사용자
const loggedInUser = computed(() => userStore.getUser);

// const articleno = ref(route.params.articleno);
const { articleno } = route.params;

const article = ref({});

onMounted(() => {
  getArticle();
});

const getArticle = () => {
  // const { articleno } = route.params;
  console.log(articleno + "번글 얻으러 가자!!!");
  detailArticle(
    articleno,
    ({ data }) => {
      article.value = data;
    },
    (error) => {
      console.log(error);
    }
  );
};

// 게시글 수정 및 삭제 허용 여부
const canModifyOrDelete = computed(() => {
  return loggedInUser.value?.id === article.value.userId;
});

function moveList() {
  router.push({ name: "article-list" });
}

function moveModify() {
  router.push({ name: "article-modify", params: { articleno } });
}

function onDeleteArticle() {
  // const { articleno } = route.params;
  console.log(articleno + "번글 삭제하러 가자!!!");
  deleteArticle(
    articleno,
    (response) => {
      if (response.status == 200) moveList();
    },
    (error) => {
      console.log(error);
    }
  );
}
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <h2 class="my-3 text-center">글보기</h2>
      <div class="card col-lg-10 text-start">
        <div class="row mb-4">
          <!-- 글 작성자 정보 및 시간 -->
          <div class="col-md-8">
            <div class="d-flex align-items-center">
              <img
                class="avatar me-3"
                src="https://raw.githubusercontent.com/twbs/icons/main/icons/person-fill.svg"
                alt="User Avatar"
              />
              <div>
                <p class="text-name">{{ article.userName }}</p>
                <p class="text-muted">{{ article.registerTime }} | 조회 : {{ article.hit }}</p>
              </div>
            </div>
          </div>
          <!-- 댓글 수 표시 -->
          <div class="col-md-4 text-end align-self-center text-muted">
            댓글 : 17
          </div>
        </div>

        <!-- 게시글 제목과 내용 -->
        <div class="mb-3">
          <h4 class="text-dark">{{ article.articleNo }}. {{ article.subject }}</h4>
          <hr>
          <p class="text-secondary">{{ article.content }}</p>
        </div>

        <div class="d-flex justify-content-between mt-4">
          <button type="button" class="btn btn-outline-primary" @click="moveList">글목록</button>
          <!-- 수정/삭제 버튼은 작성자만 노출 -->
          <div v-if="canModifyOrDelete">
            <button type="button" class="btn btn-outline-success ms-2" @click="moveModify">글수정</button>
            <button type="button" class="btn btn-outline-danger ms-2" @click="onDeleteArticle">글삭제</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/*상세 글보기*/
.card {
  padding: 30px;
  margin: auto;
}
.avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background-color: #f0f0f0;
    padding: 5px;
}

h2 {
    font-weight: bold;
}

h4 {
    font-size: 1.25rem;
}

p {
  margin: 0;
}

.text-secondary {
    color: #6c757d;
}
.text-name {
  font-weight: bold;
}
.text-muted {
    color: #868e96;
}

.btn-outline-primary,
.btn-outline-success,
.btn-outline-danger {
    padding: 10px 20px;
    border-radius: 5px;
}

.btn-outline-primary:hover {
    background-color: #007bff;
    color: white;
}

.btn-outline-success:hover {
    background-color: #28a745;
    color: white;
}

.btn-outline-danger:hover {
    background-color: #dc3545;
    color: white;
}

</style>
