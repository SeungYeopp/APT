package com.ssafy.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PineconeService {

    @Value("${spring.ai.openai.api-key}")
    private String OPENAI_API_KEY;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    @Value("${pinecone.api-key}")
    private String PINECONE_API_KEY;

    @Value("${pinecone.api-url}")
    private String PINECONE_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    // 동 이름으로 Pinecone 검색
    public List<SearchResult> searchByDongName(String dongName) {
        try {
            // 동 이름으로 임베딩 벡터 생성
            float[] queryEmbedding = getEmbedding(dongName);

            // Pinecone에서 유사 벡터 검색
            return queryPinecone(queryEmbedding, 5);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 아파트 정보 검색
    public List<SearchResult> searchHouseInfo(String query) {
        try {
            // Query에 대한 임베딩 생성
            float[] queryEmbedding = getEmbedding(query);

            // Pinecone에서 유사한 HouseInfo 데이터 검색
            return queryHouseInfoPinecone(queryEmbedding, 5);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // OpenAI API 호출로 임베딩 생성
    private float[] getEmbedding(String inputText) {
        String requestBody = "{"
                + "\"input\": \"" + inputText + "\","
                + "\"model\": \"text-embedding-ada-002\""
                + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_API_URL, HttpMethod.POST, entity, String.class);

            return parseEmbeddingFromResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
    }

    // JSON 파싱하여 "embedding" 배열 추출
    private float[] parseEmbeddingFromResponse(String jsonResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // "data" 배열의 첫 번째 객체에서 "embedding" 배열 추출
        JsonNode embeddingNode = rootNode.path("data").get(0).path("embedding");

        float[] embedding = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            embedding[i] = embeddingNode.get(i).floatValue();
        }

        return embedding;
    }

    // Pinecone에서 동 정보 유사 벡터 검색
        public List<SearchResult> queryPinecone(float[] queryVector, int topK) {
        String url = PINECONE_API_URL + "/query";

        // 벡터를 JSON으로 변환
        StringBuilder vectorJson = new StringBuilder("[");
        for (float v : queryVector) {
            vectorJson.append(v).append(",");
        }
        vectorJson.deleteCharAt(vectorJson.length() - 1).append("]");

        // 요청 본문 작성
        String requestBody = "{"
                + "\"vector\": " + vectorJson + ","
                + "\"topK\": " + topK + ","
                + "\"namespace\": \"dongcode\","
                + "\"includeMetadata\": true"
                + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", PINECONE_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            return parseSearchResults(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 아파트 정보 검색
    private List<SearchResult> queryHouseInfoPinecone(float[] queryVector, int topK) {
        String url = PINECONE_API_URL + "/query";

        String requestBody = "{"
                + "\"vector\": " + Arrays.toString(queryVector) + ","
                + "\"topK\": " + topK + ","
                + "\"namespace\": \"houseinfo\","
                + "\"includeMetadata\": true"
                + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", PINECONE_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            return parseHouseInfoResults(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 검색 결과 파싱
    private List<SearchResult> parseSearchResults(String jsonResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        List<SearchResult> results = new ArrayList<>();
        JsonNode matches = rootNode.path("matches");

        for (JsonNode match : matches) {
            String id = match.path("id").asText();
            double score = match.path("score").asDouble();

            // 메타데이터에서 텍스트 추출
            JsonNode metadataNode = match.path("metadata");
            String sidoName = metadataNode.path("sidoName").asText();
            String gugunName = metadataNode.path("gugunName").asText();
            String dongName = metadataNode.path("dongName").asText();

            String text = sidoName + " " + gugunName + " " + dongName;

            results.add(new SearchResult(id, score, text));
        }

        return results;
    }

    private List<SearchResult> parseHouseInfoResults(String jsonResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        List<SearchResult> results = new ArrayList<>();
        JsonNode matches = rootNode.path("matches");

        for (JsonNode match : matches) {
            String id = match.path("id").asText();
            double score = match.path("score").asDouble();

            JsonNode metadataNode = match.path("metadata");
            String metadata = objectMapper.writeValueAsString(metadataNode); // 메타데이터를 문자열로 직렬화

            results.add(new SearchResult(id, score, metadata));
        }

        return results;
    }


    // 검색 결과 객체
    public static class SearchResult {
        private String id;        // Pinecone 벡터 ID
        private double score;     // 유사도 점수
        private String text;      // 메타데이터 기반 텍스트

        public SearchResult(String id, double score, String text) {
            this.id = id;
            this.score = score;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public double getScore() {
            return score;
        }

        public String getText() {
            return text;
        }
    }

    public void testSearch() {
        // 예제 벡터 (실제로는 사용자가 제공한 임베딩을 사용)
        float[] queryVector = {0.1f, 0.2f, 0.3f, 0.4f};

        // Pinecone에서 상위 5개 유사 벡터 검색
        List<SearchResult> results = queryPinecone(queryVector, 5);

        // 검색 결과 출력
        for (SearchResult result : results) {
            System.out.println("ID: " + result.getId());
            System.out.println("Score: " + result.getScore());
            System.out.println("Text: " + result.getText());
            System.out.println("-----");
        }
    }
}