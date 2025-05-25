<script>
import axios from "axios";
import MarkdownIt from "markdown-it";

import { getDongCodeByDetails } from "@/api/apt.js";

export default {
  name: "AIChatView",
  data() {
    return {
      typingIndicator: null, // AI 타이핑 인디케이터 메시지
      typingDots: "", // 점점점 애니메이션을 위한 변수
      typingInterval: null, // 타이핑 애니메이션을 위한 타이머
      userId: "", // 로그인된 유저 ID
      nickname: "", // 로그인된 유저 닉네임
      messages: [], // 채팅 메시지 리스트
      newMessage: "", // 입력된 새 메시지
      recommendations: [], // 로컬 스토리지에서 불러온 추천 항목
    };
  },
  async created() {
    const user = JSON.parse(localStorage.getItem("user"));
    if (user) {
      this.userId = user.id;
      this.nickname = user.nickname;

      try {
        const response = await axios.post(
          `${import.meta.env.VITE_VUE_API_URL}/ai/chat`,
          {
            userId: this.userId,
          }
        );

        if (response.data.dialogueHistory) {
          // system 메시지 제외 및 메시지 리스트 업데이트
          this.messages = response.data.dialogueHistory
            .filter((msg) => msg.role !== "system")
            .map((msg) => ({
              text: msg.content,
              sender: msg.role === "user" ? "user" : "ai",
              nickname: msg.role === "user" ? this.nickname : "AI",
            }));
        }
      } catch (error) {
        console.error("Error fetching chat history:", error);
      }

      // 로컬 스토리지에서 추천 불러오기
      const storedRecommendations = this.getRecommendations(this.userId);

      storedRecommendations.forEach((recMessage) => {
        const { index, type, data } = recMessage;
        const recommendationMessage = {
          text: type === "neighborhood" ? "추천 동네:" : "추천 아파트:",
          sender: "ai",
          nickname: "AI",
          recommendations:
            type === "neighborhood"
              ? { neighborhood: data }
              : { apartments: data },
        };
        this.messages.splice(index, 0, recommendationMessage); // 원래 위치에 삽입
      });
    } else {
      console.error("로그인 정보가 없습니다.");
    }

    this.scrollToBottom();
  },
  methods: {
    async sendMessage() {
      if (this.newMessage.trim() === "") return;

      // 사용자 메시지 추가
      const userMessage = {
        text: this.newMessage,
        sender: "user",
        nickname: this.nickname,
      };
      this.messages.push(userMessage);
      this.scrollToBottom();

      // JSON 요청 데이터 구성
      const requestData = {
        userId: this.userId,
        userMessage: this.newMessage,
      };

      // 입력 창 초기화
      this.newMessage = "";

      // 타이핑 인디케이터 메시지 추가
      this.typingIndicator = {
        text: "", // 초기에는 빈 문자열
        sender: "ai",
        nickname: "AI",
      };
      this.messages.push(this.typingIndicator);
      this.startTypingAnimation(); // 점점점 애니메이션 시작
      this.scrollToBottom();

      // AI 응답 요청
      try {
        const response = await axios.post(
          `${import.meta.env.VITE_VUE_API_URL}/ai/chat`,
          requestData,
          {
            headers: { "Content-Type": "application/json" },
          }
        );

        // 타이핑 애니메이션 중지 및 인디케이터 제거
        this.stopTypingAnimation();

        // 타이핑 인디케이터 메시지를 실제 AI 응답으로 교체
        this.typingIndicator.text = response.data.message;
        this.typingIndicator = null; // 필요하다면 초기화
        this.scrollToBottom();

        // 추천 목록 처리
        const { neighborhoodRecommendations, apartmentRecommendations } =
          response.data;
        console.log("전체 데이터: ", response.data);

        if (
          neighborhoodRecommendations &&
          Object.keys(neighborhoodRecommendations).length > 0
        ) {
          this.messages.push({
            text: "추천 동네:",
            sender: "ai",
            nickname: "AI",
            recommendations: {
              neighborhood: neighborhoodRecommendations,
            },
          });
          const index = this.messages.length - 1;
          console.log("동네 추천:", neighborhoodRecommendations);
          this.saveRecommendations(
            this.userId,
            neighborhoodRecommendations,
            null,
            index
          ); // 아파트는 null로 저장
        }

        if (apartmentRecommendations && apartmentRecommendations.length > 0) {
          this.messages.push({
            text: "추천 아파트:",
            sender: "ai",
            nickname: "AI",
            recommendations: {
              apartments: apartmentRecommendations,
            },
          });
          const index = this.messages.length - 1;
          console.log("아파트 추천:", apartmentRecommendations);
          this.saveRecommendations(
            this.userId,
            null,
            apartmentRecommendations,
            index
          ); // 동네는 null로 저장
        }
      } catch (error) {
        // 에러 발생 시 타이핑 애니메이션 중지 및 인디케이터 제거
        this.stopTypingAnimation();
        this.messages.pop(); // 타이핑 인디케이터 메시지 제거

        // 에러 메시지 처리
        const errorMessage = {
          text: "AI 응답을 불러오지 못했습니다. 다시 시도해주세요.",
          sender: "ai",
          nickname: "AI",
        };
        this.messages.push(errorMessage);
        console.error("Error fetching AI response:", error);
      }

      // 스크롤 최신화
      this.scrollToBottom();
    },
    startTypingAnimation() {
      this.typingDots = "";
      if (this.typingInterval) clearInterval(this.typingInterval);
      this.typingInterval = setInterval(() => {
        if (this.typingDots.length >= 3) {
          this.typingDots = "";
        } else {
          this.typingDots += ".";
        }
        if (this.typingIndicator) {
          this.typingIndicator.text = this.typingDots;
        }
      }, 700); // 0.5초마다 점 추가
    },
    stopTypingAnimation() {
      if (this.typingInterval) {
        clearInterval(this.typingInterval);
        this.typingInterval = null;
      }
    },
    beforeDestroy() {
      this.stopTypingAnimation();
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight; // 스크롤을 최하단으로 이동
        }
      });
    },
    renderMarkdown(text) {
      const md = new MarkdownIt();
      return md.renderInline(text); // 인라인 Markdown만 처리
    },
    async searchDong(sidoName, gugunName, dongName) {
      try {
        getDongCodeByDetails(
          sidoName,
          gugunName,
          dongName,
          (response) => {
            console.log(response.data);

            const dongCode = response.data?.dongCd;

            if (dongCode) {
              this.$router.push({
                name: "home-list",
                params: { keyword: dongCode },
              });
            } else {
              alert(
                "해당 동에 대한 정보를 찾을 수 없습니다. 다시 시도해주세요."
              );
            }
          },
          (error) => {
            console.error("동 정보 검색 중 오류 발생:", error);
            alert("동 정보 검색 중 문제가 발생했습니다. 다시 시도해주세요.");
          }
        );
      } catch (error) {
        console.error("동 정보 검색 중 오류 발생:", error);
        alert("동 정보 검색 중 문제가 발생했습니다. 다시 시도해주세요.");
      }
    },
    // 로컬 스토리지에 추천 저장
    saveRecommendations(userId, neighborhood, apartments, index) {
      const allRecommendations = JSON.parse(
        localStorage.getItem("recommendations") || "{}"
      );
      const userRecommendations = allRecommendations[userId] || [];

      // 동네 추천 추가
      if (neighborhood) {
        userRecommendations.push({
          type: "neighborhood",
          data: neighborhood,
          index, // 메시지 인덱스 저장
        });
      }

      // 아파트 추천 추가
      if (apartments && apartments.length > 0) {
        userRecommendations.push({
          type: "apartments",
          data: apartments,
          index, // 메시지 인덱스 저장
        });
      }

      allRecommendations[userId] = userRecommendations;
      localStorage.setItem(
        "recommendations",
        JSON.stringify(allRecommendations)
      );
    },

    // 로컬 스토리지에서 추천 불러오기
    getRecommendations(userId) {
      // 로컬 스토리지에서 데이터를 가져오고, 없을 경우 빈 객체로 초기화
      const allRecommendations =
        JSON.parse(localStorage.getItem("recommendations")) || {};
      const userRecommendations = allRecommendations[userId] || [];
      // 특정 사용자에 대한 추천 목록이 없으면 기본값 제공
      return Array.isArray(userRecommendations)
        ? userRecommendations
        : { neighborhood: null, apartments: [] };
    },
  },
};
</script>

