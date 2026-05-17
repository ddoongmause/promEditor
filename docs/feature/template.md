# Promeditor — 서비스 기획서 및 개발 Phase 계획

## 서비스 개요

AI 프롬프트를 편하게 조립하고 복사할 수 있는 템플릿 에디터.  
고정 텍스트 사이에 변수 슬롯을 끼워 넣고, 슬롯만 채우면 완성된 프롬프트가 바로 나오는 구조.

---

## 핵심 구조

템플릿은 **블록의 조합**으로 구성된다.

```
[고정 텍스트] + [변수 슬롯] + [고정 텍스트] + [변수 슬롯] + [고정 텍스트]
```

변수 슬롯을 채우면 전체 텍스트가 조립되어 즉시 복사 가능한 상태가 된다.

---

## 변수 슬롯 타입

| 타입 | 설명 | 예시 |
|------|------|------|
| `TEXT` | 단순 한 줄 텍스트 입력 | 회사명, 언어 |
| `TEXTAREA` | 여러 줄 긴 텍스트 입력 | 요구사항 본문, 배경 설명 |
| `SELECT` | 드롭다운 선택 | 언어 → 한국어 / 영어 / 일본어 |

---

## 기능 목록 (우선순위별)

### 1단계 — 핵심 (MVP)

- [x] 템플릿 생성 / 편집 / 삭제
- [x] 고정 텍스트 + 변수 슬롯 혼합 구성 (`[이름]`, `[언어]` 등)
- [x] 슬롯 타입 지정 (TEXT / TEXTAREA / SELECT)
- [x] 변수 입력 → 미리보기 → 한번에 복사

### 2단계 — 정리 (템플릿이 쌓일 때)

- [x] 카테고리 / 태그 (예: 개발, 글쓰기, 디자인)
- [x] 검색
- [x] 즐겨찾기

### 3단계 — 편의 (나중에 추가)

- [x] 최근 입력값 히스토리 (같은 템플릿에 이전에 뭘 넣었는지)
- [x] 템플릿 복제
- [x] 가져오기 / 내보내기 (다른 기기 동기화용)

---

## 화면 구성 (초안)

### 템플릿 목록 화면
- [x] 템플릿 카드 리스트
- [x] 즐겨찾기 / 최근 사용 탭
- [x] 검색창
- [x] 새 템플릿 만들기 버튼

### 템플릿 편집 화면
- [x] 블록 단위 편집기 (고정 텍스트 블록 ↔ 슬롯 블록 추가/삭제/순서 변경)
- [x] 슬롯 설정 패널 (이름, 타입, SELECT면 선택지 목록)
- [x] 실시간 미리보기

### 프롬프트 사용 화면 (실행 화면)
- [x] 템플릿 선택 후 슬롯별 입력 폼 표시
- [x] 입력하는 즉시 미리보기에 반영
- [x] 복사 버튼 (전체 텍스트 클립보드 복사)

---

## 추후 고민할 것

- 여러 AI별 맞춤 설정: Claude용 / GPT용 접두어를 다르게 붙이는 기능
- 조각(Snippet) 라이브러리: "말투 지시", "출력 형식" 같은 재사용 블록을 별도로 관리하고 템플릿에 골라 끼워넣기
- 공유 기능: 템플릿 URL 공유

---

## 개발 Phase 계획 및 현황

### 개발 현황 요약

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

---

## 구현 검증 결과

> template.md 기획서 대비 실제 코드베이스 검증 (2026-05-17)

### ✅ 검증 통과 항목 (전체 12/12)

