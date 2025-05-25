<script setup>
import { ref, onMounted, watch, computed, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getAptListByArea, getAptListByKeyword } from "@/api/apt.js";
import { listSido, listGugun, listDong } from "@/api/map.js";
import apiClient from "@/util/axios-common.js";

import VKakaoMap from "@/components/common/VKakaoMap.vue";
import VSelect from "@/components/common/VSelect.vue";
import HomeList from "@/components/home/HomeList.vue";
import HomeDealList from "@/components/home/HomeDealList.vue";

const aptList = ref([]);
const sidoList = ref([]);
const gugunList = ref([{ text: "구군선택", value: "" }]);
const dongList = ref([{ text: "동선택", value: "" }]);

const searchSido = ref("");
const searchGugun = ref("");
const searchDong = ref("");

const showList = ref(false);
const showBackButton = ref(true);
const selectedApt = ref("");

onMounted(async () => {
  const route = useRoute();

  const keyword = route.params.keyword; // URL에서 dongCd 값 추출
  if (keyword == undefined) {
    const storedSido = localStorage.getItem("searchSido");
    const storedGugun = localStorage.getItem("searchGugun");
    const storedDong = localStorage.getItem("searchDong");

    if (storedSido) {
      searchSido.value = storedSido;
    }
    if (storedGugun) {
      searchGugun.value = storedGugun;
    }
    if (storedDong) {
      searchDong.value = storedDong;
    }
    if (!storedDong) {
      // 초기 동 코드로 설정
      await initializeLocation("11", "11680", "1168010100");
      AptListInArea("1168010100");
    } else {
      AptListInArea(searchDong.value);
    }
  } else {
    if (keyword && keyword.length === 10) {
      const sidoCode = keyword.substring(0, 2);
      const gugunCode = keyword.substring(0, 5);
      const dongCode = keyword;

      console.log(sidoCode);
      console.log(gugunCode);
      console.log(dongCode);

      await initializeLocation(sidoCode, gugunCode, dongCode);
      saveKeywordToLocalStorage(keyword); // LocalStorage에 저장
      AptListInArea(keyword); // 해당 동 코드로 아파트 리스트 로드
    } else if (keyword === "aptDetail") {
      try {
        const aptData = decodeBase64(route.params.data);
        showBackButton.value = false;
        clickApt(aptData); // 안전하게 데이터 전달
      } catch (error) {
        console.error("Invalid JSON data in route params:", error);
      }
    } else if (keyword) {
      searchAptList(keyword); // 아파트 검색 호출
    }
  }

  getSidoList();
  fetchTopViewedApartments();
});

async function initializeLocation(sidoCode, gugunCode, dongCode) {
  if (sidoCode) {
    await getSidoList(async () => {
      searchSido.value = sidoCode;
      if (gugunCode) {
        await updateGugunList(sidoCode, async () => {
          searchGugun.value = gugunCode;
          if (dongCode) {
            await updateDongList(gugunCode);
            searchDong.value = dongCode;
          }
        });
      }
    });
  } else {
    await getSidoList();
  }
}

const router = useRouter();
router.beforeEach((to, from, next) => {
  // '/home' 페이지를 벗어날 때 로컬 스토리지 초기화
  if (from.path === "/home") {
    localStorage.removeItem("searchSido");
    localStorage.removeItem("searchGugun");
    localStorage.removeItem("searchDong");
  }
  next();
});

const updateGugunList = async (sidoCode, callback) => {
  await listGugun(
    { sido: sidoCode },
    ({ data }) => {
      gugunList.value = [{ text: "구군선택", value: "" }];
      if (Array.isArray(data)) {
        data.forEach((gugun) => {
          gugunList.value.push({
            text: gugun.gugunName,
            value: gugun.gugunCode,
          });
        });
      } else {
        console.warn("Unexpected Gugun response format:", data);
      }
      if (callback) callback();
    },
    (err) => {
      console.error("Failed to fetch Gugun list:", err);
    }
  );
};

