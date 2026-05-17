# Promeditor 코딩 규칙 상세 가이드

---

## 1. 컨트롤러 규칙

### 1-1. 뷰 컨트롤러와 API 컨트롤러를 반드시 분리한다

모든 기능은 `@Controller`(뷰)와 `@RestController`(API) 두 개를 만든다.
Thymeleaf는 빈 껍데기만 담당하고, 비즈니스 데이터는 REST API에서 AJAX로 가져온다.

```
[브라우저] --GET /xxx--> [@Controller] --> xxx.html (빈 레이아웃)
[브라우저] --AJAX-----> [@RestController] --> Service --> DB --> JSON
```

**이유**: 나중에 React로 프론트를 교체할 때 REST API를 그대로 재사용할 수 있다.

### 1-2. 패키지 분리

| 종류 | 패키지 | 어노테이션 | 역할 |
|------|--------|-----------|------|
| 뷰 컨트롤러 | `{domain}/controller/` | `@Controller` | Thymeleaf 뷰 이름만 반환 |
| API 컨트롤러 | `{domain}/controller/api/` | `@RestController` | JSON 데이터 반환 |

### 1-3. 뷰 컨트롤러 작성법

```java
@Controller
public class XxxController {

    @GetMapping("/xxx")
    public String xxx() {
        return "xxx";  // templates/xxx.html
    }
}
```

- `Model` 파라미터를 사용하지 않는다 (비즈니스 데이터를 넣지 않음)
- Thymeleaf의 `sec:authorize`, `th:action`, `th:href` 등 보안/라우팅 용도만 허용

### 1-4. API 컨트롤러 작성법

```java
@RestController
@RequestMapping("/api/xxx")
@RequiredArgsConstructor
public class XxxApiController {

    private final XxxService xxxService;

    @GetMapping
    public ResponseEntity<XxxDto> getXxx() {
        return ResponseEntity.ok(xxxService.getXxxFacade());
    }
}
```

- URL 패턴: `/api/{도메인}/**`
- 반환 타입: `ResponseEntity<T>`
- 컨트롤러는 **Service의 Facade 메서드만 호출**한다 (Repository 직접 호출 금지)

### 1-5. Thymeleaf에서 데이터 로딩

```html
<h1 id="data-field">Loading...</h1>

<script>
document.addEventListener('DOMContentLoaded', function () {
    fetch('/api/xxx')
        .then(function (res) { return res.json(); })
        .then(function (data) {
            document.getElementById('data-field').textContent = data.name;
        })
        .catch(function (err) {
            console.error('로딩 실패:', err);
        });
});
</script>
```

---

## 2. Facade 서비스 규칙

### 2-1. 개요

컨트롤러가 호출하는 서비스는 **Facade 디자인 패턴**을 사용한다.
Facade는 **클래스명이 아닌 메서드명**에 `Facade` 접미사를 붙여 진입점임을 명시한다.
Facade 메서드가 전체 비즈니스 흐름을 조율하고, 세부 로직은 `this.xxx()` private 메서드로 분리한다.

```
Controller → XxxService.xxxFacade()        ← 공개 Facade 메서드
                 ├── this.stepA()   (private)
                 ├── this.stepB()   (private)
                 └── this.stepC()   (private)
                        └── Repository 호출
```

### 2-2. 네이밍 규칙

| 대상 | 네이밍 | 예시 |
|------|--------|------|
| 클래스 | `XxxService` (일반 서비스명) | `TemplateService`, `MemberService` |
| Facade 메서드 | `xxxFacade()` (Facade 접미사) | `createTemplateFacade()`, `getTemplateListFacade()` |
| Private 메서드 | `this.xxx()` (역할 기반) | `this.findTemplate()`, `this.validateSlots()` |

> `Facade` 뒤에 `Service`는 붙이지 않는다. 이미 Service 클래스 안에 있으므로 중복이다.

### 2-3. Facade 메서드 (공개 흐름) — 원시적으로 작성

