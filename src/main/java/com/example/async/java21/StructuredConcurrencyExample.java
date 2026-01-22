package com.example.async.java21;

import com.example.async.util.DataUtil;
import java.util.concurrent.*;

/**
 * Java 21: Structured Concurrency를 이용한 안전한 비동기 처리
 * 
 * Structured Concurrency는 Java 21에서 정식 지원되는 API로,
 * 명시적인 스코프 내에서 비동기 작업을 관리하여 리소스 누수를 방지합니다.
 */
public class StructuredConcurrencyExample {
    
    /**
     * 기본 StructuredTaskScope 사용
     */
    public static void basicStructuredTaskScopeExample() {
        System.out.println("\n=== Java 21: 기본 StructuredTaskScope ===");
        
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
            // 비동기 작업 1
            var future1 = scope.fork(() -> {
                System.out.println("작업 1 시작");
                String result = DataUtil.fetchFromDatabase("ID-1");
                System.out.println("작업 1 완료: " + result);
                return result;
            });
            
            // 비동기 작업 2
            var future2 = scope.fork(() -> {
                System.out.println("작업 2 시작");
                String result = DataUtil.fetchFromDatabase("ID-2");
                System.out.println("작업 2 완료: " + result);
                return result;
            });
            
            // 모든 작업 완료 대기
            scope.join();
            
            System.out.println("결과 1: " + future1.resultNow());
            System.out.println("결과 2: " + future2.resultNow());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 여러 작업 조합 및 실패 처리
     */
    public static void multipleTasksWithErrorHandlingExample() {
        System.out.println("\n=== Java 21: 여러 작업 및 에러 처리 ===");
        
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // 여러 비동기 작업 생성
            var future1 = scope.fork(() -> {
                System.out.println("작업 1 시작: " + DataUtil.getCurrentThreadInfo());
                return DataUtil.callExternalAPI("/api/users");
            });
            
            var future2 = scope.fork(() -> {
                System.out.println("작업 2 시작: " + DataUtil.getCurrentThreadInfo());
                return DataUtil.callExternalAPI("/api/orders");
            });
            
            var future3 = scope.fork(() -> {
                System.out.println("작업 3 시작: " + DataUtil.getCurrentThreadInfo());
                return DataUtil.callExternalAPI("/api/payments");
            });
            
            // 모든 작업 완료 대기 (하나 실패 시 즉시 종료)
            scope.join().throwIfFailed();
            
            System.out.println("모든 작업 성공!");
            System.out.println("결과 1: " + future1.resultNow());
            System.out.println("결과 2: " + future2.resultNow());
            System.out.println("결과 3: " + future3.resultNow());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("작업이 중단되었습니다.");
        } catch (ExecutionException e) {
            System.out.println("작업 실패: " + e.getCause().getMessage());
        }
    }
    
