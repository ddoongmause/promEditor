# Promeditor — 로그인 및 사용자 기능 명세서

## 1. 개요

Promeditor에 **사용자 계정**을 도입하여, 각 템플릿을 특정 사용자에게 연결하고, 삭제된 템플릿을 **휴지통**에서 복구할 수 있는 기능을 구현한다.

### 목적

- 템플릿을 사용자별로 저장하고 관리
- 삭제된 템플릿을 휴지통에서 확인하고 복구 (또는 영구 삭제)
- 향후 React 전환 시 REST API를 그대로 재사용

### 범위

- **신규**: 사용자 도메인 (`member`) — 가입, 로그인, 세션 관리
- **기존 확장**: 템플릿 도메인 (`template`) — 소유자 연결, 소프트 삭제, 휴지통
- **보안**: Spring Security 기반 인증/인가

---

## 2. 사용자흐름

### 2-1. 가입 → 로그인 → 템플릿 사용

```
[비회원]
  → 가입 (아이디, 비밀번호)
  → 로그인 (아이디, 비밀번호)
  → 템플릿 목록 (내 템플릿만 표시)
  → 템플릿 생성/편집/삭제
  → 휴지통에서 삭제 내역 확인
  → 복구 또는 영구 삭제
```

### 2-2. 비회원 접근

- 가입/로그인 페이지는 비회원도 접근 가능
- 템플릿 관련 기능은 로그인 후 사용 가능
- 비회원이 템플릿 페이지 접근 시 로그인 화면으로 리다이렉트

### 2-3. 휴지통 흐름

```
[템플릿 삭제]
  → TEMPLATES 테이블에서 DELETED_AT 설정 (소프트 삭제)
  → 휴지통 화면에서 삭제된 템플릿 목록 표시
  → [복구] → DELETED_AT = NULL (활성화)
  → [영구삭제] → DB에서 물리 삭제
```

---

## 3. 화면설계

### 3-1. 가입 화면 (`/member/register`)

| 요소 | 설명 |
|------|------|
| 아이디 입력 | 영문/숫자만, 4~30자 |
| 비밀번호 입력 | 4~50자 |
| 비밀번호 확인 | 일치 검증 |
| 가입 버튼 | POST `/api/member/register` |
| 에러 메시지 | 아이디 중복, 비밀번호 불일치 등 |

### 3-2. 로그인 화면 (`/member/login`)

| 요소 | 설명 |
|------|------|
| 아이디 입력 | |
| 비밀번호 입력 | |
| 로그인 버튼 | POST `/api/member/login` (AJAX) |
| 에러 메시지 | 아이디 또는 비밀번호가 틀렸습니다 |
| 가입 링크 | 비회원일 경우 가입 화면으로 이동 |

### 3-3. 템플릿 목록 화면 (기존 확장)

- 로그인한 사용자의 템플릿만 표시
- 기존 `/templates` 화면 유지, 조회 시 `ownerId` 필터 추가

### 3-4. 휴지통 화면 (`/templates/trash`)

| 요소 | 설명 |
|------|------|
| 삭제된 템플릿 목록 | 카드 리스트 (기존 목록과 동일한 UI) |
| 템플릿 정보 | 제목, 카테고리, 삭제일 표시 |
| [복구] 버튼 | 해당 템플릿을 활성 상태로 복원 |
| [영구삭제] 버튼 | DB에서 물리 삭제 (확인 다이얼로그) |
| [전체 영구삭제] 버튼 | 휴지통의 모든 템플릿을 영구 삭제 |

### 3-5. 네비게이션 변경

```
[헤더]
  - Promeditor (홈)
  - 템플릿 (내 템플릿 목록)
  - 휴지통 (삭제된 템플릿)
  - [사용자아이디] → 로그아웃 버튼
```

---

## 4. API설계

### 4-1. Member API (신규)

| HTTP | URL | 설명 | 요청 | 응답 |
|------|-----|------|------|------|
| POST | `/api/member/register` | 가입 | `MemberRegisterRequest` | `ApiResponse<MemberResponse>` (201) |
| POST | `/api/member/login` | 로그인 (AJAX) | `MemberLoginRequest` | `ApiResponse<MemberResponse>` (200) |
| GET | `/api/member/me` | 현재 사용자 정보 | - | `ApiResponse<MemberResponse>` (200) |
| POST | `/api/member/logout` | 로그아웃 | - | `ApiResponse<Void>` (204) |

### 4-2. Template API (기존 확장)

