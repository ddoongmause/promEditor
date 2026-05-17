# Promeditor 프로젝트 코딩 룰
## 대화 방식
- 한글로 물으면 한글로 답하기

## 기술 스택
- Spring Boot 4.0.6 / Java 25 LTS / Gradle Kotlin DSL
- PostgreSQL (Docker Compose) / Spring Data JPA (Hibernate) / Flyway
- Thymeleaf + thymeleaf-extras-springsecurity6 / SpringDoc OpenAPI
- WebSocket / HATEOAS / Cache / Actuator / Validation
- Lombok (`@Slf4j`, `@RequiredArgsConstructor` 등)
- 테스트: JUnit 5 + MockMvc + Mockito + Testcontainers

## 컨트롤러 규칙
- 모든 기능은 `@Controller` (뷰)와 `@RestController` (API) 를 **분리**하여 작성한다
- `@Controller`는 Thymeleaf 뷰(빈 껍데기)만 반환한다. Model에 비즈니스 데이터를 넣지 않는다
- `@RestController`가 JSON 데이터를 반환한다. Thymeleaf에서는 AJAX(fetch)로 호출한다
- 뷰 컨트롤러: `{domain}/controller/` 패키지, API 컨트롤러: `{domain}/controller/api/` 패키지
- 이유: 향후 React 전환 시 REST API를 그대로 재사용하기 위함

## Facade 서비스 규칙
- 컨트롤러가 호출하는 서비스는 **Facade 패턴**을 사용한다
- Facade는 **클래스명이 아닌 메서드명**에 붙인다
- 클래스명: `XxxService` (예: `OrderService`) — 일반적인 서비스 네이밍
- Facade 메서드명: `xxxFacade()` (예: `createOrderFacade()`) — 진입점임을 명시
- Facade 메서드: 전체 흐름을 **for문, if문 등 원시적인 제어문**으로 작성. Stream/람다 체인 사용 금지
- Facade 내부 private 메서드(`xxx()`): Java 스타일로 작성하고, 시작에 `/** */` Javadoc 주석으로 역할을 설명한다
- Private 메서드는 **시작과 종료 시 `log.info()` 로그**를 남긴다
  - 시작: `log.info("메서드명 시작 - key: {}", value)` — 메서드 진입 직후
  - 종료: `log.info("메서드명 종료 - key: {}, result: {}", value, result)` — return 직전
- 로깅: Lombok `@Slf4j` 사용
- 컨트롤러 → Service.xxxFacade() → (private 하위 메서드들) → Repository

## TDD 규칙
- Controller와 Service는 **반드시 테스트를 먼저 작성**한 후 구현한다
- REST 컨트롤러 테스트: `@WebMvcTest` + `MockMvc` (슬라이스 테스트)
- Service 테스트: `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` (단위 테스트)
- Mock 주입: Spring Boot 4.x부터 `@MockBean` 대신 `@MockitoBean` 사용
- **TDD 대상은 Facade 메서드(공개 메서드)만** (내부 private 메서드는 Facade 테스트로 간접 검증)
- 테스트 파일 위치는 본체와 동일한 패키지 구조를 따른다
- 테스트 메서드명: `메서드명_조건_기대결과` 형식 (한글 허용)

## 패키지 구조
```
src/main/java/com/ddoongddak/promeditor/
├── config/                  # 공통 설정 (Security, NamingStrategy 등)
└── {domain}/                # 도메인별 패키지 (예: template, member)
    ├── controller/          # @Controller (뷰 반환)
    │   └── api/             # @RestController (JSON 반환)
    ├── service/             # XxxService (Facade 메서드 + private 하위 메서드)
    ├── repository/          # Spring Data JPA Repository 인터페이스
    ├── dto/                 # 요청/응답 DTO (record 또는 class)
    └── entity/              # JPA Entity (@Entity)

src/test/java/com/ddoongddak/promeditor/
└── {domain}/                # 도메인별 테스트 (본체 패키지 구조와 동일)
```

## DB 규칙
- 테이블명과 컬럼명은 **UPPER_SNAKE_CASE**를 사용한다 (예: `MEMBERS`, `AGREED_TO_TERMS`)
- JPA Entity는 `@Table(name = "UPPER_SNAKE_CASE")`로 테이블명을 명시한다
- 컬럼명 자동 변환은 `UpperCaseNamingStrategy` (config 패키지)가 담당한다 — 별도 `@Column(name = ...)` 불필요
- DB 마이그레이션은 **Flyway**를 사용한다 (`src/main/resources/db/migration/V{버전}__{설명}.sql`)
- DDL도 동일하게 대문자로 작성한다 (PostgreSQL 문법 사용)

## 상세 규칙
자세한 내용은 `docs/coding-rules.md` 참고
