package com.example.async.java19;

import com.example.async.util.DataUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Java 19: Virtual Threads (Project Loom)를 이용한 경량 비동기 처리
 * 
 * Virtual Threads는 Java 19에서 Preview로 도입된 경량 스레드입니다.
 * 플랫폼 스레드보다 훨씬 가벼우며 수천 개를 동시에 생성할 수 있습니다.
 */
public class VirtualThreadsExample {
    
    /**
     * 기본 Virtual Thread 생성 및 실행
     */
    public static void basicVirtualThreadExample() {
        System.out.println("\n=== Java 19: 기본 Virtual Thread ===");
        
        // Virtual Thread 생성 및 시작
        Thread vThread = Thread.ofVirtual()
                .name("VThread-1")
                .start(() -> {
                    System.out.println("Virtual Thread 실행: " + DataUtil.getCurrentThreadInfo());
                    DataUtil.simulateNetworkIO(1000);
                    System.out.println("Virtual Thread 완료");
                });
        
        try {
            vThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 여러 Virtual Thread 생성
     */
    public static void multipleVirtualThreadsExample() {
        System.out.println("\n=== Java 19: 여러 Virtual Thread 생성 ===");
        
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        
        long startTime = System.currentTimeMillis();
        
        // 5개의 Virtual Thread 생성
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = Thread.ofVirtual()
                    .name("VThread-" + i)
                    .start(() -> {
                        System.out.println("[" + index + "] 시작: " + DataUtil.getCurrentThreadInfo());
                        String result = DataUtil.fetchFromDatabase("ID-" + index);
                        System.out.println("[" + index + "] " + result);
                    });
        }
        
        // 모든 Virtual Thread 완료 대기
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("모든 Virtual Thread 완료 (소요 시간: " + duration + "ms)");
    }
    
    /**
     * 대량의 Virtual Thread 생성 (1000개)
     */
    public static void massVirtualThreadsExample() {
        System.out.println("\n=== Java 19: 대량 Virtual Thread (1000개) ===");
        
        int threadCount = 1000;
        Thread[] threads = new Thread[threadCount];
        
        long startTime = System.currentTimeMillis();
        
        // 1000개의 Virtual Thread 생성
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = Thread.ofVirtual()
                    .start(() -> {
                        // 간단한 I/O 작업
                        DataUtil.fetchFromDatabase("Task-" + index);
                        if (index % 100 == 0) {
                            System.out.println("진행: " + index + "/" + threadCount);
                        }
                    });
        }
        
        // 모든 Virtual Thread 완료 대기
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("1000개 Virtual Thread 완료 (소요 시간: " + duration + "ms)");
        System.out.println("평균 처리 시간: " + (duration / threadCount) + "ms/thread");
    }
    
