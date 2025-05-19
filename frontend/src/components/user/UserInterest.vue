<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import apiClient from "@/util/axios-common.js";

const router = useRouter();
const activeTab = ref("recent-apartments");
const isLoading = ref(true);

// 관심 데이터
const recentApartments = ref([]);
const bookmarkedApartments = ref([]);
const recentNeighborhoods = ref([]);
const favoriteNeighborhoods = ref([]);

const dongCd = ref(null);
const apt_Seq = ref(null);

// 아파트 북마크 상태 관리 (기본값은 false로 설정)
const apartmentBookmarked = ref(false);

const toggleApartmentDelete = async (apartment) => {
  const aptSeq = apartment.houseInfos?.aptSeq;

  if (!aptSeq) {
    console.error("유효하지 않은 aptSeq입니다.");
    return;
  }

  try {
    if (user.value) {
      // 로그인 상태라면 DB에서 삭제
      await apiClient.delete(`/interest/delete-apartment`, {
        params: {
          userId: user.value.id,
          houseInfoId: aptSeq,
        },
      });
      console.log("DB에서 아파트가 삭제되었습니다:", aptSeq);
    } else {
      // 로컬스토리지에서 삭제
      const localData =
        JSON.parse(localStorage.getItem("recentApartments")) || [];
      const updatedData = localData.filter((item) => item.houseInfoId !== aptSeq);
      localStorage.setItem("recentApartments", JSON.stringify(updatedData));
      console.log("로컬스토리지에서 아파트가 삭제되었습니다:", aptSeq);
    }

    // 상태 업데이트: recentApartments에서 해당 아파트 제거
    recentApartments.value = recentApartments.value.filter(
      (item) => item.houseInfoId !== aptSeq
    );
  } catch (error) {
    console.error("아파트 삭제 중 오류가 발생했습니다:", error);
  }
};


// 아파트 북마크 상태 불러오기
const fetchApartmentBookmarkStatus = async () => {
  if (!apt_Seq.value || !user.value) return; // 로그인하지 않았거나 aptSeq가 없으면 중단
  try {
    const response = await apiClient.get("/interest/bookmark", {
      params: {
        houseInfoId: apt_Seq.value,
        userId: user.value.id,
      },
    });

    console.log("API 응답:", response.data); // 응답 확인

    // 서버에서 데이터가 존재하면 apartmentBookmarked 값 업데이트
    if (response.data && response.data.bookmarked !== undefined) {
      apartmentBookmarked.value = response.data.bookmarked;
    }
  } catch (error) {
    console.error("아파트 북마크 상태를 불러오는데 실패했습니다:", error);
  }
};

// 아파트 북마크 토글
const toggleApartmentBookmark = async (apartment) => {
  const aptSeq = apartment.houseInfos?.aptSeq;
  const aptBookmarkedState = !apartment.bookmarked; // 현재 상태를 반전

  const interestData = {
    houseInfoId: aptSeq,
    bookmarked: aptBookmarkedState, // 토글된 상태를 명확히 전달
    time: formatDate(new Date()), // MySQL에서 허용하는 형식으로 변환
  };

  try {
    if (user.value) {
      // 유저가 존재하면 DB 업데이트
      await apiClient.post("/interest/view-apartment", {
        userId: user.value.id,
        ...interestData,
      });
      console.log(
        aptBookmarkedState
          ? "아파트 북마크가 추가되었습니다."
          : "아파트 북마크가 취소되었습니다."
      );
    } else {
      // 로컬 스토리지 업데이트
      const localData =
        JSON.parse(localStorage.getItem("recentApartments")) || [];
      const existingIndex = localData.findIndex(
        (item) => item.houseInfoId === aptSeq
      );
      if (existingIndex > -1) {
        localData[existingIndex].bookmarked = aptBookmarkedState;
      } else {
        localData.push(interestData);
      }
      localStorage.setItem("recentApartments", JSON.stringify(localData));
      console.log(
        aptBookmarkedState
          ? "로컬 아파트 북마크 추가"
          : "로컬 아파트 북마크 취소"
      );
    }

    // 상태 업데이트: recentApartments에서 해당 apartment의 상태를 즉시 변경
    const target = recentApartments.value.find(
      (item) => item.houseInfoId === aptSeq
    );
    if (target) {
      target.bookmarked = aptBookmarkedState;
    }
  } catch (error) {
    console.error("아파트 북마크 상태 업데이트 중 오류가 발생했습니다:", error);
  }
};