| # | 검증 항목 | 결과 | 비고 |
|---|----------|------|------|
| 1 | Entity: Template (id, title, description, category, isFavorite, createdAt, updatedAt, blocks, tags) | ✅ | `@Table(name = "TEMPLATES")` 명시 |
| 2 | Entity: TemplateBlock (id, template, blockType, content, slotType, sortOrder, options) | ✅ | `@Table(name = "TEMPLATE_BLOCKS")` 명시 |
| 3 | Entity: SlotOption (id, block, label, sortOrder) | ✅ | `@Table(name = "SLOT_OPTIONS")` 명시 |
| 4 | Entity: TemplateTag, TemplateHistory 추가 | ✅ | Phase 4/5에서 추가 |
| 5 | Enum: BlockType(FIXED, SLOT), SlotType(TEXT, TEXTAREA, SELECT) | ✅ | |
| 6 | Repository: Template, TemplateBlock, SlotOption, TemplateTag, TemplateHistory | ✅ | TemplateRepository에 search/findByIsFavoriteTrue JPQL |
| 7 | Service: Facade 메서드 13종 (CRUD 5 + 검색/즐겨찾기 3 + 편의 5) | ✅ | for/if 제어문 사용, Stream/람다 체인 없음 |
| 8 | Service: private 메서드 시작/종료 log.info | ✅ | Lombok @Slf4j 사용 |
| 9 | Controller: TemplateController(뷰 4종) + TemplateApiController(API 12종) | ✅ | 뷰/REST 분리, api/ 패키지 구조 |
| 10 | DTO: Request 4종 + Response 5종 | ✅ | @Valid/@NotBlank/@NotNull 검증 |
| 11 | Flyway: V1(초기) + V2(카테고리/태그) + V3(히스토리) | ✅ | UPPER_SNAKE_CASE 테이블명 |
| 12 | Frontend: list.html, edit.html, use.html + JS 3종 | ✅ | AJAX 기반, 검색 디바운스, 히스토리 |

### ✅ 테스트 검증 (전체 4/4)

| # | 테스트 파일 | 테스트 수 | 결과 |
|---|-----------|----------|------|
| 1 | TemplateServiceTest | 7 | ✅ @ExtendWith(MockitoExtension.class) + @Mock/@InjectMocks |
| 2 | TemplateServicePhase4Test | 6 | ✅ |
| 3 | TemplateServicePhase5Test | 3 | ✅ |
| 4 | TemplateApiControllerTest | 5 | ✅ @WebMvcTest + @MockitoBean (Spring Boot 4.x) |

### ✅ TDD 규칙 준수 확인

- Facade 메서드만 테스트 대상 (private 메서드는 간접 검증)
- 테스트 메서드명: `메서드명_조건_기대결과` (한글 허용)
- REST 컨트롤러: @WebMvcTest + MockMvc (슬라이스 테스트)
- Service: @ExtendWith(MockitoExtension.class) + @Mock/@InjectMocks (단위 테스트)
- @MockitoBean 사용 (Spring Boot 4.x에서 @MockBean 대체)

### ✅ 패키지 구조 준수 확인

```
src/main/java/com/ddoongddak/promeditor/
├── config/              # SecurityConfig, UpperCaseNamingStrategy
├── common/              # ApiResponse, GlobalExceptionHandler
└── template/
    ├── controller/      # TemplateController (뷰)
    ├── controller/api/  # TemplateApiController (REST)
    ├── service/         # TemplateService (Facade)
    ├── repository/      # Repository 5종
    ├── dto/             # DTO 9종
    └── entity/         # Entity 7종 (Template, TemplateBlock, SlotOption, TemplateTag, TemplateHistory, BlockType, SlotType)
```

### ✅ DB 규칙 준수 확인

- 테이블명: UPPER_SNAKE_CASE (TEMPLATES, TEMPLATE_BLOCKS, SLOT_OPTIONS, TEMPLATE_TAGS, TEMPLATE_HISTORIES)
- `@Table(name = "UPPER_SNAKE_CASE")` 명시
- UpperCaseNamingStrategy로 컬럼명 자동 변환
- Flyway 마이그레이션: V1 → V2 → V3 순차적

### ✅ 컨트롤러 규칙 준수 확인

- @Controller (TemplateController): Thymeleaf 뷰만 반환, Model에 비즈니스 데이터 없음
- @RestController (TemplateApiController): JSON 데이터 반환 (ApiResponse<T> 래핑)
- 패키지 분리: controller/ vs controller/api/

### ✅ Facade 패턴 준수 확인

- 클래스명: TemplateService (일반 네이밍)
- Facade 메서드명: xxxFacade() (예: createTemplateFacade)
- Facade 내부: for/if 원시 제어문만, Stream/람다 체인 없음
- private 메서드: Java 스타일 + Javadoc + log.info 시작/종료
