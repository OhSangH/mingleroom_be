# MingleRoom (Backend)

실시간 채팅, WebRTC 음성 통화, FigJam 스타일 화이트보드를 한 공간에 묶은 온라인 회의/브레인스토밍 서비스의 백엔드 서버입니다.

## 1. 프로젝트 개요

### 1.1 목표
- **All-in-One**: 회의 커뮤니케이션(채팅/음성)과 협업(화이트보드/노트)을 단일 룸에서 해결
- **Persistence**: 회의가 끝나도 결과물(회의록, 보드, 액션아이템)이 남는 구조
- **Real-time**: WebSocket을 활용한 저지연 협업 환경 제공

### 1.2 핵심 가치
- **화이트보드 중심**: 커서 프레즌스, 버전 히스토리, 템플릿 지원
- **문서화 흐름**: 공유 노트 + 액션아이템 + 회의 종료 요약
- **확장성**: WebSocket 멀티 인스턴스(Redis Pub/Sub), 관리자/감사 로그

---

## 2. 사용자 및 권한 모델

| 역할 (Role) | 설명 |
| :--- | :--- |
| **HOST** | 방장. 방 잠금/설정, 강퇴, 발언권 제어, 발표자 지정 권한 |
| **PRESENTER** | 발표자. 화면 공유, 발표자 모드 제어 (호스트가 지정) |
| **MEMBER** | 일반 참가자. 채팅, 음성, 화이트보드 사용, 손들기 |
| **ADMIN** | 전체 시스템 관리자. 유저/방 관리, 신고 처리 |

---

## 3. 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.9
- **Database**: PostgreSQL (JPA/Hibernate)
- **Security**: Spring Security, JWT (jjwt 0.12.6)
- **Real-time**: Spring WebSocket (STOMP)
- **Build Tool**: Gradle
- **Utils**: Lombok, Validation

---

## 4. API 명세 (REST)

### 4.1 Auth & User (`/auth`, `/user`)
- `POST /auth/signup`: 회원가입
- `POST /auth/login`: 로그인 (JWT 발급)
- `GET /auth/me`: 내 정보 조회
- `PATCH /user/me`: 내 프로필 수정

### 4.2 Room (`/room`)
- `POST /room`: 룸 생성 (Title, Visibility, Password)
- `GET /room`: 룸 목록 조회 (검색/페이징)
- `GET /room/{id}`: 룸 상세 정보
- `PATCH /room/{id}`: 룸 설정 변경 (Host only)
- `POST /room/{id}/lock`: 룸 잠금 설정

### 4.3 Room Member (`/room/{id}/members`)
- `POST /room/{id}/join`: 룸 입장 (비밀번호/초대코드 검증)
- `POST /room/{id}/leave`: 룸 퇴장
- `GET /room/{id}/members`: 현재 참여자 목록
- `PATCH /room/{id}/members/{uid}/role`: 권한 변경 (Host only)
- `POST /room/{id}/members/{uid}/kick`: 강퇴 (Host only)

### 4.4 Collaboration (`/room/{id}`)
- `GET /room/{id}/chat`: 채팅 로그 조회 (페이징)
- `GET /room/{id}/board/snapshot`: 화이트보드 최신 스냅샷 조회
- `POST /room/{id}/board/snapshot`: 스냅샷 저장
- `GET/PUT /room/{id}/note`: 공유 노트 조회/수정
- `POST /room/{id}/action`: 액션 아이템 생성

---

## 5. Real-time 명세 (WebSocket/STOMP)

### 5.1 연결 정보
- **Endpoint**: `/ws-stomp`
- **Auth**: Header `Authorization: Bearer <JWT>`

### 5.2 Destination 규칙
- **Client Send**: `/app/**`
- **Client Subscribe**: `/topic/**` (브로드캐스트), `/user/queue/**` (개인 알림)

### 5.3 주요 토픽 (Topic)

| 기능 | Subscribe Path | 설명 |
| :--- | :--- | :--- |
| **채팅** | `/topic/room/{roomId}/chat` | 텍스트, 이미지, 파일 메시지 수신 |
| **이벤트** | `/topic/room/{roomId}/event` | 입장/퇴장, 손들기, 권한 변경 등 시스템 이벤트 |
| **시그널링** | `/topic/room/{roomId}/signal` | WebRTC P2P 연결을 위한 Offer/Answer/ICE 교환 |
| **화이트보드** | `/topic/room/{roomId}/board` | 드로잉, 도형, 커서 위치 동기화 |

### 5.4 메시지 Payload 예시 (Chat)
```json
{
  "type": "TEXT",
  "content": "안녕하세요",
  "senderUserId": 1,
  "roomId": 100
}
```

---

## 6. 데이터 모델 (ERD 초안)

- **Users**: `id`, `email`, `username`, `role`
- **Rooms**: `id`, `title`, `host_id`, `visibility`, `password`
- **RoomMembers**: `room_id`, `user_id`, `role_in_room`
- **ChatMessages**: `id`, `room_id`, `sender_id`, `content`, `type`
- **WhiteboardSnapshots**: `id`, `room_id`, `data_blob`, `version`
- **Notes**: `room_id`, `content`
- **ActionItems**: `id`, `room_id`, `assignee_id`, `status`, `due_date`

---

## 7. 환경 변수 설정

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mingleroom
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
JWT_SECRET=your_jwt_secret_key_here
```

## 8. 개발 로드맵

1. **Phase 1 (기반 구축)**: 프로젝트 세팅, JWT 인증, 룸 CRUD, 기본 채팅 (STOMP)
2. **Phase 2 (음성 통화)**: WebRTC 시그널링 구현, P2P 음성 통화
3. **Phase 3 (화이트보드)**: Canvas/SVG 기반 드로잉 동기화, 커서 공유
4. **Phase 4 (협업 도구)**: 공유 노트, 액션 아이템, 회의록 저장
5. **Phase 5 (고도화)**: Redis Pub/Sub 도입, 배포 파이프라인, 모니터링
