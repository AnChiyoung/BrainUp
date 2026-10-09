# BrainUp 출시 체크리스트 (Google Play)

> 코드 쪽 준비는 끝났고, 아래는 계정·콘솔에서 직접 해야 하는 작업이다.
> 순서대로 진행하면 비공개 테스트 트랙에 올릴 수 있다.

## 1. 업로드 키 만들기 (최초 1회)

비밀번호는 안전한 곳에 따로 보관한다. 잃어버리면 Play Console에서 업로드 키 재설정 절차가 필요하다.

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

- `keytool`은 Android Studio에 포함되어 있다: `C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe`
- 만든 `upload-keystore.jks`를 프로젝트 루트에 두고, 같은 위치에 `keystore.properties`를 만든다(둘 다 `.gitignore` 처리됨).

```properties
storeFile=upload-keystore.jks
storePassword=비밀번호
keyAlias=upload
keyPassword=비밀번호
```

## 2. AdMob

- [x] 앱 추가, 광고 단위 3개(배너·전면·보상형) 생성
- [x] `app/build.gradle.kts`의 `release` 블록에 실제 앱 ID·광고 단위 ID 입력 (2026-10-09)
  - `debug` 블록은 테스트 ID 그대로 둔다. **개발 중 실제 광고를 보거나 클릭하면 계정이 정지될 수 있다.**
  - 새 광고 단위는 실제 광고가 나오기까지 수 시간~하루 걸린다(그동안 "No fill", 코드 3).
- [ ] AdMob → 개인정보 보호 및 메시지 → **GDPR 동의 메시지 생성 및 게시**
  - 없으면 로그에 `no form(s) configured for the input app ID` 경고가 나온다. 앱은 계속 광고를 요청한다.
- [ ] 출시 후 Play 스토어와 AdMob 앱 연결
- [ ] 개발자 웹사이트에 `app-ads.txt` 게시 (AdMob 콘솔에 표시되는 한 줄 그대로)

### 실기기에서 릴리스 빌드를 시험할 때

에뮬레이터는 자동으로 테스트 기기로 처리되지만, 실제 휴대폰은 등록해야 테스트 광고를 받는다.

1. 휴대폰에서 릴리스 빌드를 한 번 실행하고 logcat에서 다음 줄을 찾는다.
   `Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("해시값"))`
2. **사용자 홈의** `~/.gradle/gradle.properties`(저장소 아님)에 추가한다.
   `brainup.adTestDeviceIds=해시값` (여러 대면 쉼표로 구분)
3. 다시 빌드하면 그 기기에서는 실제 광고 단위로도 테스트 광고만 나온다.

## 3. 릴리스 빌드

```bash
./gradlew bundleRelease
```

- 결과물: `app/build/outputs/bundle/release/app-release.aab`
- 매 업로드마다 `app/build.gradle.kts`의 `versionCode`를 1씩 올린다.
- R8(코드 축소)이 켜져 있다. 출시 전 반드시 릴리스 빌드를 실기기에서 한 번 실행해 본다.
  - 실제로 축소 빌드에서만 생기는 크래시(내비게이션 enum 인자)를 QA 중 발견해 수정했다.

## 4. Play Console 설정

| 항목 | 값 / 참고 |
|---|---|
| 앱 이름 | BrainUp - 매일 3분 두뇌 훈련 (가칭, `store-listing.md`) |
| 기본 언어 | 한국어 |
| 앱/게임 | 게임 |
| 카테고리 | 퍼즐 (또는 교육 — 의학적 효과 표현은 피한다) |
| 유료/무료 | 무료 |
| 광고 포함 | 예 |
| 개인정보처리방침 URL | `privacy-policy.md`를 웹에 게시한 주소 (GitHub Pages, Notion 공개 페이지 등) |
| 데이터 보안 양식 | `data-safety.md` 참고 |
| 광고 ID 사용 | 예 (AdMob, Firebase) — 용도: 광고, 분석 |
| 타겟 연령 | **만 13세 이상** 권장. 13세 미만을 포함하면 가족 정책(인증된 광고 SDK, 아동용 광고 설정 등)이 추가로 적용된다 |
| 콘텐츠 등급 설문 | 폭력·선정성·도박·사용자 간 소통·위치 공유 모두 '아니요' → 전체이용가 예상 |
| 스토어 그래픽 | 아이콘 512×512 PNG, 그래픽 이미지 1024×500, 휴대전화 스크린샷 2~8장 |

- 512×512 아이콘: Android Studio → `res` 우클릭 → New → Image Asset 또는 `ic_launcher_foreground.xml`을 기반으로 디자인 툴에서 내보내기
- 신규 개인 개발자 계정은 **프로덕션 출시 전 비공개 테스트(테스터 12명 이상, 14일 연속)** 요건이 있다. 일정에 반영한다.

## 5. 출시 전 최종 점검

- [ ] `google-services.json`이 빌드 PC의 `app/`에 있는지
- [x] release 광고 ID가 실제 ID인지, debug는 테스트 ID인지
- [ ] Firebase Crashlytics 대시보드에 릴리스 빌드 첫 실행이 잡히는지(Crashlytics는 디버그 빌드에서 수집하지 않음)
- [ ] 실기기에서 네 게임 → 결과 → 기록 → 설정 한 바퀴
- [ ] 비행기 모드에서 게임 흐름이 막히지 않는지(광고 없이 진행)

## QA 결과 요약 (2026-10-09, 에뮬레이터 API 36)

| 항목 | 결과 |
|---|---|
| 작은 화면(360×640dp) | 패턴 타일이 위아래와 겹치던 문제, 배너가 로드 전부터 공간을 차지하던 문제 수정 |
| 다크 모드 | 이상 없음 |
| 글꼴 1.3배 | 레이아웃 깨짐 없음. 한국어는 글자 단위 줄바꿈(Android 기본 동작) |
| 화면 회전 | 휴대폰은 세로 고정. 큰 화면은 Android 16 정책상 회전 허용(레이아웃 여유 있음) |
| 프로세스 종료 후 복원 — 결과 화면 | 화면 복원, 기록 중복 저장 없음(DB 직접 확인) |
| 프로세스 종료 후 복원 — 게임 중 | 해당 게임의 시작 전 화면으로 복원, 진행 중이던 판은 기록하지 않음 |
| 네트워크 없음 | 배너 미표시·이어하기 버튼 숨김·전면 광고 없이 즉시 이동, 30초부터 재시도 |
| 네트워크 복구 | 배너 자동 재로드 확인 |
| 릴리스(R8) 빌드 | 네 게임·결과 저장·기록·전면 광고 정상 |
