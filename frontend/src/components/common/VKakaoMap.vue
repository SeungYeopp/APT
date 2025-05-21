<script setup>
import { ref, watch, onMounted } from "vue";

// 지도 객체와 상태 변수
let map;
let clusterer;
const isMapReady = ref(false);

// 마커 데이터 저장
const positions = ref([]);

const props = defineProps({
  apartments: {
    type: Array,
    default: () => [], // 기본값: 빈 배열
  },
  selectApt: {
    type: [Object, String],
    default: null, // 기본값: null
  },
});

// 카카오 지도 스크립트 로드
const loadKakaoMapScript = async () => {
  if (window.kakao && window.kakao.maps) return Promise.resolve();

  return new Promise((resolve) => {
    const script = document.createElement("script");
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=${
      import.meta.env.VITE_KAKAO_MAP_SERVICE_KEY
    }&libraries=services,clusterer`;
    script.onload = () => kakao.maps.load(resolve);
    document.head.appendChild(script);
  });
};

onMounted(() => {
  loadKakaoMapScript().then(() => {
    initMap();
    if (props.apartments.length > 0) {
        updatePositions(props.apartments);
        loadMarkers();
        //panToFirstApartment(props.apartments);
    } else if (props.selectApt && props.selectApt.latitude && props.selectApt.longitude) {
      addSingleMarker(props.selectApt);
    } else {
        // 기본 중심 위치로 지도를 설정
        map.setCenter(new kakao.maps.LatLng(37.5012986996253, 127.039627288835));
    }
  }).catch((error) => {
    console.error("Error loading Kakao map script or initializing map:", error);
  });
});

const initMap = () => {
  const container = document.getElementById("map");
  const options = {
    center: new kakao.maps.LatLng(37.5012986996253, 127.039627288835), // 멀캠 좌표
    level: 3,
  };
  map = new kakao.maps.Map(container, options);
  isMapReady.value = true;
  categoryService();

  clusterer = new kakao.maps.MarkerClusterer({
      map: map,
      averageCenter: true, // 클러스터에 포함된 마커들의 평균 위치를 클러스터 마커 위치로 설정
      minLevel: 5, // 클러스터 할 최소 지도 레벨
      calculator: [10, 50, 100], // 클러스터 마커 색상을 구분할 기준
      styles: [
        {
            width: '50px',
            height: '50px',
            background: 'var(--figma-blue-tp)', // 10개 이하: 파랑
            borderRadius: '25px',
            color: '#fff',
            textAlign: 'center',
            lineHeight: '50px',
            fontSize: '14px',
        },
        {
            width: '60px',
            height: '60px',
            background: 'var(--jade-tp)', // 10~50개: 빨강
            borderRadius: '50%',
            color: '#fff',
            textAlign: 'center',
            lineHeight: '60px',
            fontSize: '16px',
        },
        {
            width: '70px',
            height: '70px',
            background: 'var(--xanthous-tp)', // 50~100개: 보라
            borderRadius: '35px',
            color: '#fff',
            textAlign: 'center',
            lineHeight: '70px',
            fontSize: '18px',
        },
        {
            width: '80px',
            height: '80px',
            background: 'var(--indian-red-tp)', // 100개 이상: 초록
            borderRadius: '40px',
            color: '#fff',
            textAlign: 'center',
            lineHeight: '80px',
            fontSize: '20px',
        },
    ],
  });
};

// 단일 마커 추가
const addSingleMarker = (selectedApt) => {
  if (!isMapReady.value || !selectedApt) return;

  const position = new kakao.maps.LatLng(selectedApt.latitude, selectedApt.longitude);

  // 지도 중심 설정
  map.setCenter(position);

  // 마커 생성
  const marker = new kakao.maps.Marker({
    position,
    map,
  });

  // 인포윈도우 생성
  const infoWindow = new kakao.maps.InfoWindow({
    content: `
      <div class="custom-info-window">
        ${selectedApt.aptNm || "선택된 위치"}
      </div>
    `,
  });

  // 마커 이벤트
  kakao.maps.event.addListener(marker, "mouseover", () => {
    infoWindow.open(map, marker);
  });
  kakao.maps.event.addListener(marker, "mouseout", () => {
    infoWindow.close();
  });

  // 클러스터 초기화
  clusterer.clear();
  clusterer.addMarkers([marker]); // 단일 마커도 클러스터에 추가
};


watch(
  () => props.selectApt,
  (newVal) => {
    if (!isMapReady.value || !newVal || typeof newVal !== "object" || !newVal.latitude || !newVal.longitude) {
      return;
    }

    // 이동할 위도 경도 위치를 생성합니다
    const moveLatLon = new kakao.maps.LatLng(newVal.latitude, newVal.longitude);
    map.setLevel(2);
    map.setCenter(moveLatLon);

    // 클러스터와 지도 이벤트 동기화
    kakao.maps.event.addListener(clusterer, "clusterclick", () => {
      map.setCenter(moveLatLon);
      map.setLevel(4); // 확대 레벨
    });
  },
  { deep: true }
  // (newVal) => {
  //   if (newVal && newVal.latitude && newVal.longitude) {
  //     addSingleMarker(newVal);
  //   }
  // },
  // { immediate: true, deep: true }
);

watch(
  () => props.apartments,
  (newApartments) => {
    if (!isMapReady.value) {
      console.warn("Map is not ready yet");
      return;
    }

    deleteMarkers();

    if (newApartments && newApartments.length > 0) {
      updatePositions(newApartments);
      loadMarkers();
      //panToFirstApartment(newApartments);
    } else if (props.selectApt) {
      addSingleMarker(props.selectApt);
    } else {
      console.warn("No apartments provided");
      // 기본 동작 추가 (지도 초기화 등)
      map.setCenter(new kakao.maps.LatLng(37.5012986996253, 127.039627288835));
    }
  },
  { immediate: true, deep: true }
);

const panToFirstApartment = (apartments) => {
  if (!apartments || apartments.length === 0) {
        console.warn("No apartments to pan to");
        return;
    }

  const { latitude, longitude } = apartments[0];
  if (latitude !== 0 && longitude !== 0 && latitude !== null && longitude !== null) {
    const moveLatLon = new kakao.maps.LatLng(latitude, longitude);
    map.panTo(moveLatLon);
  }
};



const updatePositions = (apartments) => {
  positions.value = apartments.map((apartment) => ({
    latlng: new kakao.maps.LatLng(apartment.latitude, apartment.longitude),
    title: apartment.aptNm,
  }));
};

const loadMarkers = () => {
  deleteMarkers();

  // 마커를 생성합니다.
  const clusterPositions = positions.value.map((position) => {
    var marker = new kakao.maps.Marker({
      position: position.latlng, // 마커를 표시할 위치
      //title: position.title, // 마커의 타이틀
      clickable: true, // 마커 클릭 시 이벤트를 발생시키지 않도록 설정
    });
    // 마커에 표시할 인포윈도우를 생성합니다
    var infowindow = new kakao.maps.InfoWindow({
        content: `
            <div class="custom-info-window">
                ${position.title}
            </div>
        `
    });

    // 마커에 이벤트를 등록하는 함수 만들고 즉시 호출하여 클로저를 만듭니다
    // 클로저를 만들어 주지 않으면 마지막 마커에만 이벤트가 등록됩니다
    (function(marker, infowindow) {
        // 마커에 mouseover 이벤트를 등록하고 마우스 오버 시 인포윈도우를 표시합니다
        kakao.maps.event.addListener(marker, 'mouseover', function() {
            infowindow.open(map, marker);
        });

        // 마커에 mouseout 이벤트를 등록하고 마우스 아웃 시 인포윈도우를 닫습니다
        kakao.maps.event.addListener(marker, 'mouseout', function() {
            infowindow.close();
        });
    })(marker, infowindow);

    return marker;
  });
  // 기존 클러스터 제거 후 새 마커 추가
  clusterer.clear();
  clusterer.addMarkers(clusterPositions);

  // 지도 범위 설정
  const bounds = positions.value.reduce((bounds, position) => {
    if (position.latlng && (position.latlng.getLat() !== 0 || position.latlng.getLng() !== 0)) {
      bounds.extend(position.latlng);
    }
    return bounds;
  }, new kakao.maps.LatLngBounds());

  if (isMapReady.value) map.setBounds(bounds);
};


const deleteMarkers = () => {
  clusterer.clear(); // 클러스터 내 모든 마커 삭제
};


/////////////////////MAP TYPE 체크박스//////////////////////////////
const activeButtons = new Set(); // 활성화된 지도 타입 저장

function toggleMapType(type) {
  const mapTypes = {
    terrain: kakao.maps.MapTypeId.TERRAIN,
    useDistrict: kakao.maps.MapTypeId.USE_DISTRICT,
  };
  const button = document.getElementById(`btn${capitalize(type)}`);

  if (activeButtons.has(type)) {
    // 이미 활성화된 상태면 제거
    map.removeOverlayMapTypeId(mapTypes[type]);
    activeButtons.delete(type);
    button.classList.remove('active');
  } else {
    // 비활성화 상태면 추가
    map.addOverlayMapTypeId(mapTypes[type]);
    activeButtons.add(type);
    button.classList.add('active');
  }
}

// 첫 글자 대문자 변환 함수
function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

//////////////////////////CATEGORY/////////////////////////////////////////////////////
var placeOverlay = null, ps=null, contentNode = document.createElement('div'), category_markers=[], currCategory = '';
function categoryService() {
  placeOverlay = new kakao.maps.CustomOverlay({zIndex:1});

  // 장소 검색 객체를 생성합니다
  ps = new kakao.maps.services.Places(map);

  // 지도에 idle 이벤트를 등록합니다
  kakao.maps.event.addListener(map, 'idle', searchPlaces);

  // 커스텀 오버레이의 컨텐츠 노드에 css class를 추가합니다
  contentNode.className = 'placeinfo_wrap';

  // 커스텀 오버레이의 컨텐츠 노드에 mousedown, touchstart 이벤트가 발생했을때
  // 지도 객체에 이벤트가 전달되지 않도록 이벤트 핸들러로 kakao.maps.event.preventMap 메소드를 등록합니다
  addEventHandle(contentNode, 'mousedown', kakao.maps.event.preventMap);
  addEventHandle(contentNode, 'touchstart', kakao.maps.event.preventMap);

  // 커스텀 오버레이 컨텐츠를 설정합니다
  placeOverlay.setContent(contentNode);

  // 각 카테고리에 클릭 이벤트를 등록합니다
  addCategoryClickEvent();
}

// 엘리먼트에 이벤트 핸들러를 등록하는 함수입니다
function addEventHandle(target, type, callback) {
    if (target.addEventListener) {
        target.addEventListener(type, callback);
    } else {
        target.attachEvent('on' + type, callback);
    }
}

// 카테고리 검색을 요청하는 함수입니다
function searchPlaces() {
    if (!currCategory) {
        return;
    }

    // 커스텀 오버레이를 숨깁니다
    placeOverlay.setMap(null);

    // 지도에 표시되고 있는 마커를 제거합니다
    removeMarker();

    ps.categorySearch(currCategory, placesSearchCB, {useMapBounds:true});
}

// 장소검색이 완료됐을 때 호출되는 콜백함수 입니다
function placesSearchCB(data, status, pagination) {
    if (status === kakao.maps.services.Status.OK) {

        // 정상적으로 검색이 완료됐으면 지도에 마커를 표출합니다
        displayPlaces(data);
    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
        // 검색결과가 없는경우 해야할 처리가 있다면 이곳에 작성해 주세요

    } else if (status === kakao.maps.services.Status.ERROR) {
        // 에러로 인해 검색결과가 나오지 않은 경우 해야할 처리가 있다면 이곳에 작성해 주세요

    }
}

// 지도에 마커를 표출하는 함수입니다
function displayPlaces(places) {

    // 몇번째 카테고리가 선택되어 있는지 얻어옵니다
    // 이 순서는 스프라이트 이미지에서의 위치를 계산하는데 사용됩니다
    var order = document.getElementById(currCategory).getAttribute('data-order');



    for ( var i=0; i<places.length; i++ ) {

            // 마커를 생성하고 지도에 표시합니다
            var marker = addMarker(new kakao.maps.LatLng(places[i].y, places[i].x), order);

            // 마커와 검색결과 항목을 클릭 했을 때
            // 장소정보를 표출하도록 클릭 이벤트를 등록합니다
            (function(marker, place) {
                kakao.maps.event.addListener(marker, 'click', function() {
                    displayPlaceInfo(place);
                });
            })(marker, places[i]);
    }
}

// 마커를 생성하고 지도 위에 마커를 표시하는 함수입니다
function addMarker(position, order) {
    var imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/places_category.png', // 마커 이미지 url, 스프라이트 이미지를 씁니다
        imageSize = new kakao.maps.Size(27, 28),  // 마커 이미지의 크기
        imgOptions =  {
            spriteSize : new kakao.maps.Size(72, 208), // 스프라이트 이미지의 크기
            spriteOrigin : new kakao.maps.Point(46, (order*36)), // 스프라이트 이미지 중 사용할 영역의 좌상단 좌표
            offset: new kakao.maps.Point(11, 28) // 마커 좌표에 일치시킬 이미지 내에서의 좌표
        },
        markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imgOptions),
            marker = new kakao.maps.Marker({
            position: position, // 마커의 위치
            image: markerImage
        });

    marker.setMap(map); // 지도 위에 마커를 표출합니다
    category_markers.push(marker);  // 배열에 생성된 마커를 추가합니다

    return marker;
}

// 지도 위에 표시되고 있는 마커를 모두 제거합니다
function removeMarker() {
    for ( var i = 0; i < category_markers.length; i++ ) {
      category_markers[i].setMap(null);
    }
    category_markers = [];
}

// 클릭한 마커에 대한 장소 상세정보를 커스텀 오버레이로 표시하는 함수입니다
function displayPlaceInfo (place) {
    var content = '<div class="placeinfo">' +
                    '   <a class="title" href="' + place.place_url + '" target="_blank" title="' + place.place_name + '">' + place.place_name + '</a>';

    if (place.road_address_name) {
        content += '    <span title="' + place.road_address_name + '">' + place.road_address_name + '</span>' +
                    '  <span class="jibun" title="' + place.address_name + '">(지번 : ' + place.address_name + ')</span>';
    }  else {
        content += '    <span title="' + place.address_name + '">' + place.address_name + '</span>';
    }

    content += '    <span class="tel">' + place.phone + '</span>' +
                '</div>' +
                '<div class="after"></div>';

    contentNode.innerHTML = content;
    placeOverlay.setPosition(new kakao.maps.LatLng(place.y, place.x));
    placeOverlay.setMap(map);
}


// 각 카테고리에 클릭 이벤트를 등록합니다
function addCategoryClickEvent() {
    var category = document.getElementById('category'),
        children = category.children;

    for (var i=0; i<children.length; i++) {
        children[i].onclick = onClickCategory;
    }
}

// 카테고리를 클릭했을 때 호출되는 함수입니다
function onClickCategory() {
    var id = this.id,
        className = this.className;

    placeOverlay.setMap(null);

    if (className === 'on') {
        currCategory = '';
        changeCategoryClass();
        removeMarker();
    } else {
        currCategory = id;
        changeCategoryClass(this);
        searchPlaces();
    }
}

// 클릭된 카테고리에만 클릭된 스타일을 적용하는 함수입니다
function changeCategoryClass(el) {
    var category = document.getElementById('category'),
        children = category.children,
        i;

    for ( i=0; i<children.length; i++ ) {
        children[i].className = '';
    }

    if (el) {
        el.className = 'on';
    }
}
//////////////////////////CATEGORY/////////////////////////////////////////////////////
</script>

<template>
  <div id="map"></div>
  <ul id="category">
    <li id="BK9" data-order="0">
        <!-- <span class="category_bg bank"></span> -->
        <img src="https://map.pstatic.net/resource/api/v2/image/maps/around-category/bank_category_pc.png?version=21" alt="">
        은행
    </li>
    <li id="MT1" data-order="1">
        <!-- <span class="category_bg mart"></span> -->
        <img src="https://map.pstatic.net/resource/api/v2/image/maps/around-category/mart_category_pc.png?version=21" alt="">
        마트
    </li>
    <li id="PM9" data-order="2">
        <!-- <span class="category_bg pharmacy"></span> -->
        <img src="https://map.pstatic.net/resource/api/v2/image/maps/around-category/pharmacy_category_pc.png?version=21" alt="">
        약국
    </li>
    <li id="OL7" data-order="3">
        <!-- <span class="category_bg oil"></span> -->
        <img src="https://map.pstatic.net/resource/api/v2/image/maps/around-category/oil_category_pc.png?version=21" alt="">
        주유소
    </li>
    <li id="CE7" data-order="4">
        <!-- <span class="category_bg cafe"></span> -->
        <img src="https://map.pstatic.net/resource/api/v2/image/maps/around-category/cafe_category_pc.png?version=21" alt="">
        카페
    </li>
    <li id="CS2" data-order="5">
        <!-- <span class="category_bg store"></span> -->
        <img src="https://map.pstatic.net/resource/api/v2/image/maps/around-category/store_category_pc.png?version=21" alt="">
        편의점
    </li>
  </ul>
  <div class="map-type">
    <button id="btnUseDistrict" class="map-btn" @click="toggleMapType('useDistrict')">지적편집도</button>
    <button id="btnTerrain" class="map-btn" @click="toggleMapType('terrain')">지형정보</button>
  </div>
</template>

<style>
#map {
  width: 100%;
  height: 100%;
}
ul#category {
  padding: 0;
}
#category {position:absolute;min-width: fit-content; top:10px;right:20px;border-radius: 5px; gap: 20px; overflow: hidden;z-index: 2;}
#category li {float:left;list-style: none;background-color: white;box-shadow: 2px 1px 4px rgba(0, 0, 0, 0.25); width:85px;height:35px; border-radius:15px; border:0.5px solid #acacac;padding:6px 0; margin: 6px;text-align: center; cursor: pointer;}
#category li.on {color: var(--figma-blue);}
#category li:hover {background: #e6effb;border-left:1px solid #acacac;}
#category li:last-child{margin-right:0;border-right:0;}
#category li:first-child{margin-left:0;border-left:0;}
#category li span {display: block;margin:0 auto 3px;width:27px;height: 28px;}
#category li .category_bg {background:url(https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/places_category.png) no-repeat;}
#category li .bank {background-position: -10px 0px;}
#category li .mart {background-position: -10px -36px;}
#category li .pharmacy {background-position: -10px -72px;}
#category li .oil {background-position: -10px -108px;}
#category li .cafe {background-position: -10px -144px;}
#category li .store {background-position: -10px -180px;}
#category li.on .category_bg {background-position-x:-46px;}
.placeinfo_wrap {position:absolute;bottom:28px;left:-150px;width:300px;}
.placeinfo {position:relative;width:100%;border-radius:6px;border: 1px solid #ccc;border-bottom:2px solid #ddd;padding-bottom: 10px;background: #fff;}
.placeinfo:nth-of-type(n) {border:0; box-shadow:0px 1px 2px #888;}
.placeinfo_wrap .after {content:'';position:relative;margin-left:-12px;left:50%;width:22px;height:12px;background:url('https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/vertex_white.png')}
.placeinfo a, .placeinfo a:hover, .placeinfo a:active{color:#fff;text-decoration: none;}
.placeinfo a, .placeinfo span {display: block;text-overflow: ellipsis;overflow: hidden;white-space: nowrap;}
.placeinfo span {margin:5px 5px 0 5px;cursor: default;font-size:13px;}
.placeinfo .title {font-weight: bold; font-size:14px;border-radius: 6px 6px 0 0;margin: -1px -1px 0 -1px;padding:10px; color: #fff;background: #d95050;background: #d95050 url(https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/arrow_white.png) no-repeat right 14px center;}
.placeinfo .tel {color:#0f7833;}
.placeinfo .jibun {color:#999;font-size:11px;margin-top:0;}

.custom-info-window {
    padding: 2px;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    color: var(--eerie-black);
}

.map-type {
  display: flex;
  position:absolute;
  bottom:10px;
  right:20px;
  gap: 10px;
  z-index: 2;
}
.map-btn {
  padding: 10px 15px;
  border: 1px solid #ccc;
  background-color: #f9f9f9;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
}
.map-btn.active {
  background-color: #a3a3a3;
  color: #fff;
}
.map-btn:hover {
  background-color: #a3a3a3;
  color: #fff;
}
</style>
