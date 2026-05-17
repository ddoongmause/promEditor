# Promeditor — 개발 Phase 계획 및 현황

---

## 개발 현황 요약

| Phase | 이름 | 상태 | 시작일 | 완료일 |
|-------|------|------|--------|--------|
| Phase 1 | 프로젝트 기반 세팅 | ✅ 완료 | 2026-05-16 | 2026-05-16 |
| Phase 2 | 템플릿 핵심 기능 (MVP) | ✅ 완료 | 2026-05-16 | 2026-05-16 |
| Phase 3 | 프롬프트 실행 화면 | ✅ 완료 | 2026-05-16 | 2026-05-16 |
| Phase 4 | 정리 기능 (분류/검색) | ✅ 완료 | 2026-05-16 | 2026-05-16 |
| Phase 5 | 편의 기능 | ✅ 완료 | 2026-05-16 | 2026-05-16 |

> 상태 아이콘: ✅ 완료 / 🟡 진행중 / ⬜ 대기 / ❌ 보류

---

## Phase 1 — 프로젝트 기반 세팅

> 개발을 시작하기 위한 공통 기반 구성. 이후 모든 Phase의 토대.

### 작업 목록

- [x] DB 스키마 설계 및 Flyway 마이그레이션 작성 (`V1__init_schema.sql`)
- [x] JPA Entity 작성 (`Template`, `TemplateBlock`, `SlotOption`)
- [x] `UpperCaseNamingStrategy` 설정
- [x] Spring Security 기본 설정 (현재는 전체 permitAll, 추후 인증 추가)
- [x] 공통 응답 구조 정의 (`ApiResponse<T>`)
- [x] Docker Compose PostgreSQL 연결 확인 (compose.yaml 기존 설정 활용)
- [x] Thymeleaf 레이아웃 템플릿 구성 (공통 헤더/푸터 fragment)

### DB 스키마 (초안)

```
TEMPLATES
  - ID (PK)
  - TITLE
  - DESCRIPTION
  - CREATED_AT
  - UPDATED_AT

TEMPLATE_BLOCKS          ← 고정 텍스트 or 슬롯, 순서 포함
  - ID (PK)
  - TEMPLATE_ID (FK)
  - BLOCK_TYPE            ← FIXED / SLOT
  - CONTENT               ← FIXED면 텍스트, SLOT이면 슬롯 이름
  - SLOT_TYPE             ← TEXT / TEXTAREA / SELECT (SLOT일 때만)
  - SORT_ORDER

SLOT_OPTIONS             ← SELECT 타입 슬롯의 선택지
  - ID (PK)
  - BLOCK_ID (FK)
  - LABEL
  - SORT_ORDER
```

### 완료 이력

| 날짜 | 작업 내용 | 비고 |
|------|----------|------|
| 2026-05-16 | build.gradle Flyway 의존성 추가 | flyway-core, flyway-database-postgresql |
| 2026-05-16 | application.yaml 전체 설정 구성 | JPA ddl-auto: validate, Flyway 활성화 |
| 2026-05-16 | UpperCaseNamingStrategy 작성 | config 패키지 |
| 2026-05-16 | SecurityConfig 작성 | 전체 permitAll (Phase 1 임시) |
| 2026-05-16 | ApiResponse<T> 공통 응답 클래스 작성 | common 패키지 |
| 2026-05-16 | BlockType, SlotType enum 작성 | template/entity 패키지 |
| 2026-05-16 | Template, TemplateBlock, SlotOption Entity 작성 | 연관관계 매핑 포함 |
| 2026-05-16 | V1__init_schema.sql 작성 | TEMPLATES, TEMPLATE_BLOCKS, SLOT_OPTIONS |
| 2026-05-16 | Thymeleaf fragments/header, footer 작성 | |
| 2026-05-16 | index.html 홈 화면 작성 | |
| 2026-05-16 | main.css 기본 스타일 작성 | |
| 2026-05-16 | HomeController 작성 | GET / → index |

---

## Phase 2 — 템플릿 핵심 기능 (MVP)

> 템플릿을 만들고 블록을 구성하는 핵심 편집 기능.

### 작업 목록

