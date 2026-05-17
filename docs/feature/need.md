# Promeditor — 필요 기능 정리

## 1단계 — 핵심 (MVP)

### Template 도메인
- [ ] TemplateService — 템플릿 CRUD (생성/조회/수정/삭제)
- [ ] TemplateBlockService — 블록(고정 텍스트, 변수 슬롯) 관리
- [ ] TemplatePreviewService — 변수 입력값으로 완성된 프롬프트 조립 및 미리보기

## 2단계 — 정리 (템플릿이 쌓일 때)

### Category 도메인
- [ ] CategoryService — 카테고리/태그 CRUD
- [ ] TemplateCategoryService — 템플릿-카테고리 관계 관리

### Bookmark 도메인
- [ ] BookmarkService — 즐겨찾기 토글 및 목록 조회

### Search 도메인
- [ ] TemplateSearchService — 템플릿 검색 (이름, 내용, 태그)

## 3단계 — 편의 (나중에 추가)

### History 도메인
- [ ] InputHistoryService — 최근 입력값 기록 및 조회

### Snippet 도메인 (추후)
- [ ] SnippetService — 재사용 블록 라이브러리 관리
