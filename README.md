# Java 버전별 비동기 프로그래밍 예제

Java 버전 진화에 따른 비동기 프로그래밍 방식의 변화를 보여주는 예제 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 Java의 여러 버전에서 비동기 프로그래밍을 구현하는 다양한 방식들을 비교하고 설명합니다.

### 포함된 예제

- **Java 5-7**: 기본 멀티스레딩 (Thread, Runnable)
- **Java 8**: CompletableFuture, Lambda 표현식
- **Java 9-10**: Reactive Streams, Project Reactor
- **Java 11-14**: HTTP/2 Client, 기능 개선
- **Java 15-16**: Records, Sealed Classes를 활용한 비동기 처리
- **Java 17**: 정식 기능으로 통합된 기능들
- **Java 19**: Virtual Threads (Project Loom)
- **Java 21**: Virtual Threads 정식 지원, Structured Concurrency

## 기술 스택

- **Java 21**: 최신 Java 버전
- **Maven**: 빌드 및 의존성 관리
- **Project Reactor**: 비동기 Reactive 처리
- **RxJava 3**: 다른 Reactive 구현
- **JUnit 5**: 단위 테스트
- **Lombok**: 코드 간소화

## 프로젝트 구조

```
java-async-examples/
├── src/
│   ├── main/
│   │   └── java/com/example/
│   │       ├── async/
│   │       │   ├── java5/        # 기본 멀티스레딩 (Thread)
│   │       │   ├── java8/        # CompletableFuture, Lambda
│   │       │   ├── java9/        # Reactive Streams
│   │       │   ├── java11/       # HTTP Client, var
│   │       │   ├── java15/       # Records
│   │       │   ├── java19/       # Virtual Threads
│   │       │   └── java21/       # Structured Concurrency
│   │       └── util/
│   │           └── DataUtil.java
│   └── test/
│       └── java/com/example/...
├── pom.xml
└── README.md
```

## 각 버전별 주요 특징

### Spring Framework: @Async 어노테이션

```java
// 간단한 @Async 메서드
@Async
public CompletableFuture<String> asyncTask(String id) {
    return CompletableFuture.completedFuture("결과");
}
```

**특징**:
- 메서드 레벨의 비동기 처리
- 별도의 스레드 풀에서 실행
- CompletableFuture, AsyncResult, ListenableFuture 반환 가능
- 자동 리소스 관리
- 예외 처리 용이

### Java 5-7: Thread 기반 멀티스레딩

```java
// 기본 Thread 생성 및 실행
Thread thread = new Thread(() -> {
    System.out.println("비동기 작업 수행");
});
thread.start();
```

**특징**:
- 저수준의 Thread API
- synchronized를 통한 동기화
- 복잡한 코드 구조
- 많은 오버헤드

### Java 8: CompletableFuture

```java
// CompletableFuture를 이용한 비동기 처리
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "비동기 결과")
    .thenApply(result -> result.toUpperCase())
    .thenAccept(System.out::println);
```

**특징**:
- 함수형 프로그래밍 지원
- 체이닝을 통한 직관적인 코드
- 결합 및 변환 가능
- 에러 처리 개선

### Java 9-10: Reactive Streams

```java
// Project Reactor를 이용한 Reactive 처리
Flux.range(1, 10)
    .map(i -> i * 2)
    .filter(i -> i > 5)
    .subscribe(System.out::println);
```

**특징**:
- 백프레셔(Backpressure) 지원
- 함수형 조합 가능
- 구독 기반 모델
- 스트림 처리 최적화

### Java 15-16: Records

```java
// Record를 이용한 데이터 클래스
record AsyncResult(String id, String data, long timestamp) {}
```

**특징**:
- 불변 데이터 클래스
- 보일러플레이트 코드 감소
- 동시성 안전
- 캡슐화 개선

### Java 19: Virtual Threads (Preview)

```java
// Virtual Thread를 이용한 경량 스레드
Thread vThread = Thread.ofVirtual()
    .start(() -> {
        System.out.println("Virtual Thread에서 실행");
    });
```