const updateDongList = async (gugunCode) => {
  await listDong(
    { gugun: gugunCode },
    ({ data }) => {
      dongList.value = [{ text: "동선택", value: "" }];
      if (Array.isArray(data)) {
        data.forEach((dong) => {
          dongList.value.push({
            text: dong.dongName,
            value: dong.dongCd,
          });
        });
        console.log("Dong list updated:", dongList.value);
      } else {
        console.warn("Unexpected Dong response format:", data);
      }
    },
    (err) => {
      console.error("Failed to fetch Dong list:", err);
    }
  );
};

function toMysqlDatetime(isoString) {
  const date = new Date(isoString);
  return date.toISOString().slice(0, 19).replace("T", " ");
}

// LocalStorage에 keyword 저장
const saveKeywordToLocalStorage = async (keyword) => {
  const user = JSON.parse(localStorage.getItem("user"));
  const currentTime = new Date().toISOString();

  // 유저가 존재하는 경우 DB에 저장
  if (user) {
    try {
      const payload = {
        dongCd: keyword,
        time: toMysqlDatetime(currentTime),
        bookmarked: false, // 필요 시 설정
        userId: user.id,
      };
      await apiClient.post("/interest/view-neighborhood", payload);
      console.log("DB에 저장되었습니다:", payload);
    } catch (error) {
      console.error("DB 저장 실패:", error.response?.data || error.message);
    }
  } else {
    // 유저가 존재하지 않으면 LocalStorage에 저장
    let recentNeighborhoods =
      JSON.parse(localStorage.getItem("recentNeighborhoods")) || [];
    const existingIndex = recentNeighborhoods.findIndex(
      (item) => item.dongCd === keyword
    );

    if (existingIndex > -1) {
      // 이미 존재하면 가장 최근으로 이동
      recentNeighborhoods.splice(existingIndex, 1);
    }
    recentNeighborhoods.unshift({ dongCd: keyword, time: currentTime });

    // 최근 10개까지만 유지
    if (recentNeighborhoods.length > 10) {
      recentNeighborhoods.pop();
    }
    localStorage.setItem(
      "recentNeighborhoods",
      JSON.stringify(recentNeighborhoods)
    );
    console.log("로컬 스토리지에 저장되었습니다:", recentNeighborhoods);
  }
};

const searchAptList = async (keyword) => {
  getAptListByKeyword(
    keyword,
    ({ data }) => {
      aptList.value = data;
      console.log("Fetched Apartments:", aptList.value);
    },
    (error) => {
      console.error("Error fetching apartments:", error);
    }
  );
};

const AptListInArea = async function (dongCd) {
  getAptListByArea(
    dongCd,
    ({ data }) => {
      aptList.value = data;
      console.log("Fetched Apartments:", aptList.value);
    },
    (error) => {
      console.error("Error fetching apartments:", error);
    }
  );
};

const getSidoList = async (callback) => {
  await listSido(
    ({ data }) => {
      sidoList.value = [{ text: "시도선택", value: "" }];
      if (Array.isArray(data)) {
        data.forEach((sido) => {
          sidoList.value.push({ text: sido.sidoName, value: sido.sidoCode });
        });
      } else {
        console.warn("Unexpected Sido response format:", data);
      }
    },
    (err) => {
      console.error("Failed to fetch Sido list:", err);
    }
  );
};

// 시도 선택 시
// 시도 선택 시
const onChangeSido = (val) => {
  searchSido.value = val; // 선택된 시도 값 저장
  console.log("선택된 시도:", val); // 시도 값 콘솔에 출력

  // 구군과 동 초기화
  searchGugun.value = ""; // 구군 초기화
  searchDong.value = ""; // 동 초기화
  gugunList.value = [{ text: "구군선택", value: "" }]; // 구군 목록 초기화
  dongList.value = [{ text: "동선택", value: "" }]; // 동 목록 초기화

  if (val) {
    // 구군 목록 갱신
    listGugun(
      { sido: val },
      ({ data }) => {
        gugunList.value = [{ text: "구군선택", value: "" }];
        data.forEach((gugun) => {
          gugunList.value.push({
            text: gugun.gugunName,
            value: gugun.gugunCode,
          });
        });
      },
      (err) => {
        console.error("Failed to fetch Gugun list:", err);
      }
    );
  }
};

