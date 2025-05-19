<template>
  
</template>

<script>
export default {
  async mounted() {
    try {
      // Kakao 객체 확인 및 초기화
      if (!window.Kakao) {
        console.error("Kakao SDK is not loaded");
        return;
      }

      if (!window.Kakao.isInitialized()) {
        window.Kakao.init(import.meta.env.VITE_KAKAO_CLIENT_ID);
      }

      // 프로필 정보에 대한 동의 요청 포함
      await window.Kakao.Auth.authorize({
        redirectUri: import.meta.env.VITE_REDIRECT_URI,
        scope: "profile_nickname profile_image", 

      });
    } catch (error) {
      console.error("Error during Kakao login process:", error);
    }
  },
};
</script>