<template>
  <main>
    <div class="chat-header" v-if="messages.length === 0">
      <h1>무엇을 도와드릴까요?</h1>
    </div>
    <div class="chat-container">
      <!-- 채팅 메시지 표시 영역 -->
      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="[
            'message',
            message.sender === 'user' ? 'user-message' : 'ai-message',
          ]"
        >
          <div class="message-content">
            <!-- 추천 메시지 -->
            <div v-if="message.recommendations" class="recommendations-list">
              <!-- 동네 추천 -->
              <div
                v-for="(rec, recIndex) in message.recommendations.neighborhood"
                :key="recIndex"
                class="recommendation-item"
              >
                <div class="recommendation-text">
                  <span
                    ><strong>{{ rec.dong_name }}</strong> ({{ rec.sido_name }}
                    {{ rec.gugun_name }})</span
                  >
                  <p>{{ rec.reason }}</p>
                </div>
                <button
                  @click="
                    searchDong(rec.sido_name, rec.gugun_name, rec.dong_name)
                  "
                  class="recommendation-button"
                >
                  {{ rec.dong_name }} 검색
                </button>
              </div>

              <!-- 아파트 추천 -->
              <div v-if="message.recommendations.apartments">
                <div
                  v-for="(apt, aptIndex) in message.recommendations.apartments"
                  :key="aptIndex"
                  class="recommendation-item"
                >
                  <div class="recommendation-text">
                    <span
                      ><strong>{{ apt.apt_name }}</strong> ({{
                        apt.location
                      }})</span
                    >
                    <p>{{ apt.price_range }}</p>
                    <p>{{ apt.reason }}</p>
                  </div>
                  <button
                    @click="
                      $router.push({
                        name: 'home-list',
                        params: { keyword: apt.apt_name },
                      })
                    "
                    class="recommendation-button"
                  >
                    {{ apt.apt_name }} 찾기
                  </button>
                </div>
              </div>
            </div>

            <!-- 추천 항목이 없을 경우 Markdown 처리 -->
            <div v-else class="basic-message" v-html="message.text"></div>
          </div>
        </div>
      </div>

      <!-- 채팅 입력 영역 -->
      <div class="chat-input">
        <div class="input-container">
          <input
            v-model="newMessage"
            type="text"
            placeholder="메시지를 입력하세요..."
            @keyup.enter="sendMessage"
          />
          <button @click="sendMessage" class="send-button">전송</button>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 공통 스타일 */
