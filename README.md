# Zombie Defence Game Server

모바일 좀비 디펜스 게임의 Java 서버 소스 아카이브입니다. 사용자 게임 데이터, 플레이 기록, 재화·상품 지급, 스킬 성장, 퀘스트·우편 보상, 시즌·랭킹을 처리하는 백엔드 코드가 포함되어 있습니다. 현재 남아 있는 코드에서 확인한 기술과 기능만 정리했습니다.

**현재 상태로 빌드·실행할 수 없는 아카이브 프로젝트입니다.** 빌드 정의, Gradle Wrapper 구성, 인증 구현, 환경 설정과 DB 스키마가 없어 실행 검증을 완료할 수 없습니다.

## 확인된 기술

- Java, Spring Boot, Spring MVC
- MyBatis 인터페이스와 XML SQL
- Spring Security API 참조 및 JWT 발급·검사 구현 호출. `JwtProvider` 구현은 누락되어 있습니다.
- Lombok, Swagger 애너테이션, Apache Commons Lang, org.json
- Apple 영수증 검증 API, Google Android Publisher API 호출
- Gradle 실행 스크립트. 빌드 정의가 없어 목표 JDK와 의존성 버전은 확정하지 않습니다.

## 담당 개발 영역

이 문서의 기능 설명은 **저장소에 포함된 구현 범위**를 나타냅니다. 소스만으로 개인별 기여를 확정할 수 없으므로, 아래 항목을 작성자의 담당 경험으로 단정하지 않습니다.

| 코드에서 확인되는 개발 영역 | 개인 담당 범위 확인 사항 |
| --- | --- |
| 게임 재화·상품 지급 및 경험치 처리 | 직접 구현·수정한 상품과 재화 처리 범위 |
| 퀘스트·우편 보상 | 진행 집계, 보상 지급, 우편 수령 중 담당한 부분 |
| 플레이어·에픽 스킬과 유닛 강화 | 성장 규칙, 강화 처리, 데이터 관리 중 담당한 부분 |
| MyBatis SQL, 시즌·랭킹·예약 작업 | 직접 작성·수정한 쿼리와 서비스·스케줄러 |
| 모바일 결제 API 및 관리자 이벤트 | 플랫폼별 연동과 이벤트 보상 중 담당한 부분 |

개인 담당 범위는 작성자의 확인이 필요합니다. 개발 기간, 팀 규모, 운영 성과 수치도 현재 자료만으로 확정하지 않습니다.

## 주요 구현 기능

핵심 구현은 게임 활동을 재화·성장 데이터에 반영하고, 상품·퀘스트·우편·시즌별 지급 규칙을 Service와 MyBatis SQL로 처리하는 부분입니다. 아래 내용은 실행 결과가 아닌 소스 분석을 기준으로 합니다.

### 1. 사용자 생성·조회 및 초기 게임 데이터

새 플레이어가 게임을 시작하고, 다시 접속했을 때 필요한 성장·시즌 데이터를 준비합니다.

- UUID·닉네임 중복 확인과 사용자 생성·조회 기능을 제공합니다. 생성 시 사용자 정보와 유닛 강화·광고 시간 초기 데이터를 등록합니다.
- 사용자 조회 시 플레이어 스킬·유닛 강화 데이터의 존재 여부를 확인해 기본값을 등록하고, 현재 시즌의 사용자 데이터도 없으면 생성합니다. JWT 발급 호출과 사용자 토큰 갱신도 포함됩니다.
- **처리 흐름:** 사용자 요청 → 중복·기존 데이터 조회 → 사용자 생성 또는 초기 데이터 보완 → 사용자 정보 응답. 생성 시 초기화와 접속 시 보완 처리가 나뉘어 있습니다.

관련 코드: [UserController](src/main/java/com/goall/zombie_defence/controller/api/inGame/UserController.java), [UserService](src/main/java/com/goall/zombie_defence/service/UserService.java), [UserMapper.xml](src/main/resources/mapper/member/UserMapper.xml)

### 2. 게임 시작·종료 및 플레이 기록

한 판의 시작과 결과를 기록하고, 플레이 결과를 성장·퀘스트·랭킹 데이터에 연결합니다.

- 시작 처리에서 게임 횟수를 갱신하고 웨이브 정보를 조회합니다. 모드별 분기에서 입장 재화 처리와 시작 로그 저장을 수행합니다.
- 종료 요청에서는 시작 기록을 조회하고 결과·주간 기록을 저장합니다. 기존 최고 점수와 비교해 사용자 최고 기록을 갱신하거나 최초 등록합니다.
- **처리 흐름:** 시작 기록 → 종료 결과 수신 → 재화·경험치 반영 → 결과·최고 기록 저장 → 일일·주간 퀘스트와 시즌 경험치 갱신. 클라이언트가 보낸 결과를 바탕으로 서버 데이터를 갱신하는 구조입니다.

