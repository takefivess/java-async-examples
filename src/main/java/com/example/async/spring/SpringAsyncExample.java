package com.example.async.spring;

import com.example.async.util.DataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;

/**
 * Spring Framework @Async 예제 애플리케이션
 * 
 * @EnableAsync 어노테이션을 통해 @Async 기능을 활성화합니다.
 * AsyncConfigurer를 구현하여 스레드 풀을 커스터마이징할 수 있습니다.
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "com.example.async.spring")
public class SpringAsyncExample implements AsyncConfigurer, ApplicationRunner {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    /**
     * 비동기 작업용 Executor 설정
     * 
     * @return ThreadPoolTaskExecutor - 커스터마이징된 스레드 풀
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 코어 스레드 수 (기본 활성 스레드)
        executor.setCorePoolSize(5);
        
        // 최대 스레드 수
        executor.setMaxPoolSize(20);
        
        // 큐 크기 (대기 중인 작업)
        executor.setQueueCapacity(100);
        
        // 스레드 이름 프리픽스
        executor.setThreadNamePrefix("spring-async-");
        
        // 모든 작업이 완료될 때까지 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 최대 대기 시간 (초)
        executor.setAwaitTerminationSeconds(60);
        
        // Executor 초기화
        executor.initialize();
        
        return executor;
    }
    
    /**
     * 처리되지 않은 예외 핸들러
     */
    @Override
    public org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler 
            getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("비동기 메서드 {} 에서 예외 발생", method.getName(), ex);
        };
    }
    
    /**
     * 애플리케이션 실행 로직
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("========================================");
        System.out.println("Spring Framework @Async 예제");
        System.out.println("========================================");
        
        simpleAsyncExample();
        CompletableFutureExample();
        asyncResultExample();
        parallelAsyncExample();
        errorHandlingAsyncExample();
        chainedAsyncExample();
        
        // 비동기 작업 완료 대기 (간단한 방법)
        Thread.sleep(5000);
        
        System.out.println("\n========================================");
        System.out.println("모든 예제 완료");
        System.out.println("========================================");
        
        // SpringApplication 종료
        System.exit(0);
    }
    
    /**
     * 간단한 비동기 작업 예제
     */
    private void simpleAsyncExample() {
        System.out.println("\n=== Spring @Async: 간단한 비동기 작업 ===");
        System.out.println("메인 스레드: " + DataUtil.getCurrentThreadInfo());
        
        // void를 반환하는 비동기 작업
        asyncTaskService.simpleAsyncTask("Task-1");
        asyncTaskService.simpleAsyncTask("Task-2");
        asyncTaskService.simpleAsyncTask("Task-3");
        
        System.out.println("메인 스레드에서는 비동기 작업이 시작되었습니다. (완료 대기 안 함)");
    }
    
    /**
     * CompletableFuture를 반환하는 비동기 작업 예제
     */
    private void CompletableFutureExample() {
        System.out.println("\n=== Spring @Async: CompletableFuture 반환 ===");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 여러 비동기 작업 생성
            CompletableFuture<String> future1 = asyncTaskService.asyncTaskWithResult("USER-001");
            CompletableFuture<String> future2 = asyncTaskService.asyncTaskWithResult("USER-002");
            CompletableFuture<String> future3 = asyncTaskService.asyncTaskWithResult("USER-003");
            
            // 모든 작업 완료 대기
            CompletableFuture<Void> allDone = CompletableFuture.allOf(future1, future2, future3);
            allDone.join();
            
            // 결과 출력
            System.out.println("결과 1: " + future1.join());
            System.out.println("결과 2: " + future2.join());
            System.out.println("결과 3: " + future3.join());
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("소요 시간: " + duration + "ms");
        } catch (Exception e) {
            log.error("CompletableFuture 예제 오류", e);
        }
    }
    
    /**
     * AsyncResult를 반환하는 비동기 작업 예제
     */
    private void asyncResultExample() {
        System.out.println("\n=== Spring @Async: AsyncResult 반환 ===");
        
        try {
            // AsyncResult를 반환하는 비동기 작업
            // 주의: AsyncResult는 CompletableFuture보다 낮은 수준의 API입니다.
            asyncTaskService.asyncTaskWithAsyncResult("ORDER-001");
            asyncTaskService.asyncTaskWithAsyncResult("ORDER-002");
            
            System.out.println("AsyncResult 작업 시작됨");
            Thread.sleep(2000);
        } catch (Exception e) {
            log.error("AsyncResult 예제 오류", e);
        }
    }
    
    /**
     * 병렬 비동기 작업 예제
     */
    private void parallelAsyncExample() {
        System.out.println("\n=== Spring @Async: 병렬 비동기 작업 ===");
        
        try {
            List<String> userIds = Arrays.asList("USER-001", "USER-002", "USER-003", "USER-004", "USER-005");
            
            long startTime = System.currentTimeMillis();
            
            // 여러 사용자를 병렬로 처리
            CompletableFuture<List<String>> results = asyncTaskService.parallelAsyncTasks(userIds);
            
            // 결과 대기
            List<String> resultList = results.join();
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("병렬 처리 결과:");
            for (int i = 0; i < resultList.size(); i++) {
                System.out.println("  [" + i + "] " + resultList.get(i));
            }
            System.out.println("소요 시간: " + duration + "ms");
        } catch (Exception e) {
            log.error("병렬 비동기 예제 오류", e);
        }
    }
    
    /**
     * 예외 처리를 포함한 비동기 작업 예제
     */
    private void errorHandlingAsyncExample() {
        System.out.println("\n=== Spring @Async: 예외 처리 ===");
        
        try {
            CompletableFuture<String> future1 = asyncTaskService.asyncTaskWithErrorHandling("PAY-001");
            CompletableFuture<String> future2 = asyncTaskService.asyncTaskWithErrorHandling("PAY-002");
            
            // 결과 및 예외 처리
            future1.thenAccept(result -> System.out.println("결과 1: " + result))
                    .exceptionally(ex -> {
                        System.out.println("예외 1: " + ex.getMessage());
                        return null;
                    })
                    .join();
            
            future2.thenAccept(result -> System.out.println("결과 2: " + result))
                    .exceptionally(ex -> {
                        System.out.println("예외 2: " + ex.getMessage());
                        return null;
                    })
                    .join();
        } catch (Exception e) {
            log.error("예외 처리 예제 오류", e);
        }
    }
    
    /**
     * 체이닝된 비동기 작업 예제
     */
    private void chainedAsyncExample() {
        System.out.println("\n=== Spring @Async: 체이닝 비동기 작업 ===");
        
        try {
            long startTime = System.currentTimeMillis();
            
            CompletableFuture<String> result = asyncTaskService.chainedAsyncTasks("ORD-12345");
            
            // 최종 결과 대기
            String finalResult = result.join();
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("최종 결과: " + finalResult);
            System.out.println("소요 시간: " + duration + "ms");
        } catch (Exception e) {
            log.error("체이닝 예제 오류", e);
        }
    }
    
    public static void main(String[] args) {
        SpringApplication.run(SpringAsyncExample.class, args);
    }
}