#### 백엔드
- [x] `TemplateRepository`, `TemplateBlockRepository`, `SlotOptionRepository`
- [x] `TemplateService` — Facade 메서드
  - [x] `createTemplateFacade()` — 템플릿 + 블록 + 슬롯옵션 일괄 저장
  - [x] `getTemplateFacade()` — 단건 조회 (블록 포함)
  - [x] `getTemplateListFacade()` — 목록 조회
  - [x] `updateTemplateFacade()` — 수정
  - [x] `deleteTemplateFacade()` — 삭제
- [x] DTO 정의 (`TemplateCreateRequest`, `TemplateResponse`, `BlockResponse`, `TemplateSummaryResponse` 등)
- [x] `TemplateApiController` — REST API
- [x] `GlobalExceptionHandler` — 404/400 공통 예외 처리
- [x] Service / Controller 단위 테스트 (TDD)

#### 프론트
- [x] 템플릿 목록 화면 (`/templates`) — 카드 리스트
- [x] 템플릿 편집 화면 (`/templates/{id}/edit`)
  - [x] 블록 추가/삭제 UI
  - [x] FIXED 블록: textarea 입력
  - [x] SLOT 블록: 이름, 타입(TEXT/TEXTAREA/SELECT) 설정
  - [x] SELECT 타입: 선택지 목록 추가/삭제
- [x] 저장 / 삭제 버튼 동작 (AJAX)

### 완료 이력

| 날짜 | 작업 내용 | 비고 |
|------|----------|------|
| 2026-05-16 | TemplateRepository, TemplateBlockRepository, SlotOptionRepository 작성 | |
| 2026-05-16 | DTO 6종 작성 (Request/Response) | |
| 2026-05-16 | TemplateService Facade 메서드 5종 구현 + private 메서드 | TDD |
| 2026-05-16 | TemplateServiceTest 작성 (7개 테스트) | |
| 2026-05-16 | TemplateApiController (GET/POST/PUT/DELETE) | |
| 2026-05-16 | TemplateController (뷰 4개) | |
| 2026-05-16 | TemplateApiControllerTest 작성 (5개 테스트) | |
| 2026-05-16 | GlobalExceptionHandler 작성 (404, 400) | |
| 2026-05-16 | list.html + template-list.js 작성 | |
| 2026-05-16 | edit.html + template-edit.js 작성 | 블록 편집기, 미리보기, 저장/삭제 |

---

## Phase 3 — 프롬프트 실행 화면

> 템플릿을 선택해 슬롯을 채우고 완성된 프롬프트를 복사하는 사용 화면.

### 작업 목록

#### 백엔드
- [x] `getTemplateFacade()` — Phase 2에서 구현한 단건 조회로 실행 화면에 재사용

#### 프론트
- [x] 프롬프트 실행 화면 (`/templates/{id}/use`)
  - [x] 슬롯 타입별 입력 폼 렌더링 (TEXT → input, TEXTAREA → textarea, SELECT → select)
  - [x] 입력값 변경 시 미리보기 실시간 반영 (JS)
  - [x] 복사 버튼 — 클립보드 복사 + 복사 완료 토스트 피드백

### 완료 이력

| 날짜 | 작업 내용 | 비고 |
|------|----------|------|
| 2026-05-16 | use.html 작성 | 슬롯 폼 + 미리보기 + 복사 버튼 |
| 2026-05-16 | template-use.js 작성 | 실시간 미리보기, 클립보드 복사 |

---

## Phase 4 — 정리 기능 (분류 / 검색)

> 템플릿이 쌓였을 때 찾기 쉽게 하는 기능.

### 작업 목록

#### 백엔드
- [x] `V2__add_category_favorite_tags.sql` Flyway 마이그레이션
- [x] `TEMPLATE_TAGS` 테이블 + `TemplateTag` 엔티티/리포지터리
- [x] `Template` 엔티티에 `category`, `isFavorite`, `tags` 추가
- [x] `TemplateRepository` 검색 쿼리 (`search`, `findByIsFavoriteTrue`)
- [x] `searchTemplatesFacade()` — 제목/태그 키워드 + 카테고리 필터
- [x] `getFavoritesFacade()` — 즐겨찾기 목록
- [x] `toggleFavoriteFacade()` — 즐겨찾기 토글
- [x] `TemplateApiController` 에 `/favorites`, `/{id}/favorite` 엔드포인트 추가
- [x] `TemplateServicePhase4Test` 작성 (5개 테스트)