| HTTP | URL | 설명 | 변경 사항 |
|------|-----|------|----------|
| GET | `/api/templates` | 목록 조회 | `ownerId` 파라미터 추가 (기본값: 현재 사용자) |
| DELETE | `/api/templates/{id}` | 삭제 (소프트) | `deleteTemplateFacade()` → `softDeleteTemplateFacade()` 로 변경 |
| POST | `/api/templates/{id}/restore` | 휴지통 복구 (신규) | `restoreTemplateFacade()` |
| DELETE | `/api/templates/{id}/permanent` | 영구삭제 (신규) | `permanentDeleteTemplateFacade()` |
| GET | `/api/templates/trash` | 휴지통 목록 (신규) | `getTrashListFacade()` |
| DELETE | `/api/templates/trash` | 전체 영구삭제 (신규) | `permanentDeleteAllTrashFacade()` |

### 4-3. 요청/응답 DTO

#### MemberRegisterRequest
```
record MemberRegisterRequest(
    @NotBlank @Size(min=4, max=30) @Pattern(regexp="^[a-zA-Z0-9]+$") String id,
    @NotBlank @Size(min=4, max=50) String password,
    @NotBlank @Size(min=4, max=50) String passwordConfirm
)
```

#### MemberLoginRequest
```
record MemberLoginRequest(
    @NotBlank String id,
    @NotBlank String password
)
```

#### MemberResponse
```
record MemberResponse(
    Long id,
    String memberId,
    LocalDateTime createdAt
)
```

### 4-4. 인증/인가 설정

| URL 패턴 | 접근 권한 |
|----------|----------|
| `/member/login`, `/member/register` | 비회원 허용 |
| `/api/member/register`, `/api/member/login` | 비회원 허용 |
| `/api/member/me`, `/api/member/logout` | 로그인 필요 |
| `/api/templates/**` | 로그인 필요 |
| `/templates/**`, `/templates/trash` | 로그인 필요 |
| `/`, `/member/login`, `/member/register` (뷰) | 비회원 허용 |

---

## 5. 데이터모델

### 5-1. MEMBERS (신규)

```
MEMBERS
  - ID (PK, BIGSERIAL)
  - MEMBER_ID (VARCHAR(30), UNIQUE, NOT NULL)     ← 영문/숫자만
  - PASSWORD (VARCHAR(100), NOT NULL)              ← BCrypt 암호화
  - CREATED_AT (TIMESTAMP, NOT NULL)
```

### 5-2. TEMPLATES (기존 확장)

```
TEMPLATES (기존 컬럼 유지 + 신규 컬럼 추가)
  - ID (PK)
  - TITLE
  - DESCRIPTION
  - CATEGORY
  - IS_FAVORITE
  - CREATED_AT
  - UPDATED_AT
  - OWNER_ID (BIGINT, FK → MEMBERS.ID, NULLABLE)  ← 신규: 소유자
  - DELETED_AT (TIMESTAMP, NULLABLE)                ← 신규: 소프트 삭제
```

> **DELETED_AT**: NULL이면 활성, 값이 있으면 휴지통 상태

### 5-3. Flyway 마이그레이션

- `V4__add_members_and_template_ownership.sql`
  - MEMBERS 테이블 생성
  - TEMPLATES 테이블에 OWNER_ID, DELETED_AT 컬럼 추가

---

## 6. 코드구조

### 6-1. Member 도메인 (신규)

```
src/main/java/com/ddoongddak/promeditor/member/
├── controller/
│   ├── MemberController.java          # @Controller (뷰 반환)
│   └── api/
│       └── MemberApiController.java   # @RestController (JSON 반환)
├── service/
│   └── MemberService.java            # Facade 메서드 + private 메서드
├── repository/
│   └── MemberRepository.java         # Spring Data JPA Repository
├── dto/
│   ├── MemberRegisterRequest.java    # 가입 요청
│   ├── MemberLoginRequest.java       # 로그인 요청
│   └── MemberResponse.java           # 응답 DTO
└── entity/
    └── Member.java                   # JPA Entity

src/main/resources/templates/member/
├── register.html                     # 가입 화면
└── login.html                       # 로그인 화면
```

### 6-2. Template 도메인 (기존 확장)

기존 `template/` 패키지의 Entity, Service, Repository, DTO를 확장.

### 6-3. SecurityConfig (기존 수정)

기존 `permitAll()`에서 인증/인가 규칙으로 변경.

---

## 7. Facade설계

### 7-1. MemberService Facade 메서드