const bookmarked = ref(false);

// 북마크 상태 불러오기
const fetchBookmarkStatus = async () => {
  if (!dongCd.value || !user.value) {
    return;
  }

  try {
    const response = await apiClient.get("/interest/bookmarkNh", {
      params: {
        dongCd: dongCd.value,
        userId: user.value.id,
      },
    });

    if (response.data && response.data.bookmarked !== undefined) {
      bookmarked.value = response.data.bookmarked;
    }
  } catch (error) {
    console.error("북마크 상태를 불러오는데 실패했습니다:", error);
  }
};

const toggleBookmark = async (neighborhood) => {
  const dongCd = neighborhood.dongCd; // 클릭된 동네의 dongCd 가져오기
  const bookmarkedState = !neighborhood.bookmarked; // 현재 상태를 반전

  const interestData = {
    dongCd,
    bookmarked: bookmarkedState, // 토글된 상태를 명확히 전달
    time: formatDate(new Date()), // MySQL에서 허용하는 형식으로 변환
  };

  try {
    if (user.value) {
      // 유저가 존재하면 DB 업데이트
      await apiClient.post("/interest/view-neighborhood", {
        userId: user.value.id,
        ...interestData,
      });
      console.log(
        bookmarkedState ? "북마크가 추가되었습니다." : "북마크가 취소되었습니다."
      );

      // 상태 업데이트
      const target = recentNeighborhoods.value.find(
        (item) => item.dongCd === dongCd
      );
      if (target) target.bookmarked = bookmarkedState;
    } else {
      // 로컬 스토리지 업데이트
      const localData = JSON.parse(localStorage.getItem("recentNeighborhoods")) || [];
      const existingIndex = localData.findIndex((item) => item.dongCd === dongCd);

      if (existingIndex > -1) {
        localData[existingIndex].bookmarked = bookmarkedState;
      } else {
        localData.push(interestData);
      }

      localStorage.setItem("recentNeighborhoods", JSON.stringify(localData));
      console.log(
        bookmarkedState ? "로컬 북마크 추가" : "로컬 북마크 취소"
      );

      // 상태 업데이트
      const target = recentNeighborhoods.value.find(
        (item) => item.dongCd === dongCd
      );
      if (target) target.bookmarked = bookmarkedState;
    }
  } catch (error) {
    console.error("북마크 상태 업데이트 중 오류가 발생했습니다:", error);
  }
};


// 유저 정보 가져오기
const user = ref(localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : null);

// 로컬 스토리지 데이터 관리
const localApartments = ref(
  JSON.parse(localStorage.getItem("recentApartments")) || []
);
const localNeighborhoods = ref(
  JSON.parse(localStorage.getItem("recentNeighborhoods")) || []
);

// 로컬 스토리지에 저장하는 함수
const saveToLocalStorage = (key, value) => {
  localStorage.setItem(key, JSON.stringify(value));
};

const sortByTime = (data) => {
  return data.sort((a, b) => new Date(b.time) - new Date(a.time));
};

// 데이터 저장 함수 (로그인 상태 확인)
const saveApartment = async (apartment) => {
  const dataToSave = { ...apartment, time: new Date().toISOString() };

  if (user.value) {
    try {
      await apiClient.post("/interest/add-apartment", {
        ...dataToSave,
        userId: user.value.id,
      });
      console.log("DB에 저장:", dataToSave);
    } catch (error) {
      console.error("DB 저장 실패:", error);
    }
  } else {
    localApartments.value.push(dataToSave);
    saveToLocalStorage("recentApartments", localApartments.value);
    console.log("로컬 스토리지에 저장:", localApartments.value);
  }
};

