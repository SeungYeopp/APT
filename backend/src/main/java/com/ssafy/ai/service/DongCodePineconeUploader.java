package com.ssafy.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.apt.mapper.AptMapper;
import com.ssafy.apt.vo.DongCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@EnableAsync // 비동기 처리를 활성화
@RequiredArgsConstructor
public class DongCodePineconeUploader {

    @Value("${spring.ai.openai.api-key}")
    private String OPENAI_API_KEY;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    @Value("${pinecone.api-key}")
    private String PINECONE_API_KEY;

    @Value("${pinecone.api-url}")
    private String PINECONE_API_URL;

    private final AptMapper mapper;

    public void uploadAllDongCodesToPinecone() {
        try {
            // DB에서 모든 DongCode 데이터 가져오기
            List<DongCode> dongCodes = mapper.findAllDongCodes();
            if (dongCodes == null || dongCodes.isEmpty()) {
                log.warn("No DongCode data available for upload.");
                return;
            }

            // DongCode 리스트를 텍스트로 변환 및 매핑 저장
            List<String> inputTexts = new ArrayList<>();
            Map<String, DongCode> dongCodeMap = new HashMap<>();
            for (DongCode dongCode : dongCodes) {
                String inputText = dongCode.getSidoName() + " " +
                        dongCode.getGugunName() + " " +
                        dongCode.getDongName();
                inputTexts.add(inputText);
                dongCodeMap.put(inputText, dongCode); // 텍스트를 키로 매핑
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
                List<DongCode> batchDongCodes = dongCodes.subList(start, end);

                // 병렬 처리 제출
                futureResults.add(executor.submit(() -> processDongCodeBatch(batchTexts, batchDongCodes, dongCodeMap, failedIds)));
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
            log.info("DongCode data successfully uploaded to Pinecone. Failed IDs logged: {}", failedIds.size());

        } catch (Exception e) {
            log.error("Error uploading DongCode data to Pinecone: {}", e.getMessage(), e);
        }
    }

    private List<String> processDongCodeBatch(List<String> batchTexts, List<DongCode> batchDongCodes,
                                              Map<String, DongCode> dongCodeMap, List<String> failedIds) {
        List<String> failedBatchIds = new ArrayList<>();
        try {
            // 배치 임베딩 생성
            List<float[]> embeddings = getEmbeddings(batchTexts);
            if (embeddings == null || embeddings.size() != batchTexts.size()) {
                log.error("Mismatch between input texts and generated embeddings for batch.");
                for (DongCode dongCode : batchDongCodes) {
                    failedBatchIds.add(dongCode.getDongCd());
                }
                return failedBatchIds;
            }

            // Pinecone 업로드
            for (int i = 0; i < batchTexts.size(); i++) {
                String inputText = batchTexts.get(i);
                float[] embedding = embeddings.get(i);
                DongCode dongCode = dongCodeMap.get(inputText);
                try {
                    uploadToPinecone(dongCode.getDongCd(), embedding, dongCode);
                } catch (Exception e) {
                    log.error("Failed to upload DongCode ID: {}", dongCode.getDongCd(), e);
                    failedBatchIds.add(dongCode.getDongCd());
                }
            }
        } catch (Exception e) {
            log.error("Error processing batch: {}", e.getMessage(), e);
            for (DongCode dongCode : batchDongCodes) {
                failedBatchIds.add(dongCode.getDongCd());
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

//    public void retryFailedUploads() {
//        List<String> failedIds = readFailedIds();
//        if (failedIds.isEmpty()) {
//            System.out.println("No failed IDs to retry.");
//            return;
//        }
//
//        List<DongCode> failedDongCodes = mapper.findDongCodesByIds(failedIds);
//
//        List<String> inputTexts = new ArrayList<>();
//        for (DongCode dongCode : failedDongCodes) {
//            String inputText = dongCode.getDongCd() + " " +
//                    dongCode.getSidoName() + " " +
//                    dongCode.getGugunName() + " " +
//                    dongCode.getDongName();
//            inputTexts.add(inputText);
//        }
//
//        List<float[]> embeddings = getEmbeddings(inputTexts);
//
//        for (int i = 0; i < failedDongCodes.size(); i++) {
//            DongCode dongCode = failedDongCodes.get(i);
//            float[] embedding = embeddings.get(i);
//            try {
//                uploadToPinecone(dongCode.getDongCd(), embedding, dongCode);
//            } catch (Exception e) {
//                System.err.println("Retry failed for DongCode ID: " + dongCode.getDongCd());
//            }
//        }
//
//        System.out.println("Retry process completed.");
//    }
//
//    private List<String> readFailedIds() {
//        List<String> failedIds = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader("failed_ids.log"))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                failedIds.add(line.trim());
//            }
//        } catch (IOException e) {
//            System.err.println("Failed to read failed IDs from log file.");
//            e.printStackTrace();
//        }
//        return failedIds;
//    }


    // OpenAI API 호출로 배치 임베딩 생성
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
            e.printStackTrace();
            return null;
        }
    }

    // JSON 응답에서 임베딩 배열 리스트 추출
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
            e.printStackTrace();
            return null;
        }
    }

    // Pinecone에 벡터 업로드
    private void uploadToPinecone(String id, float[] embedding, DongCode dongCode) {
        RestTemplate restTemplate = new RestTemplate();
        String url = PINECONE_API_URL + "/vectors/upsert";

        // 메타데이터 추가
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sidoName", dongCode.getSidoName());
        metadata.put("gugunName", dongCode.getGugunName());
        metadata.put("dongName", dongCode.getDongName());

        // 벡터 데이터 생성
        Map<String, Object> vector = new HashMap<>();
        vector.put("id", id);
        vector.put("values", embedding);
        vector.put("metadata", metadata);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vectors", Collections.singletonList(vector));
        requestBody.put("namespace", "dongcode"); // 네임스페이스 지정

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", PINECONE_API_KEY);


        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            System.out.println("Uploaded vector for DongCode: " + id);
        } catch (Exception e) {
            log.error("Failed to upload vector for DongCode ID: {}: {}", id, e.getMessage());
        }
    }
}