| Facade 메서드 | 설명 | 내부 private 메서드 |
|--------------|------|-------------------|
| `registerFacade(MemberRegisterRequest)` | 회원 가입 | `validateRegisterRequest()`, `checkDuplicateId()`, `encodePassword()`, `saveMember()` |
| `loginFacade(MemberLoginRequest)` | 로그인 (인증) | `findMemberById()`, `verifyPassword()`, `authenticateMember()` |
| `getMeFacade()` | 현재 사용자 정보 조회 | `getAuthenticatedMember()` |
| `logoutFacade()` | 로그아웃 (세션 무효화) | `invalidateSession()` |

### 7-2. TemplateService Facade 메서드 (신규/변경)

| Facade 메서드 | 설명 | 내부 private 메서드 |
|--------------|------|-------------------|
| `softDeleteTemplateFacade(Long templateId)` | 소프트 삭제 (기존 `deleteTemplateFacade` 변경) | `findTemplate()`, `checkOwnership()`, `markAsDeleted()` |
| `restoreTemplateFacade(Long templateId)` | 휴지통에서 복구 | `findDeletedTemplate()`, `checkOwnership()`, `restoreTemplate()` |
| `permanentDeleteTemplateFacade(Long templateId)` | 영구 삭제 | `findDeletedTemplate()`, `checkOwnership()`, `deleteTemplate()` |
| `getTrashListFacade()` | 휴지통 목록 조회 | `findDeletedTemplates()`, `convertToSummary()` |
| `permanentDeleteAllTrashFacade()` | 휴지통 전체 영구 삭제 | `findAllDeletedTemplates()`, `deleteAllTemplates()` |

### 7-3. 기존 TemplateService Facade 변경 사항

| 기존 메서드 | 변경 내용 |
|------------|----------|
| `getTemplateListFacade()` | `getTemplateListFacade(String ownerId)` — 소유자 필터 추가 |
| `deleteTemplateFacade()` | `softDeleteTemplateFacade()`로 이름 변경, 물리 삭제 → 소프트 삭제 |
| `getTemplateFacade()` | 삭제된 템플릿은 조회 불가 (DELETED_AT IS NULL 조건) |

---

## 8. TDD계획

### 8-1. MemberService 테스트

**파일**: `src/test/java/.../member/service/MemberServiceTest.java`

| # | 테스트 메서드 | 설명 |
|---|-------------|------|
| 1 | `registerFacade_정상요청_저장성공` | 아이디/비밀번호로 가입 시 Member 저장 |
| 2 | `registerFacade_아이디중복_예외발생` | 중복 아이디로 가입 시 예외 |
| 3 | `registerFacade_비밀번호4글자미만_검증실패` | 비밀번호 4글자 미만 시 검증 실패 |
| 4 | `loginFacade_정상요청_인증성공` | 올바른 아이디/비밀번호로 로그인 성공 |
| 5 | `loginFacade_잘못된비밀번호_예외발생` | 틀린 비밀번호로 로그인 시 예외 |
| 6 | `loginFacade_존재하지않는아이디_예외발생` | 없는 아이디로 로그인 시 예외 |
| 7 | `getMeFacade_로그인상태_사용자정보반환` | 로그인한 사용자 정보 조회 |

### 8-2. TemplateService (신규 Facade) 테스트

**파일**: `src/test/java/.../template/service/TemplateServiceTrashTest.java`

| # | 테스트 메서드 | 설명 |
|---|-------------|------|
| 1 | `softDeleteTemplateFacade_정상요청_삭제마크설정` | 템플릿 소프트 삭제 시 DELETED_AT 설정 |
| 2 | `softDeleteTemplateFacade_남의템플릿_예외발생` | 다른 사용자의 템플릿 삭제 시 예외 |
| 3 | `restoreTemplateFacade_삭제템플릿_복구성공` | 휴지통 템플릿 복구 시 DELETED_AT = NULL |
| 4 | `permanentDeleteTemplateFacade_삭제템플릿_물리삭제` | 영구 삭제 시 DB에서 제거 |
| 5 | `getTrashListFacade_삭제템플릿존재_목록반환` | 휴지통 목록 조회 |
| 6 | `getTrashListFacade_삭제템플릿없음_빈목록반환` | 휴지통이 비어있으면 빈 목록 |
| 7 | `permanentDeleteAllTrashFacade_휴지통전체삭제_모두제거` | 휴지통 전체 영구 삭제 |

### 8-3. MemberApiController 테스트

