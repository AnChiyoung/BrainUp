# 데이터 보안 양식 답변 초안

> 앱 코드는 사용자 데이터를 직접 서버로 보내지 않는다. 기기 밖으로 나가는 데이터는 모두 아래 SDK가 수집한다.
> 제출 전 각 SDK의 최신 공식 안내를 다시 확인한다.
> - AdMob: https://developers.google.com/admob/android/privacy/play-data-disclosure
> - Firebase: https://firebase.google.com/docs/android/play-data-disclosure

## 앱에 포함된 SDK

| SDK | 용도 |
|---|---|
| Google Mobile Ads (AdMob) + UMP | 배너·전면·보상형 광고, 광고 동의 |
| Firebase Analytics | 게임 시작/완료, 광고 노출 등 이벤트 분석 |
| Firebase Crashlytics | 비정상 종료 보고(릴리스 빌드만) |

## 기기에만 저장되는 데이터 (수집 아님)

- 게임 기록, 일일 진행, 광고 빈도 카운터 → Room DB / DataStore, 외부 전송 없음, 앱 삭제 시 삭제

## 질문별 답변

| 질문 | 답변 |
|---|---|
| 사용자 데이터를 수집하거나 공유하나요? | 예 (SDK를 통해) |
| 전송 중 암호화되나요? | 예 |
| 사용자가 데이터 삭제를 요청할 수 있나요? | 계정이 없어 별도 요청 수단 없음. 앱 삭제 시 기기 내 데이터 삭제. Analytics 데이터는 보존 기간 설정에 따름 |

## 데이터 유형별

| 데이터 유형 | 수집 | 공유 | 목적 | 출처 |
|---|---|---|---|---|
| 위치 › 대략적인 위치 | 예 | 예 | 광고, 분석 | AdMob(IP 기반) |
| 앱 활동 › 앱 상호작용 | 예 | 예 | 분석, 광고 | Firebase Analytics, AdMob |
| 앱 정보 및 성능 › 비정상 종료 로그 | 예 | 아니요 | 앱 기능, 분석 | Crashlytics, AdMob |
| 앱 정보 및 성능 › 진단 | 예 | 아니요 | 앱 기능, 분석 | Crashlytics, AdMob |
| 기기 또는 기타 ID | 예 | 예 | 광고, 분석, 사기 방지 | 광고 ID(AdMob), 앱 인스턴스 ID(Firebase) |

- 모든 항목 "필수(사용자가 끌 수 없음)"로 답하되, EEA·영국 등에서는 UMP 동의로 맞춤 광고를 제한할 수 있다.
- 이름, 이메일, 연락처, 사진, 결제 정보 등은 수집하지 않는다.
