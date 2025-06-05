# 🏙️ AI 기반 아파트 및 동네 추천 플랫폼
<img src="./images/1.png" alt="메인 화면" width="800" />

## 📌 프로젝트 개요
AI 기반 대화 추천, 아파트 실거래 정보, 관심 목록, 리뷰, 문의 기능을 통합한 부동산 정보 플랫폼입니다.

---

## 🚀 주요 기능

### 🤖 AI 추천 기능
- **사용자 대화 기반 추천**
  - 대화 이력 분석을 통해 동네 또는 아파트 추천
- **동네 추천**
  - Pinecone 유사도 검색을 통해 관련 동네 추천
  - 결과는 JSON 형식 반환
- **아파트 추천**
  - Pinecone 유사도 검색을 통해 조건에 맞는 아파트 추천
- **데이터 업로드**
  - OpenAI Embedding API를 활용해 벡터화 후 Pinecone 저장
- **유사 검색**
  - 텍스트 임베딩 기반 유사 동네/아파트 검색
 
<table>
  <tr>
    <td align="center"><img src="./images/16.png" alt="AI 채팅 화면" width="500" /></td>
  </tr>
  <tr>
    <td align="center">AI 채팅 화면</td>
  </tr>
</table>

---
### 메인 화면
<table>
  <tr>
    <td align="center"><img src="./images/2.png" alt="메인 화면 1" width="500" /></td>
    <td align="center"><img src="./images/3.png" alt="메인 화면 2" width="500" /></td>
  </tr>
  <tr>
    <td align="center">메인 화면 1</td>
    <td align="center">메인 화면 2</td>
  </tr>
</table>

### 🏢 APT 기능
- 지역 기반 아파트 검색 (동/구/시도 코드 기반)
- Trie 기반 아파트/동 이름 키워드 검색
- 실거래가 정보 제공 (연도/월/일 정렬)
- Kakao API를 활용한 지도 및 길찾기 기능

<!-- **지도 기반 UI/UX**
![](./images/4.png)
**Trie 기반 검색**
![](./images/3_1.png)
**주변 편의시설**
![](./images/5.png)
**지적편집도 / 지형**
![](./images/6.png)
**지도 기반 필터링**
![](./images/7.png)
**실거래가 정보 및 주변 상세 정보**
![](./images/8.png)
![](./images/9.png)
**길찾기**
![](./images/10.png) -->

<table border="0" cellspacing="0" cellpadding="8">
  <tr>
    <td align="center">
      <img src="./images/4.png" alt="지도 기반 UI/UX" width="500" />
    </td>
    <td align="center">
      <img src="./images/3_1.png" alt="Trie 기반 검색" width="500" />
    </td>
  </tr>
  <tr>
    <td align="center">지도 기반 UI/UX</td>
    <td align="center">Trie 기반 검색</td>
  </tr>

  <tr>
    <td align="center">
      <img src="./images/5.png" alt="주변 편의시설" width="500" />
    </td>
    <td align="center">
      <img src="./images/6.png" alt="지적편집도 / 지형" width="500" />
    </td>
  </tr>
  <tr>
    <td align="center">주변 편의시설</td>
    <td align="center">지적편집도 / 지형</td>
  </tr>

  <tr>
    <td align="center">
      <img src="./images/7.png" alt="지도 기반 필터링" width="500" />
    </td>
    <td align="center">
      <img src="./images/8.png" alt="실거래가 상세 정보" width="500" />
    </td>
  </tr>
  <tr>
    <td align="center">지도 기반 필터링</td>
    <td align="center">실거래가 상세 정보</td>
  </tr>

  <tr>
    <td align="center">
      <img src="./images/9.png" alt="실거래가 추가 정보" width="500" />
    </td>
    <td align="center">
      <img src="./images/10.png" alt="길찾기 기능" width="500" />
    </td>
  </tr>
  <tr>
    <td align="center">실거래가 추가 정보</td>
    <td align="center">길찾기 기능</td>
  </tr>
</table>

---

### 📬 1:1 문의
- 문의 작성/조회/수정/삭제
- 이메일 알림 발송
- 파일 첨부 기능 (추후 확장 가능)

<table>
  <tr>
    <td align="center"><img src="./images/11.png" alt="1:1 문의" width="500" /></td>
    <td align="center"><img src="./images/12.png" alt="자주 묻는 질문" width="500" /></td>
  </tr>
  <tr>
    <td align="center">1:1 문의</td>
    <td align="center">자주 묻는 질문</td>
  </tr>
  <tr>
    <td align="center"><img src="./images/13.png" alt="공지사항" width="500" /></td>
    <td></td>
  </tr>
  <tr>
    <td align="center">공지사항</td>
    <td></td>
  </tr>
</table>