**파일**: `src/test/java/.../member/controller/api/MemberApiControllerTest.java`

| # | 테스트 메서드 | 설명 |
|---|-------------|------|
| 1 | `register_정상요청_201응답` | 가입 API 정상 응답 |
| 2 | `login_정상요청_200응답` | 로그인 API 정상 응답 |
| 3 | `login_잘못된정보_401응답` | 로그인 실패 시 401 |
| 4 | `me_로그인상태_200응답` | 현재 사용자 정보 조회 |
| 5 | `logout_정상요청_204응답` | 로그아웃 API |

### 8-4. TemplateApiController (신규 API) 테스트

**파일**: `src/test/java/.../template/controller/api/TemplateApiControllerTrashTest.java`

| # | 테스트 메서드 | 설명 |
|---|-------------|------|
| 1 | `restore_정상요청_200응답` | 복구 API |
| 2 | `permanentDelete_정상요청_204응답` | 영구 삭제 API |
| 3 | `getTrash_정상요청_200응답` | 휴지통 목록 API |
| 4 | `permanentDeleteAllTrash_정상요청_204응답` | 전체 영구 삭제 API |

---

## 9. 개발순서 (Phase)

### Phase A — Member 도메인 (가입/로그인)

1. [ ] `V4__add_members_and_template_ownership.sql` Flyway 마이그레이션
2. [ ] `Member` Entity 작성
3. [ ] `MemberRepository` 작성 (findById JPQL)
4. [ ] Member DTO 3종 작성 (MemberRegisterRequest, MemberLoginRequest, MemberResponse)
5. [ ] `MemberService` Facade 메서드 4종 + private 메서드 (TDD)
6. [ ] `MemberServiceTest` 작성 (7개 테스트)
7. [ ] `MemberApiController` 작성 (4개 엔드포인트)
8. [ ] `MemberApiControllerTest` 작성 (5개 테스트)
9. [ ] `MemberController` 작성 (2개 뷰: register, login)
10. [ ] `register.html` + `login.html` 작성
11. [ ] `SecurityConfig` 수정 (인증/인가 규칙 적용)

### Phase B — Template 도메인 확장 (소유자 + 휴지통)

1. [ ] `Template` Entity에 `ownerId`, `deletedAt` 필드 추가
2. [ ] `TemplateRepository`에 삭제 관련 쿼리 추가
3. [ ] `TemplateService` 기존 Facade 수정 (소유자 필터, 소프트 삭제)
4. [ ] `TemplateService` 신규 Facade 4종 + private 메서드 (TDD)
5. [ ] `TemplateServiceTrashTest` 작성 (7개 테스트)
6. [ ] `TemplateApiController` 신규 API 4종 추가
7. [ ] `TemplateApiControllerTrashTest` 작성 (4개 테스트)
8. [ ] `TemplateController`에 `/trash` 뷰 추가
9. [ ] `trash.html` + `template-trash.js` 작성
10. [ ] 기존 `list.html` 수정 (소유자 필터 반영)
11. [ ] 기존 `edit.html` 수정 (삭제 시 소프트 삭제 API 호출)

### Phase C — 통합 테스트 및 검증

1. [ ] 가입 → 로그인 → 템플릿 생성 → 삭제 → 휴지통 → 복구 전체 흐름 검증
2. [ ] `./gradlew test` 전체 테스트 통과 확인
3. [ ] SecurityConfig 인증/인가 규칙 검증 (비회원 접근 차단 확인)

---

## 10. 결정사항

### 10-1. 도메인 분리: `member` 도메인 신규 생성

**선택지 비교:**

| 옵션 | 내용 | 장단점 |
|------|------|--------|
| A. `member` 도메인 신규 (추천) | 독립적인 member 도메인 생성 | ✅ 도메인 경계 명확, 향후 역할/권한 확장 용이 |
| B. `template` 도메인에 포함 | Member를 template 도메인 하위에 포함 | ❌ 도메인 혼동, 단일 책임 원칙 위배 |

**결정**: **A** — `member` 도메인을 신규 생성

### 10-2. 휴지통 구현 방식: 소프트 삭제 (DELETED_AT)

**선택지 비교:**

