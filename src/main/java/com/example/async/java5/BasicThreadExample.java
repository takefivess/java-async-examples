package com.example.async.java5;

import com.example.async.util.DataUtil;

/**
 * Java 5-7: 기본 멀티스레딩 방식
 * 
 * Thread와 Runnable을 사용한 기초적인 비동기 처리 방식입니다.
 * 가장 저수준의 API이지만 모든 비동기 처리의 기반이 됩니다.
 */
public class BasicThreadExample {
    
    /**
     * 단일 스레드 실행 예제
     */
    public static void singleThreadExample() {
        System.out.println("\n=== Java 5-7: 기본 Thread 실행 ===");
        System.out.println("메인 스레드: " + DataUtil.getCurrentThreadInfo());
        
        // 새로운 스레드 생성 및 시작
        Thread workerThread = new Thread(() -> {
            System.out.println("워커 스레드: " + DataUtil.getCurrentThreadInfo());
            String result = DataUtil.simulateNetworkIO(1000);
            System.out.println("결과: " + result);
        });
        
        workerThread.setName("Worker-1");
        workerThread.start();
        
        try {
            // 워커 스레드가 완료될 때까지 대기
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("메인 스레드 종료");
    }
    
    /**
     * 여러 스레드 병렬 실행 예제
     */
    public static void multipleThreadsExample() {
        System.out.println("\n=== Java 5-7: 여러 Thread 병렬 실행 ===");
        
        // 5개의 스레드 생성
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                System.out.println("스레드 " + index + " 시작: " + DataUtil.getCurrentThreadInfo());
                String result = DataUtil.fetchFromDatabase("ID-" + index);
                System.out.println("스레드 " + index + " 결과: " + result);
            });
            threads[i].setName("Worker-" + index);
        }
        
        // 모든 스레드 시작
        long startTime = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("모든 스레드 완료 (소요 시간: " + duration + "ms)");
    }
    
    /**
     * 스레드 풀 패턴 (수동 구현)
     */
    public static void threadPoolPatternExample() {
        System.out.println("\n=== Java 5-7: 수동 스레드 풀 패턴 ===");
        
        int numThreads = 3;
        Thread[] threadPool = new Thread[numThreads];
        boolean[] threadActive = new boolean[numThreads];
        
        // 스레드 풀 초기화
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threadPool[i] = new Thread(() -> {
                threadActive[threadId] = true;
                System.out.println("스레드 풀 " + threadId + " 시작");
                
                // 작업 수행
                for (int j = 0; j < 3; j++) {
                    DataUtil.fetchFromDatabase("Task-" + threadId + "-" + j);
                    System.out.println("  태스크 " + j + " 완료");
                }
                
                threadActive[threadId] = false;
                System.out.println("스레드 풀 " + threadId + " 종료");
            });
            threadPool[i].setName("PoolThread-" + i);
            threadPool[i].start();
        }
        
        // 모든 스레드 완료 대기
        for (Thread thread : threadPool) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("스레드 풀 작업 완료");
    }
    
    /**
     * 스레드 간 통신 예제 (synchronized 사용)
     */
    public static void interThreadCommunicationExample() {
        System.out.println("\n=== Java 5-7: 스레드 간 통신 ===");
        
        // 공유 데이터
        int[] sharedData = {0};
        Object lock = new Object();
        
        // 프로듀서 스레드
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                synchronized (lock) {
                    sharedData[0] = i;
                    System.out.println("프로듀서: 데이터 생성 - " + i);
                    lock.notifyAll();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // 컨슈머 스레드
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                synchronized (lock) {
                    while (sharedData[0] < i) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    System.out.println("컨슈머: 데이터 소비 - " + sharedData[0]);
                }
            }
        });
        
        producer.setName("Producer");
        consumer.setName("Consumer");
        
        producer.start();
        consumer.start();
        
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("스레드 간 통신 완료");
    }
    
    /**
     * 예외 처리 예제
     */
    public static void exceptionHandlingExample() {
        System.out.println("\n=== Java 5-7: 스레드에서 예외 처리 ===");
        
        Thread workerThread = new Thread(() -> {
            try {
                System.out.println("작업 중...");
                throw new RuntimeException("의도적인 예외 발생");
            } catch (RuntimeException e) {
                System.out.println("예외 처리: " + e.getMessage());
            }
        });
        
        // 처리하지 못한 예외를 위한 핸들러
        workerThread.setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("미처리 예외 핸들러: " + throwable.getMessage());
        });
        
        workerThread.start();
        
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 데몬 스레드 예제
     */
    public static void daemonThreadExample() {
        System.out.println("\n=== Java 5-7: 데몬 스레드 ===");
        
        Thread daemonThread = new Thread(() -> {
            System.out.println("데몬 스레드 시작");
            int count = 0;
            while (true) {
                System.out.println("데몬 스레드 실행 중... (" + (++count) + ")");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // 데몬 스레드 설정
        daemonThread.setDaemon(true);
        daemonThread.setName("DaemonThread");
        daemonThread.start();
        
        System.out.println("메인 스레드 작업...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("메인 스레드 종료 (데몬 스레드도 함께 종료됨)");
    }
    
    /**
     * 스레드 우선순위 예제
     */
    public static void threadPriorityExample() {
        System.out.println("\n=== Java 5-7: 스레드 우선순위 ===");
        
        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            int priority = Thread.currentThread().getPriority();
            System.out.println(threadName + " (우선순위: " + priority + ") 실행");
            
            // 작업 수행
            DataUtil.performCalculation(10000000);
            System.out.println(threadName + " 완료");
        };
        
        Thread highPriority = new Thread(task);
        highPriority.setName("HighPriority");
        highPriority.setPriority(Thread.MAX_PRIORITY);
        
        Thread normalPriority = new Thread(task);
        normalPriority.setName("NormalPriority");
        normalPriority.setPriority(Thread.NORM_PRIORITY);
        
        Thread lowPriority = new Thread(task);
        lowPriority.setName("LowPriority");
        lowPriority.setPriority(Thread.MIN_PRIORITY);
        
        highPriority.start();
        normalPriority.start();
        lowPriority.start();
        
        try {
            highPriority.join();
            normalPriority.join();
            lowPriority.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("우선순위 테스트 완료");
    }
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Java 5-7 기본 멀티스레딩 예제");
        System.out.println("========================================");
        
        singleThreadExample();
        multipleThreadsExample();
        threadPoolPatternExample();
        interThreadCommunicationExample();
        exceptionHandlingExample();
        daemonThreadExample();
        threadPriorityExample();
        
        System.out.println("\n========================================");
        System.out.println("모든 예제 완료");
        System.out.println("========================================");
    }
}
