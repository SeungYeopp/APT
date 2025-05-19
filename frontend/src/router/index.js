import { createRouter, createWebHistory } from "vue-router";
import MainPage from "@/views/MainPage.vue";
import { useUserStore } from "@/stores/user";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "main",
      component: MainPage,
      meta: { scrollable: true },
    },
    {
      path: "/aichat",
      name: "aichat",
      component: () => import("@/components/ai/AIChatView.vue"),
      meta: { scrollable: true, requiresAuth: true },
    },
    {
      path: "/home",
      name: "home",
      component: () => import("@/views/TheHomeView.vue"),
      children: [
        {
          path: "list/:keyword/:data?",
          name: "home-list",
          component: () => import("@/components/home/HomeList.vue"),
        },
        {
          path: "deal",
          name: "home-deal",
          component: () => import("@/components/home/HomeDealList.vue"),
        },
      ],
    },
    {
      path: "/user",
      name: "user",
      component: () => import("@/views/TheUserView.vue"),
      children: [
        {
          path: "/user/login",
          name: "user-login",
          component: () => import("@/components/user/UserLogin.vue"), // 기존 UserLogin
        },
        {
          path: "/user/login/email",
          name: "email-login",
          component: () => import("@/components/user/LoginForm.vue"), // 이메일 로그인
        },
        {
          path: "/user/login/kakao",
          name: "kakao-login",
          component: () => import("@/components/user/KakaoLogin.vue"), // 카카오 로그인
        },
        {
          path: "/user/join",
          name: "user-join",
          component: () => import("@/components/user/RegisterForm.vue"), // 회원가입
        },
        {
          path: "mypage",
          name: "user-mypage",
          component: () => import("@/components/user/UserMyPage.vue"),
          meta: { scrollable: true },
        },
        // {
        //   path: "modify/:userid",
        //   name: "user-modify",
        //   component: () => import("@/components/user/UserModify.vue"),
        // },
        {
          path: "interest",
          name: "user-interest",
          component: () => import("@/components/user/UserInterest.vue"),
          meta: { scrollable: true },
        },
      ],
    },
    {
      path: "/oauth/redirect",
      name: "kakao-redirect",
      component: () => import("@/components/user/KakaoRedirect.vue"), // 카카오 리다이렉트 처리
    },
    {
      path: "/board",
      name: "board",
      component: () => import("@/views/TheBoardView.vue"),
      redirect: { name: "article-list" },
      children: [
        {
          path: "list",
          name: "article-list",
          component: () => import("@/components/boards/BoardList.vue"),
        },
        {
          path: "view/:articleno",
          name: "article-view",
          component: () => import("@/components/boards/BoardDetail.vue"),
        },
        {
          path: "write",
          name: "article-write",
          component: () => import("@/components/boards/BoardWrite.vue"),
          meta: { requiresAuth: true }, // 로그인 필요
        },
        {
          path: "modify/:articleno",
          name: "article-modify",
          component: () => import("@/components/boards/BoardModify.vue"),
          meta: { requiresAuth: true }, // 로그인 필요
        },
        {
          path: "faq", // 자주 묻는 질문
          name: "faq",
          component: () => import("@/components/boards/FAQ.vue"),
        },
        {
          path: "notice", // 공지사항
          name: "notice",
          component: () => import("@/components/boards/Notice.vue"),
        },
      ],
    },
    {
      path: "/map",
      name: "map",
      component: () => import("@/components/home/TheMap.vue"),
    },
  ],
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  if (to.meta.requiresAuth && !userStore.getUser) {
    // 로그인 필요한 라우트인데 로그인 상태가 아닐 경우
    alert("로그인이 필요합니다.");
    next({ name: "user-login" }); // 로그인 페이지로 이동
  } else {
    next(); // 다음 라우트로 이동
  }
});

export default router;