| 옵션 | 내용 | 장단점 |
|------|------|--------|
| A. TEMPLATES에 DELETED_AT 추가 (추천) | 기존 테이블에 DELETED_AT 컬럼 추가. NULL=활성, 값=휴지통 | ✅ 스키마 단순, 조회 쿼리에서 IS NULL/IS NOT NULL로 분리, 복구 시 NULL로 되돌림 |
| B. 별도 DELETED_TEMPLATES 테이블 | 삭제 시 별도 테이블로 이동 | ❌ 테이블 2개 관리, 복구 시 테이블 간 이동 로직 필요 |
| C. IS_DELETED boolean + DELETED_AT | boolean 플래그 + 삭제일 | ⚠️ A와 유사하지만 boolean 불필요 (DELETED_AT의 NULL/NOT NULL로 충분) |

**결정**: **A** — TEMPLATES 테이블에 `DELETED_AT TIMESTAMP` 컬럼 추가

### 10-3. 로그인 방식: AJAX 기반 커스텀 로그인

**선택지 비교:**

| 옵션 | 내용 | 장단점 |
|------|------|--------|
| A. AJAX 로그인 (추천) | `/api/member/login` POST → JSON 응답 → JS로 세션 설정 | ✅ 기존 AJAX 기반 아키텍처와 일관, React 전환 시 그대로 사용 |
| B. Spring Security 폼 로그인 | 기본 로그인 폼 (`/login`) | ❌ Thymeleaf 폼 기반, React 전환 시 재작업 필요 |

**결정**: **A** — AJAX 로그인 API + Spring Security `AuthenticationManager` 직접 호출

### 10-4. 비밀번호 암호화: BCrypt

**선택지 비교:**

| 옵션 | 내용 | 장단점 |
|------|------|--------|
| A. BCrypt (추천) | Spring Security `BCryptPasswordEncoder` | ✅ 산업 표준, Spring Security와 통합, rainbow table 공격 방지 |
| B. Argon2 | 더 강력한 알고리즘 | ⚠️ 추가 의존성 필요, 현재 프로젝트에 BCrypt가 가장 적절 |

**결정**: **A** — `BCryptPasswordEncoder`

### 10-5. 템플릿 소유자: OWNER_ID FK

**선택지 비교:**

| 옵션 | 내용 | 장단점 |
|------|------|--------|
| A. TEMPLATES에 OWNER_ID FK (추천) | 기존 TEMPLATES 테이블에 OWNER_ID 컬럼 추가 | ✅ 기존 테이블 유지, 기존 템플릿은 OWNER_ID = NULL (공유 템플릿) |
| B. MEMBER_TEMPLATES 중간 테이블 | N:M 관계 테이블 생성 | ❌ 템플릿은 1명의 소유자만 가지는 것이므로 N:M 불필요 |

**결정**: **A** — TEMPLATES 테이블에 `OWNER_ID BIGINT (FK → MEMBERS.ID)` 추가

### 10-6. 기존 템플릿 처리

- Phase 1~5에서 생성된 기존 템플릿은 `OWNER_ID = NULL`로 유지
- 로그인 후 새로 생성하는 템플릿부터 `OWNER_ID` 설정
- 기존 템플릿은 모든 로그인한 사용자가 접근 가능 (공유 템플릿)
- 추후 필요시 "템플릿 소유자 변경" 기능 추가 가능

### 10-7. 아이디 형식: 영문/숫자만 (4~30자)

**선택지 비교:**

| 옵션 | 내용 | 장단점 |
|------|------|--------|
| A. 영문/숫자만 (추천) | `^[a-zA-Z0-9]+$`, 4~30자 | ✅ 일반적인 계정 형식과 일치, URL/파일명 등에 안전하게 사용 가능 |
| B. 한글 포함 | 한글, 영문, 숫자 모두 허용 | ❌ URL 인코딩 문제, DB 인덱스 성능 저하 가능성 |

**결정**: **A** — `@Pattern(regexp="^[a-zA-Z0-9]+$")` 검증

---

## 11. 개발현황

> 상태 아이콘: ✅ 완료 / 🟡 진행중 / ⬜ 대기 / ❌ 보류

| Phase | 이름 | 상태 | 시작일 | 완료일 |
|-------|------|------|--------|--------|
| Phase A | Member 도메인 (가입/로그인) | ⬜ 대기 | - | - |
| Phase B | Template 도메인 확장 (소유자 + 휴지통) | ⬜ 대기 | - | - |
| Phase C | 통합 테스트 및 검증 | ⬜ 대기 | - | - |

### 완료 이력

| 날짜 | Phase | 작업 내용 | 비고 |
|------|-------|----------|------|
| (미정) | Phase A | Member 도메인 전체 구현 | |
| (미정) | Phase B | Template 도메인 확장 | |
| (미정) | Phase C | 통합 검증 | |
