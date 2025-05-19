<script setup>
import { storeToRefs } from "pinia";
import { useNavBar } from "@/stores/navbar";
import { useUserStore } from "@/stores/user";
import { useRouter } from "vue-router";

const store = useUserStore();
const { getUser: user } = storeToRefs(store);
const { userLogout } = store;
const router = useRouter();

const nav = useNavBar();
const { menuList } = storeToRefs(nav);
const { changeMenuState } = nav; // 메뉴 업데이트 함수

const logout = async () => {
  try {
    await userLogout(); // 로그아웃 처리
    changeMenuState(null); // 로그아웃 후 메뉴 상태 초기화
    console.log("logout!!!");
    router.push({ name: "main" }); // 메인 페이지로 이동
  } catch (error) {
    console.error("Logout failed:", error);
  }
};

function refreshPage() {
    // 새로 고침을 트리거하는 방법
    window.location.reload();
}

function closeMenu() {
  const menu = document.getElementById("navbarNav");
  if (menu.classList.contains("show")) {
    // Bootstrap Collapse를 통해 메뉴를 닫기
    const bsCollapse = new bootstrap.Collapse(menu, { toggle: false });
    bsCollapse.hide();
  }
}

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

const defaultApt = { 
    "aptSeq": "11680-3621", 
    "aptNm": "역삼래미안", 
    "sggCd": "11680", 
    "umdCd": "10100", 
    "umdNm": "역삼동", 
    "roadNm": "선릉로69길", 
    "jibun": "757", 
    "buildYear": 2005, 
    "latitude": 37.4981263526043, 
    "longitude": 127.050691041401, 
    "dongCd": null, 
    "deals": null, 
    "reviews": null 
};

</script>

<template>
<!-- Navigation Bar -->
<header>
    <nav class="navbar navbar-expand-lg sticky-top">
        <div class="header-container column-gap-3">
            <router-link :to="{ name: 'main' }" class="navbar-brand">
                <!-- <img src="logo.png" alt="APT Logo" style="width: 40px;"> -->
                <h1 id="home">APT</h1>
            </router-link>
            <button
                class="navbar-toggler"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#navbarNav"
                aria-controls="navbarNav"
                aria-expanded="false"
                aria-label="Toggle navigation"
            >
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse " id="navbarNav">
                <b-nav class="navbar-nav me-auto my-2 my-lg-0 navbar-nav-scroll">
                    <b-nav-item>
                        <router-link
                        :to="{ name: 'home-list', params: { keyword: 'aptDetail', data: encodeBase64(defaultApt) } }"
                        class="nav-link" @click="closeMenu"
                        >지도</router-link>
                    </b-nav-item>
                    <b-nav-item>
                        <router-link :to="{ name: 'home' }" class="nav-link" @click="closeMenu">동네별 아파트
                        </router-link>
                    </b-nav-item>
                    <b-nav-item>
                        <router-link :to="{ name: 'board' }" class="nav-link" @click="closeMenu">문의하기
                            <!-- &nbsp;<img src="@/components/icon/bottom-arrow.png" alt="arrow"> -->
                        </router-link>
                    </b-nav-item>
                    <b-nav-item>
                        <router-link :to="{ name: 'user-interest' }" class="nav-link" @click="closeMenu">관심목록
                        </router-link>
                    </b-nav-item>
                    <b-nav-item>
                        <router-link :to="{ name: 'aichat' }" class="nav-link" @click="closeMenu">AI 채팅
                        </router-link>
                    </b-nav-item>
                </b-nav>
                <b-nav class="navbar-nav ms-auto my-2 navbar-nav-scroll column-gap-3">
                    <template v-for="menu in menuList" :key="menu.routeName">
                        <template v-if="menu.show">
                            <template v-if="menu.routeName === 'user-logout'">
                                <button class="header-btn" :style="menu.style">
                                <router-link to="/" @click.prevent="logout" class="nav-link" :style="menu.style" @click="closeMenu">{{menu.name}}</router-link>
                                </button>
                            </template>
                            <template v-else>
                                <button class="header-btn" :style="menu.style">
                                <router-link :to="{ name: menu.routeName }" class="nav-link" :style="menu.style" @click="closeMenu">{{menu.name}}
                                    <span v-if="menu.routeName === 'home'">→</span>
                                </router-link>
                                </button>
                            </template>
                        </template>
                    </template>
                </b-nav>
            </div>
        </div>
    </nav>
</header>
</template>

<style scoped>
.header-container{
    width: 100%;
    height: 100%;
    align-items: center;
    display: flex;
    justify-content: space-between;
}
.header-btn {
    border: none;
    padding: 2px 10px;
    border-radius: 13px;
}
.navbar {
    padding: 4px 0px;
}
.navbar .nav-link {
    color: var(--eerie-black)
}

.navbar-collapse {
    text-align: center;
}

.navbar-toggler {
    border: none;
}

/* 화면이 좁아졌을 때 (반응형 스타일) */
@media (max-width: 991px) {
    .header-container {
        align-items: center; /* 중앙 정렬 */
    }

    .navbar-collapse {
        width: 100%;
        background-color: #fff;
        border: 1px solid #ddd;
        padding: 10px;
        border-radius: 8px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    }

    .navbar-nav {
        flex-direction: column; /* 메뉴 리스트를 세로로 정렬 */
        width: 100%; /* 드롭다운 메뉴가 전체 너비를 차지 */
        z-index: 1000px;
    }

    .nav-item {
        margin-bottom: 10px; /* 리스트 간 간격 */
    }

    .nav-item:last-child {
        margin-bottom: 0;
    }
}

</style>
