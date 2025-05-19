import { ref, computed } from "vue";
import { defineStore } from "pinia";
import { useNavBar } from "@/stores/navbar";
import { logout } from "@/api/user";

export const useUserStore = defineStore("userStore", () => {
  const user = ref(null);
  const navBar = useNavBar();

  const setUser = (userData) => {
    user.value = { ...user.value, ...userData }; // 기존 상태 유지, 새 데이터 병합
  };
  

  // 사용자 상태 초기화
  const initUser = () => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      user.value = JSON.parse(storedUser);
      navBar.changeMenuState(user.value); // 메뉴 업데이트
    }
  };

  initUser();

  const getUser = computed(() => user.value);

  const userLogout = async () => {
    try {
      await logout();
      user.value = null; // 상태 초기화
      localStorage.removeItem("user"); // localStorage 초기화
    } catch (error) {
      console.error("Logout Error:", error);
    }
  };

  return { user, getUser, userLogout, setUser };
});