관련 코드: [InGameService](src/main/java/com/goall/zombie_defence/service/InGameService.java), [InGameMapper.xml](src/main/resources/mapper/member/InGameMapper.xml)

### 3. 게임 재화·경험치·상품 구매 처리

게임 입장, 성장, 상점 구매에 쓰이는 재화와 상품별 지급 효과를 서버에서 관리합니다.

- 키·골드·루비·보스 포인트의 사용·충전 처리에서 사용자 잔액을 조회하고, 변경 수량과 변경 전후 잔액을 로그에 저장한 뒤 보유량을 갱신합니다.
- 상품 구매는 `ProductService`에서 상품 종류를 구분해 `GoodsService` 또는 `EpicSkillService`로 전달합니다. 재화 지급 외에도 경험치 부스터, 광고 스킵, 추가 스킬 포인트, 시즌 티어, 우편 지급 패키지 등 상품별 처리 메서드가 있습니다.
- **처리 흐름:** 구매 요청 → 상품별 처리 분기 → 상품 정의·사용자 데이터 조회 → 필요한 재화 확인·차감 및 지급 효과 반영 → 구매·재화 이력과 결과 응답. 상품 정의와 사용자 보유 데이터, 처리 로그가 분리되어 있습니다.
- 경험치는 도달 웨이브, 모드별 보너스, 부스터 상태로 계산합니다. 누적 경험치에 따른 레벨과 레벨업 보상을 반영하고 경험치 이력을 저장합니다.

관련 코드: [ProductService](src/main/java/com/goall/zombie_defence/service/ProductService.java), [GoodsService](src/main/java/com/goall/zombie_defence/service/GoodsService.java), [GoodsMapper](src/main/java/com/goall/zombie_defence/mapper/GoodsMapper.java), [GoodsMapper.xml](src/main/resources/mapper/member/GoodsMapper.xml), [ProductMapper.xml](src/main/resources/mapper/member/ProductMapper.xml)

### 4. 캐릭터·유닛 스킬과 강화

플레이어의 능력 배분, 보유 스킬, 유닛별 성장 상태를 구분해 관리합니다.

- **플레이어 스킬:** 공격력·공격 속도·재화 보너스 등의 배분값을 조회·갱신합니다. 레벨과 추가 스킬 포인트로 사용 가능한 총 포인트를 계산하고 요청한 배분 합계와 비교합니다.
- **에픽 스킬:** 뽑기·장착·강화·합성·판매·교환 처리가 있습니다. 강화에서는 대상·재료 데이터의 등급별 확률로 성공 여부를 계산하고, 재료 사용 상태, 대상 레벨, 골드, 강화 결과 로그와 퀘스트 진행을 갱신합니다.
- **유닛 강화 흐름:** 유닛 종류와 현재 강화 레벨 조회 → 강화 기준 데이터의 요구 레벨·비용 확인 → 골드 또는 루비 차감 → 강화 레벨·추가 스킬 포인트 반영 → 강화 로그 저장. 기준 데이터와 사용자별 성장 상태를 별도로 조회하는 구조입니다.

관련 코드: [PlayerSkillService](src/main/java/com/goall/zombie_defence/service/PlayerSkillService.java), [EpicSkillService](src/main/java/com/goall/zombie_defence/service/EpicSkillService.java), [UnitUpgradeService](src/main/java/com/goall/zombie_defence/service/UnitUpgradeService.java), [EpicSkillMapper.xml](src/main/resources/mapper/member/EpicSkillMapper.xml), [UnitUpgradeMapper.xml](src/main/resources/mapper/member/UnitUpgradeMapper.xml)

### 5. 퀘스트 진행 및 보상 지급

플레이와 강화 활동을 일일·주간 목표로 연결하고, 달성 조건에 따라 보상을 지급합니다.

- 조회 시 해당 기간의 사용자 퀘스트 데이터가 없으면 초기화하고, 퀘스트 정의·진행 수치·완료 목록을 반환합니다. 게임 결과의 처치·클리어·유닛 관련 수치와 에픽 스킬 강화 횟수가 진행 데이터에 반영됩니다.
- **처리 흐름:** 보상 요청 → 퀘스트 정의와 일일·주간 진행 수치 조회 → 목표 수치 비교 → 보상 종류별 키·골드·루비 지급 → 재화 로그·퀘스트 상태·완료 이력 저장.
- 일일·주간 진행 테이블을 구분하며, 완료 개수에 따른 추가 보상과 광고 보상 처리도 별도로 구현되어 있습니다.

관련 코드: [QuestController](src/main/java/com/goall/zombie_defence/controller/api/inGame/QuestController.java), [QuestService](src/main/java/com/goall/zombie_defence/service/QuestService.java), [QuestMapper](src/main/java/com/goall/zombie_defence/mapper/QuestMapper.java), [QuestMapper.xml](src/main/resources/mapper/member/QuestMapper.xml)

