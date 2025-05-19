<instruction>
{city}의 {dong} 지역을 기반으로 사용자 맞춤형 아파트 추천 정보를 생성해줘.
최근 실거래 내역과 사용자 선호 조건을 고려하여 매물 리스트를 제공하되, 관심 있는 아파트의 위치를 중심으로 가까운 매물을 추천해줘.
JSON 형식은 아래를 참고해 작성해줘.
1. 매물명: name
2. 거래 타입: dealType (값의 경우 1:매매, 2:전세, 3:월세)
3. 가격: price (단위는 원, 세부 단위 표기 필수)
4. 면적: size (단위는 m²)
5. 위치: 위도 (latitude), 경도 (longitude)
6. 주소: address
7. 주요 특징: features (예: 학군, 교통, 상업지구 등)
8. 정확한 json 파일 형식만 답변 부탁해(```json 제외).
</instruction>

<example>
\{
    "recommendations": [
        \{
            "name": "래미안 아파트",
            "dealType": 1,
            "price": "10억 5천만 원",
            "size": "84 m²",
            "latitude": 37.4956,
            "longitude": 127.0505,
            "address": "서울시 강남구 대치동 510",
            "features": ["우수 학군", "지하철 접근성", "상업지구 인접"]
        \},
        \{
            "name": "힐스테이트 아파트",
            "dealType": 2,
            "price": "5억 원",
            "size": "72 m²",
            "latitude": 37.4982,
            "longitude": 127.0510,
            "address": "서울시 강남구 도곡동 523",
            "features": ["우수 학군", "주요 대로와 접근 용이", "편의시설 인접"]
        \},
        \{
            "name": "삼성동 센트럴파크",
            "dealType": 3,
            "price": "월세 300만 원",
            "size": "115 m²",
            "latitude": 37.5081,
            "longitude": 127.0619,
            "address": "서울시 강남구 삼성동 120",
            "features": ["프리미엄 상업지구 접근성", "고급 인테리어", "문화시설 근접"]
        \}
    ]
\}
</example>