.chat-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: fit-content;
  max-width: 1000px;
  min-width: 800px;
  margin: 0 auto;
  /* border: 1px solid #ddd;
  border-radius: 10px;
  background-color: #ffffff; */
  overflow: hidden;
}

::-webkit-scrollbar {
  display: none;
  /* Chrome, Safari */
}

.chat-header h1 {
  display: flex;
  justify-content: center;
  font-weight: 600;
  max-width: 1000px;
}

.chat-messages {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  /* background-color: #f9f9f9; */
}

/* 개별 메시지 */
.message {
  max-width: 60%;
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 10px;
  word-wrap: break-word;
  position: relative;
  font-size: 14px;
}

/* 사용자 메시지 스타일 */
.user-message {
  align-self: flex-end;
  background-color: #d1e7ff;
  color: #003366;
  /* 사용자 메시지는 파란 톤 */
  padding: 10px;
  border-radius: 15px;
}

/* AI 메시지 스타일 */
.ai-message {
  align-self: flex-start;
  background-color: #ffe4b5;
  color: #664500;
  /* AI 메시지는 주황 톤 */
  white-space: pre-wrap;
  /* 공백과 줄바꿈을 그대로 표시 */
  word-wrap: break-word;
  /* 긴 단어도 적절히 줄바꿈 */
  padding: 10px;
  border-radius: 15px;
  font-size: 14px;
}

/* 추천 항목 리스트 스타일 */
.recommendations-list {
  margin-top: 15px;
  /* AI 메시지와의 간격 */
  padding: 10px;
  background-color: #fff9e6;
  /* AI 배경색과 어울리는 연한 노란색 */
  border-radius: 10px;
  /* 부드러운 모서리 */
  border: 1px solid #ffd27f;
  /* 주황 톤의 테두리 */
}

/* 추천 항목 개별 아이템 스타일 */
.recommendation-item {
  display: flex;
  justify-content: space-between;
  /* 텍스트와 버튼 양쪽 정렬 */
  align-items: center;
  padding: 8px 10px;
  background-color: #fffdf6;
  /* 약간 밝은 배경색 */
  border-radius: 8px;
  margin-bottom: 8px;
  /* 항목 간 간격 */
  font-size: 14px;
  color: #664500;
}

/* 추천 항목 텍스트 */
.recommendation-text {
  flex: 1;
  /* 버튼과의 간격 조정 */
  margin-right: 10px;
  font-size: 14px;
  color: #664500;
  word-wrap: break-word; /* 긴 단어 줄바꿈 */
  overflow-wrap: break-word; /* 최신 브라우저 지원 */
  white-space: normal; /* 텍스트를 자동 줄바꿈 */
}

/* 추천 항목 버튼 스타일 */
.recommendation-button {
  padding: 6px 12px;
  background-color: #ffd27f;
  /* AI 배경과 어울리는 주황색 */
  color: #fff;
  /* 버튼 텍스트 흰색 */
  border: none;
  border-radius: 5px;
  font-size: 12px;
  /* 간결한 버튼 텍스트 크기 */
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.recommendation-button:hover {
  background-color: #ffbf60;
  /* 호버 시 약간 진한 주황색 */
}

/* 마지막 추천 항목의 마진 제거 */
.recommendation-item:last-child {
  margin-bottom: 0;
}

/* 메시지 정보 스타일 */
.message-info {
  text-align: right;
  font-size: 12px;
  color: #888;
  margin-bottom: 5px;
}

.chat-input {
  display: flex;
  align-items: center;
  padding: 10px;
  /* border-top: 1px solid #ddd; */
  background-color: #ffffff;
  height: 60px;
}

.input-container {
  position: relative;
  width: 100%;
}

.chat-input input {
  width: 100%;
  padding: 10px 50px 10px 10px;
  /* 버튼 위치를 고려해 오른쪽 패딩 추가 */
  border: 1px solid #bcbcbc;
  border-radius: 30px;
  outline: none;
  font-size: 14px;
}

.chat-input .send-button {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  padding: 5px 10px;
  background-color: var(--eerie-black);
  color: white;
  border: none;
  border-radius: 15px;
  font-size: 14px;
  cursor: pointer;
}

.chat-input .send-button:hover {
  background-color: var(--eerie-grey);
}

.search-button {
  margin-left: 10px;
  padding: 5px 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
}

.search-button:hover {
  background-color: #0056b3;
}
</style>
