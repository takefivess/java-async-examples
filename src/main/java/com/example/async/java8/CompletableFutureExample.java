package com.example.async.java8;

import com.example.async.util.DataUtil;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Java 8: CompletableFuture를 이용한 비동기 처리
 * 
 * CompletableFuture는 Java 8에서 도입된 고급 Future 구현으로,
 * 함수형 프로그래밍과 체이닝을 지원하여 비동기 처리를 더 직관적으로 만듭니다.
 */
public class CompletableFutureExample {
    
    /**
     * 기본 CompletableFuture 예제
     */
    public static void basicCompletableFutureExample() {
        System.out.println("\n=== Java 8: 기본 CompletableFuture ===");
        
        // supplyAsync: 값을 반환하는 작업을 비동기로 실행
        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("비동기 작업 수행: " + DataUtil.getCurrentThreadInfo());
                    return DataUtil.simulateNetworkIO(1000);
                });
        
        // thenApply: 이전 결과를 받아서 새로운 작업 수행
        future.thenApply(result -> {
            System.out.println("변환 작업 수행: " + result);
            return result.toUpperCase();
        })
        // thenAccept: 최종 결과를 소비
        .thenAccept(result -> System.out.println("최종 결과: " + result))
        // 완료 대기
        .join();
    }
    
    /**
     * 여러 CompletableFuture 조합 (allOf)
     */
    public static void combinationAllOfExample() {
        System.out.println("\n=== Java 8: 여러 작업 조합 (allOf) ===");
        
        // 3개의 비동기 작업 생성
        CompletableFuture<String> future1 = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("[작업1] 시작");
                    String result = DataUtil.fetchFromDatabase("ID-1");
                    System.out.println("[작업1] " + result);
                    return result;
                });
        
        CompletableFuture<String> future2 = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("[작업2] 시작");
                    String result = DataUtil.fetchFromDatabase("ID-2");
                    System.out.println("[작업2] " + result);
                    return result;
                });
        
        CompletableFuture<String> future3 = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("[작업3] 시작");
                    String result = DataUtil.fetchFromDatabase("ID-3");
                    System.out.println("[작업3] " + result);
                    return result;
                });
        
        // 모든 작업이 완료될 때까지 대기
        long startTime = System.currentTimeMillis();
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);
        allFutures.join();
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("모든 작업 완료 (소요 시간: " + duration + "ms)");
    }
    
    /**
     * 여러 CompletableFuture 조합 (anyOf)
     */
    public static void combinationAnyOfExample() {
        System.out.println("\n=== Java 8: 여러 작업 중 가장 빠른 것 (anyOf) ===");
        
        // 3개의 비동기 작업 생성 (다양한 시간 소요)
        CompletableFuture<String> future1 = CompletableFuture
                .supplyAsync(() -> {
                    DataUtil.simulateNetworkIO(500);
                    return "작업1 완료";
                });
        
        CompletableFuture<String> future2 = CompletableFuture
                .supplyAsync(() -> {
                    DataUtil.simulateNetworkIO(200);  // 가장 빠름
                    return "작업2 완료";
                });
        
        CompletableFuture<String> future3 = CompletableFuture
                .supplyAsync(() -> {
                    DataUtil.simulateNetworkIO(800);
                    return "작업3 완료";
                });
        
        // 가장 빨리 완료되는 작업의 결과 반환
        long startTime = System.currentTimeMillis();
        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(future1, future2, future3);
        String result = (String) anyFuture.join();
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("가장 빠른 결과: " + result + " (소요 시간: " + duration + "ms)");
    }
    
    /**
     * 체이닝 변환 예제
     */
    public static void chainingTransformationExample() {
        System.out.println("\n=== Java 8: 체이닝 변환 ===");
        
        CompletableFuture<Integer> result = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("[Step 1] 데이터 조회");
                    return "100";
                })
                // String -> Integer 변환
                .thenApply(str -> {
                    System.out.println("[Step 2] 파싱: " + str);
                    return Integer.parseInt(str);
                })
                // 값 증가
                .thenApply(num -> {
                    System.out.println("[Step 3] 계산: " + num + " * 2");
                    return num * 2;
                })
                // 새로운 CompletableFuture 반환 (평탄화)
                .thenCompose(num -> CompletableFuture.supplyAsync(() -> {
                    System.out.println("[Step 4] 데이터베이스 저장: " + num);
                    DataUtil.simulateNetworkIO(500);
                    return num + 10;
                }));
        
        int finalResult = result.join();
        System.out.println("최종 결과: " + finalResult);
    }
    
    /**
     * 예외 처리 예제
     */
    public static void exceptionHandlingExample() {
        System.out.println("\n=== Java 8: 예외 처리 ===");
        
        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("작업 수행 중...");
                    throw new RuntimeException("의도적인 예외 발생");
                })
                // exceptionally: 예외 발생 시 기본값 반환
                .exceptionally(ex -> {
                    System.out.println("예외 처리 (exceptionally): " + ex.getMessage());
                    return "기본값";
                });
        
        String result = future.join();
        System.out.println("결과: " + result);
        
        // handle: 성공/실패 모두 처리
        System.out.println("\n=== handle 메서드를 이용한 처리 ===");
        CompletableFuture<String> handleFuture = CompletableFuture
                .supplyAsync(() -> {
                    throw new RuntimeException("오류 발생");
                })
                .handle((result2, throwable) -> {
                    if (throwable != null) {
                        System.out.println("오류 처리 (handle): " + throwable.getMessage());
                        return "오류 발생함";
                    } else {
                        return "성공: " + result2;
                    }
                });
        
        String handleResult = handleFuture.join();
        System.out.println("결과: " + handleResult);
    }
    
    /**
     * 타임아웃 설정 예제 (Java 9+)
     */
    public static void timeoutExample() {
        System.out.println("\n=== Java 8: 타임아웃 설정 (orTimeout) ===");
        
        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // 5초 대기 (타임아웃보다 길음)
                        Thread.sleep(5000);
                        return "작업 완료";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "중단됨";
                    }
                });
        
        try {
            // 2초 내에 완료되지 않으면 TimeoutException 발생
            // (Java 9+ 기능)
            // String result = future.orTimeout(2, TimeUnit.SECONDS).join();
            
            // Java 8 호환 방식
            String result = future.completeOnTimeout("타임아웃", 2, TimeUnit.SECONDS).join();
            System.out.println("결과: " + result);
        } catch (CompletionException e) {
            System.out.println("타임아웃 발생: " + e.getMessage());
        }
    }
    
    /**
     * 커스텀 Executor 사용 예제
     */
    public static void customExecutorExample() {
        System.out.println("\n=== Java 8: 커스텀 Executor 사용 ===");
        
        // 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            CompletableFuture<String> future1 = CompletableFuture
                    .supplyAsync(() -> {
                        System.out.println("[작업1] 스레드: " + DataUtil.getCurrentThreadInfo());
                        return DataUtil.fetchFromDatabase("ID-A");
                    }, executor);
            
            CompletableFuture<String> future2 = CompletableFuture
                    .supplyAsync(() -> {
                        System.out.println("[작업2] 스레드: " + DataUtil.getCurrentThreadInfo());
                        return DataUtil.fetchFromDatabase("ID-B");
                    }, executor);
            
            // 두 작업 모두 완료 대기
            CompletableFuture<Void> combined = CompletableFuture.allOf(future1, future2);
            combined.join();
            
            System.out.println("작업1 결과: " + future1.join());
            System.out.println("작업2 결과: " + future2.join());
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * 대량 비동기 작업 처리 예제
     */
    public static void bulkAsyncProcessingExample() {
        System.out.println("\n=== Java 8: 대량 비동기 작업 처리 ===");
        
        int taskCount = 10;
        long startTime = System.currentTimeMillis();
        
        // 10개의 비동기 작업 생성
        var futures = IntStream.range(0, taskCount)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    String result = DataUtil.fetchFromDatabase("Task-" + i);
                    System.out.println("작업 " + i + " 완료: " + result);
                    return result;
                }))
                .collect(Collectors.toList());
        
        // 모든 작업 완료 대기
        CompletableFuture<Void> allDone = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]));
        allDone.join();
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("대량 작업 완료 (소요 시간: " + duration + "ms)");
    }
    
    /**
     * 동기와 비동기 혼합 예제
     */
    public static void syncAsyncMixExample() {
        System.out.println("\n=== Java 8: 동기와 비동기 혼합 ===");
        
        // 동기 작업
        System.out.println("동기 작업: 데이터 준비");
        String data = "기본 데이터";
        
        // 비동기 작업
        CompletableFuture<String> asyncResult = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("비동기 작업: 네트워크 요청");
                    return DataUtil.simulateNetworkIO(1000);
                })
                .thenCombine(
                        CompletableFuture.completedFuture(data),
                        (networkData, baseData) -> {
                            System.out.println("결합: " + networkData + " + " + baseData);
                            return networkData + " | " + baseData;
                        }
                );
        
        String result = asyncResult.join();
        System.out.println("최종 결과: " + result);
    }
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Java 8 CompletableFuture 예제");
        System.out.println("========================================");
        
        basicCompletableFutureExample();
        combinationAllOfExample();
        combinationAnyOfExample();
        chainingTransformationExample();
        exceptionHandlingExample();
        timeoutExample();
        customExecutorExample();
        bulkAsyncProcessingExample();
        syncAsyncMixExample();
        
        System.out.println("\n========================================");
        System.out.println("모든 예제 완료");
        System.out.println("========================================");
    }
}