### 6. 우편함 보상 지급·수령

이벤트와 패키지 등의 보상을 우편으로 전달하고, 플레이어가 수령할 때 실제 보유 데이터에 반영합니다.

- 사용자·우편함 조회에서 유효한 전체 공지 우편을 확인하고, 사용자별 우편 등록 여부를 조회해 개인 우편으로 등록합니다. 목록 SQL은 우편의 유효 기간과 사용 상태를 조건으로 조회합니다.
- **처리 흐름:** 수령할 우편의 상태 확인 → 보상 식별자로 보상 목록 조회 → 키·골드·루비 또는 에픽 스킬 지급 → 재화 이력·보유 데이터 갱신 → 우편 사용 상태 변경.
- 우편 제목·내용·기간을 담은 사용자 우편과 보상 구성을 별도로 관리합니다. 이벤트·상품 처리에서 우편을 등록하고, 실제 지급은 수령 처리에서 수행할 수 있는 구조입니다.

관련 코드: [MailService](src/main/java/com/goall/zombie_defence/service/MailService.java), [MailMapper.xml](src/main/resources/mapper/member/MailMapper.xml)

### 7. 시즌 시스템과 랭킹 조회

시즌별 성장 보상과 게임 모드·난이도별 경쟁 기록을 제공합니다. 시즌 패키지와 전체·주간 랭킹은 각각의 데이터 흐름으로 관리합니다.

- **시즌 처리:** 현재 시즌·보상 목록·사용자 경험치·구매 티어·수령 상태 조회 → 보상 요청의 티어·경험치 조건과 수령 기록 확인 → 재화·스킬 보상 지급 → 시즌별 수령 데이터 저장. 시즌 경험치는 게임 결과 처리에서 누적합니다.
- **랭킹 처리:** 전체 최고 기록 또는 주간 기록 → 사용자별 최고 점수 선택 → 전체 순번 계산 → 상위 목록과 내 순위 조회. SQL의 `ROW_NUMBER() OVER (PARTITION BY ...)`와 점수순 `ROW_NUMBER()`를 사용하며 모드·난이도·기간 조건을 적용합니다.
- 지난주 랭킹을 바탕으로 보상 수령 가능 여부를 조회하고, 보상 재화와 수령 이력을 반영하는 서비스도 포함되어 있습니다.

관련 코드: [SeasonPackageService](src/main/java/com/goall/zombie_defence/service/SeasonPackageService.java), [SeasonPackageMapper.xml](src/main/resources/mapper/member/SeasonPackageMapper.xml), [StatisticsService](src/main/java/com/goall/zombie_defence/service/StatisticsService.java), [StatisticsMapper.xml](src/main/resources/mapper/member/StatisticsMapper.xml)

### 8. 주간·시즌 예약 작업

Spring `@Scheduled`로 주간 기록 정리와 시즌 전환을 수행하는 코드가 있습니다.

- **주간 흐름:** 매주 일요일 23:59:57 → 스테이지별 주간 랭킹 조회 → 주간 순위 저장 → 이전 주차 플레이 기록 삭제. 스케줄러의 랭커 보상 우편 등록 부분은 주석 처리되어 있어 활성 기능으로 설명하지 않습니다.
- **시즌 흐름:** 매월 14일 02:00에 현재 시즌 종료 정보를 갱신하고, 15일 02:00에 다음 시즌 번호와 시작·종료 기간을 등록합니다. 사용자별 시즌 기본 데이터는 접속 시 조회 과정에서 준비합니다.
- 예약 시각은 코드의 cron 기준이며, 실제 배포 환경의 실행 여부와 시간대 설정은 검증하지 않았습니다.

관련 코드: [BatchScheduler](src/main/java/com/goall/zombie_defence/batchScheduler/BatchScheduler.java)

### 9. 관리자 이벤트·보상 기능

운영자가 이벤트 대상 사용자를 조회하고 보상 우편을 등록하는 기능입니다.

- **처리 흐름:** 닉네임으로 사용자 검색 → 이벤트 보상 우편 이력 조회 → 대상 사용자의 이벤트 우편 등록 → 사용자가 우편함에서 보상 수령.
- `UserAdminPageController`가 `UserService`로 처리를 전달하고, `UserMapper`의 XML SQL에서 카페 가입 이벤트 우편을 등록합니다. 이벤트 지급은 우편함의 보상 수령 흐름과 연결됩니다.

관련 코드: [UserAdminPageController](src/main/java/com/goall/zombie_defence/controller/api/admin/UserAdminPageController.java), [UserService](src/main/java/com/goall/zombie_defence/service/UserService.java), [UserMapper.xml](src/main/resources/mapper/member/UserMapper.xml)