// 구군 선택 시
const onChangeGugun = (val) => {
  searchGugun.value = val; // 선택된 구군 값 저장
  console.log("선택된 구군:", val); // 구군 값 콘솔에 찍기
  showBackButton.value = true;
  // 동 목록 갱신
  listDong(
    { gugun: val },
    ({ data }) => {
      let options = [];
      options.push({ text: "동선택", value: "" });
      data.forEach((dong) => {
        options.push({ text: dong.dongName, value: dong.dongCd });
      });
      dongList.value = options;
    },
    (err) => {
      console.log(err);
    }
  );
  hideDealList();
  // 선택된 값으로 아파트 리스트 갱신
  fetchAptList(val); // 구군 값으로 아파트 리스트를 갱신
};

// 동 선택 시
const onChangeDong = (val) => {
  searchDong.value = val; // 선택된 동 값 저장
  console.log("선택된 동:", val); // 동 값 콘솔에 찍기
  saveKeywordToLocalStorage(val);

  hideDealList();
  // 아파트 리스트 갱신
  fetchAptList(val); // 동 값으로 아파트 리스트를 갱신
};

// 아파트 리스트를 fetch하는 함수
const fetchAptList = async (val) => {
  // 시도, 구군, 동 값들을 결합하여 해당 지역의 아파트 리스트를 가져오기
  let location = val;

  console.log("검색할 지역:", location); // 최종 검색할 지역 출력

  // 아파트 목록을 API로 가져오기
  getAptListByArea(
    location,
    ({ data }) => {
      aptList.value = data;
      console.log("Fetched Apartments:", aptList.value);
    },
    (error) => {
      console.error("Error fetching apartments:", error);
    }
  );
};

function clickApt(apt) {
  selectedApt.value = apt;
  showList.value = true;
  console.log("Selected Apt:", selectedApt.value);
}

function hideDealList() {
  showList.value = false;
}

const filterCriteria = ref(""); // 필터 기준 저장
const filterYearCriteria = ref(""); // 건축 연도 필터 기준 저장
const filterPriceCriteria = ref(""); // 가격 필터 기준 저장
const filteredAptList = computed(() => {
  if (!Array.isArray(aptList.value)) return [];

  return aptList.value.filter((apt) => {
    const area = parseFloat(apt.exclusiveArea);
    const buildYear = parseInt(apt.buildYear);
    const price = parseInt(apt.recentDealAmount.replace(/,/g, ""));

    if (filterCriteria.value === "small" && area > 99) return false;
    if (filterCriteria.value === "medium" && (area <= 99 || area > 165))
      return false;
    if (filterCriteria.value === "large" && area <= 165) return false;

    if (filterYearCriteria.value === "recent" && buildYear < 2010) return false;
    if (filterYearCriteria.value === "old" && buildYear >= 2010) return false;

    if (filterPriceCriteria.value === "below5" && price > 50000) return false;
    if (
      filterPriceCriteria.value === "5to10" &&
      (price <= 50000 || price > 100000)
    )
      return false;
    if (
      filterPriceCriteria.value === "10to20" &&
      (price <= 100000 || price > 200000)
    )
      return false;
    if (
      filterPriceCriteria.value === "20to50" &&
      (price <= 200000 || price > 500000)
    )
      return false;
    if (filterPriceCriteria.value === "above50" && price <= 500000)
      return false;

    hideDealList();
    return true;
  });
});

const topViewedApartments = ref([]); // 랭킹 데이터
const currentApartment = ref(null); // 현재 표시 중인 아파트
const currentRank = ref(1); // 현재 표시 중인 순위
let intervalId = null; // setInterval ID

// API 호출
const fetchTopViewedApartments = async () => {
  try {
    const { data } = await apiClient.get("/interest/top-viewed-apartments");
    topViewedApartments.value = data;
    if (data.length > 0) {
      startRankingRotation();
    }
  } catch (error) {
    console.error("Failed to fetch top viewed apartments:", error);
  }
};

