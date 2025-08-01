# 🗄️ Unble Budget App - 데이터베이스 설계

## 📋 테이블 구조

### 1. users (사용자)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 사용자 ID (자동증가) |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 이메일 (로그인 ID) |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| name | VARCHAR(100) | NOT NULL | 사용자 이름 |
| created_at | TIMESTAMP | DEFAULT NOW() | 계정 생성일 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 마지막 수정일 |
| is_active | BOOLEAN | DEFAULT TRUE | 계정 활성화 상태 |
| last_login_at | TIMESTAMP | NULL | 마지막 로그인 시간 |

### 2. categories (카테고리)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 카테고리 ID |
| name | VARCHAR(100) | NOT NULL | 카테고리 이름 |
| color | VARCHAR(7) | DEFAULT '#007AFF' | 색상 코드 |
| icon | VARCHAR(50) | DEFAULT 'other' | 아이콘 (이모지/코드) |
| category_type | VARCHAR(20) | NOT NULL | 카테고리 유형 (INCOME/EXPENSE/ASSET) |
| is_default | BOOLEAN | DEFAULT FALSE | 기본 카테고리 여부 |
| user_id | BIGINT | FOREIGN KEY | 사용자 ID (NULL이면 시스템 기본) |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |
| sort_order | INTEGER | DEFAULT 0 | 정렬 순서 |

### 3. transactions (거래)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 거래 ID |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | 사용자 ID |
| category_id | BIGINT | FOREIGN KEY | 카테고리 ID |
| amount | DECIMAL(15,2) | NOT NULL | 거래 금액 |
| description | TEXT | NULL | 거래 설명 |
| transaction_type | VARCHAR(20) | NOT NULL | 거래 유형 (INCOME/EXPENSE) |
| transaction_date | DATE | NOT NULL | 거래 날짜 |
| created_at | TIMESTAMP | DEFAULT NOW() | 등록일 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 수정일 |

### 4. user_settings (사용자 설정)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 설정 ID |
| user_id | BIGINT | FOREIGN KEY, UNIQUE | 사용자 ID |
| currency | VARCHAR(10) | DEFAULT 'KRW' | 기본 통화 |
| date_format | VARCHAR(20) | DEFAULT 'YYYY-MM-DD' | 날짜 형식 |
| theme | VARCHAR(20) | DEFAULT 'light' | 테마 (light/dark) |
| notification_enabled | BOOLEAN | DEFAULT TRUE | 알림 설정 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 수정일 |

### 5. budget_plans (예산 계획)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGSERIAL | PRIMARY KEY | 예산 ID |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | 사용자 ID |
| category_id | BIGINT | FOREIGN KEY | 카테고리 ID |
| budget_month | DATE | NOT NULL | 예산 월 (YYYY-MM-01) |
| planned_amount | DECIMAL(15,2) | NOT NULL | 계획 금액 |
| actual_amount | DECIMAL(15,2) | DEFAULT 0 | 실제 사용 금액 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 수정일 |

## 🔗 관계도

```
users (1) ──── (N) transactions
users (1) ──── (N) categories (사용자 정의)
users (1) ──── (1) user_settings
users (1) ──── (N) budget_plans

categories (1) ──── (N) transactions
categories (1) ──── (N) budget_plans
```

## 📊 인덱스 설계

```sql
-- 성능 최적화를 위한 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(category_type);
CREATE INDEX idx_categories_default ON categories(is_default);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_category ON transactions(category_id);

CREATE INDEX idx_budget_user_month ON budget_plans(user_id, budget_month);
```

## 🛡️ 보안 및 제약조건

```sql
-- 체크 제약조건
ALTER TABLE transactions ADD CONSTRAINT chk_amount_positive 
    CHECK (amount > 0);

ALTER TABLE transactions ADD CONSTRAINT chk_transaction_type 
    CHECK (transaction_type IN ('INCOME', 'EXPENSE'));

ALTER TABLE categories ADD CONSTRAINT chk_category_type 
    CHECK (category_type IN ('INCOME', 'EXPENSE', 'ASSET'));

-- 트리거: updated_at 자동 업데이트
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';
```