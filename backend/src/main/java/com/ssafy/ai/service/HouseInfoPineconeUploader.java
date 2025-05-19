package com.ssafy.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.apt.dto.HouseInfoDto;
import com.ssafy.apt.mapper.AptMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class HouseInfoPineconeUploader {

    @Value("${spring.ai.openai.api-key}")
    private String OPENAI_API_KEY;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    @Value("${pinecone.api-key}")
    private String PINECONE_API_KEY;

    @Value("${pinecone.api-url}")
    private String PINECONE_API_URL;

    private final AptMapper mapper;

    @Autowired
    public HouseInfoPineconeUploader(AptMapper mapper) {
        this.mapper = mapper;
    }

    public void uploadAllHouseInfosToPinecone() {
        try {
            // HouseInfos 데이터를 DB에서 가져오기
            List<HouseInfoDto> houseInfos = mapper.getAllHouseInfosWithDeals();
            if (houseInfos == null || houseInfos.isEmpty()) {
                log.warn("No house info data available for upload.");
                return;
            }

            // 입력 텍스트 생성 및 매핑 저장
            List<String> inputTexts = new ArrayList<>();
            Map<String, HouseInfoDto> houseInfoMap = new HashMap<>();
            for (HouseInfoDto houseInfo : houseInfos) {
                String inputText = buildInputText(houseInfo);
                inputTexts.add(inputText);
                houseInfoMap.put(inputText, houseInfo); // 입력 텍스트를 키로 매핑
            }

            // 배치 크기 설정
            int batchSize = 100; // 요청당 처리할 데이터 수
            int totalBatches = (int) Math.ceil((double) inputTexts.size() / batchSize);

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<List<String>>> futureResults = new ArrayList<>();
            List<String> failedIds = new CopyOnWriteArrayList<>();

            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                int start = batchIndex * batchSize;
                int end = Math.min(start + batchSize, inputTexts.size());
                List<String> batchTexts = inputTexts.subList(start, end);
                List<HouseInfoDto> batchHouseInfos = houseInfos.subList(start, end);

                // 병렬 처리 제출
                futureResults.add(executor.submit(() -> processBatch(batchTexts, batchHouseInfos, houseInfoMap, failedIds)));
            }

            // 결과 처리
            for (Future<List<String>> future : futureResults) {
                try {
                    failedIds.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error processing batch: {}", e.getMessage(), e);
                }
            }

            // 실패한 ID 기록
            if (!failedIds.isEmpty()) {
                recordFailedIds(failedIds);
            }

            executor.shutdown();
            log.info("HouseInfos data successfully uploaded to Pinecone. Failed IDs logged: {}", failedIds.size());

        } catch (Exception e) {
            log.error("Error uploading HouseInfos data to Pinecone: {}", e.getMessage(), e);
        }
    }

    private List<String> processBatch(List<String> batchTexts, List<HouseInfoDto> batchHouseInfos,
                                      Map<String, HouseInfoDto> houseInfoMap, List<String> failedIds) {
        List<String> failedBatchIds = new ArrayList<>();
        try {
            // 배치 임베딩 생성
            List<float[]> embeddings = getEmbeddings(batchTexts);
            if (embeddings == null || embeddings.size() != batchTexts.size()) {
                log.error("Mismatch between input texts and generated embeddings for batch.");
                for (HouseInfoDto houseInfo : batchHouseInfos) {
                    failedBatchIds.add(houseInfo.getAptSeq());
                }
                return failedBatchIds;
            }

            RestTemplate restTemplate = new RestTemplate();

            // Pinecone 업로드
            for (int i = 0; i < batchTexts.size(); i++) {
                String inputText = batchTexts.get(i);
                float[] embedding = embeddings.get(i);
                HouseInfoDto houseInfo = houseInfoMap.get(inputText);
                try {
                    uploadHouseInfoToPinecone(houseInfo.getAptSeq(), embedding, houseInfo, restTemplate);
                } catch (Exception e) {
                    log.error("Failed to upload HouseInfo ID: {}", houseInfo.getAptSeq(), e);
                    failedBatchIds.add(houseInfo.getAptSeq());
                }
            }
        } catch (Exception e) {
            log.error("Error processing batch: {}", e.getMessage(), e);
            for (HouseInfoDto houseInfo : batchHouseInfos) {
                failedBatchIds.add(houseInfo.getAptSeq());
            }
        }
        return failedBatchIds;
    }

    private void recordFailedIds(List<String> failedIds) {
        try (FileWriter writer = new FileWriter("failed_ids.log", true)) {
            for (String id : failedIds) {
                writer.write(id + "\n");
            }
            log.info("Failed IDs written to failed_ids.log");
        } catch (IOException e) {
            log.error("Failed to write failed IDs to log file.", e);
        }
    }

    private String buildInputText(HouseInfoDto houseInfo) {
        return String.format(
                "%s, %s, %s, %s, %s",
                houseInfo.getAptNm(),                     // 아파트 이름
                houseInfo.getRoadNm(),                   // 도로명
                houseInfo.getBuildYear(),                // 건축 연도
                houseInfo.getRecentDealAmount(),         // 최근 거래 금액
                houseInfo.getExclusiveArea()            // 평수 (전용면적)
        );
    }

    private List<float[]> getEmbeddings(List<String> inputTexts) {
        RestTemplate restTemplate = new RestTemplate();

        String requestBody = "{"
                + "\"input\": " + new ObjectMapper().valueToTree(inputTexts) + ","
                + "\"model\": \"text-embedding-ada-002\""
                + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_API_URL, HttpMethod.POST, entity, String.class);

            return parseEmbeddingsFromResponse(response.getBody());

        } catch (Exception e) {
            log.error("Error generating embeddings: {}", e.getMessage());
            return null;
        }
    }

    private List<float[]> parseEmbeddingsFromResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            List<float[]> embeddings = new ArrayList<>();
            JsonNode dataArray = rootNode.path("data");

            for (JsonNode dataNode : dataArray) {
                JsonNode embeddingNode = dataNode.path("embedding");
                float[] embedding = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    embedding[i] = embeddingNode.get(i).floatValue();
                }
                embeddings.add(embedding);
            }

            return embeddings;

        } catch (Exception e) {
            log.error("Error parsing embeddings from response: {}", e.getMessage());
            return null;
        }
    }

    private void uploadHouseInfoToPinecone(String id, float[] embedding, HouseInfoDto houseInfo, RestTemplate restTemplate) {
        String url = PINECONE_API_URL + "/vectors/upsert";

        // 메타데이터 추가
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("aptSeq", houseInfo.getAptSeq());
        metadata.put("aptNm", houseInfo.getAptNm());
        metadata.put("dongCd", houseInfo.getDongCd());
        metadata.put("roadNm", houseInfo.getRoadNm());
        metadata.put("buildYear", houseInfo.getBuildYear());
        metadata.put("recentDealAmount", houseInfo.getRecentDealAmount());
        metadata.put("exclusiveArea", houseInfo.getExclusiveArea());
        metadata.put("recentFloor", houseInfo.getRecentFloor());
        metadata.put("recentDealDate", houseInfo.getRecentDealDate());
        metadata.put("latitude", houseInfo.getLatitude());
        metadata.put("longitude", houseInfo.getLongitude());

        // 벡터 데이터 생성
        Map<String, Object> vector = new HashMap<>();
        vector.put("id", id);
        vector.put("values", embedding);
        vector.put("metadata", metadata);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vectors", Collections.singletonList(vector));
        requestBody.put("namespace", "houseinfo"); // 아파트 정보 네임스페이스 지정

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", PINECONE_API_KEY);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Successfully uploaded vector for HouseInfo ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to upload vector for HouseInfo ID: {}: {}", id, e.getMessage());
        }
    }

}