const startRankingRotation = () => {
  let index = 0;

  // 초기 값 설정
  currentApartment.value = topViewedApartments.value[index];
  currentRank.value = index + 1;

  // 3초마다 다음 아파트로 전환
  intervalId = setInterval(() => {
    index = (index + 1) % topViewedApartments.value.length; // 순환
    currentApartment.value = topViewedApartments.value[index];
    currentRank.value = index + 1;
  }, 3000);
};

onBeforeUnmount(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});

const decodeBase64 = (encodedData) => {
  try {
    return JSON.parse(
      decodeURIComponent(
        atob(encodedData)
          .split("")
          .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
          .join("")
      )
    );
  } catch (error) {
    console.error("Base64 decoding failed:", error);
    return null;
  }
};

const navigateToDealList = (apartment) => {
  if (!apartment || !apartment.houseInfoId) {
    console.error("Invalid apartment data:", apartment);
    return;
  }

  selectedApt.value = {
    aptSeq: apartment.houseInfoId,
  };

  console.log("Selected Apartment:", selectedApt.value); // 디버깅용 로그
  showList.value = true;
};
</script>

<template>
  <div class="home-container">
    <div class="search-container">
      <!-- 필터와 랭킹 컨테이너 -->
      <div class="filter-ranking-container">
        <!-- 필터 섹션 -->
        <div class="filters">
          <label for="areaSelect" class="label">동네 선택:</label>
          <VSelect
            :selectOption="sidoList"
            v-model="searchSido"
            @onKeySelect="onChangeSido"
          />
          <VSelect
            :selectOption="gugunList"
            v-model="searchGugun"
            @onKeySelect="onChangeGugun"
          />
          <VSelect
            :selectOption="dongList"
            v-model="searchDong"
            @onKeySelect="onChangeDong"
          />

          <label for="filterSelect" class="label">필터 선택:</label>
          <select
            id="filterSelect"
            class="filter-select"
            v-model="filterCriteria"
          >
            <option value="">평수</option>
            <option value="small">30평 이하</option>
            <option value="medium">30평 이상 50평 이하</option>
            <option value="large">50평 이상</option>
          </select>
          <select
            id="filterYearSelect"
            class="filter-select"
            v-model="filterYearCriteria"
          >
            <option value="">건축 연도</option>
            <option value="recent">2010년 이후</option>
            <option value="old">2010년 이전</option>
          </select>
          <select
            id="filterPriceSelect"
            class="filter-select"
            v-model="filterPriceCriteria"
          >
            <option value="">가격</option>
            <option value="below5">5억 이하</option>
            <option value="5to10">5억 ~ 10억</option>
            <option value="10to20">10억 ~ 20억</option>
            <option value="20to50">20억 ~ 50억</option>
            <option value="above50">50억 이상</option>
          </select>
        </div>

        <!-- 인기 단지 랭킹 -->
        <div
          class="ranking-container"
          @click="navigateToDealList(currentApartment)"
        >
          <span class="ranking-badge">인기 단지</span>
          <span class="ranking-text">
            {{ currentRank }} -
            {{ currentApartment?.apartmentName || "데이터 없음" }}
          </span>
          <span class="ranking-count">
            {{ currentApartment?.viewCount || 0 }}명
          </span>
        </div>
      </div>
    </div>

    <!-- 본문 내용 -->
    <div class="home-contents">
      <aside>
        <HomeList
          v-if="!showList"
          :apartments="filteredAptList"
          @key-select="clickApt"
        />
        <div v-if="showList" class="map-list">
          <HomeDealList
            :apt="selectedApt"
            :showBackButton="showBackButton"
            :key="selectedApt.aptSeq"
            @hide-list="hideDealList"
          />
        </div>
      </aside>
      <section>
        <VKakaoMap :apartments="filteredAptList" :selectApt="selectedApt" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - 70px);
}

.home-contents {
  display: flex;
  flex-direction: row;
  flex-grow: 1;
  overflow: hidden;
}

aside {
  position: relative;
  /* 자식 요소의 position:absolute를 기준으로 위치 잡기 위해 필요 */
  flex: 2;
  min-width: fit-content;
  max-height: 100vh;
  background-color: #fefefe;
  border-right: 1px solid #ddd;
  overflow: auto;
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE, Edge */
}