전체 비즈니스 흐름을 **for문, if문 등 기본 제어문**으로 작성한다.
Stream, 람다, 메서드 체이닝을 사용하지 않는다.
**누구나 위에서 아래로 읽으면 전체 흐름이 파악되는 것**이 목표다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final SlotRepository slotRepository;

    public TemplateResultDto createTemplateFacade(TemplateCreateRequest request) {
        // 1. 템플릿 저장
        Template template = this.saveTemplate(request);

        // 2. 슬롯 저장
        for (SlotRequest slotRequest : request.getSlots()) {
            this.saveSlot(template, slotRequest);
        }

        // 3. 결과 반환
        TemplateResultDto result = this.buildResult(template);
        return result;
    }
}
```

### 2-4. Private 메서드 (this.xxx) — 모던 Java 스타일

역할과 구현을 분리하는 원칙에 따라, 세부 구현은 private 메서드에서 **모던 Java 스타일**(람다, Stream, Optional 등)로 간결하게 작성한다.

**반드시 `/** */` Javadoc 주석으로 시작**하여 역할을 설명한다.

```java
    /** 템플릿 ID로 템플릿을 조회한다. 존재하지 않으면 예외 발생. */
    private Template findTemplate(Long templateId) {
        log.info("findTemplate 시작 - templateId: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NoSuchElementException("템플릿 없음: " + templateId));

        log.info("findTemplate 종료 - templateId: {}, template: {}", templateId, template);
        return template;
    }

    /** 요청 DTO로 템플릿 엔티티를 생성하고 저장한다. */
    private Template saveTemplate(TemplateCreateRequest request) {
        log.info("saveTemplate 시작 - title: {}", request.getTitle());

        Template template = Template.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        Template saved = templateRepository.save(template);

        log.info("saveTemplate 종료 - title: {}, templateId: {}", request.getTitle(), saved.getId());
        return saved;
    }

    /** 슬롯을 생성하고 저장한다. */
    private void saveSlot(Template template, SlotRequest slotRequest) {
        log.info("saveSlot 시작 - templateId: {}, slotName: {}", template.getId(), slotRequest.getName());

        Slot slot = Slot.builder()
                .template(template)
                .name(slotRequest.getName())
                .slotType(slotRequest.getSlotType())
                .build();
        slotRepository.save(slot);

        log.info("saveSlot 종료 - templateId: {}, slotName: {}", template.getId(), slotRequest.getName());
    }

    /** 저장된 템플릿으로 결과 DTO를 조립한다. */
    private TemplateResultDto buildResult(Template template) {
        log.info("buildResult 시작 - templateId: {}", template.getId());

        TemplateResultDto result = TemplateResultDto.builder()
                .templateId(template.getId())
                .title(template.getTitle())
                .build();

        log.info("buildResult 종료 - templateId: {}, result: {}", template.getId(), result);
        return result;
    }
```

### 2-5. Private 메서드 로깅 규칙

Private 메서드는 **시작과 종료 시 `log.info()` 로그를 반드시 남긴다.**

| 시점 | 로그 내용 |
|------|----------|
| 메서드 시작 | `"메서드명 시작"` + 입력 파라미터를 키-값으로 |
| 메서드 종료 | `"메서드명 종료"` + 입력 파라미터를 키-값으로 + 리턴 값을 키-값으로 |

> 리턴 타입이 `void`인 경우 종료 로그에 리턴 값은 생략한다.

> **참고**: Service 클래스에 `@Slf4j` (Lombok) 어노테이션을 선언하여 `log` 객체를 사용한다.

### 2-6. Facade 규칙 요약

| 구분 | Facade 메서드 (`xxxFacade()`) | Private 메서드 (`this.xxx()`) |
|------|-------------------------------|-------------------------------|
| 코딩 스타일 | for, if 등 원시적 제어문 | 람다, Stream, Optional 등 모던 Java |
| 목적 | 전체 흐름 파악 (가독성) | 간결한 구현 (효율성) |
| 주석 | 단계별 번호 주석 (`// 1. xxx`) | `/** */` Javadoc으로 역할 설명 |
| 람다/체이닝 | 사용 금지 | 적극 사용 |
| 로깅 | 없음 | `log.info()` 시작/종료 로그 필수 |
| 접근 제어 | `public` (컨트롤러가 호출) | `private` (Facade만 호출) |

---

## 3. TDD 규칙

### 3-1. 개발 순서 (Red → Green → Refactor)

```
1. 테스트 작성 (Red)    → 실패하는 테스트를 먼저 작성
2. 최소 구현 (Green)    → 테스트를 통과하는 최소한의 코드 작성
3. 리팩토링 (Refactor)  → 테스트가 통과하는 상태에서 코드 개선
```

**Controller와 Service는 반드시 이 순서를 따른다.**

### 3-2. 테스트 대상과 방식

| 대상 | 테스트 방식 | 핵심 어노테이션 | TDD 필수 |
|------|-----------|----------------|----------|
| REST 컨트롤러 | 슬라이스 테스트 | `@WebMvcTest` + `MockMvc` | O |
| Service (Facade 메서드) | 단위 테스트 | `@ExtendWith(MockitoExtension.class)` | O |
| Repository | 통합 테스트 (필요시) | `@DataJpaTest` + Testcontainers | X (선택) |

> **Service TDD는 Facade 메서드(공개 메서드) 단위로 작성한다.**
> Facade 내부 private 메서드(`this.xxx()`)는 Facade 테스트를 통해 간접적으로 검증된다.

### 3-3. REST 컨트롤러 테스트 예시

```java
@WebMvcTest(TemplateApiController.class)
class TemplateApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // Spring Boot 4.x: @MockBean 대신 @MockitoBean 사용
    private TemplateService templateService;

    @Test
    void getTemplate_정상요청_200응답() throws Exception {
        // given
        TemplateResultDto dto = TemplateResultDto.builder()
                .templateId(1L)
                .title("테스트 템플릿")
                .build();
        given(templateService.getTemplateFacade(1L)).willReturn(dto);

        // when & then
        mockMvc.perform(get("/api/templates/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("테스트 템플릿"));
    }
}
```

### 3-4. Service (Facade 메서드) 테스트 예시

```java
@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TemplateService templateService;

    @Test
    void getTemplateFacade_데이터존재_정상반환() {
        // given
        Template template = Template.builder()
                .id(1L)
                .title("테스트 템플릿")
                .build();
        given(templateRepository.findById(1L)).willReturn(Optional.of(template));

        // when
        TemplateResultDto result = templateService.getTemplateFacade(1L);

        // then
        assertThat(result.getTitle()).isEqualTo("테스트 템플릿");
        verify(templateRepository).findById(1L);
    }

    @Test
    void getTemplateFacade_데이터없음_예외발생() {
        // given
        given(templateRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> templateService.getTemplateFacade(999L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
```

### 3-5. 테스트 작성 규칙

**파일 위치**: 본체와 동일한 패키지 구조

```
src/main/java/.../template/service/TemplateService.java
src/test/java/.../template/service/TemplateServiceTest.java

src/main/java/.../template/controller/api/TemplateApiController.java
src/test/java/.../template/controller/api/TemplateApiControllerTest.java
```

### 3-6. 메서드 네이밍과 구조

**메서드 네이밍**: `메서드명_조건_기대결과`

```java
// 좋은 예
void getTemplateFacade_정상요청_템플릿반환()
void getTemplateFacade_데이터없음_예외발생()
void createTemplateFacade_슬롯포함_저장성공()

// 나쁜 예
void test1()
void testGetTemplate()
```

**Given-When-Then 구조를 반드시 사용한다**

```java
@Test
void 메서드명_조건_기대결과() {
    // given - 테스트 데이터 준비 및 Mock 설정

    // when - 테스트 대상 실행

    // then - 결과 검증
}
```

### 3-7. 새 기능 개발 시 체크리스트

```
1. [ ] Service Facade 메서드 테스트 작성 (Red)
2. [ ] Service Facade 메서드 + private 메서드 구현 (Green)
3. [ ] Service 리팩토링
4. [ ] Controller 테스트 작성 (Red)
5. [ ] Controller 구현 (Green)
6. [ ] Controller 리팩토링
7. [ ] 전체 테스트 통과 확인 (./gradlew test)
```

---

## 4. 패키지 구조

```
src/main/java/com/ddoongddak/promeditor/
├── config/                      # 공통 설정 클래스 (SecurityConfig, UpperCaseNamingStrategy 등)
└── {domain}/                    # 도메인별 패키지 (예: template, member)
    ├── controller/              # @Controller (Thymeleaf 뷰 반환)
    │   └── api/                 # @RestController (JSON 반환)
    ├── service/                 # XxxService (Facade 메서드 + private 하위 메서드)
    ├── repository/              # Spring Data JPA Repository 인터페이스
    ├── dto/                     # 요청/응답 DTO (record 또는 class + Lombok)
    └── entity/                  # JPA Entity (@Entity)

src/test/java/com/ddoongddak/promeditor/
└── {domain}/                    # 도메인별 테스트 (본체 패키지 구조와 동일)
    ├── controller/
    │   └── api/                 # @WebMvcTest 컨트롤러 테스트
    └── service/                 # Service (Facade 메서드) 단위 테스트

src/main/resources/
├── templates/
│   └── {domain}/                # 도메인별 Thymeleaf HTML (예: template/list.html)
├── static/{css,js,images}/
├── db/migration/                # Flyway 마이그레이션 SQL
│   └── V{버전}__{설명}.sql       # 예: V1__init_schema.sql
├── application.yml
└── application-dev.yml
```

---

## 5. 기타 규칙

### Security
- 공개 페이지(뷰)와 공개 API는 `SecurityConfig`에서 `permitAll()` 설정
- 인증이 필요한 API는 별도 경로로 분리 (예: `/api/admin/**`)

### DTO
- 요청/응답에는 Entity를 직접 노출하지 않고 DTO를 사용한다
- 단순 읽기 전용 DTO는 Java `record` 사용, 수정이 필요한 경우 `class` + Lombok

### JPA Repository
- `JpaRepository<Entity, ID>` 를 상속하여 사용한다
- 복잡한 조회는 `@Query` (JPQL) 또는 Querydsl 사용
- 직접 EntityManager를 사용하지 않는다

---

## 6. DB 규칙

### 6-1. 테이블명 / 컬럼명은 UPPER_SNAKE_CASE

| 대상 | 규칙 | 예시 |
|------|------|------|
| 테이블명 | `UPPER_SNAKE_CASE` | `TEMPLATES`, `TEMPLATE_SLOTS` |
| 컬럼명 | `UPPER_SNAKE_CASE` | `TITLE`, `SLOT_TYPE`, `CREATED_AT` |

**이유**: DB 툴에서 테이블/컬럼을 한눈에 식별하기 쉽고, Java 필드명(camelCase)과 명확히 구분된다.

### 6-2. JPA Entity 설정

테이블명은 `@Table(name = "UPPER_SNAKE_CASE")`로 명시한다.
컬럼명은 `UpperCaseNamingStrategy`가 자동 변환하므로 `@Column(name = ...)` 생략 가능하다.

```java
@Entity
@Table(name = "TEMPLATES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // → ID (자동 변환)

    @Column(nullable = false, length = 100)
    private String title;       // → TITLE (자동 변환)

    @Column(columnDefinition = "TEXT")
    private String description; // → DESCRIPTION (자동 변환)

    @Column(nullable = false)
    private LocalDateTime createdAt;  // → CREATED_AT (자동 변환)

    @Builder
    public Template(String title, String description) {
        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}
```

### 6-3. UpperCaseNamingStrategy 설정

`src/main/java/com/ddoongddak/promeditor/config/UpperCaseNamingStrategy.java`:

```java
package com.ddoongddak.promeditor.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class UpperCaseNamingStrategy extends PhysicalNamingStrategySnakeCaseImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        Identifier id = super.toPhysicalTableName(logicalName, jdbcEnvironment);
        return id == null ? null : Identifier.toIdentifier(id.getText().toUpperCase(), id.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        Identifier id = super.toPhysicalColumnName(logicalName, jdbcEnvironment);
        return id == null ? null : Identifier.toIdentifier(id.getText().toUpperCase(), id.isQuoted());
    }
}
```

`application.yml`에 등록:

```yaml
spring:
  jpa:
    hibernate:
      naming:
        physical-strategy: com.ddoongddak.promeditor.config.UpperCaseNamingStrategy
```

### 6-4. Flyway 마이그레이션

`schema.sql` / `data.sql` 대신 **Flyway**를 사용한다.

- 파일 위치: `src/main/resources/db/migration/`
- 파일명 규칙: `V{버전}__{설명}.sql` (버전은 정수, 설명은 snake_case)
- 예: `V1__init_schema.sql`, `V2__add_template_slots.sql`

```sql
-- V1__init_schema.sql (PostgreSQL 문법)
CREATE TABLE IF NOT EXISTS TEMPLATES (
    ID          BIGSERIAL    NOT NULL,
    TITLE       VARCHAR(100) NOT NULL,
    DESCRIPTION TEXT,
    CREATED_AT  TIMESTAMP    NOT NULL,
    UPDATED_AT  TIMESTAMP    NOT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS TEMPLATE_SLOTS (
    ID          BIGSERIAL   NOT NULL,
    TEMPLATE_ID BIGINT      NOT NULL,
    NAME        VARCHAR(50) NOT NULL,
    SLOT_TYPE   VARCHAR(20) NOT NULL,  -- TEXT, TEXTAREA, SELECT
    SORT_ORDER  INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (ID),
    FOREIGN KEY (TEMPLATE_ID) REFERENCES TEMPLATES(ID)
);
```