const saveNeighborhood = async (neighborhood) => {
  const dataToSave = { ...neighborhood, time: new Date().toISOString() };

  if (user.value) {
    try {
      await apiClient.post("/interest/add-neighborhood", {
        ...dataToSave,
        userId: user.value.id,
      });
      console.log("DB에 저장:", dataToSave);
    } catch (error) {
      console.error("DB 저장 실패:", error);
    }
  } else {
    localNeighborhoods.value.push(dataToSave);
    saveToLocalStorage("recentApartments", localNeighborhoods.value);
    console.log("로컬 스토리지에 저장:", localNeighborhoods.value);
  }
};

// 데이터 동기화
const syncLocalStorageToDB = async () => {
  const localApartmentsData = JSON.parse(localStorage.getItem("recentApartments")) || [];
  const localNeighborhoodsData = JSON.parse(localStorage.getItem("recentNeighborhoods")) || [];

  if (!user.value) return; // 로그인 상태 확인

  try {
    // 아파트 데이터 동기화
    if (localApartmentsData.length > 0) {
      const apartmentsPayload = localApartmentsData.map((item) => ({
        ...item,
        userId: user.value.id,
      }));
      await apiClient.post("/interest/sync-apartments", apartmentsPayload);
      localStorage.removeItem("recentApartments");
    }

    // 동네 데이터 동기화
    if (localNeighborhoodsData.length > 0) {
      const neighborhoodsPayload = localNeighborhoodsData.map((item) => ({
        ...item,
        userId: user.value.id,
      }));
      await apiClient.post("/interest/sync-neighborhoods", neighborhoodsPayload);
      localStorage.removeItem("recentNeighborhoods");
    }

    console.log("로컬 스토리지 데이터를 DB에 성공적으로 동기화했습니다.");
  } catch (error) {
    console.error("로컬 스토리지 동기화 실패:", error);
  }
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




const fetchApartmentDetails = async (houseInfoId) => {
  try {
    const response = await apiClient.get(`/interest/apartment-details/${houseInfoId}`);
    return response.data;
  } catch (error) {
    console.error(`아파트 정보 가져오기 실패 (houseInfoId: ${houseInfoId}):`, error);
    throw error;
  }
};

const fetchNeighborhoodDetails = async (dongCd) => {
  try {
    const response1 = await apiClient.get(`/interest/neighborhood-details/${dongCd}`);
    console.log(response1.data);
    return response1.data;
  } catch (error) {
    console.error(`동 정보 가져오기 실패 (dongCd: ${dongCd}):`, error);
    throw error;
  }
};


// 데이터 가져오기
const fetchData = async (tab) => {
  try {
    isLoading.value = true;

    if (tab === "recent-apartments") {
      if (user.value) {
        const response = await apiClient.get("/interest/recent-apartments", {
          params: { userId: user.value.id },
        });
        recentApartments.value = sortByTime(response.data || []);
      } else {
        // 로컬스토리지 데이터를 활용
        const localData = JSON.parse(localStorage.getItem("recentApartments")) || [];
        const sortedLocalData = sortByTime(localData); // 시간순 정렬
        recentApartments.value = await Promise.all(
          sortedLocalData.map(async (item) => {
            const details = await fetchApartmentDetails(item.houseInfoId);
            return { ...item, houseInfos: details };
          })
        );
      }
    } else if (tab === "bookmarked-apartments") {
      if (!user.value) {
        alert("찜한 아파트는 로그인 후 이용 가능합니다.");
        router.push({ name: "user-login" });
        return;
      }
      try {
        const response = await apiClient.get("/interest/bookmarked-apartments", {
          params: { userId: user.value.id },
        });
        bookmarkedApartments.value = response.data || [];
      } catch (error) {
        console.error("찜한 아파트 가져오기 실패:", error);
      }
    } else if (tab === "recent-neighborhoods") {
      if (user.value) {
        const response = await apiClient.get("/interest/recent-neighborhoods", {
          params: { userId: user.value.id },
        });
        recentNeighborhoods.value = sortByTime(response.data || []);
      } else {
        const localData = JSON.parse(localStorage.getItem("recentNeighborhoods")) || [];
        const sortedLocalData = sortByTime(localData); // 시간순 정렬
        recentNeighborhoods.value = await Promise.all(
          sortedLocalData.map(async (item) => {
            const details = await fetchNeighborhoodDetails(item.dongCd);
            return { ...item, dongCd: details };
          })
        );
      }
    } else if (tab === "favorite-neighborhoods") {
      if (!user.value) {
        alert("즐겨찾는 동네는 로그인 후 이용 가능합니다.");
        router.push({ name: "user-login" });
        return;
      }
      const response = await apiClient.get("/interest/favorite-neighborhoods", {
        params: { userId: user.value.id },
      });
      favoriteNeighborhoods.value = response.data || [];
    }
  } catch (error) {
    console.error(`데이터 가져오기 실패 (${tab}):`, error);
  } finally {
    isLoading.value = false;
  }
};

// 로그인 상태 변화 감지
watch(
  () => user.value,
  (newValue) => {
    if (newValue) {
      syncLocalStorageToDB();
    }
  }
);

// 탭 변경
const changeTab = (tab) => {
  activeTab.value = tab;
  fetchData(tab);
};

// 초기 로드
onMounted(() => {
  fetchData(activeTab.value);
  fetchBookmarkStatus();
  fetchApartmentBookmarkStatus();
});

const encodeBase64 = (data) => {
  try {
    return btoa(
      encodeURIComponent(JSON.stringify(data))
        .replace(/%([0-9A-F]{2})/g, (_, p1) => String.fromCharCode("0x" + p1))
    );
  } catch (error) {
    console.error("Base64 encoding failed:", error);
    return null;
  }
};

</script>

<template>
  <div class="interest-container">
    <div class="interest-header">
      <h1 class="fw-bold" 
      @click="$router.push({ name: 'user-interest' })"
      style="cursor: pointer;">
        관심 목록
      </h1>

      <!-- Tab Navigation -->
      <div class="tab-container">
        <button class="tab-button" :class="{ active: activeTab === 'recent-apartments' }"
          @click="changeTab('recent-apartments')">
          최근 본 아파트
        </button>
        <button class="tab-button" :class="{ active: activeTab === 'bookmarked-apartments' }"
          @click="changeTab('bookmarked-apartments')">
          찜한 아파트
        </button>
        <button class="tab-button" :class="{ active: activeTab === 'recent-neighborhoods' }"
          @click="changeTab('recent-neighborhoods')">
          최근 본 동네
        </button>
        <button class="tab-button" :class="{ active: activeTab === 'favorite-neighborhoods' }"
          @click="changeTab('favorite-neighborhoods')">
          즐겨찾는 동네
        </button>
      </div>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading">로딩 중...</div>

    <!-- Content -->
    <div v-else>
      <div v-if="activeTab === 'recent-apartments'">
        <div class="card-grid">
          <div class="card" v-for="apartment in recentApartments" :key="apartment.houseInfoId"
          @click="$router.push({ name: 'home-list', params: { keyword: 'aptDetail', data: encodeBase64(apartment.houseInfos) } })">
            <div class="card-header">
              <h3>{{ apartment.houseInfos?.aptNm || "이름 없음" }}</h3>
              <div class="service-buttons">
                <!-- 북마크 버튼 -->
                 
                <div class="bookmark-button" @click.stop="toggleApartmentBookmark(apartment)">
                  <img v-if="apartment.bookmarked" src='@/components/icon/heartFilled.png' alt="Bookmark" />
                  <img v-else src='@/components/icon/heartEmpty.png' alt="Bookmark" />
                </div>
                <div class="delete-button" @click.stop="toggleApartmentDelete(apartment)">
                  <img src='@/components/icon/cancel.png' alt="delete" />
                </div>
              </div>
            </div>
            <div class="card-content">
              <p>{{ apartment.houseInfos?.umdNm || "위치 정보 없음" }} {{ apartment.houseInfos?.roadNm || "" }} {{ apartment.houseInfos?.jibun || "" }}</p>
              <!-- <p>건축 연도: {{ apartment.houseInfos?.buildYear || "연도 정보 없음" }}</p> -->
            </div>
          </div>
        </div>
        <div v-if="!recentApartments.length" class="empty-message">최근 본 아파트가 없습니다.</div>
      </div>
    </div>

    <div v-if="activeTab === 'bookmarked-apartments'">
      <div class="card-grid">
        <div class="card" v-for="apartment in bookmarkedApartments" :key="apartment.houseInfoId"
        @click="$router.push({ name: 'home-list', params: { keyword: 'aptDetail', data: encodeBase64(apartment.houseInfos) } })">
          <div class="card-header">
            <h3>{{ apartment.houseInfos?.aptNm || "이름 없음" }}</h3>
            <div class="service-buttons">
              <!-- 북마크 버튼 -->
              <div class="bookmark-button" @click.stop="toggleApartmentBookmark(apartment)">
                <img v-if="apartment.bookmarked" src='@/components/icon/heartFilled.png' alt="Bookmark" />
                <img v-else src='@/components/icon/heartEmpty.png' alt="Bookmark" />
              </div>
            </div>
          </div>
          <div class="card-content">
            <p>{{ apartment.houseInfos?.umdNm || "위치 정보 없음" }} {{ apartment.houseInfos?.roadNm || "" }} {{ apartment.houseInfos?.jibun || "" }}</p>
            <!-- <p>건축 연도: {{ apartment.houseInfos?.buildYear || "연도 정보 없음" }}</p> -->
          </div>
        </div>
      </div>
      <div v-if="!bookmarkedApartments.length" class="empty-message">
        찜한 아파트가 없습니다.
      </div>
    </div>

    <!-- 최근 본 동네 -->
    <div v-if="activeTab === 'recent-neighborhoods'">
      <!-- 유저가 있는 경우 -->
      <div v-if="user">
        <div class="card-grid-dong">
          <div class="card" v-for="neighborhood in recentNeighborhoods" :key="neighborhood.dongCd"
          @click="$router.push({ name: 'home-list', params: { keyword: neighborhood.dongCd } })">
            <div class="card-header-dong">
              <h3>{{ neighborhood.sidoName }} {{ neighborhood.gugunName }} {{ neighborhood.dongName }}</h3>
              <div class="service-buttons">
                <!-- 북마크 버튼 -->
                <div class="bookmark-button" @click.stop="toggleBookmark(neighborhood)">
                  <img v-if="neighborhood.bookmarked" src='@/components/icon/heartFilled.png' alt="Bookmark" />
                  <img v-else src='@/components/icon/heartEmpty.png' alt="Bookmark" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 유저가 없는 경우 -->
      <div v-else>
        <div class="card-grid-dong">
          <div class="card" v-for="neighborhood in recentNeighborhoods" :key="neighborhood.dongCd"
          @click="$router.push({ name: 'home-list', params: { keyword: neighborhood.dongCd } })">
            <div class="card-header-dong">
              <h3>{{ neighborhood.dongCd.sidoName }} {{ neighborhood.dongCd.gugunName }} {{ neighborhood.dongCd.dongName }}</h3>
              <div class="service-buttons">
                <!-- 북마크 버튼 -->
                <div class="bookmark-button" @click.stop="toggleBookmark(neighborhood)">
                  <img v-if="neighborhood.bookmarked" src='@/components/icon/heartFilled.png' alt="Bookmark" />
                  <img v-else src='@/components/icon/heartEmpty.png' alt="Bookmark" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 데이터가 없는 경우 -->
      <div v-if="!recentNeighborhoods.length" class="empty-message">최근 본 동네가 없습니다.</div>
    </div>

    <!-- 즐겨찾는 동네 -->
    <div v-if="activeTab === 'favorite-neighborhoods'">
      <div class="card-grid-dong">
        <div class="card" v-for="neighborhood in favoriteNeighborhoods" :key="neighborhood.dongCd"
        @click="$router.push({ name: 'home-list', params: { keyword: neighborhood.dongCd } })">
          <div class="card-header-dong">
            <h3>{{ neighborhood.sidoName }} {{ neighborhood.gugunName }} {{ neighborhood.dongName }}</h3>
            <div class="service-buttons">
              <!-- 북마크 버튼 -->
              <div class="bookmark-button" @click.stop="toggleBookmark(neighborhood)">
                <img v-if="neighborhood.bookmarked" src='@/components/icon/heartFilled.png' alt="Bookmark" />
                <img v-else src='@/components/icon/heartEmpty.png' alt="Bookmark" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!favoriteNeighborhoods.length" class="empty-message">즐겨찾는 동네가 없습니다.</div>
    </div>

  </div>

  

</template>



<style scoped>
.interest-container {
  width: 90%;
  margin: 40px auto;
  padding-bottom: 40px;
  line-height: 1.6;
}
.interest-container h1 {
  font-size: 2rem;
  font-weight: 800;
  color: var(--eerie-black);
  margin-bottom: 1rem;
}

.interest-header {
  position: sticky; /* 컨테이너 내에서 고정 */
  top: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0; /* 여백 제거 */
  padding: 10px;
  width: 100%;
  background-color: white; /* 배경색 */
  border-bottom: 1px solid #ddd;
  z-index: 10; /* 스크롤되는 다른 요소 위로 올림 */
}

.tab-container {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-button {
  padding: 10px 20px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.tab-button.active {
  background-color: black;
  color: white;
}

.loading {
  text-align: center;
  font-size: 16px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.card-grid-dong {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.15);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.15);
}

.empty-message {
  text-align: center;
  color: #999;
  margin-top: 20px;
}

.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  background-color: #f9f9f9;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 20px 10px;
  background-color: white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: transform 0.5s ease, box-shadow 0.1s ease;
}
.card-header {
  width: 90%;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background-color: white;
  border-bottom: 2px solid #ddd;
  margin-bottom: 10px;
}

.card-header-dong {
  width: 90%;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background-color: white;
  margin: 10px;
}

.service-buttons {
  margin-left: 10px; /* 텍스트와 하트 사이 간격 */
}

.card-header h3 {
  flex: 1;
  text-align: left;
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 0;
  white-space: nowrap; /* 텍스트를 한 줄로 표시 */
  overflow: hidden; /* 넘치는 텍스트를 숨김 */
  text-overflow: ellipsis; /* 말줄임표(...) 추가 */
  margin: 0;
}
.card-header-dong h3 {
  flex: 1;
  text-align: left;
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 0;
  white-space: nowrap; /* 텍스트를 한 줄로 표시 */
  overflow: hidden; /* 넘치는 텍스트를 숨김 */
  text-overflow: ellipsis; /* 말줄임표(...) 추가 */
  margin: 0;
}

.card-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  justify-content: flex-start;
  width: 90%;
  padding: 10px;
  white-space: nowrap; /* 텍스트를 한 줄로 표시 */
  overflow: hidden; /* 넘치는 텍스트를 숨김 */
  text-overflow: ellipsis; /* 말줄임표(...) 추가 */
}
.card-content p {
  font-size: 1rem;
  font-weight: 400;
  margin-bottom: 0;
  text-align: left;
}

.service-buttons {
  margin-left: 10px; /* 텍스트와 하트 사이 간격 */
  display: flex;
  flex-direction: row;
  gap: 10px;
}
.service-buttons img {
  flex-shrink: 0;
  cursor: pointer;
  width: 24px;
  height: 24px;
  cursor: pointer;
}

</style>