aside::-webkit-scrollbar {
  display: none;
  /* Chrome, Safari */
}

section {
  flex: 8;
  position: relative;
  border-top: 1px solid #ddd;
}

/* 검색 컨테이너 */
.search-container {
  /* position: absolute; 
  top: 10px; 
  right: 10px; 
  z-index: 1000; 
  background-color: rgba(255, 255, 255, 0.9);  
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); */
  padding: 12px;
  border-top: 1px solid #ddd;
  display: flex;
  flex-direction: column;
  height: 70px;
  background-color: white;
}

.select-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.select {
  flex: 1;
  /* 동일 비율로 크기 조정 */
  min-width: 100px;
  max-width: 150px;
}

.label {
  font-size: 1.1rem;
  font-weight: bold;
  /* margin: 0px 8px; */
  color: #333;
}

.filter-select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-color: #ffffff;
  border: 1px solid #ddd;
  text-align: center;
  flex: 1;
  padding: 4px;
  font-size: 16px;
  width: 100%;
  cursor: pointer;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath fill='gray' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
}

.map-list {
  position: fixed;
  top: 140px;
  /* 헤더와 검색창 높이만큼 아래로 이동 */
  left: 0;
  width: 40%;
  /* 화면의 40% 차지 */
  min-width: fit-content;
  height: calc(100vh - 140px);
  /* 헤더와 검색창을 제외한 높이 */
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: white;
  z-index: 1000;
  /* 최상위 레이어 */
  overflow: auto;
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE, Edge */
  border-top: 1px solid #ddd;
  border-right: 1px solid #ddd;
}

/* 스크롤바 숨기기 (웹킷 기반 브라우저) */
.map-list::-webkit-scrollbar {
  display: none;
}

.radius_border {
  border: 1px solid #919191;
  border-radius: 5px;
  padding: 0.5rem;
  background-color: white;
}

.filter-ranking-container {
  display: flex;
  justify-content: space-between;
  /* 양쪽 정렬 */
  align-items: center;
  /* 필터와 랭킹 박스를 수직 정렬 */
  gap: 20px;
  /* 필터와 랭킹 간 간격 */
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  /* 필터 요소 간 간격 */
  flex: 3;
  /* 필터가 더 많은 공간 차지 */
}

.ranking-container {
  flex: 1;
  /* 랭킹 박스의 크기를 필터 대비 적게 설정 */
  display: flex;
  align-items: center;
  /* 텍스트를 수직 중앙 정렬 */
  justify-content: center;
  /* 텍스트를 수평 중앙 정렬 */
  background-color: #f5faff;
  /* 연한 파란색 배경 */
  border: 1px solid #007bff;
  /* 파란색 테두리 */
  border-radius: 20px;
  /* 둥근 모서리 */
  padding: 5px 10px;
  /* 여백 조정 */
  text-align: center;
  /* 텍스트 중앙 정렬 */
  font-size: 1rem;
  /* 글씨 크기 */
  font-weight: bold;
  /* 글씨 두께 */
  color: #333;
  /* 텍스트 색상 */
  gap: 10px;
  /* 내부 요소 간 간격 */
}

.ranking-badge {
  background-color: #007bff;
  /* 배경색 */
  color: white;
  /* 텍스트 색상 */
  border-radius: 15px;
  /* 둥근 모서리 */
  padding: 5px 10px;
  /* 내부 여백 */
  font-size: 0.9rem;
  /* 배지 글씨 크기 */
  font-weight: bold;
  /* 글씨 두께 */
}

.ranking-text {
  display: flex;
  align-items: center;
  /* 텍스트를 수직 중앙 정렬 */
  font-size: 1rem;
  /* 글씨 크기 */
  font-weight: bold;
  /* 글씨 두께 */
  color: #333;
  /* 텍스트 색상 */
}

.ranking-count {
  margin-left: auto;
  /* 명 수를 오른쪽으로 배치 */
  font-size: 1rem;
  /* 글씨 크기 */
  font-weight: 500;
  /* 글씨 두께 */
  color: gray;
}
</style>
