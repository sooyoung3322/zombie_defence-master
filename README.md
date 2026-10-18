# Zombie Defence Game Server

모바일 좀비 디펜스 게임의 Java 서버 소스 아카이브입니다. 현재 남아 있는 코드에서 확인한 기술과 기능만 정리했습니다.

**현재 상태로 빌드·실행할 수 없는 아카이브 프로젝트입니다.** 빌드 정의, Gradle Wrapper 구성, 인증 구현, 환경 설정과 DB 스키마가 없어 실행 검증을 완료할 수 없습니다.

## 확인된 기술

- Java, Spring Boot, Spring MVC
- MyBatis 인터페이스와 XML SQL
- Spring Security API 참조 및 JWT 발급·검사 구현 호출. JwtProvider 구현은 누락되어 있습니다.
- Lombok, Swagger 애너테이션, Apache Commons Lang, org.json
- Apple 영수증 검증 API, Google Android Publisher API 호출
- Gradle 실행 스크립트. 빌드 정의가 없어 목표 JDK와 의존성 버전은 확정하지 않습니다.

## 코드에 포함된 기능

- 사용자 UUID·닉네임 중복 확인, 사용자 생성·조회와 초기 데이터 등록
- 게임 시작·결과 기록, 재화와 경험치 및 관련 진행 상태 갱신
- 재화 사용·충전 이력, 상품 구매와 상품별 지급 처리
- 에픽 스킬, 플레이어 스킬 및 유닛 강화
- 퀘스트 진행·보상과 우편 보상 수령
- 시즌 패키지, SQL 기반 랭킹 조회 및 주간·시즌 예약 작업
- 관리자 닉네임 검색과 이벤트 보상 우편 API
- iOS·Android 인앱 결제 검증 API 연동 코드

결제 연동은 안전한 결제 검증이 완성되었다는 의미가 아닙니다. 확인한 한계는 [보안 검토](SECURITY_REVIEW.md)에 기록했습니다. 동시성, 중복 지급 방지, 관리자 권한 및 JWT 정책도 실행 검증하지 못했습니다.

## 구조

Controller → Service → MyBatis Mapper와 XML SQL 흐름으로 구성되어 있습니다.

- src/main/java/com/goall/commonUtil/: 공통 유틸리티
- src/main/java/com/goall/zombie_defence/controller/api/: 게임 및 관리자 API
- src/main/java/com/goall/zombie_defence/service/: 게임 비즈니스 로직
- src/main/java/com/goall/zombie_defence/mapper/: MyBatis 인터페이스
- src/main/java/com/goall/zombie_defence/batchScheduler/: 예약 작업
- src/main/resources/mapper/member/: SQL 매핑
- src/main/resources/mybatis/: MyBatis 설정

일부 메서드에 @Transactional이 선언되어 있으나 원자성과 롤백 동작을 검증한 상태는 아닙니다.

## 실행 상태와 공개 범위

build.gradle, settings.gradle, gradle/wrapper/ 구성, JwtProvider, GoogleCredentialsConfig, 애플리케이션 설정 및 DB 초기화 자료가 현재 폴더에 없습니다. src/test도 없습니다. 실행 명령이나 성공한 빌드 결과를 제시하지 않습니다.

로컬 환경·인증 설정, 로그·백업·실제 사용자 데이터는 커밋 대상에서 제외합니다. 공개 가능성이 확인되지 않은 nickNameFilter.txt와 웹 리소스 전체(src/main/webapp/, JSP 포함)도 제외했습니다. 기존 게임 소스와 로직은 수정하지 않았습니다.

실행 환경을 복원하려면 빌드·인증 구현·DB 스키마를 준비하고, 운영 환경과 분리된 합성 데이터로 인증·결제·재화·보상·예약 작업을 검증해야 합니다.
