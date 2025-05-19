import { ref } from "vue";
import { defineStore } from "pinia";

export const useNavBar = defineStore("nav", () => {
  const menuList = ref([
    {
      name: "내정보",
      show: false,
      routeName: "user-mypage",
      style: {
        "background-color": "var(--jonquil)",
        color: "var(--eerie-grey)",
      },
    },
    {
      name: "로그인",
      show: true,
      routeName: "user-login",
      style: {
        "background-color": "var(--jonquil)",
        color: "var(--eerie-black)",
      },
    },
    {
      name: "로그아웃",
      show: false,
      routeName: "user-logout",
      style: {
        "background-color": "var(--khaki-grey)",
        color: "var(--eerie-black)",
      },
    },
    {
      name: "아파트 보기",
      show: true,
      routeName: "home",
      style: {
        "background-color": "var(--figma-blue)",
        color: "white",
      },
    },
  ]);

  const changeMenuState = (user) => {
    menuList.value = menuList.value.map((menu) => {
      if (menu.routeName === "user-login") {
        menu.show = !user;
      } else if (
        menu.routeName === "user-logout" ||
        menu.routeName === "user-mypage"
      ) {
        menu.show = !!user;
      }
      return menu;
    });
  };

  return { menuList, changeMenuState };
});
