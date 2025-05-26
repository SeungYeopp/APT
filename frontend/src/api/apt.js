import axios from "@/util/axios-common";

function getAptListByArea(location, success, fail) {
  let url = "";
  url = `${import.meta.env.VITE_VUE_API_URL}/apt/area/${location}`;
  axios.get(url).then(success).catch(fail);
}

// 아파트 이름으로 리스트 조회
function getAptListByKeyword(keyword, success, fail) {
  axios
    .get(`${import.meta.env.VITE_VUE_API_URL}/apt/name/${keyword}`)
    .then(success)
    .catch(fail);
}

// 특정 아파트 거래 정보 조회
function getAptDeal(aptSeq, success, fail) {
  axios
    .get(`${import.meta.env.VITE_VUE_API_URL}/apt/housedeal/${aptSeq}`)
    .then(success)
    .catch(fail);
}

function getDongCodeByName(keyword, success) {
  axios
    .get(`${import.meta.env.VITE_VUE_API_URL}/apt/dong/${keyword}`)
    .then(success);
}

function getDongCodeByDetails(sidoName, gugunName, dongName, success, fail) {
  axios
    .get("${import.meta.env.VITE_VUE_API_URL}/apt/code", {
      params: { sidoName, gugunName, dongName },
    })
    .then(success)
    .catch(fail);
}

export {
  getAptListByArea,
  getAptListByKeyword,
  getAptDeal,
  getDongCodeByName,
  getDongCodeByDetails,
};