    /**
     * Virtual Thread Executor 사용
     */
    public static void virtualThreadExecutorExample() {
        System.out.println("\n=== Java 19: Virtual Thread Executor ===");
        
        // Virtual Thread 전용 Executor 생성
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 10개의 작업을 Virtual Thread에서 실행
            for (int i = 0; i < 10; i++) {
                final int index = i;
                executor.submit(() -> {
                    System.out.println("[작업" + index + "] 시작: " + DataUtil.getCurrentThreadInfo());
                    String result = DataUtil.fetchFromDatabase("ID-" + index);
                    System.out.println("[작업" + index + "] " + result);
                });
            }
            
            // 모든 작업 완료 대기
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.SECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Executor 작업 완료 (소요 시간: " + duration + "ms)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Virtual Thread vs Platform Thread 비교
     */
    public static void virtualThreadVsPlatformThreadComparison() {
        System.out.println("\n=== Java 19: Virtual Thread vs Platform Thread 비교 ===");
        
        int taskCount = 100;
        
        // Platform Thread 테스트
        System.out.println("\n[Platform Thread]");
        long platformStartTime = System.currentTimeMillis();
        ExecutorService platformExecutor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < taskCount; i++) {
            platformExecutor.submit(() -> DataUtil.fetchFromDatabase("Task"));
        }
        
        platformExecutor.shutdown();
        try {
            platformExecutor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long platformDuration = System.currentTimeMillis() - platformStartTime;
        System.out.println("Platform Thread 완료 시간: " + platformDuration + "ms");
        
        // Virtual Thread 테스트
        System.out.println("\n[Virtual Thread]");
        long virtualStartTime = System.currentTimeMillis();
        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
        
        for (int i = 0; i < taskCount; i++) {
            virtualExecutor.submit(() -> DataUtil.fetchFromDatabase("Task"));
        }
        
        virtualExecutor.shutdown();
        try {
            virtualExecutor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long virtualDuration = System.currentTimeMillis() - virtualStartTime;
        System.out.println("Virtual Thread 완료 시간: " + virtualDuration + "ms");
        
        System.out.println("\n비교 결과:");
        System.out.println("Platform Thread: " + platformDuration + "ms");
        System.out.println("Virtual Thread: " + virtualDuration + "ms");
        System.out.println("개선율: " + ((platformDuration - virtualDuration) * 100.0 / platformDuration) + "%");
    }
    
    /**
     * Virtual Thread 내에서 예외 처리
     */
    public static void exceptionHandlingExample() {
        System.out.println("\n=== Java 19: Virtual Thread 예외 처리 ===");
        
        Thread vThread = Thread.ofVirtual()
                .name("VThread-Exception")
                .start(() -> {
                    try {
                        System.out.println("작업 수행 중...");
                        throw new RuntimeException("Virtual Thread 내 예외 발생");
                    } catch (RuntimeException e) {
                        System.out.println("예외 처리: " + e.getMessage());
                    }
                });
        
        try {
            vThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Virtual Thread 내에서 스레드 로컬 사용
     */
    public static void threadLocalExample() {
        System.out.println("\n=== Java 19: Virtual Thread와 ThreadLocal ===");
        
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        
        Thread vThread1 = Thread.ofVirtual()
                .name("VThread-1")
                .start(() -> {
                    threadLocal.set("데이터-1");
                    System.out.println("스레드 1: " + threadLocal.get());
                });
        
        Thread vThread2 = Thread.ofVirtual()
                .name("VThread-2")
                .start(() -> {
                    threadLocal.set("데이터-2");
                    System.out.println("스레드 2: " + threadLocal.get());
                });
        
        try {
            vThread1.join();
            vThread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Virtual Thread 그룹 관리
     */
    public static void threadGroupManagementExample() {
        System.out.println("\n=== Java 19: Virtual Thread 관리 ===");
        
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        
        // Virtual Thread 생성
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = Thread.ofVirtual()
                    .name("ManagedVThread-" + i)
                    .start(() -> {
                        try {
                            Thread.sleep(500);
                            System.out.println("스레드 " + index + " 완료");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
        }
        
        // 생성된 스레드 정보 출력
        System.out.println("생성된 스레드 정보:");
        for (int i = 0; i < threadCount; i++) {
            System.out.println("  - " + threads[i].getName() + ": " + 
                    (threads[i].isAlive() ? "실행중" : "완료"));
        }
        
        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * I/O 바운드 작업 최적화 (Virtual Thread의 강점)
     */
    public static void ioOptimizationExample() {
        System.out.println("\n=== Java 19: I/O 바운드 작업 최적화 ===");
        
        int apiCount = 50;  // 50개의 API 호출
        
        long startTime = System.currentTimeMillis();
        
        // Virtual Thread를 이용한 병렬 API 호출
        Thread[] threads = IntStream.range(0, apiCount)
                .mapToObj(i -> Thread.ofVirtual()
                        .start(() -> {
                            String result = DataUtil.callExternalAPI("/api/endpoint-" + i);
                            System.out.println("API 호출 " + i + " 완료");
                        }))
                .toArray(Thread[]::new);
        
        // 모든 API 호출 완료 대기
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("50개 API 호출 완료 (소요 시간: " + duration + "ms)");
        System.out.println("평균 호출 시간: " + (duration / apiCount) + "ms/call");
    }
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Java 19 Virtual Threads 예제");
        System.out.println("========================================");
        
        basicVirtualThreadExample();
        multipleVirtualThreadsExample();
        massVirtualThreadsExample();
        virtualThreadExecutorExample();
        virtualThreadVsPlatformThreadComparison();
        exceptionHandlingExample();
        threadLocalExample();
        threadGroupManagementExample();
        ioOptimizationExample();
        
        System.out.println("\n========================================");
        System.out.println("모든 예제 완료");
        System.out.println("========================================");
    }
}
