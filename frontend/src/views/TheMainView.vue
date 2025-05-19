<script setup>
import { ref, computed } from "vue";
import debounce from "lodash.debounce";
import { useRouter } from "vue-router";

const router = useRouter();

// 검색 상태 관리
const searchInput = ref(""); // 검색 입력 값
const dongCdInput = ref(""); // 동 코드 저장
const searchResults = ref([]); // 검색 결과 상태
const showResults = ref(false); // 결과 표시 상태
const searchType = ref("apartment"); // 검색 타입 ('apartment' or 'dong')
const clickedResult = ref(true); // 클릭 상태를 추적

let currentController = null; // AbortController 저장

// 검색 처리 함수
const handleSearch = async (query) => {
  if (currentController) {
    currentController.abort();
  }

  if (query !== "") {
    try {
      currentController = new AbortController();
      const url =
        searchType.value === "dong"
          ? `/apt/searchAjax?keyword=${encodeURIComponent(query)}`
          : `/apt/searchApartmentAjax?keyword=${encodeURIComponent(query)}`;

      const response = await fetch(url, { signal: currentController.signal });

      if (!response.ok) {
        throw new Error(`Network response was not ok, status: ${response.status}`);
      }

      const data = await response.json();

      searchResults.value =
        searchType.value === "dong"
          ? data
              .map((item) => {
                const [code, name] = item.replace(/"/g, "").split(", ");
                return { code, name };
              })
              .filter((item) => item.code && item.name)
          : data.map((aptName) => ({
              code: null, // 아파트 검색에는 코드가 필요 없을 수 있음
              name: aptName, // 배열에 담긴 아파트 이름 그대로 사용
            }));

      showResults.value = true;
    } catch (error) {
      if (error.name === "AbortError") {
        console.log("Previous request aborted");
      } else {
        console.error("Error fetching data:", error.message);
      }
      searchResults.value = [];
      showResults.value = true;
    }
  } else {
    searchResults.value = [];
    showResults.value = false;
  }
};




// 디바운스된 검색 함수
const debouncedSearch = debounce(async (query) => {
  await handleSearch(query);
}, 300);

const handleInput = (event) => {
  searchInput.value = event.target.value;
  debouncedSearch(searchInput.value.trim());
};

const handleResultClick = (code, name) => {
  searchInput.value = name;
  dongCdInput.value = code;
  searchResults.value = [];
  showResults.value = false;
  clickedResult.value = false;
};

const shouldShowResults = computed(() => {
  return showResults.value && clickedResult.value && searchInput.value.trim() !== "";
});

const handleSearchSubmit = () => {
  if (searchType.value === "apartment") {
    if (searchInput.value.trim() !== "") {
      router.push({ name: "home-list", params: { keyword: searchInput.value.trim() } });
    } else {
      alert("검색어를 입력하세요.");
    }
  } else if (searchType.value === "dong") {
    if (dongCdInput.value.trim() !== "") {
      router.push({ name: "home-list", params: { keyword: dongCdInput.value.trim() } });
    } else {
      alert("검색어를 입력하고 선택하세요!");
    }
  }
};
</script>

<template>
  <main class="main-view">
    <!-- <div class="overlay"></div> -->
    <div class="main-container text-center">
      <aside>
        <img src="@/img/Saly-22.png" style="width: 550px;"/>
      </aside>
      <!-- Main Section: Text and Search -->
      <section>
        <div>
          <h1 class="fw-bold mb-3">
            <!-- <span class="text-white">당신의 집을 찾아보세요.</span> -->
            당신의 집을 찾아보세요.
          </h1>
          <p class="mb-4 text-content">
            APT는 전국의 아파트 목록뿐만 아니라, 최신 거래 내역을 보여줍니다.
          </p>
          <div class="search-div">
            <form @submit.prevent="handleSearchSubmit" class="search-form">
              <div class="input-group">
                <!-- 검색 타입 선택 -->
                <select class="form-select" v-model="searchType" required>
                  <option value="" disabled selected>&nbsp;&nbsp;검색 유형</option> <!-- 기본값: 검색 유형 -->
                  <option value="apartment">&nbsp;&nbsp;아파트</option>
                  <option value="dong">&nbsp;&nbsp;동</option>
                </select>

                <!-- 검색 입력 -->
                <input type="text" class="form-control" :placeholder="searchType ? '검색어를 입력하세요' : '검색 유형을 선택하세요'"
                  v-model="searchInput" :disabled="!searchType" @input="handleInput" />

                <!-- 검색 버튼 -->
                <button type="submit" class="btn search-btn">
                  <img src="@/components/icon/search.png" alt="Search Icon" class="search-icon" />
                </button>
              </div>
            </form>

            <!-- 검색 결과 리스트 -->
            <ul v-if="shouldShowResults" class="search-results">
              <li v-for="result in searchResults" :key="result.code" class="autocomplete-result-item"
                @click="handleResultClick(result.code, result.name)">
                {{ result.name }}
              </li>
            </ul>
          </div>
          <!-- <router-link :to="{ name: 'home' }" class="btn btn-primary btn-lg mb-3" style="margin-top: 20px;">
            아파트 보기 →
          </router-link> -->
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
/* 메인 뷰 스타일 */
.main-container {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  width: 70%;
}
section {
  display: flex;
  align-items: center;
}

.main-view {
  /* min-height: calc(100vh - 80px);
  position: relative;
  background-color: black;
  background-size: cover;
  background-position: center;
  animation: slideBackground 30s infinite linear;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('@/img/redsapanda.png');
  will-change: background-image; */
  position: relative;
  background-color: white;
  display: flex;
  justify-content: center;
  align-items: center;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.content-container {
  position: relative;
  z-index: 2;
  text-align: center;
  max-width: 600px;
}

h1 {
  font-size: 2rem;
  color: var(--eerie-black);
  margin-bottom: 20px;
}

p {
  font-size: 1.2rem;
  color: #ddd;
  margin-bottom: 30px;
}

/* 검색 폼 */
.search-form {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}
.search-div {
  position: relative; /* 결과 리스트를 검색 폼 안에서 기준 잡기 위해 추가 */
}

.input-group {
  display: flex;
  align-items: center;
  max-width: 480px;
  margin: 0 auto;
}

.form-select {
  border-radius: 15px;
  height: 36px;
  padding: 2px;
  font-size: 14px;
  width: 100px;
  flex: none;
  text-align: left;
}

.form-control {
  flex: 1;
  border-radius: 15px;
  padding: 10px;
  height: 36px;
  font-size: 14px;
}

.search-btn {
  height: 36px;
  background-color: var(--figma-blue);
  border: none;
  border-radius: 15px;
  padding: 0 10px;
  cursor: pointer;
}

.search-btn:hover {
  background-color: var(--figma-blue-hover);
}

.search-icon {
  width: 16px;
  height: 16px;
}

/* 검색 결과 스타일 */
.search-results {
  position: absolute; /* 부모 요소(.search-form)를 기준으로 위치 */
  top: 100%; /* 검색 창 바로 아래에 붙게 조정 */
  left: 0;
  width: 346px; /* 입력 창 너비에 맞춤 */
  background: white;
  border: 1px solid #ddd;
  border-top: none;
  overflow-y: auto;
  max-height: 180px;
  padding: 0;
  list-style: none;
  z-index: 3;
  left: 115px;
}

/* 전체 스크롤바 영역 */
::-webkit-scrollbar {
  width: 10px; /* 세로 스크롤바 너비 */
  height: 10px; /* 가로 스크롤바 높이 */
}

/* 스크롤바 트랙 */
::-webkit-scrollbar-track {
  background: #f7f7f7; /* 트랙 배경색 */
  border-radius: 10px; /* 둥근 모서리 */
}

/* 스크롤바 드래그 가능한 영역 */
::-webkit-scrollbar-thumb {
  background: #cacaca; /* 드래그 영역 색상 */
  border-radius: 10px; /* 둥근 모서리 */
}

/* 스크롤바 드래그 영역에 호버 효과 */
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8; /* 호버 시 색상 */
}


.autocomplete-result-item {
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid #ddd;
}

.autocomplete-result-item:last-child {
  border-bottom: none;
}

.autocomplete-result-item:hover {
  background-color: #f1f1f1;
}

/* 포커스 스타일 제거 */
.form-control,
.form-select {
  outline: none;
  box-shadow: none;
}

.form-control:focus,
.form-select:focus {
  outline: none;
  box-shadow: none;
}

/* "아파트 보기" 버튼 스타일 */
.btn-primary {
  margin-top: 20px;
  background-color: var(--figma-blue);
  border: none;
  color: #fff;
  font-size: 1rem;
  padding: 12px 20px;
  text-decoration: none;
  display: inline-block;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}

.btn-primary:hover {
  background-color: var(--figma-blue-hover);
}

.text-content {
  color: #bdbdbd;
  font-weight: 300;
}

/* 배경 전환 애니메이션 */
@keyframes slideBackground {
  0% {
    background-image: url('@/img/redsapanda.png');
  }
  10% {
    background-image: url('@/img/nurgulman.png');
  }
  20% {
    background-image: url('@/img/raccoon.png');
  }
  30% {
    background-image: url('@/img/1.png');
  }
  40% {
    background-image: url('@/img/2.png');
  }
  50% {
    background-image: url('@/img/3.png');
  }
  60% {
    background-image: url('@/img/4.png');
  }
  70% {
    background-image: url('@/img/5.png');
  }
  80% {
    background-image: url('@/img/6.png');
  }
  90% {
    background-image: url('@/img/7.png');
  }
  100% {
    background-image: url('@/img/redsapanda.png');
  }
}
</style>
