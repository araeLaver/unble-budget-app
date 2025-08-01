# Koyeb 배포 가이드

## 1. GitHub 리포지토리 생성 및 푸시

### GitHub에 새 리포지토리 생성
1. GitHub.com에 로그인
2. "New repository" 클릭
3. Repository name: `unble-budget-app`
4. Description: `Smart Budget Management Application with Charts and Admin Panel`
5. Public 선택
6. Create repository 클릭

### 로컬 리포지토리를 GitHub에 연결
```bash
git remote add origin https://github.com/YOUR_USERNAME/unble-budget-app.git
git branch -M main
git push -u origin main
```

## 2. Koyeb 배포 설정

### Koyeb 계정 및 프로젝트 설정
1. [Koyeb.com](https://www.koyeb.com)에 로그인
2. "Create App" 클릭
3. 다음 설정 사용:

#### App Configuration
- **App name**: `unble-budget-app`
- **Service name**: `budget-service`

#### Source
- **Deployment method**: Git repository
- **Repository**: `YOUR_USERNAME/unble-budget-app`
- **Branch**: `main`
- **Build configuration**: Dockerfile

#### Environment Variables
다음 환경 변수들을 설정:

```
PORT=8080
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app/untab?user=untab&password=0AbVNOIsl2dn&sslmode=require&currentSchema=unble_prod
SPRING_DATASOURCE_USERNAME=untab
SPRING_DATASOURCE_PASSWORD=0AbVNOIsl2dn
JWT_SECRET=unble-budget-secret-key-2024-super-secure-jwt-token
```

#### Instance Configuration
- **Instance type**: Free (Nano)
- **Regions**: Frankfurt (eu-fra)
- **Auto-scaling**: Disabled (Free tier)

#### Health Check
- **Health check path**: `/api/status`
- **Port**: 8080

## 3. 배포 과정

1. 모든 설정을 확인한 후 "Deploy" 클릭
2. 빌드 과정 모니터링 (약 5-10분 소요)
3. 배포 완료 후 제공된 URL로 접속 테스트

## 4. 배포 후 확인사항

### 접속 확인
- 메인 페이지: `https://your-app-name-your-org.koyeb.app`
- 상태 확인: `https://your-app-name-your-org.koyeb.app/api/status`
- 관리자 페이지: `https://your-app-name-your-org.koyeb.app/admin.html`

### 기본 계정
- **관리자 계정**: `admin@unble.com` / `admin123`

### 기능 테스트
1. 회원가입/로그인
2. 거래 추가
3. 차트 조회
4. 관리자 기능

## 5. 문제 해결

### 로그 확인
Koyeb 대시보드에서 "Logs" 탭으로 이동하여 애플리케이션 로그 확인

### 일반적인 문제들
- **데이터베이스 연결 실패**: 환경 변수 확인
- **포트 오류**: PORT 환경 변수가 8080으로 설정되었는지 확인
- **스키마 오류**: unble_prod 스키마가 데이터베이스에 존재하는지 확인

### 재배포
코드 변경 후 GitHub에 푸시하면 자동으로 재배포됩니다:
```bash
git add .
git commit -m "Update: feature description"
git push origin main
```

## 6. 도메인 설정 (선택사항)

사용자 정의 도메인을 사용하려면:
1. Koyeb 대시보드에서 "Domains" 탭 클릭
2. "Add domain" 클릭
3. 도메인 추가 및 DNS 설정 안내 따르기