**특징**:
- 수천 개의 경량 스레드 생성 가능
- 플랫폼 스레드보다 훨씬 가벼움
- 문맥 전환 오버헤드 최소화
- 기존 코드와 호환 가능

### Java 21: Virtual Threads (정식 지원)

```java
// Structured Concurrency API
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var future1 = scope.fork(() -> asyncTask1());
    var future2 = scope.fork(() -> asyncTask2());
    
    scope.join();
    
    var result1 = future1.resultNow();
    var result2 = future2.resultNow();
}
```

**특징**:
- Virtual Threads 정식 지원
- Structured Concurrency 정식 지원
- 안전한 리소스 관리
- 예외 처리 개선

## 설치 및 실행

### 사전 요구사항

- Java 21 이상
- Maven 3.6.0 이상

### 빌드

```bash
cd java-async-examples
mvn clean package
```

### 실행 예제

```bash
# 특정 클래스 실행
mvn exec:java -Dexec.mainClass="com.example.async.java8.CompletableFutureExample"

# 또는 JAR 파일 실행
java -cp target/java-async-examples-1.0.0.jar com.example.async.java8.CompletableFutureExample
```

## 예제별 설명

### 1. Java 8 예제: CompletableFuture

**파일**: `java8/CompletableFutureExample.java`

데이터를 비동기로 조회하고 처리하는 예제:
- 여러 CompletableFuture 조합
- 예외 처리
- 타임아웃 설정
- 결과 수집

### 2. Java 9 예제: Reactive Streams

**파일**: `java9/ReactiveStreamsExample.java`

Reactor를 이용한 Reactive 처리:
- Mono와 Flux 사용
- 연산자 체이닝
- 에러 처리
- 백프레셔 처리

### 3. Java 11 예제: HTTP Client

**파일**: `java11/HttpClientExample.java`

Java 11의 새로운 HTTP Client API:
- HTTP 요청 비동기 처리
- CompletableFuture 통합
- 성능 최적화

### 4. Java 15 예제: Records

**파일**: `java15/RecordsExample.java`

Record를 이용한 데이터 처리:
- 불변 데이터 클래스
- 패턴 매칭 (Java 16+)
- 직렬화

### 5. Java 19 예제: Virtual Threads

**파일**: `java19/VirtualThreadsExample.java`

Virtual Threads를 이용한 경량 비동기:
- 많은 수의 동시 작업
- 기존 코드와의 호환성
- 성능 비교

### 6. Java 21 예제: Structured Concurrency

**파일**: `java21/StructuredConcurrencyExample.java`

Structured Concurrency를 이용한 안전한 동시성:
- 명시적인 스코프 관리
- 예외 처리 개선
- 리소스 자동 해제

### 7. Spring Framework 예제: @Async

**파일**: `spring/SpringAsyncExample.java`, `spring/AsyncTaskService.java`

Spring Framework의 @Async 어노테이션을 이용한 비동기 처리:
- 간단한 @Async 메서드
- CompletableFuture 반환
- AsyncResult 반환
- ListenableFuture 반환
- 병렬 비동기 작업 처리
- 예외 처리
- 작업 체이닝

### 8. Spring MVC 웹 요청 비동기 처리

**파일**: `spring/AsyncWebController.java`

Spring MVC의 비동기 웹 요청 처리:
- DeferredResult를 이용한 비동기 응답
- Callable을 이용한 자동 스레드 풀 처리
- CompletableFuture 반환
- WebAsyncTask로 타임아웃 및 콜백 처리

## 성능 비교

각 접근 방식의 성능 특성:

| 방식 | 처리량 | 지연시간 | 메모리 | 복잡도 | 학습곡선 |
|------|--------|---------|--------|--------|---------|
| Thread | 낮음 | 높음 | 높음 | 높음 | 낮음 |
| CompletableFuture | 중간 | 중간 | 중간 | 중간 | 중간 |
| Reactive | 높음 | 낮음 | 중간 | 높음 | 높음 |
| Virtual Threads | 매우높음 | 매우낮음 | 낮음 | 낮음 | 낮음 |
| Spring @Async | 중간 | 중간 | 중간 | 낮음 | 낮음 |

