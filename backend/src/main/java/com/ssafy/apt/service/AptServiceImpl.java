package com.ssafy.apt.service;

import com.ssafy.apt.dto.HouseInfoDto;
import com.ssafy.apt.mapper.AptMapper;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.HouseDeals;
import com.ssafy.apt.vo.HouseInfos;
import com.ssafy.util.TrieSearch;
import com.ssafy.util.TrieSearchApt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AptServiceImpl implements AptService {

    private final AptMapper mapper;

    @Override
    public List<HouseInfos> getAptInfoByArea(String dongCode) {
        return mapper.getAptInfoByArea(dongCode);
    }
    public List<HouseInfoDto> getAptInfoByAreaWithDeals(String location) {
        if(location.length() == 10){
            return mapper.getAptInfoByAreaWithDeals(location);
        } else if(location.length() == 5){
            return mapper.getAptInfoByAreaWithDealsBySggAndSido(location);
        } else{
            return mapper.getAptInfoByAreaWithDealsBySido(location);
        }

    }

    @Override
    public List<HouseInfos> getAptInfoByName(String keyword) {
        return mapper.getAptInfoByName(keyword);
    }
    public List<HouseInfoDto> getAptInfoByNameWithDeals(String keyword) {
        return mapper.getAptInfoByNameWithDeals(keyword);
    }

    @Override
    public List<HouseDeals> getAptDealByAptNM(String aptSeq) {
        return mapper.getAptDealByAptNM(aptSeq);
    }

    @Override
    public DongCode findDongCodeByName(String dongName) {
        return mapper.findDongCodeByName(dongName);
    }

    @Override
    public DongCode getDongCodeByDetails(String sidoName, String gugunName, String dongName) {
        Map<String, String> params = new HashMap<>();
        params.put("sidoName", sidoName);
        params.put("gugunName", gugunName);
        params.put("dongName", dongName);

        return mapper.findDongCodeByDetails(params);
    }

    // @PostConstruct ensures this method is called after the bean is initialized
    //@PostConstruct

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        initializeTrieSearch();
        initializeTrieApt();
    }

    public void initializeTrieSearch() {
        // Fetch data from the database using AptMapper
        List<DongCode> dongCodes = mapper.findAllDongCodes();
        if (dongCodes == null || dongCodes.isEmpty()) {
            log.debug("Dong codes not found!");
            return;
        }
        log.info("findAllDongCodes Size: "+dongCodes.size());
        log.info("findAllDongCodes 3: "+dongCodes.get(3).toString());
        log.info("initializeTrieSearch......");
        // Insert data into TrieSearch
        for (DongCode dong : dongCodes) {
            if (dong != null) {
                TrieSearch.insert(dong.getDongCd(), dong.getSidoName() + " " + dong.getGugunName() + " " + dong.getDongName());
            }
        }
        log.info("initializeTrieSearch done!!");

    }

    public void initializeTrieApt() {
        List<String> apartmentNames = mapper.getAllApartmentNames();
        log.info("findAllDongCodes Size11111: "+apartmentNames.size());
        log.info("initializeTrieSearch11111......");
        for (String aptName : apartmentNames) {
            TrieSearchApt.insert(aptName);
        }
        log.info("initializeTrieSearch done1232131231!!");
    }

    @Override
    public List<String> searchApt(String keyword) {
        return TrieSearch.search(keyword);
    }

    @Override
    public List<String> searchAptNm(String keyword) {
        return TrieSearchApt.search(keyword);
    }

    @Override
    public String findRoute(String userLatitude, String userLongitude, String destLatitude, String destLongitude) throws Exception {
        System.out.println("출발지 좌표: 위도(" + userLatitude + "), 경도(" + userLongitude + ")");
        System.out.println("목적지 좌표: 위도(" + destLatitude + "), 경도(" + destLongitude + ")");

        // Kakao Mobility API 호출 URL
        String apiKey = "bef70af12e75fe9c6b2c17b647859897";  // Kakao Mobility API Key
        String url = "https://apis-navi.kakaomobility.com/v1/directions?origin=" + userLongitude + "," + userLatitude +
                "&destination=" + destLongitude + "," + destLatitude +
                "&priority=RECOMMEND"; // 경로 우선순위: 추천 경로

        // URL 연결
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "KakaoAK " + apiKey);

        // 응답 코드 확인
        int responseCode = connection.getResponseCode();
        System.out.println("API 응답 코드: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder responseBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();

            // JSON 파싱
            String response = responseBuilder.toString();
            System.out.println("API 응답 결과: " + response);

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray routes = jsonResponse.getJSONArray("routes");

            if (routes.length() > 0) {
                JSONArray sections = routes.getJSONObject(0).getJSONArray("sections");
                JSONArray path = new JSONArray();

                // 경로의 각 섹션에서 vertex(좌표) 추출
                for (int i = 0; i < sections.length(); i++) {
                    JSONObject section = sections.getJSONObject(i);
                    JSONArray roads = section.getJSONArray("roads");
                    for (int j = 0; j < roads.length(); j++) {
                        JSONArray vertexes = roads.getJSONObject(j).getJSONArray("vertexes");
                        for (int k = 0; k < vertexes.length(); k += 2) {
                            double lng = vertexes.getDouble(k);
                            double lat = vertexes.getDouble(k + 1);
                            JSONObject point = new JSONObject();
                            point.put("lat", lat);
                            point.put("lng", lng);
                            path.put(point);
                        }
                    }
                }

                // 경로 데이터 반환
                return path.toString();
            } else {
                throw new Exception("경로를 찾을 수 없습니다.");
            }
        } else {
            throw new Exception("Kakao Mobility API 호출 실패: 응답 코드 " + responseCode);
        }
    }


    @Override
    public String getCoordinates(String query) throws Exception {
        System.out.println("주소 또는 키워드 검색: " + query);

        // Kakao Local API 호출 URL (키워드 검색)
        String apiKey = "bef70af12e75fe9c6b2c17b647859897";
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + URLEncoder.encode(query, "UTF-8");

        // URL 연결
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "KakaoAK " + apiKey);

        // 응답 코드 확인
        int responseCode = connection.getResponseCode();
        System.out.println("API 응답 코드: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 응답 데이터 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder responseBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();

            // JSON 파싱
            String response = responseBuilder.toString();
            System.out.println("API 응답 결과: " + response);

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray documents = jsonResponse.getJSONArray("documents");
            if (documents.length() > 0) {
                JSONObject firstResult = documents.getJSONObject(0);
                double lat = firstResult.getDouble("y"); // 위도
                double lng = firstResult.getDouble("x"); // 경도

                System.out.println("변환된 위경도: 위도(" + lat + "), 경도(" + lng + ")");
                return "{\"lat\": " + lat + ", \"lng\": " + lng + "}";
            }
            throw new Exception("검색 결과가 없습니다.");
        } else {
            throw new Exception("API 호출 실패: HTTP 응답 코드 " + responseCode);
        }
    }

}