    /**
     * 동적 작업 추가
     */
    public static void dynamicTaskAdditionExample() {
        System.out.println("\n=== Java 21: 동적 작업 추가 ===");
        
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
            // 초기 작업
            var initialFuture = scope.fork(() -> {
                System.out.println("초기 작업 수행");
                DataUtil.simulateNetworkIO(500);
                return 5;
            });
            
            // 초기 작업 완료 후 추가 작업 생성
            scope.join();
            
            int initialResult = initialFuture.resultNow();
            System.out.println("초기 결과: " + initialResult);
            
            // 다른 스코프에서 추가 작업 수행
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 병렬 데이터 처리
     */
    public static void parallelDataProcessingExample() {
        System.out.println("\n=== Java 21: 병렬 데이터 처리 ===");
        
        String[] dataIds = {"A", "B", "C", "D", "E"};
        
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
            // 각 데이터를 병렬로 처리
            var futures = new Future[dataIds.length];
            
            for (int i = 0; i < dataIds.length; i++) {
                final int index = i;
                futures[i] = scope.fork(() -> {
                    System.out.println("[처리 " + index + "] " + dataIds[index] + " 처리 중...");
                    String result = DataUtil.fetchFromDatabase(dataIds[index]);
                    System.out.println("[처리 " + index + "] " + result);
                    return result;
                });
            }
            
            // 모든 처리 완료 대기
            scope.join();
            
            System.out.println("모든 데이터 처리 완료");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 중첩된 StructuredTaskScope
     */
    public static void nestedStructuredTaskScopeExample() {
        System.out.println("\n=== Java 21: 중첩된 StructuredTaskScope ===");
        
        try (var outerScope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
            // 외부 레벨 작업 1
            var outerFuture1 = outerScope.fork(() -> {
                System.out.println("[외부] 작업 1 시작");
                
                // 내부 스코프 (중첩)
                try (var innerScope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
                    var innerFuture1 = innerScope.fork(() -> {
                        System.out.println("[내부-1] 작업 1-1");
                        return DataUtil.fetchFromDatabase("ID-1-1");
                    });
                    
                    var innerFuture2 = innerScope.fork(() -> {
                        System.out.println("[내부-1] 작업 1-2");
                        return DataUtil.fetchFromDatabase("ID-1-2");
                    });
                    
                    innerScope.join();
                    return innerFuture1.resultNow() + " | " + innerFuture2.resultNow();
                }
            });
            
            // 외부 레벨 작업 2
            var outerFuture2 = outerScope.fork(() -> {
                System.out.println("[외부] 작업 2 시작");
                return DataUtil.fetchFromDatabase("ID-2");
            });
            
            outerScope.join();
            
            System.out.println("외부 결과 1: " + outerFuture1.resultNow());
            System.out.println("외부 결과 2: " + outerFuture2.resultNow());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 타임아웃을 포함한 작업 스코프
     */
    public static void timeoutTaskScopeExample() {
        System.out.println("\n=== Java 21: 타임아웃을 포함한 작업 스코프 ===");
        
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
            // 빠른 작업
            var fastFuture = scope.fork(() -> {
                System.out.println("빠른 작업 시작");
                DataUtil.simulateNetworkIO(500);
                return "빠른 결과";
            });
            
            // 느린 작업
            var slowFuture = scope.fork(() -> {
                System.out.println("느린 작업 시작");
                DataUtil.simulateNetworkIO(5000);
                return "느린 결과";
            });
            
            // 타임아웃 설정
            try {
                scope.joinUntil(System.currentTimeMillis() + 2000);
            } catch (InterruptedException | TimeoutException e) {
                System.out.println("타임아웃 발생: " + e.getMessage());
            }
            
            System.out.println("빠른 작업 결과: " + fastFuture.resultNow());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Virtual Threads와 Structured Concurrency 조합
     */
    public static void virtualThreadsWithStructuredConcurrencyExample() {
        System.out.println("\n=== Java 21: Virtual Threads + Structured Concurrency ===");
        
        try {
            // Virtual Thread를 사용하여 StructuredTaskScope 실행
            Thread vThread = Thread.ofVirtual()
                    .start(() -> {
                        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
                            var future1 = scope.fork(() -> {
                                System.out.println("[VThread] 작업 1");
                                return DataUtil.fetchFromDatabase("ID-1");
                            });
                            
                            var future2 = scope.fork(() -> {
                                System.out.println("[VThread] 작업 2");
                                return DataUtil.fetchFromDatabase("ID-2");
                            });
                            
                            scope.join();
                            System.out.println("[VThread] 결과: " + future1.resultNow() + 
                                    " | " + future2.resultNow());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
            
            vThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 리소스 관리 및 자동 정리
     */
    public static void resourceManagementExample() {
        System.out.println("\n=== Java 21: 리소스 관리 및 자동 정리 ===");
        
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<>()) {
            // try-with-resources로 자동 정리 보장
            var future1 = scope.fork(() -> {
                System.out.println("리소스 1 생성 및 사용");
                DataUtil.simulateNetworkIO(500);
                return "리소스 1 처리 완료";
            });
            
            var future2 = scope.fork(() -> {
                System.out.println("리소스 2 생성 및 사용");
                DataUtil.simulateNetworkIO(500);
                return "리소스 2 처리 완료";
            });
            
            scope.join();
            System.out.println("결과: " + future1.resultNow());
            System.out.println("결과: " + future2.resultNow());
            
            // 스코프 종료 시 자동으로 모든 작업 정리
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("스코프 종료 - 모든 리소스가 자동으로 정리됨");
    }
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Java 21 Structured Concurrency 예제");
        System.out.println("========================================");
        
        basicStructuredTaskScopeExample();
        multipleTasksWithErrorHandlingExample();
        dynamicTaskAdditionExample();
        parallelDataProcessingExample();
        nestedStructuredTaskScopeExample();
        timeoutTaskScopeExample();
        virtualThreadsWithStructuredConcurrencyExample();
        resourceManagementExample();
        
        System.out.println("\n========================================");
        System.out.println("모든 예제 완료");
        System.out.println("========================================");
    }
}