## Spring Framework의 장점

### @Async vs 다른 방식

**Spring @Async의 장점**:
- 선언적 프로그래밍 (코드 간결함)
- Spring의 다양한 기능 통합 (AOP, 의존성 주입 등)
- 기존 코드에 추가하기 쉬움
- 커스텀 Executor 설정 가능

**사용 사례**:
- 웹 요청 처리 중 오래 걸리는 작업을 비동기로 분리
- 이메일 발송, 로그 기록 등 Background 작업
- 외부 API 호출
- 캐시 업데이트

### Spring MVC 비동기 웹 요청 처리

| 기능 | 사용 사례 | 복잡도 |
|------|---------|--------|
| DeferredResult | 복잡한 비동기 로직 | 중간 |
| Callable | 간단한 블로킹 작업 | 낮음 |
| CompletableFuture | 체이닝된 작업 | 중간 |
| WebAsyncTask | 타임아웃 제어 필요 | 중간 |

## 테스트 실행

```bash
# 모든 테스트 실행
mvn test

# 특정 테스트 클래스 실행
mvn test -Dtest=CompletableFutureExampleTest
```

## 주요 개념

### 1. Callback
가장 기초적인 비동기 패턴으로 콜백 함수를 전달합니다.

**장점**: 간단함
**단점**: 콜백 지옥, 에러 처리 복잡

### 2. Future
Java 5에서 도입된 비동기 결과 표현입니다.

**장점**: 결과 대기 가능
**단점**: 조합 어려움, 논블로킹 아님

### 3. CompletableFuture
Java 8의 고급 Future 구현입니다.

**장점**: 체이닝, 조합, 에러 처리 용이
**단점**: 여전히 스레드 기반

### 4. Reactive Streams
백프레셔를 지원하는 비동기 처리 표준입니다.

**장점**: 확장성, 백프레셔
**단점**: 학습 곡선 가파름

### 5. Virtual Threads
Java 19+의 경량 스레드입니다.

**장점**: 매우 가벼움, 기존 코드 호환
**단점**: 플랫폼 버전 제한

### 6. Structured Concurrency
Java 21의 구조화된 동시성입니다.

**장점**: 안전, 명시적, 리소스 관리
**단점**: 새로운 학습 필요

## 실전 팁

### Virtual Threads 사용 시기

- 많은 수의 I/O 작업 (1000+)
- 블로킹 라이브러리 사용
- 기존 스레드 코드 마이그레이션

### Reactive 사용 시기

- 복잡한 데이터 변환
- 백프레셔 필요
- 함수형 프로그래밍 선호

### CompletableFuture 사용 시기

- 적은 수의 비동기 작업 (<100)
- 간단한 조합
- Java 8 호환성 필요

## 주의사항

1. **Virtual Threads의 한계**
   - CPU 바운드 작업에는 부적합
   - 기본 동기화 메커니즘 필요

2. **Reactive의 복잡성**
   - 디버깅이 어려울 수 있음
   - 스택 트레이스 추적 어려움

3. **블로킹 작업**
   - Virtual Thread 내 블로킹 라이브러리 사용 권장
   - CompletableFuture는 I/O 대기 시에만 효율적

## 확장 주제

이 프로젝트를 확장하여 다음을 추가할 수 있습니다:

1. **메트릭 수집**: Micrometer로 성능 측정
2. **모니터링**: 비동기 작업 모니터링
3. **분산 추적**: OpenTelemetry 통합
4. **성능 벤치마크**: JMH를 이용한 벤치마크
5. **실전 예제**: 웹 애플리케이션, 데이터 처리 파이프라인

## 참고 자료

- [Java CompletableFuture 공식 문서](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)
- [Project Reactor](https://projectreactor.io/)
- [Project Loom (Virtual Threads)](https://wiki.openjdk.org/display/loom)
- [Structured Concurrency](https://openjdk.org/jeps/431)

## 라이선스

MIT License

## 저자

takefivess (https://github.com/takefivess)

---

**마지막 업데이트**: 2024년 1월 22일
