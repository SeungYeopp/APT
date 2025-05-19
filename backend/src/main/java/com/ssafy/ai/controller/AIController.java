package com.ssafy.ai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ai.dto.ChatHistory;
import com.ssafy.ai.dto.ChatHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.ssafy.util.PromptTemplateLoader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "AI 컨트롤러", description = "chatGPT를 사용해보자!")
@RequestMapping("/ai")
public class AIController {

    private final ChatModel chatModel;
    private final PromptTemplateLoader promptLoader;
    private final ChatHistoryRepository chatHistoryRepository;

    @Value("${spring.ai.openai.api-key}")
    private String OPENAI_API_KEY;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    @Value("${pinecone.api-key}")
    private String PINECONE_API_KEY;


    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> requestBody) {
        try {
            String userId = requestBody.get("userId");
            String userMessage = requestBody.get("userMessage");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID is required"));
            }

            // DB에서 대화 히스토리 가져오기
            List<ChatHistory> chatHistoryList = chatHistoryRepository.findByUserId(userId);
            List<Map<String, String>> dialogueHistory = convertChatHistoryToDialogueHistory(chatHistoryList);

            // 초기 메시지 추가 (없을 경우)
            if (dialogueHistory.isEmpty()) {
                ChatHistory initialChatHistory = new ChatHistory();
                initialChatHistory.setUserId(userId);
                initialChatHistory.setRole("system");
                initialChatHistory.setContent("You are a helpful assistant for real estate recommendations.");
                chatHistoryRepository.save(initialChatHistory);
            }

            // 사용자 메시지 저장
            if (userMessage != null && !userMessage.trim().isEmpty()) {
                ChatHistory userChatHistory = new ChatHistory();
                userChatHistory.setUserId(userId);
                userChatHistory.setRole("user");
                userChatHistory.setContent(userMessage);
                chatHistoryRepository.save(userChatHistory);

                Map<String, String> userMessageMap = Map.of("role", "user", "content", userMessage);
                dialogueHistory.add(userMessageMap);

                // GPT 프롬프트 생성 및 호출
                String prompt = generateDynamicPrompt(dialogueHistory);
                String gptResponse = chatModel.call(new UserMessage(prompt));

                // GPT 응답 저장
                ChatHistory assistantChatHistory = new ChatHistory();
                assistantChatHistory.setUserId(userId);
                assistantChatHistory.setRole("assistant");
                assistantChatHistory.setContent(gptResponse);
                chatHistoryRepository.save(assistantChatHistory);

                // 추천 타이밍 확인 및 처리
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("message", gptResponse);

                if (isNeighborhoodRecommendationTiming(gptResponse)) {
                    // 추천 동네 키워드 추출
                    List<String> neighborhoods = extractNeighborhoodsFromResponse(gptResponse);
                    log.info("Extracted neighborhoods: {}", neighborhoods);

                    // 각 동네 키워드로 벡터 검색 실행
                    List<Map<String, String>> similarDongResults = new ArrayList<>();
                    for (String neighborhood : neighborhoods) {
                        similarDongResults.addAll(fetchSimilarDongData(neighborhood));
                    }

                    // 추천 동네 JSON 생성
                    String recommendationJson = generateRecommendationWithNeighborhoods(dialogueHistory, similarDongResults);
                    List<Map<String, String>> recommendations = parseRecommendations(recommendationJson);

                    // 응답에 동네 추천 결과 포함
                    responseBody.put("neighborhoodRecommendations", recommendations);
                    log.info("Final Neighborhood Recommendations: {}", recommendations);

                } else if (isApartmentRecommendationTiming(gptResponse)) {
                    // 추천 아파트 키워드 추출
                    List<String> apartments = extractSearchContextFromResponse(gptResponse);
                    log.info("Extracted APT keywords: {}", apartments);

                    // 각 아파트 키워드로 벡터 검색 실행
                    List<String> similarApartmentDescriptions = new ArrayList<>();
                    for (String apartment : apartments) {
                        // 각 아파트 데이터를 검색하고 설명 생성
                        List<String> fetchedDescriptions = fetchSimilarHouseInfoData(apartment);
                        similarApartmentDescriptions.addAll(fetchedDescriptions);
                    }
                    log.info("similarApartmentDescriptions: {}", similarApartmentDescriptions);

                    // 추천 아파트 JSON 생성
                    String recommendationJson = generateRecommendationWithApartments(dialogueHistory, similarApartmentDescriptions);
                    log.info("recommendationJson: {}", recommendationJson);
                    List<Map<String, String>> recommendations = parseAPTRecommendations(recommendationJson);

                    // 응답에 아파트 추천 결과 포함
                    responseBody.put("apartmentRecommendations", recommendations);
                    log.info("Final Apartment Recommendations: {}", recommendations);

                } else {
                    // 추천 없음
                    responseBody.put("neighborhoodRecommendations", null);
                    responseBody.put("apartmentRecommendations", null);
                }


                // 대화 히스토리 반환
                responseBody.put("dialogueHistory", dialogueHistory);
                return ResponseEntity.ok(responseBody);
            }

            // 대화 히스토리만 반환
            return ResponseEntity.ok(Map.of("dialogueHistory", dialogueHistory));
        } catch (Exception e) {
            log.error("Error occurred in /chat endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private List<Map<String, String>> convertChatHistoryToDialogueHistory(List<ChatHistory> chatHistoryList) {
        return chatHistoryList.stream()
                .filter(chat -> !"system".equals(chat.getRole())) // system 메시지 제외
                .map(chat -> Map.of(
                        "role", chat.getRole(),
                        "content", chat.getContent()
                ))
                .collect(Collectors.toList());
    }

    private String generateDynamicPrompt(List<Map<String, String>> dialogueHistory) {
        // 대화 히스토리를 기반으로 동적 프롬프트 생성
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("<dialogue history>\n");
        for (Map<String, String> message : dialogueHistory) {
            promptBuilder.append(message.get("role")).append(": ").append(message.get("content")).append("\n");
        }
        promptBuilder.append(
                """
                     <instruction>
                     위 대화를 바탕으로 사용자의 요구를 분석하고 적절히 응답하세요.
                        - 사용자가 아파트 추천을 요청하거나 관련 키워드(예: 평수, 가격, 학군, 교통 등)가 포함된 경우 이를 바탕으로 아파트를 추천하세요.
                        - 아파트 추천은 다음 형식으로 응답하세요: "추천 아파트: [검색 문맥]. [이 문맥을 추천한 이유.]"
                        - 검색 문맥은 Pinecone 임베딩 검색에 적합한 단어와 문장을 포함해야 하며, 다음과 같은 요소를 고려하여 생성하세요:
                            - 사용자가 요구한 평수 (예: "30평대", "100㎡ 이상")
                            - 예상 가격 범위 (예: "10억~15억")
                        - 사용자가 동네 추천을 요청하거나 관련 키워드(예: 지역, 학군, 교통, 상업지구 등)가 포함된 경우 이를 바탕으로 동네를 추천하세요.
                        - 동네 추천은 다음 형식으로 응답하세요: "추천 동네: [동네 이름]. [이 동네의 장점과 특징에 대한 간단한 설명.]"
                        - 사용자의 요청이 모호하면 현재 대화를 바탕으로 추가로 물어볼 질문을 생성하세요.
        
                     응답을 작성할 때 다음 규칙을 따르세요:
                      1. 가독성을 위해 적절한 html 태그를 사용하세요.
                      2. 줄바꿈으로 비어있는 문장을 사용하지 마세요.
                      3. 응답은 자연스럽고 친근하게 작성하며, 불필요한 공백 없이 간결하게 표현하세요.
                     </instruction>"""
        );
        return promptBuilder.toString();
    }

    private boolean isNeighborhoodRecommendationTiming(String gptResponse) {
        // GPT 응답에서 "추천 동네:" 키워드가 포함되어 있는지 확인
        return gptResponse.toLowerCase().contains("추천 동네:");
    }

    private boolean isApartmentRecommendationTiming(String gptResponse) {
        return gptResponse.contains("추천 아파트:");
    }

    private List<String> extractNeighborhoodsFromResponse(String gptResponse) {
        List<String> neighborhoods = new ArrayList<>();

        // 정규식: "추천 동네" 뒤에 나오는 텍스트를 매칭
        Pattern pattern = Pattern.compile("추천 동네[:\\s]*<strong>(.*?)</strong>|추천 동네[:\\s]*(.*?)(\\.|\\n|$)");
        Matcher matcher = pattern.matcher(gptResponse);

        while (matcher.find()) {
            // 매칭된 그룹에서 동네 이름 추출
            String matchedText = matcher.group(1).trim();
            // 동네 이름을 ','로 분리하여 리스트에 추가
            String[] splits = matchedText.split(",");
            for (String neighborhood : splits) {
                neighborhoods.add(neighborhood.trim());
            }
        }

        return neighborhoods;
    }

    private List<String> extractSearchContextFromResponse(String gptResponse) {
        List<String> searchContexts = new ArrayList<>();

        // 정규식: "추천 아파트" 뒤에 나오는 검색 문맥 추출
        Pattern pattern = Pattern.compile("추천 아파트[:\\s]*<strong>(.*?)</strong>|추천 아파트[:\\s]*(.*?)(\\.|\\n|$)");
        Matcher matcher = pattern.matcher(gptResponse);

        while (matcher.find()) {
            // 매칭된 그룹에서 검색 문맥 추출
            String matchedText = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (matchedText != null) {
                // 문맥을 ','로 분리하여 리스트에 추가
                String[] splits = matchedText.split(",");
                for (String context : splits) {
                    searchContexts.add(context.trim());
                }
            }
        }

        // 위치, 평수, 가격 범위와 같은 키워드만 포함되도록 필터링
        return searchContexts;
    }

    private String generateRecommendationWithNeighborhoods(List<Map<String, String>> dialogueHistory, List<Map<String, String>> similarDongResults) {
        try {
            StringBuilder recommendationPrompt = new StringBuilder();
            recommendationPrompt.append("<dialogue history>\n");
            for (Map<String, String> message : dialogueHistory) {
                recommendationPrompt.append(message.get("role")).append(": ").append(message.get("content")).append("\n");
            }

            recommendationPrompt.append(
                    """
                    <instruction>
                    다음의 대화 내용을 바탕으로 사용자의 요구에 가장 적합한 동네를 JSON 형식으로 추천하세요.
                    - JSON 형식으로만 작성하세요. JSON 외의 텍스트는 포함하지 마세요.
                    - 출력 형식:
                      {
                        "recommendations": [
                          {"sido_name": "시도 이름", "gugun_name": "구군 이름", "dong_name": "동 이름", "reason": "추천 이유"},
                          ...
                        ]
                      }
                    - sido_name, gugun_name, dong_name, reason을 반드시 포함해야 합니다.
                    - 아래는 사용자의 요구와 관련된 유사한 동네 데이터입니다:
                    """
            );

            // 유사한 동네 데이터 추가
            recommendationPrompt.append("\n[동네 데이터베이스]\n");
            for (Map<String, String> dongData : similarDongResults) {
                recommendationPrompt.append(
                        String.format("- %s\n", dongData.get("text"))
                );
            }

            recommendationPrompt.append(
                    """
                    
                    <주의사항>
                    - 반드시 추천 이유를 포함하세요.
                    - 사용자 요구와 유사한 동네 중에서 최적의 동네를 선택하세요.
                    - JSON 형식 오류가 있으면 유효하지 않은 결과로 간주됩니다.
                
                    아래는 예시입니다:
                    {
                      "recommendations": [
                        {"sido_name": "서울특별시", "gugun_name": "강남구", "dong_name": "압구정동", "reason": "고급 주택가와 한강 접근성으로 유명합니다."},
                        {"sido_name": "서울특별시", "gugun_name": "송파구", "dong_name": "잠실동", "reason": "조용한 환경과 학군이 우수합니다."},
                        {"sido_name": "부산광역시", "gugun_name": "해운대구", "dong_name": "중동", "reason": "해운대 해수욕장과 인접하여 휴양과 생활이 모두 가능합니다."}
                      ]
                    }
                    </주의사항>
                    </instruction>
                    """
            );

            log.info("Generated Dong Prompt: {}", recommendationPrompt);

            // AI 호출
            return chatModel.call(new UserMessage(recommendationPrompt.toString()));

        } catch (Exception e) {
            log.error("Error generating recommendation: {}", e.getMessage(), e);
            return "{\"recommendations\": []}";
        }
    }

    private String generateRecommendationWithApartments(List<Map<String, String>> dialogueHistory, List<String> similarApartmentDescriptions) {
        try {
            StringBuilder recommendationPrompt = new StringBuilder();
            recommendationPrompt.append("<dialogue history>\n");
            for (Map<String, String> message : dialogueHistory) {
                recommendationPrompt.append(message.get("role")).append(": ").append(message.get("content")).append("\n");
            }

            recommendationPrompt.append(
                    """
                    <instruction>
                    다음의 대화 내용을 바탕으로 사용자의 요구에 가장 적합한 아파트를 JSON 형식으로 추천하세요.
                    - JSON 형식으로만 작성하세요. JSON 외의 텍스트는 포함하지 마세요.
                    - 출력 형식:
                      {
                        "recommendations": [
                          {"apt_seq": "아파트 ID", "apt_name": "아파트 이름", "location": "아파트 위치", "price_range": "가격 범위", "reason": "추천 이유"},
                          ...
                        ]
                      }
                    - apt_seq, apt_name, location, price_range, reason을 반드시 포함해야 합니다.
                    - 아래는 사용자의 요구와 관련된 아파트 데이터베이스입니다.:
                    """
            );

            // 유사한 아파트 데이터 추가
            recommendationPrompt.append("\n[아파트 데이터베이스]\n");
            for (String description : similarApartmentDescriptions) {
                recommendationPrompt.append("- ").append(description).append("\n");
            }

            recommendationPrompt.append(
                    """
                    
                    <주의사항>
                      - 반드시 추천 이유를 포함하세요.
                      - 반드시 데이터베이스에 있는 아파트 이름만 추천하세요.
                      - 사용자 요구에 맞는 최적의 아파트를 선택하세요.
                      - JSON 형식 오류가 있으면 유효하지 않은 결과로 간주됩니다.
                      - 반드시 최소 한 개 이상 추천하세요. 하지만 데이터베이스에 사용자의 요구와 일치하는 아파트가 없을 경우 아래 메시지를 포함하세요:
                        {
                          "recommendations": [
                            "message": "현재 데이터베이스에 사용자의 요구와 일치하는 아파트 정보가 없습니다. 다른 지역이나 조건을 입력해 주시면 더 나은 추천을 드릴 수 있습니다."
                          ]
                        }
                    
                    아래는 예시입니다:
                    {
                      "recommendations": [
                        {"apt_seq": "11680-3621", "apt_name": "역삼래미안", "location": "서울특별시 강남구 역삼동", "price_range": "20억~25억", "reason": "학군이 우수하고 교통이 편리합니다."},
                        {"apt_seq": "11650-3579", "apt_name": "서초교대e편한세상", "location": "서울특별시 서초구 서초동", "price_range": "20억~25억", "reason": "대규모 단지로 인프라가 뛰어납니다."},
                        {"apt_seq": "41135-137", "apt_name": "장미마을(동부)", "location": "경기도 성남시 분당구 야탑동", "price_range": "10억~15억", "reason": "가족 단위 거주에 적합합니다."},
                      ]
                    }
                    </주의사항>
                    </instruction>
                    """
            );

            log.info("Generated APT Prompt: {}", recommendationPrompt);

            // AI 호출
            return chatModel.call(new UserMessage(recommendationPrompt.toString()));

        } catch (Exception e) {
            log.error("Error generating apartment recommendation: {}", e.getMessage(), e);
            return "{\"recommendations\": []}";
        }
    }


    private List<Map<String, String>> fetchSimilarDongData(String searchKeyword) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            log.info("Fetching similar dong data for: {}", searchKeyword);

            // 벡터 검색 API 호출 URL
            String apiUrl = "http://localhost:8080/api/v1/search/dong?dongName=" + searchKeyword;

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            log.info("API response: {}", response.getBody());

            // 응답 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            // 응답이 비었는지 확인
            if (root.isEmpty()) {
                log.warn("Empty response for searchKeyword: {}", searchKeyword);
                return Collections.emptyList();
            }

            // 결과 리스트 생성
            List<Map<String, String>> results = new ArrayList<>();
            for (JsonNode result : root) {
                Map<String, String> dongData = new HashMap<>();

                // text 필드만 추가
                dongData.put("text", result.get("text").asText());

                results.add(dongData);
            }

            return results;


        } catch (Exception e) {
            log.error("Error fetching similar dong data: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<String> fetchSimilarHouseInfoData(String searchContext) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            log.info("Fetching similar dong data for: {}", searchContext);

            // 벡터 검색 API 호출 URL
            String apiUrl = "http://localhost:8080/api/v1/search/houseinfo?query=" + searchContext;

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            //log.info("API response: {}", response.getBody());

            // JSON 데이터 파싱 및 필터링
            List<String> results = parseAndFilterResults(response.getBody());
            //log.info("Filtered API response: {}", results);

            return results;

        } catch (Exception e) {
            log.error("Error fetching similar house info data: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<String> parseAndFilterResults(String jsonResponse) {
        List<String> results = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 응답 파싱
            JsonNode root = objectMapper.readTree(jsonResponse);

            // JSON 배열에서 각 항목 처리
            for (JsonNode match : root) {
                try {
                    // "text" 필드에서 메타데이터 추출
                    JsonNode metadataNode = match.path("text");
                    if (metadataNode.isNull() || metadataNode.isMissingNode() || metadataNode.asText().isEmpty()) {
                        continue;
                    }

                    // 메타데이터를 JSON 객체로 변환
                    JsonNode metadata = objectMapper.readTree(metadataNode.asText());

                    // 아파트 설명 생성
                    String aptDescription = generateAptDescription(metadata);
                    results.add(aptDescription);
                } catch (Exception e) {
                    log.warn("Skipping invalid metadata: {}", match, e);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing API response: {}", e.getMessage(), e);
        }

        return results;
    }



    private boolean isValidRecommendation(JsonNode metadata) {
        try {
            double exclusiveArea = metadata.path("exclusiveArea").asDouble(0);
            double recentDealAmount = Double.parseDouble(metadata.path("recentDealAmount").asText().replace(",", ""));
            String recentDealDate = metadata.path("recentDealDate").asText("");

            // 최소 평수: 30㎡, 최소 거래 금액: 30,000만원, 최신 거래: 2022년 이후
            return exclusiveArea > 30 && recentDealAmount > 30000 && recentDealDate.compareTo("2022-01-01") > 0;

        } catch (Exception e) {
            log.warn("Error validating metadata: {}", metadata, e);
            return false;
        }
    }

    private String generateAptDescription(JsonNode metadata) {
        System.out.println(metadata);
        try {
            String aptName = metadata.path("aptNm").asText("N/A");
            String location = metadata.path("roadNm").asText("N/A");
            String buildYear = metadata.path("buildYear").asText("N/A");

            String exclusiveArea = metadata.path("exclusiveArea").asText("N/A");
            String formattedArea = convertToPyeong(exclusiveArea);

            // 최근 거래 금액을 원 단위로 변환
            String recentDealAmount = metadata.path("recentDealAmount").asText("N/A");
            String formattedDealAmount = formatDealAmount(recentDealAmount);

            String recentDealDate = metadata.path("recentDealDate").asText("N/A");
            String recentFloor = metadata.path("recentFloor").asText("N/A");

            return String.format(
                    "아파트 이름: %s, 위치: %s, 건축 연도: %s, 평수: %s, 최근 거래 금액: %s, 최근 거래 날짜: %s, 거래 층: %s층",
                    aptName, location, buildYear, formattedArea, formattedDealAmount, recentDealDate, recentFloor
            );
        } catch (Exception e) {
            log.error("Error generating apartment description: {}", e.getMessage(), e);
            return "데이터를 처리할 수 없습니다.";
        }
    }

    public static String convertToPyeong(String squareMeters) {
        try {
            // 문자열에서 숫자만 추출하여 Double로 변환
            double squareMeterValue = Double.parseDouble(squareMeters.replaceAll("[^\\d.]", ""));
            // 평수 계산 (1평 = 3.3㎡)
            int pyeong = (int) Math.round(squareMeterValue / 3.3);
            // "XX평대" 표현으로 변환
            return pyeong + "평대";
        } catch (NumberFormatException e) {
            // 변환 실패 시 기본 값 반환
            return "N/A";
        }
    }

    private String formatDealAmount(String dealAmount) {
        try {
            // 금액을 숫자로 변환
            long amountInMillion = Long.parseLong(dealAmount.replace(",", ""));
            long amountInWon = amountInMillion * 10000; // 만원 단위 -> 원 단위

            if (amountInWon >= 1_0000_0000) { // 1억 이상일 경우 "억" 단위 표현
                long billions = amountInWon / 1_0000_0000;
                long remainder = (amountInWon % 1_0000_0000) / 10000; // 나머지를 "만" 단위로 표현
                if (remainder > 0) {
                    return String.format("%d억 %d만 원", billions, remainder);
                } else {
                    return String.format("%d억 원", billions);
                }
            } else {
                return String.format("%d만 원", amountInMillion);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid deal amount format: {}", dealAmount, e);
            return "알 수 없음";
        }
    }

    public boolean isValidJSON(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(response);
            return true; // JSON이 유효함
        } catch (Exception e) {
            return false; // JSON 형식이 아님
        }
    }

    private List<Map<String, String>> parseRecommendations(String recommendationJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(recommendationJson);
            JsonNode recommendationsNode = rootNode.path("recommendations");

            List<Map<String, String>> recommendations = new ArrayList<>();
            if (recommendationsNode.isArray()) {
                for (JsonNode node : recommendationsNode) {
                    String sidoName = node.path("sido_name").asText("알 수 없음");
                    String gugunName = node.path("gugun_name").asText("알 수 없음");
                    String dongName = node.path("dong_name").asText("알 수 없음");
                    String reason = node.path("reason").asText("추천 이유 없음");

                    recommendations.add(Map.of(
                            "sido_name", sidoName,
                            "gugun_name", gugunName,
                            "dong_name", dongName,
                            "reason", reason
                    ));
                }
            }
            return recommendations;
        } catch (Exception e) {
            log.error("Error parsing recommendations JSON: {}", recommendationJson, e);
            return List.of(Map.of(
                    "sido_name", "알 수 없음",
                    "gugun_name", "알 수 없음",
                    "dong_name", "알 수 없음",
                    "reason", "추천 결과를 처리할 수 없습니다."
            ));
        }
    }

    private List<Map<String, String>> parseAPTRecommendations(String recommendationJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(recommendationJson);
            JsonNode recommendationsNode = rootNode.path("recommendations");

            List<Map<String, String>> recommendations = new ArrayList<>();
            if (recommendationsNode.isArray()) {
                for (JsonNode node : recommendationsNode) {
                    // 필드별 값 추출
                    String aptSeq = node.path("apt_seq").asText("알 수 없음");
                    String aptName = node.path("apt_name").asText("알 수 없음");
                    String location = node.path("location").asText("알 수 없음");
                    String priceRange = node.path("price_range").asText("알 수 없음");
                    String reason = node.path("reason").asText("추천 이유 없음");
                    String message = node.path("message").asText("메시지 없음");

                    // 추천 항목 추가
                    recommendations.add(Map.of(
                            "apt_seq", aptSeq,
                            "apt_name", aptName,
                            "location", location,
                            "price_range", priceRange,
                            "reason", reason,
                            "message", message
                    ));
                }
            }
            return recommendations;
        } catch (Exception e) {
            log.error("Error parsing recommendations JSON: {}", recommendationJson, e);
            // 에러 발생 시 기본 값 반환
            return List.of(Map.of(
                    "apt_seq", "알 수 없음",
                    "apt_name", "알 수 없음",
                    "location", "알 수 없음",
                    "price_range", "알 수 없음",
                    "reason", "추천 결과를 처리할 수 없습니다.",
                    "message", "메시지가 없습니다."
            ));
        }
    }


    /////////////////////////////////legacy///////////////////////////////////////
    @Operation(summary = "아파트 추천", description = "사용자 맞춤 아파트를 추천해보자")
    @Parameters({
            @Parameter(name = "city", description = "도시", required = true, in = ParameterIn.PATH, example = "서울"),
            @Parameter(name = "dong", description = "동네", required = true, in = ParameterIn.PATH, example = "화곡")
    })
    @GetMapping("/apt/{city}/{dong}")
    public ResponseEntity<String> getRecommend(@PathVariable("city") String city, @PathVariable("dong") String dong) {
        try {
            System.out.println(city+" "+dong);
            // 유저 프롬프트 템플릿 로드 및 변수 설정
            String userPromptTemplate = promptLoader.loadUserPrompt();
            PromptTemplate userTemplate = new PromptTemplate(userPromptTemplate);
            // city와 dong 변수를 모두 추가
            userTemplate.add("city", city);
            //userTemplate.add("dong", dong);
            String userCommand = userTemplate.render();

            // 시스템 프롬프트 로드
            String systemPromptTemplate = promptLoader.loadSystemPrompt();
            PromptTemplate systemTemplate = new PromptTemplate(systemPromptTemplate);
            systemTemplate.add("city", city);
            //systemTemplate.add("dong", dong);
            String systemCommand = systemTemplate.render();

            // 메시지 생성
            Message userMessage = new UserMessage(userCommand);
            Message systemMessage = new SystemMessage(systemCommand);

            // API 호출
            String response = chatModel.call(userMessage, systemMessage);
            System.out.println(response);
            log.info("Generated response for city: {}, dong: {}", city, dong);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing attraction request for city: " + city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }
}
