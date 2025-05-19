import { ref, computed, watch } from "vue";
import { defineStore } from "pinia";
import { getAptListByArea } from "@/api/apt.js";

export const useAptStore = defineStore("aptStore", () => {
  const aptList = ref([]); // 아파트 리스트
  const selectedApt = ref(null); // 선택된 아파트
  const dongCd = ref("1168010100"); // 역삼동 (기본값)

  const setDongCd = (newDongCd) => {
    dongCd.value = newDongCd;
    selectedApt.value = null;
  };

  // 아파트 리스트를 가져오는 함수
  const initAptList = () => {
    getAptListByArea(
      dongCd.value,
      ({ data }) => {
        aptList.value = data;
      },
      (error) => {
        console.error("Error fetching apartments:", error);
      }
    );
  };

  // 동 코드(dongCd)가 변경될 때마다 아파트 목록을 새로 가져오도록 watch
  watch(dongCd, () => {
    initAptList(); // 동 코드가 변경되면 새로운 아파트 리스트를 불러옵니다.
  });

  // 처음 store가 초기화 될 때 아파트 리스트를 불러옵니다.
  initAptList();

  // 아파트 리스트를 가져오는 computed
  const getAptList = computed(() => aptList.value);

  return {
    aptList,
    selectedApt,
    dongCd,
    setDongCd,
    getAptList,
  };
});
