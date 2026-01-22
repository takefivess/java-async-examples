package com.example.async.java9;

import com.example.async.util.DataUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Arrays;

/**
 * Java 9-10: Reactive Streams (Project Reactor)를 이용한 비동기 처리
 * 
 * Reactive Streams는 백프레셔(Backpressure)를 지원하는 비동기 처리 표준입니다.
 * Project Reactor는 이 표준을 구현한 라이브러리입니다.
 */
public class ReactiveStreamsExample {
    
    /**
     * Mono 기본 예제 (단일 값)
     */
    public static void monoBasicExample() {
        System.out.println("\n=== Java 9: Mono 기본 예제 ===");
        
        // Mono는 0개 또는 1개의 값을 발출
        Mono<String> mono = Mono.just("Reactive 데이터")
                .doOnNext(data -> System.out.println("데이터 수신: " + data))
                .map(String::toUpperCase)
                .doOnNext(data -> System.out.println("변환 결과: " + data));
        
        // subscribe: 구독
        mono.subscribe();
    }
    
    /**
     * Flux 기본 예제 (여러 값)
     */
    public static void fluxBasicExample() {
        System.out.println("\n=== Java 9: Flux 기본 예제 ===");
        
        // Flux는 0개 이상의 여러 값을 발출
        Flux<Integer> flux = Flux.range(1, 5)
                .doOnNext(num -> System.out.println("생성: " + num))
                .map(num -> num * 2)
                .doOnNext(num -> System.out.println("변환: " + num))
                .filter(num -> num > 4)
                .doOnNext(num -> System.out.println("필터링 통과: " + num));
        
        // subscribe: 최종 구독으로 실행
        flux.subscribe();
    }
    
    /**
     * 데이터 변환 예제
     */
    public static void dataTransformationExample() {
        System.out.println("\n=== Java 9: 데이터 변환 ===");
        
        Flux.just("apple", "banana", "cherry")
                .map(String::toUpperCase)
                .map(fruit -> fruit + "!")
                .doOnNext(System.out::println)
                .subscribe();
    }
    
    /**
     * 필터링 예제
     */
    public static void filteringExample() {
        System.out.println("\n=== Java 9: 필터링 ===");
        
        Flux.range(1, 10)
                .filter(n -> n % 2 == 0)
                .doOnNext(n -> System.out.println("짝수: " + n))
                .subscribe();
    }
    
    /**
     * 여러 소스 조합 예제
     */
    public static void mergingExample() {
        System.out.println("\n=== Java 9: 여러 소스 조합 (merge) ===");
        
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("1", "2", "3");
        
        // merge: 두 플럭스 합치기 (순서 보장 안 함)
        Flux.merge(flux1, flux2)
                .doOnNext(data -> System.out.println("병합 결과: " + data))
                .subscribe();
    }
    
    /**
     * 순차 조합 예제
     */
    public static void concatExample() {
        System.out.println("\n=== Java 9: 순차 조합 (concat) ===");
        
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("1", "2", "3");
        
        // concat: 순차적으로 조합 (첫 번째 완료 후 두 번째 시작)
        Flux.concat(flux1, flux2)
                .doOnNext(data -> System.out.println("순차 결과: " + data))
                .subscribe();
    }
    
    /**
     * 지연된 데이터 처리 예제
     */
    public static void delayedDataExample() {
        System.out.println("\n=== Java 9: 지연된 데이터 처리 ===");
        
        Flux.range(1, 5)
                .delayElement(Duration.ofMillis(500))
                .doOnNext(n -> System.out.println("지연 후 데이터: " + n))
                .blockLast();  // 완료 대기
    }
    
    /**
     * 비동기 변환 (flatMap) 예제
     */
    public static void flatMapExample() {
        System.out.println("\n=== Java 9: 비동기 변환 (flatMap) ===");
        
        Flux.just("사용자1", "사용자2", "사용자3")
                .flatMap(user -> {
                    // 각 사용자에 대해 비동기 작업 수행
                    return Mono.fromCallable(() -> {
                        System.out.println(user + " 처리 중...");
                        DataUtil.simulateNetworkIO(500);
                        return user + " 처리 완료";
                    });
                })
                .doOnNext(System.out::println)
                .blockLast();
    }
    
