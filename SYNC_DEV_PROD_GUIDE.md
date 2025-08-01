# 🔄 개발/운영 환경 테이블 동기화 가이드

## 📋 현재 상황
- **개발 환경**: `unble_dev` 스키마
- **운영 환경**: `unble_prod` 스키마  
- **문제**: 두 환경의 테이블 구조가 다를 수 있음

## 🎯 목표
개발과 운영 환경의 테이블 구조를 완전히 동일하게 맞추기

## 🚀 실행 순서

### 1. 현재 상태 확인
먼저 두 환경의 테이블 구조를 비교합니다.

### 2. SQL 스크립트 실행
`sync-dev-prod-tables.sql` 파일의 내용을 데이터베이스에서 실행합니다.

### 3. 생성되는 테이블들

#### 📊 **Users** (사용자)
```sql
- id (PK, BIGSERIAL)
- email (UNIQUE, VARCHAR(255))
- password (VARCHAR(255))  
- name (VARCHAR(100))
- is_active (BOOLEAN, DEFAULT true)
- last_login_at (TIMESTAMP)
- created_at, updated_at (TIMESTAMP)
```

#### 🏷️ **Categories** (카테고리)
```sql
- id (PK, BIGSERIAL)
- name (VARCHAR(100))
- color (VARCHAR(7), DEFAULT '#000000')
- icon (VARCHAR(50), DEFAULT '💰')
- category_type (ENUM: INCOME/EXPENSE/ASSET)
- is_default (BOOLEAN, DEFAULT false)
- sort_order (INTEGER, DEFAULT 0)
- user_id (FK to users)
- created_at (TIMESTAMP)
```

#### 💰 **Transactions** (거래)
```sql
- id (PK, BIGSERIAL)
- user_id (FK to users, NOT NULL)
- category_id (FK to categories, NOT NULL)
- amount (DECIMAL(15,2), NOT NULL)
- description (TEXT)
- transaction_type (ENUM: INCOME/EXPENSE)
- transaction_date (DATE, NOT NULL)
- created_at, updated_at (TIMESTAMP)
```

#### ⚙️ **User_Settings** (사용자 설정)
```sql
- id (PK, BIGSERIAL)
- user_id (FK to users, UNIQUE)
- currency (VARCHAR(3), DEFAULT 'KRW')
- theme (VARCHAR(20), DEFAULT 'light')
- notification_enabled (BOOLEAN, DEFAULT true)
- created_at, updated_at (TIMESTAMP)
```

#### 📅 **Budget_Plans** (예산 계획)
```sql
- id (PK, BIGSERIAL)
- user_id (FK to users)
- category_id (FK to categories)
- budget_month (DATE)
- planned_amount (DECIMAL(15,2))
- actual_amount (DECIMAL(15,2), DEFAULT 0)
- created_at, updated_at (TIMESTAMP)
```

### 4. 기본 카테고리 (23개)

#### 🏦 자산 관리 (5개)
- 현금 💰, 은행예금 🏦, 적금 💎, 투자 📈, 카드잔액 💳

#### 💸 지출 카테고리 (10개)  
- 식비 🍽️, 교통비 🚗, 생활용품 🛒, 의료비 🏥, 문화생활 🎬
- 통신비 📱, 주거비 🏠, 보험료 🛡️, 교육비 📚, 기타지출 💸

#### 💼 수입 카테고리 (4개)
- 급여 💼, 부업 💻, 투자수익 📊, 기타수입 💰

### 5. 인덱스 최적화
- 사용자별 데이터 조회 최적화
- 날짜별 거래 검색 최적화  
- 카테고리별 필터링 최적화

## ✅ 실행 후 확인사항

1. **테이블 개수**: 개발/운영 환경 모두 5개 테이블
2. **기본 카테고리**: 23개 동일하게 생성
3. **외래키 제약조건**: 모든 관계 설정 완료
4. **인덱스**: 성능 최적화 인덱스 생성

## 🎯 최종 결과

- ✅ **unble_dev**: 개발 환경 (완전한 테이블 구조)
- ✅ **unble_prod**: 운영 환경 (개발환경과 동일한 구조)
- 🔄 **동기화 완료**: 두 환경 완전 일치

## 🔧 애플리케이션 설정 확인

### application-dev.properties
```properties
spring.datasource.url=jdbc:postgresql://ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app/untab?currentSchema=unble_dev
spring.jpa.properties.hibernate.default_schema=unble_dev
```

### application-prod.properties  
```properties
spring.datasource.url=jdbc:postgresql://ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app/untab?currentSchema=unble_prod
spring.jpa.properties.hibernate.default_schema=unble_prod
```

이제 개발과 운영 환경이 완전히 동일한 테이블 구조를 가지게 됩니다!