---

### ⭐ 관심 목록 (Interest)
- 최근 열람 및 북마크 관리 (아파트, 동네)
- 북마크 상태 확인 및 삭제
- 로컬 스토리지 ↔ DB 동기화

<table>
  <tr>
    <td align="center"><img src="./images/14.png" alt="최근 본 목록" width="500" /></td>
    <td align="center"><img src="./images/15.png" alt="찜" width="500" /></td>
  </tr>
  <tr>
    <td align="center">최근 본 목록</td>
    <td align="center">찜</td>
  </tr>
</table>

---

### 📝 리뷰 기능
- 리뷰 작성 (이미지 첨부 가능)
- 리뷰 조회 (아파트별/사용자별/평점 필터)
- 리뷰 수정/삭제
- 업로드 이미지 조회 (URL 반환)

<table>
  <tr>
    <td align="center"><img src="./images/17.png" alt="마이페이지" width="500" /></td>
    <td align="center"><img src="./images/18.png" alt="리뷰 목록" width="500" /></td>
  </tr>
  <tr>
    <td align="center">마이페이지</td>
    <td align="center">리뷰 목록</td>
  </tr>
</table>

---

### 👤 사용자 기능 (User)
- 회원가입, 프로필 수정, 비밀번호 암호화
- 소셜 로그인 (카카오 OAuth)
- 세션 기반 로그인/로그아웃
- 이메일 인증 (코드 전송 및 검증)

<table>
  <tr>
    <td align="center"><img src="./images/19.png" alt="회원가입" width="500" /></td>
    <td align="center"><img src="./images/20.png" alt="로그인" width="500" /></td>
  </tr>
  <tr>
    <td align="center">회원가입</td>
    <td align="center">로그인</td>
  </tr>
</table>

---

## 🧱 기술 스택

| 구분        | 기술                                                   |
|-------------|--------------------------------------------------------|
| Backend     | Spring Boot, MySQL, Redis                              |
| Frontend    | Vue 3, Vite, Axios, Chart.js, Kakao Map API            |
| AI Server   | FastAPI (Python), OpenAI API, Pinecone                 |
| 인증/보안   | Spring Security, OAuth2, Email 인증                    |
| 기타 도구   | Docker, GitHub Actions or Jenkins (선택)               |

---

## Use Case Diagram
![](./images/apt_diagram.png)

## ERD
<table>
  <tr>
    <td align="center"><img src="./images/APT_ERD.png" alt="ERD" width="300" /></td>
  </tr>
  <tr>
    <td align="center">ERD</td>
  </tr>
</table>

## 클래스 다이어그램
<table>
  <tr>
    <td align="center"><img src="./images/ai.png" alt="AI" width="600" /></td>
    <td align="center"><img src="./images/apt.png" alt="APT" width="600" /></td>
  </tr>
  <tr>
    <td align="center">AI</td>
    <td align="center">APT</td>
  </tr>
  <tr>
    <td align="center"><img src="./images/board.png" alt="Board" width="600" /></td>
    <td align="center"><img src="./images/interest.png" alt="Interest" width="600" /></td>
  </tr>
  <tr>
    <td align="center">Board</td>
    <td align="center">Interest</td>
  </tr>
  <tr>
    <td align="center"><img src="./images/review.png" alt="Review" width="600" /></td>
    <td align="center"><img src="./images/user.png" alt="User" width="600" /></td>
  </tr>
  <tr>
    <td align="center">Review</td>
    <td align="center">User</td>
  </tr>
</table>

---

## 👥 팀 구성 및 역할

> 본 프로젝트는 **총 2명**이 참여하였으며, **역할 분담을 명확히 하여 효율적인 협업**을 진행했습니다.

| 이름 | 담당 역할 | 주요 업무 |
|------|-----------|-----------|
| 👤 공예슬 (FE) | 프론트엔드 개발 | Vue 기반 UI/UX 구현, AI 챗봇 인터페이스, Kakao 지도 연동, 관심목록/리뷰/1:1 문의 화면 개발 |
| 👤 강승엽 (BE) | 백엔드 API 개발 | Spring Boot 기반 API 서버 구축, 실거래가/검색/리뷰/문의 API 설계 및 구현 |
| 👤 강승엽, 공예슬 (AI) | AI 추천 시스템 개발 | Pinecone + OpenAI Embedding을 활용한 AI 추천 시스템 개발 및 벡터 데이터 관리 |



### 💬 협업 방식

- GitHub Issues 및 Projects를 활용한 작업 분담
- API 명세서는 Swagger 기반으로 자동 문서화
- 기능 단위 커밋 및 PR 리뷰로 코드 품질 관리
- 정기 회의를 통한 진행 상황 공유 및 기능 통합 조율

---
