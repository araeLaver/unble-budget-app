# 🗄️ PostgreSQL 스키마 정리 가이드

## 📋 현재 스키마 구조

### ✅ 사용 중인 스키마 (최종)
- **`unble_dev`** - 개발 환경 (🟢 명확함)
- **`unble_prod`** - 운영 환경 (🟢 명확함)

### ❌ 제거 대상 스키마 (헷갈리는 것들)
- **`dev_schema`** - 이전 개발 스키마
- **`prod_schema`** - 이전 운영 스키마  
- **`unble_budget_dev`** - 중간에 만든 개발 스키마
- **`unble_budget_prod`** - 중간에 만든 운영 스키마
- **기타 불필요한 스키마들**

## 🛠️ 정리 절차

### 1. 현재 스키마 상태 점검
```sql
-- 모든 스키마 목록 확인
SELECT schemaname, schemaowner 
FROM pg_catalog.pg_namespace n
LEFT JOIN pg_catalog.pg_user u ON n.nspowner = u.usesysid
WHERE schemaname NOT IN ('information_schema', 'pg_catalog', 'pg_toast', 'public')
ORDER BY schemaname;

-- 스키마별 테이블 목록 확인
SELECT schemaname, tablename, tableowner
FROM pg_tables 
WHERE schemaname LIKE '%budget%' OR schemaname LIKE '%dev%' OR schemaname LIKE '%prod%'
ORDER BY schemaname, tablename;
```

### 2. 데이터 마이그레이션 (필요시)
```sql
-- dev_schema에서 unble_budget_dev로 데이터 이동 (예시)
-- 주의: 실제 데이터가 있다면 백업 후 진행

-- 사용자 데이터 이동
INSERT INTO unble_budget_dev.users 
SELECT * FROM dev_schema.users 
ON CONFLICT (email) DO NOTHING;

-- 거래 데이터 이동  
INSERT INTO unble_budget_dev.transactions
SELECT * FROM dev_schema.transactions
WHERE user_id IN (SELECT id FROM unble_budget_dev.users);
```

### 3. 불필요한 스키마 삭제
```sql
-- 데이터 확인 후 실행
DROP SCHEMA IF EXISTS dev_schema CASCADE;
DROP SCHEMA IF EXISTS prod_schema CASCADE;

-- 기타 불필요한 스키마들
-- DROP SCHEMA IF EXISTS unble_budget CASCADE;
```

### 4. 권한 설정
```sql
-- 새 스키마에 권한 부여
GRANT ALL PRIVILEGES ON SCHEMA unble_budget_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_budget_prod TO untab;

-- 테이블 권한 부여
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_budget_dev TO untab;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_budget_prod TO untab;

-- 시퀀스 권한 부여
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_budget_dev TO untab;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_budget_prod TO untab;
```

## ⚠️ 주의사항

1. **백업 필수**: 삭제 전 반드시 데이터 백업
2. **서비스 중단**: 운영 환경 작업 시 서비스 중단 필요  
3. **단계적 진행**: 개발 → 스테이징 → 운영 순서로 진행
4. **롤백 계획**: 문제 발생 시 되돌릴 수 있는 계획 수립

## 🚀 실행 순서

1. **백업**: `pg_dump`로 전체 데이터베이스 백업
2. **개발 환경 정리**: 개발 DB에서 먼저 테스트
3. **운영 환경 적용**: 문제없으면 운영에 적용
4. **검증**: 애플리케이션 정상 동작 확인
5. **모니터링**: 일정 기간 모니터링 후 완료

## 📞 문제 발생 시

- 애플리케이션 로그 확인
- PostgreSQL 연결 상태 점검  
- 스키마 권한 확인
- 필요시 백업으로 복구