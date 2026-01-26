# MingleRoom (Backend)

실시간 채팅, WebRTC 음성 통화, FigJam 스타일 화이트보드를 한 공간에 묶은 온라인 회의/브레인스토밍 서비스의 백엔드 서버입니다.

## 1. 프로젝트 개요
- **목표**: 회의 커뮤니케이션(채팅/음성)과 협업(화이트보드/노트)을 단일 룸에서 해결 및 결과물 보존
- **핵심 기능**:
    - **화이트보드**: 커서 프레즌스, 버전 히스토리, 템플릿
    - **문서화**: 공유 노트, 액션아이템, 자동 요약
    - **운영**: WebSocket 멀티 인스턴스(Redis), 관리자 페이지

## 2. 기술 스택
- **Core**: Java 21, Spring Boot 3.5.9
- **Data**: PostgreSQL, Spring Data JPA
- **Security**: Spring Security, JWT (jjwt 0.12.6)
- **Real-time**: Spring WebSocket (STOMP), Redis Pub/Sub (예정)
- **Build**: Gradle

## 3. API 명세 요약

### Auth & User
- `POST /auth/signup`, `/auth/login`: 회원가입 및 JWT 발급
- `GET /auth/me`, `/user/me`: 내 정보 조회 및 프로필 수정

### Workspace & Room
- `POST /workspaces`: 워크스페이스 생성
- `GET /workspaces`: 내 워크스페이스 목록 조회
- `POST /rooms`: 룸 생성 (워크스페이스에 속하거나 독립적으로)
- `GET /rooms`: 룸 목록 검색
- `POST /rooms/{id}/join`: 룸 입장 (비밀번호/초대코드)

### Real-time (WebSocket/STOMP)
- **Endpoint**: `/ws-stomp`
- **Subscribe**: `/topic/room/{roomId}/{chat|event|signal|board}`
- **Features**:
    - **Chat**: 텍스트, 파일, 이미지 메시지
    - **Event**: 입장/퇴장, 손들기, 리액션
    - **Signal**: WebRTC P2P 연결 (Offer/Answer/ICE)
    - **Board**: 드로잉, 커서 이동, 스냅샷 동기화

### Collaboration
- `GET/PUT /rooms/{id}/note`: 공유 노트 조회/저장
- `POST /rooms/{id}/action-items`: 액션 아이템 생성 및 관리
- `GET /rooms/{id}/board/snapshot`: 화이트보드 스냅샷 저장/로드
- `POST /rooms/{id}/polls`: 투표 생성 및 관리
- `POST /rooms/{id}/bookmarks`: 타임스탬프 북마크 생성

### Admin & Management
- `POST /reports`: 사용자/콘텐츠 신고
- `GET /audit-logs`: 감사 로그 조회 (Admin)
- `POST /attachments/presigned-url`: 파일 업로드를 위한 Presigned URL 요청

## 4. 환경 변수
- `SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD`: DB 연결 정보
- `JWT_SECRET`: 토큰 서명 키

## 5. 개발 로드맵
1. **Phase 1**: 룸 생성/입장, JWT 인증, 기본 채팅
2. **Phase 2**: WebRTC 음성 통화 (P2P)
3. **Phase 3**: 화이트보드 MVP (드로잉, 커서)
4. **Phase 4**: 심화 협업 (노트, 액션아이템, 히스토리)
5. **Phase 5**: 확장성 (Redis, SFU, 관리자)

*상세 문서는 `기록/` 폴더를 참조하세요.*
