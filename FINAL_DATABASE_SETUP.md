# 🎯 최종 데이터베이스 설정 완료 가이드

## 📋 현재 상황
- **시스템**: Maven과 PostgreSQL 클라이언트 미설치
- **목표**: 3개 스키마를 2개로 정리하고 개발/운영 환경 동일하게 맞추기

## 🛠️ 준비된 솔루션들

### 1. 수동 실행 파일들
- `DB_CLEANUP_MANUAL.md` - 브라우저/pgAdmin으로 실행할 SQL 가이드
- `sync-dev-prod-tables.sql` - 완전한 테이블 동기화 스크립트
- `final-cleanup.sql` - 불필요한 스키마 제거 스크립트

### 2. 자동화 유틸리티들
- `SimpleDbChecker.java` - 영어로 작성된 데이터베이스 정리 유틸리티
- `DatabaseSyncUtil.java` - Spring Boot 시작 시 자동 동기화

## 🚀 실행 방법 (우선순위 순)

### 방법 1: Koyeb Dashboard (가장 쉬움) ⭐
1. https://app.koyeb.com 접속
2. Database 섹션 → SQL Query 도구
3. 아래 SQL을 순서대로 실행:

```sql
-- 1단계: 현재 상태 확인
SELECT schemaname FROM pg_namespace 
WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' OR nspname LIKE '%budget%'
ORDER BY schemaname;

-- 2단계: 불필요한 스키마 삭제
DROP SCHEMA IF EXISTS dev_schema CASCADE;
DROP SCHEMA IF EXISTS unble_budget_dev CASCADE;

-- 3단계: 운영 스키마 생성
CREATE SCHEMA IF NOT EXISTS unble_prod;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;

-- 4단계: 최종 확인
SELECT schemaname FROM pg_namespace 
WHERE nspname IN ('unble_dev', 'unble_prod')
ORDER BY schemaname;
```

### 방법 2: pgAdmin 4 설치 후 실행
1. https://www.pgadmin.org/download/ 설치
2. 연결 정보:
   - Host: `ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app`
   - Port: `5432`
   - Database: `untab`
   - Username: `untab`
   - Password: `0AbVNOIsl2dn`
3. `sync-dev-prod-tables.sql` 파일 실행

### 방법 3: Maven 설치 후 Spring Boot 실행
1. Maven 설치: https://maven.apache.org/download.cgi
2. `run.bat` 실행 → DatabaseSyncUtil이 자동으로 실행됨

## ✅ 최종 결과

### 삭제될 스키마
- ❌ `dev_schema` (제거)
- ❌ `unble_budget_dev` (제거)

### 남을 스키마
- ✅ `unble_dev` (개발 환경)
- ✅ `unble_prod` (운영 환경)

### 테이블 구조 (동일)
1. **users** - 사용자 정보
2. **categories** - 카테고리 (23개 기본 카테고리)
3. **transactions** - 거래 내역
4. **user_settings** - 사용자 설정
5. **budget_plans** - 예산 계획

## 🔧 Spring Boot 설정 확인

### application-dev.properties ✓
```properties
spring.datasource.url=...currentSchema=unble_dev
spring.jpa.properties.hibernate.default_schema=unble_dev
```

### application-prod.properties ✓
```properties
spring.datasource.url=...currentSchema=unble_prod
spring.jpa.properties.hibernate.default_schema=unble_prod
```

## 🎯 완료 후 테스트

1. **개발 환경**: `run.bat` 실행
2. **운영 환경**: `run-prod.bat` 실행
3. **접속**: http://localhost:9090
4. **관리자**: http://localhost:9090/admin.html

## 📞 문제 해결

- **연결 실패**: 스키마 이름 확인
- **테이블 없음**: Spring Boot가 자동 생성 (ddl-auto=update)
- **권한 오류**: GRANT 명령 재실행

---

**결론**: 가장 쉬운 방법은 **Koyeb Dashboard**에서 위의 4단계 SQL을 실행하는 것입니다. 
이후 Spring Boot 애플리케이션 실행 시 모든 테이블이 자동으로 동일하게 생성됩니다!