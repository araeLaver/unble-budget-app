# 🚀 Unble - 스마트 가계부 앱

혁신적인 가계부 관리 솔루션으로 개인과 가족의 재정을 체계적으로 관리할 수 있습니다.

## 📋 프로젝트 정보

- **프로젝트명**: Unble Budget App
- **버전**: 1.0.0
- **Java**: 17
- **Spring Boot**: 3.2.0
- **빌드 도구**: Maven
- **IDE**: Spring Tool Suite (STS) 최적화

## 🛠️ 기술 스택

### Backend
- **Spring Boot 3.2.0** - 메인 프레임워크
- **Spring Security** - JWT 기반 인증
- **Spring Data JPA** - 데이터 접근 계층
- **PostgreSQL** - 개발/운영 데이터베이스 (스키마 분리)
- **BigDecimal** - 정확한 금액 계산

### 데이터베이스 스키마
- **개발 환경**: `unble_dev` (최종)
- **운영 환경**: `unble_prod` (최종)

### Frontend
- **HTML5/CSS3/JavaScript** - 반응형 웹 인터페이스
- **Vanilla JS** - 프레임워크 없는 순수 자바스크립트

## 🚀 STS에서 실행하기

### 1. 프로젝트 Import
```
File > Import > Existing Maven Projects
Root Directory: C:\Develop\unble\unble-budget-app
```

### 2. Maven Dependencies 다운로드
```
우클릭 프로젝트 > Maven > Reload Projects
```

### 3. 애플리케이션 실행
```
UnbleBudgetApplication.java 우클릭 > Run As > Spring Boot App
```

## 🌐 접속 정보

- **웹 애플리케이션**: http://localhost:9090
- **관리자 페이지**: http://localhost:9090/admin.html
- **데이터베이스**: PostgreSQL (Koyeb Cloud)
  - 개발 스키마: `unble_dev`
  - 운영 스키마: `unble_prod`

## ✅ 주요 기능

### 인증 시스템
- 회원가입/로그인 (JWT 토큰 기반)
- 사용자별 데이터 격리
- 보안 필터링

### 가계부 관리
- 수입/지출 거래 추가, 수정, 삭제
- 8개 기본 카테고리 (식비, 교통비, 생활용품 등)
- 월별 수입/지출 요약 통계
- 거래 내역 조회 및 필터링

### 사용자 인터페이스
- 반응형 디자인 (모바일/태블릿/데스크톱)
- 직관적인 탭 기반 네비게이션
- 실시간 데이터 동기화

## 📊 데이터베이스 스키마

```sql
users (사용자)
├── id (PK)
├── email (Unique)
├── password (암호화)
├── name
├── is_active (활성화 상태)
├── last_login_at (마지막 로그인)
└── created_at, updated_at

categories (카테고리)
├── id (PK)
├── name
├── color, icon
├── category_type (INCOME/EXPENSE/ASSET)
├── is_default (기본 카테고리 여부)
├── sort_order (정렬 순서)
└── user_id (FK)

transactions (거래)
├── id (PK)
├── user_id (FK)
├── category_id (FK)
├── amount (DECIMAL 15,2)
├── description
├── transaction_type (INCOME/EXPENSE)
├── transaction_date
└── created_at, updated_at

user_settings (사용자 설정)
├── id (PK)
├── user_id (FK)
├── currency (통화)
├── theme (테마)
└── notification_enabled

budget_plans (예산 계획)
├── id (PK)
├── user_id (FK)
├── category_id (FK)
├── budget_month
├── planned_amount (계획 금액)
├── actual_amount (실제 금액)
└── created_at, updated_at
```

## 🔧 API 엔드포인트

### 인증
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `GET /api/auth/me` - 사용자 정보 조회

### 거래 관리
- `GET /api/transactions` - 거래 목록 조회
- `POST /api/transactions` - 거래 추가
- `PUT /api/transactions/{id}` - 거래 수정
- `DELETE /api/transactions/{id}` - 거래 삭제
- `GET /api/transactions/summary` - 월별 요약

### 카테고리
- `GET /api/categories` - 카테고리 목록 조회

## 🔒 보안

- **JWT 토큰**: 24시간 유효
- **비밀번호 암호화**: BCrypt 해싱
- **CORS 설정**: 허용된 도메인만 접근
- **입력 검증**: Bean Validation 적용

## 🚀 배포

### 개발 환경
- Profile: `dev`
- Database: PostgreSQL (`unble_dev`)
- Port: 9090
- 실행: `run.bat` 또는 `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

### 운영 환경  
- Profile: `prod`
- Database: PostgreSQL (`unble_prod`)
- Port: 환경변수 PORT 또는 9090
- 실행: `run-prod.bat` 또는 `mvn spring-boot:run -Dspring-boot.run.profiles=prod`

## 📝 라이센스

This project is licensed under the MIT License.

## 👥 개발팀

- **개발자**: Unble Team
- **연락처**: support@unble.com