#### 프론트
- [x] list.html 검색바 + 카테고리 드롭다운 + 탭(전체/즐겨찾기) 추가
- [x] template-list.js 검색 디바운스, 카테고리 자동 구성, 즐겨찾기 토글, 내보내기/가져오기
- [x] edit.html 카테고리/태그 입력 필드 추가
- [x] template-edit.js 카테고리/태그 저장 반영

### 완료 이력

| 날짜 | 작업 내용 | 비고 |
|------|----------|------|
| 2026-05-16 | V2 마이그레이션 + TemplateTag Entity/Repository | |
| 2026-05-16 | Template 엔티티 category/isFavorite/tags 추가 | |
| 2026-05-16 | TemplateRepository 검색 JPQL 쿼리 추가 | |
| 2026-05-16 | TemplateService 검색/즐겨찾기 Facade 3종 추가 | |
| 2026-05-16 | TemplateApiController 즐겨찾기 API 추가 | |
| 2026-05-16 | TemplateServicePhase4Test 작성 | TDD |
| 2026-05-16 | list.html + template-list.js 검색/탭/즐겨찾기/내보내기/가져오기 UI | |
| 2026-05-16 | edit.html + template-edit.js 카테고리/태그 필드 추가 | |

---

## Phase 5 — 편의 기능

> 반복 작업을 줄이는 편의 기능.

### 작업 목록

- [x] 최근 입력값 히스토리 — 복사 시 자동 저장, 최근 5개 표시, 클릭 시 슬롯에 자동 적용
- [x] 템플릿 복제 — 기존 템플릿을 "(복사본)" 제목으로 복사
- [x] 내보내기 — 목록에서 JSON 파일 다운로드
- [x] 가져오기 — 목록에서 JSON 파일 업로드로 템플릿 복원

### 완료 이력

| 날짜 | 작업 내용 | 비고 |
|------|----------|------|
| 2026-05-16 | V3 마이그레이션 + TemplateHistory Entity/Repository | |
| 2026-05-16 | TemplateService Phase5 Facade 5종 추가 (복제, 내보내기, 가져오기, 히스토리 저장/조회) | |
| 2026-05-16 | TemplateApiController Phase5 API 6종 추가 | |
| 2026-05-16 | TemplateServicePhase5Test 작성 | TDD |
| 2026-05-16 | edit.html + template-edit.js 복제 버튼 추가 | |
| 2026-05-16 | use.html + template-use.js 히스토리 저장/표시/적용 | |

---

## 추후 검토 기능 (미정)

> 기획 중 나온 아이디어. 방향이 확정되면 Phase에 편입.

| 기능 | 설명 | 검토 상태 |
|------|------|----------|
| AI별 맞춤 설정 | Claude용 / GPT용 접두어를 다르게 붙이는 기능 | 검토중 |
| Snippet 라이브러리 | 말투/출력형식 등 재사용 블록을 별도 관리하고 템플릿에 끼워넣기 | 검토중 |
| 공유 기능 | 템플릿 URL 공유 | 검토중 |

---

## 개발 이력 (전체)

> 각 Phase 완료 또는 주요 변경 시 기록.

| 날짜 | Phase | 내용 | 담당 |
|------|-------|------|------|
| 2026-05-16 | - | 프로젝트 기획 완료, Phase 계획 수립 | - |
| 2026-05-16 | Phase 1 | 프로젝트 기반 세팅 완료 (빌드 설정, Entity, Flyway, Security, 레이아웃) | - |
| 2026-05-16 | Phase 2 | 템플릿 핵심 기능 완료 (CRUD API, 편집기 UI, TDD) | - |
| 2026-05-16 | Phase 3 | 프롬프트 실행 화면 완료 (슬롯 폼, 미리보기, 복사) | - |
| 2026-05-16 | Phase 4 | 정리 기능 완료 (카테고리/태그/검색/즐겨찾기) | - |
| 2026-05-16 | Phase 5 | 편의 기능 완료 (복제, 내보내기/가져오기, 히스토리) | - |
