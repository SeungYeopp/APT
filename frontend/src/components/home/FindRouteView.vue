<template>
  <div class="modal-overlay" @click.self="$emit('click-outside')">
    <div class="modal-content">
      <button type="button" class="close-button" @click="$emit('close')"><img src="@/components/icon/cancel.png" alt="닫기 버튼" /></button>
      <h2>길찾기</h2>
      <form @submit.prevent="handleFindRoute" class="form-inline">
        <label for="startLocation">출발지:</label>
        <input v-model="startLocation" id="startLocation" type="text" placeholder="예: 강남역" required />
        <button type="submit" class="submit-button">경로 찾기</button>
      </form>
      <div id="map" style="width: 100%; height: 500px; margin-top: 20px; position: relative;">
        <div v-if="routeInfo" class="route-info-overlay">
          <p><strong>총 거리:</strong> {{ routeInfo.distance }}m</p>
          <p><strong>도보:</strong> {{ routeInfo.walkTime }}</p>
          <p><strong>자전거:</strong> {{ routeInfo.bicycleTime }}</p>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  destination: {
    type: Object,
    required: true,
  },
});

const startLocation = ref('');
const map = ref(null);
const polyline = ref(null);
const routeInfo = ref(null); // 경로 정보 저장

const handleFindRoute = async () => {
  if (!startLocation.value.trim()) {
    alert('출발지를 입력해주세요.');
    return;
  }

  try {
    // 1. 출발지 좌표 가져오기
    const { data: startCoordinates } = await axios.post('/apt/getCoordinates', null, {
      params: { query: startLocation.value },
    });

    // 2. 경로 데이터 가져오기
    const { data: path } = await axios.post('/apt/findRoute', null, {
      params: {
        userLatitude: startCoordinates.lat,
        userLongitude: startCoordinates.lng,
        destLatitude: props.destination.lat,
        destLongitude: props.destination.lng,
      },
    });

    // 지도에 경로 그리기
    drawRoute(path);

    // 경로 정보 계산
    const totalDistance = Math.round(
      path.reduce((sum, point, i) =>
        i > 0 ? sum + calculateDistance(path[i - 1], point) : sum, 0)
    );

    routeInfo.value = {
      distance: totalDistance,
      walkTime: getTime(totalDistance, 67),
      bicycleTime: getTime(totalDistance, 227),
    };
  } catch (error) {
    console.error('경로 찾기 실패:', error);
    alert('경로 찾기 중 오류가 발생했습니다. 다시 시도해주세요.');
  }
};

const drawRoute = (path) => {
  if (polyline.value) {
    polyline.value.setMap(null);
  }

  const linePath = path.map((point) => new kakao.maps.LatLng(point.lat, point.lng));

  polyline.value = new kakao.maps.Polyline({
    path: linePath,
    strokeWeight: 5,
    strokeColor: '#FF0000',
    strokeOpacity: 1.0,
    strokeStyle: 'solid',
  });
  polyline.value.setMap(map.value);

  new kakao.maps.Marker({
    map: map.value,
    position: linePath[0],
    title: '출발지',
  });

  new kakao.maps.Marker({
    map: map.value,
    position: linePath[linePath.length - 1],
    title: '목적지',
  });
};

// 거리 계산 (좌표 2개의 거리 반환)
const calculateDistance = (point1, point2) => {
  const toRad = (deg) => (deg * Math.PI) / 180;
  const lat1 = toRad(point1.lat);
  const lng1 = toRad(point1.lng);
  const lat2 = toRad(point2.lat);
  const lng2 = toRad(point2.lng);

  const R = 6371000; // 지구 반지름 (m)
  const dLat = lat2 - lat1;
  const dLng = lng2 - lng1;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

// 시간 계산 (거리와 분속으로 시간 반환)
const getTime = (distance, speed) => {
  const timeInMinutes = Math.round(distance / speed);
  const hours = Math.floor(timeInMinutes / 60);
  const minutes = timeInMinutes % 60;
  return `${hours > 0 ? `${hours}시간 ` : ''}${minutes}분`;
};

onMounted(() => {
  map.value = new kakao.maps.Map(document.getElementById('map'), {
    center: new kakao.maps.LatLng(props.destination.lat, props.destination.lng),
    level: 3,
  });
});
</script>

<style scoped>
.route-info-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
  /* 살짝 투명한 흰색 배경 */
  border: 1px solid #ddd;
  /* 연한 회색 테두리 */
  border-radius: 10px;
  /* 둥근 모서리 */
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
  /* 그림자 효과 */
  padding: 15px;
  width: 200px;
  /* 박스 크기 */
  text-align: left;
  font-size: 14px;
  color: #333;
  /* 텍스트 색상 */
  z-index: 10;
  /* 지도 위에 표시되도록 z-index 설정 */
}

.route-info-overlay p {
  margin: 5px 0;
  /* 간격 */
}

.route-info-overlay strong {
  color: #0056b3;
  /* 강조 텍스트 색상 */
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  position: relative;
  background: white;
  padding: 20px;
  border-radius: 10px;
  width: 50%;
  text-align: center;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.form-inline {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20px 0;
}

label {
  margin-right: 10px;
}

input {
  width: 50%;
  padding: 10px;
  margin-right: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.submit-button {
  background-color: #0056b3;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 5px;
  cursor: pointer;
}

.close-button img {
  width: 30px; /* 아이콘 크기 */
  height: 30px;
}
</style>