### 10. iOS·Android 인앱 결제 API 연동

모바일 스토어 구매 정보를 외부 API로 확인하고 상품 지급 처리에 연결하는 코드입니다.

- **iOS:** `ProductService`가 영수증 정보를 JSON 요청으로 구성해 `RestTemplate`으로 Apple 영수증 검증 API를 호출합니다. 환경 설정에 따른 호출 대상 구분과 구매 실패 처리 분기가 있습니다.
- **Android:** 영수증 JSON에서 패키지명·상품 ID·구매 토큰을 추출하고 `InAppPurchaseService`로 전달합니다. Google Android Publisher 클라이언트로 상품 구매 정보를 조회하고 구매 상태를 확인합니다.
- **처리 흐름:** 구매 요청·시작 로그 → 플랫폼별 외부 API 호출 → 연동 결과에 따른 분기 → 상품별 지급 서비스 또는 실패 응답. iOS 상품명의 플랫폼 접미사를 제거해 공통 상품 처리로 연결합니다.

외부 API 호출 코드가 있다는 사실이 안전한 결제 검증의 완성을 의미하지는 않습니다. 인증 설정과 실행 환경이 없어 실제 결제 검증·지급의 정상 동작은 확인하지 못했습니다.

관련 코드: [ProductController](src/main/java/com/goall/zombie_defence/controller/api/inGame/ProductController.java), [ProductService](src/main/java/com/goall/zombie_defence/service/ProductService.java), [InAppPurchaseService](src/main/java/com/goall/zombie_defence/service/InAppPurchaseService.java)

## 구조와 데이터 처리 방식

기본 흐름은 **Controller → Service → MyBatis Mapper 인터페이스 → XML SQL → 응답**입니다. 일부 조회는 Controller에서 Mapper를 직접 호출합니다.

- **Controller:** Spring MVC 요청 매핑과 JSON 입력을 받아 서비스에 전달합니다. Swagger 애너테이션으로 API 설명을 작성했습니다.
- **Service:** 상품·보상 종류별 분기, 재화 계산, 성장 조건 확인, 여러 Mapper 호출을 조합합니다. 입력과 조회 결과는 주로 `Map`·`HashMap`과 목록으로 전달합니다.
- **MyBatis:** Mapper 메서드와 XML의 SQL ID를 연결합니다. `#{...}` 바인딩, `<if>` 조건 분기, `<foreach>` 목록 처리, 생성 키 반환을 사용하며, 기간별 조회와 랭킹 집계도 SQL로 처리합니다.
- **데이터 구분:** 상품·스킬·강화·퀘스트·보상 기준 데이터, 사용자별 현재 상태, 구매·재화·강화·수령 이력을 별도로 다루는 구조가 확인됩니다.

일부 메서드에 `@Transactional`이 선언되어 있으나 원자성과 롤백 동작을 검증한 상태는 아닙니다. 동시성 제어, 중복 지급 방지, 관리자 권한 및 JWT 정책은 검증되지 않았으며, 성능 최적화나 대규모 트래픽 대응 성과를 주장하지 않습니다.

| 경로 | 역할 |
| --- | --- |
| [commonUtil](src/main/java/com/goall/commonUtil/) | 공통 유틸리티 |
| [controller/api](src/main/java/com/goall/zombie_defence/controller/api/) | 게임 및 관리자 API |
| [service](src/main/java/com/goall/zombie_defence/service/) | 게임 비즈니스 로직 |
| [mapper](src/main/java/com/goall/zombie_defence/mapper/) | MyBatis 인터페이스 |
| [batchScheduler](src/main/java/com/goall/zombie_defence/batchScheduler/) | 예약 작업 |
| [mapper/member](src/main/resources/mapper/member/) | XML SQL 매핑 |
| [mybatis](src/main/resources/mybatis/) | MyBatis 설정 |

## 실행 상태와 공개 범위

`build.gradle`, `settings.gradle`, `gradle/wrapper/` 구성, `JwtProvider`, `GoogleCredentialsConfig`, 애플리케이션 설정 및 DB 초기화 자료가 현재 폴더에 없습니다. `src/test`도 없습니다. 실행 명령이나 성공한 빌드 결과를 제시하지 않습니다.

로컬 환경·인증 설정, 로그·백업·실제 사용자 데이터는 커밋 대상에서 제외합니다. 공개 가능성이 확인되지 않은 `nickNameFilter.txt`와 웹 리소스 전체(`src/main/webapp/`, JSP 포함)도 제외했습니다. 기존 게임 소스와 로직은 수정하지 않았습니다.

실행 환경을 복원하려면 빌드·인증 구현·DB 스키마를 준비하고, 운영 환경과 분리된 합성 데이터로 인증·결제·재화·보상·예약 작업을 검증해야 합니다.