    /**
     * 에러 처리 예제
     */
    public static void errorHandlingExample() {
        System.out.println("\n=== Java 9: 에러 처리 ===");
        
        Flux.range(1, 5)
                .map(n -> {
                    if (n == 3) {
                        throw new RuntimeException("의도적인 에러 (n=3)");
                    }
                    return n * 2;
                })
                .onErrorReturn(-1)
                .doOnNext(n -> System.out.println("결과: " + n))
                .subscribe();
    }
    
    /**
     * 재시도 예제
     */
    public static void retryExample() {
        System.out.println("\n=== Java 9: 재시도 (retry) ===");
        
        Mono.fromCallable(() -> {
            System.out.println("API 호출...");
            throw new RuntimeException("API 호출 실패");
        })
        .retry(3)  // 최대 3회 재시도
        .onErrorReturn("최종 실패")
        .doOnNext(System.out::println)
        .subscribe();
    }
    
    /**
     * 백프레셔 예제
     */
    public static void backpressureExample() {
        System.out.println("\n=== Java 9: 백프레셔 ===");
        
        Flux.range(1, 10)
                .doOnRequest(n -> System.out.println("요청: " + n + "개"))
                .doOnNext(n -> System.out.println("생성: " + n))
                .subscribe(
                        item -> System.out.println("수신: " + item),
                        error -> System.err.println("에러: " + error.getMessage()),
                        () -> System.out.println("완료"),
                        subscription -> subscription.request(3)  // 처음 3개 요청
                );
    }
    
    /**
     * 캐싱 예제
     */
    public static void cachingExample() {
        System.out.println("\n=== Java 9: 캐싱 (cache) ===");
        
        Mono<String> cachedMono = Mono.fromCallable(() -> {
            System.out.println("비용이 큰 작업 수행");
            DataUtil.simulateNetworkIO(1000);
            return "결과 데이터";
        })
        .cache();  // 결과 캐시
        
        System.out.println("첫 번째 구독:");
        cachedMono.subscribe(System.out::println);
        
        System.out.println("두 번째 구독 (캐시된 값 사용):");
        cachedMono.subscribe(System.out::println);
    }
    
    /**
     * 타임아웃 예제
     */
    public static void timeoutExample() {
        System.out.println("\n=== Java 9: 타임아웃 ===");
        
        Mono.delay(Duration.ofSeconds(5))
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn("타임아웃 발생")
                .doOnNext(System.out::println)
                .subscribe();
    }
    
    /**
     * 복합 예제: 여러 API 호출 조합
     */
    public static void complexCombinationExample() {
        System.out.println("\n=== Java 9: 복합 예제 (여러 API 호출) ===");
        
        Mono<String> userMono = Mono.fromCallable(() -> {
            System.out.println("사용자 정보 조회");
            return "User#1";
        });
        
        Mono<String> orderMono = Mono.fromCallable(() -> {
            System.out.println("주문 정보 조회");
            return "Order#100";
        });
        
        Mono<String> paymentMono = Mono.fromCallable(() -> {
            System.out.println("결제 정보 조회");
            return "Payment#50000";
        });
        
        // 3개의 Mono를 조합하여 하나의 결과 생성
        Mono.zip(userMono, orderMono, paymentMono)
                .map(tuple -> tuple.getT1() + " - " + tuple.getT2() + " - " + tuple.getT3())
                .doOnNext(System.out::println)
                .subscribe();
    }
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Java 9-10 Reactive Streams 예제");
        System.out.println("========================================");
        
        monoBasicExample();
        fluxBasicExample();
        dataTransformationExample();
        filteringExample();
        mergingExample();
        concatExample();
        delayedDataExample();
        flatMapExample();
        errorHandlingExample();
        retryExample();
        backpressureExample();
        cachingExample();
        timeoutExample();
        complexCombinationExample();
        
        System.out.println("\n========================================");
        System.out.println("모든 예제 완료");
        System.out.println("========================================");
    }
}
