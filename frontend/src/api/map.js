import axios from "@/util/axios-common";

function listSido(success, fail) {
  axios.get(`/map/sido`).then(success).catch(fail);
}

function listGugun(param, success, fail) {
  axios.get(`/map/gugun`, { params: param }).then(success).catch(fail);
}

function listDong(param, success, fail) {
  axios.get(`/map/dong`, { params: param }).then(success).catch(fail);
}

export { listSido, listGugun, listDong };
