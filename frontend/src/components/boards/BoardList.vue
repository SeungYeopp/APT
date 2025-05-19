<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { listArticle } from "@/api/board.js";

import VSelect from "@/components/common/VSelect.vue";
import BoardListItem from "@/components/boards/item/BoardListItem.vue";
import VPageNavigation from "@/components/common/VPageNavigation.vue";
import { useUserStore } from "@/stores/user"; // UserStore 불러오기

const router = useRouter();
const userStore = useUserStore(); // UserStore 사용

const selectOption = ref([
  { text: "검색조건", value: "" },
  { text: "글번호", value: "article_no" },
  { text: "제목", value: "subject" },
  { text: "작성자아이디", value: "user_id" },
]);

const articles = ref([]);
const currentPage = ref(1);
const totalPage = ref(0);
const { VITE_ARTICLE_LIST_SIZE } = import.meta.env;
const param = ref({
  pgno: currentPage.value,
  spp: VITE_ARTICLE_LIST_SIZE,
  key: "",
  word: "",
});

onMounted(() => {
  getArticleList();
});

const changeKey = (val) => {
  console.log("BoarList에서 선택한 조건 : " + val);
  param.value.key = val;
};

const getArticleList = () => {
  console.log("서버에서 글목록 얻어오자!!!", param.value);
  listArticle(
    param.value,
    ({ data }) => {
      articles.value = data.articles;
      currentPage.value = data.currentPage;
      totalPage.value = data.totalPageCount;
    },
    (error) => {
      console.log(error);
    }
  );
};

const onPageChange = (val) => {
  console.log(val + "번 페이지로 이동 준비 끝!!!");
  currentPage.value = val;
  param.value.pgno = val;
  getArticleList();
};

const moveWrite = () => {
  router.push({ name: "article-write" });
};

function onSubmit() {
  // 로그인 상태 확인
  if (!userStore.getUser) {
    alert("로그인이 필요합니다."); // 로그인 필요 메시지
    router.push({ name: "login" }); // 로그인 페이지로 이동
    return;
  }

  // 유효성 검사
  if (subjectErrMsg.value) {
    alert(subjectErrMsg.value);
  } else if (contentErrMsg.value) {
    alert(contentErrMsg.value);
  } else {
    props.type === "regist" ? writeArticle() : updateArticle();
  }
}
</script>

<template>
  <div class="qna-container">
      <div class="col-lg-10">
        <div class="row align-self-center mb-2">
          <div class="col-md-5 text-start">
            <form class="d-flex">
              <VSelect :selectOption="selectOption" @onKeySelect="changeKey" />
              <div class="input-group input-group-sm ms-1">
                <input
                  type="text"
                  class="form-control"
                  v-model="param.word"
                  placeholder="검색어..."
                />
                <button class="btn btn-dark" type="button" @click="getArticleList">검색</button>
              </div>
            </form>
          </div>
          <div class="col-md offset-5">
            <button type="button" class="btn btn-outline-primary btn-sm" @click="moveWrite">
              글쓰기
            </button>
          </div>
          
        </div>
        <table class="table table-hover">
          <thead>
            <tr class="text-center">
              <th scope="col">글번호</th>
              <th scope="col">제목</th>
              <th scope="col">작성자</th>
              <th scope="col">조회수</th>
              <th scope="col">작성일</th>
            </tr>
          </thead>
          <tbody>
            <BoardListItem
              v-for="article in articles"
              :key="article.articleNo"
              :article="article"
            ></BoardListItem>
          </tbody>
        </table>
      </div>
      <VPageNavigation
        :current-page="currentPage"
        :total-page="totalPage"
        @pageChange="onPageChange"
      ></VPageNavigation>
      
  </div>
</template>

<style scoped>
/* 전체 컨테이너 */
.qna-container {
    width: 85%;
    margin: 40px auto;
    font-family: 'Arial', sans-serif;
    line-height: 1.6;
}
/* 검색 및 글쓰기 컨테이너 */
.col-lg-10 {
    margin: 20px auto;
    padding: 15px;
    background-color: #fff;
    border: 1px solid #eee;
    border-radius: 8px;
}

/* 검색 폼 */
.d-flex {
    align-items: center;
    gap: 10px;
}

.input-group-sm {
    flex-grow: 1;
}

.btn {
    transition: all 0.3s ease;
}

.btn:hover {
    opacity: 0.8;
}

/* 테이블 */
.table {
    margin-top: 20px;
    border-spacing: 0 10px;
}

.table-hover tbody tr:hover {
    background-color: #f0f8ff;
}

.table th {
    font-size: 16px;
    text-align: center;
    color: #333;
    font-weight: bold;
    border-bottom: 2px solid #ddd;
}

.table td {
    font-size: 14px;
    text-align: center;
    padding: 12px;
    border-bottom: 1px solid #eee;
}
</style>
