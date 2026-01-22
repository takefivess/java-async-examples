package com.example.async.spring;

import com.example.async.util.DataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Spring @Async 어노테이션을 이용한 비동기 처리 서비스
 * 
 * @Async 어노테이션은 메서드가 별도의 스레드에서 비동기로 실행되도록 합니다.
 * 스레드 풀을 통해 관리되므로 리소스 효율성이 높습니다.
 */
@Slf4j
@Service
public class AsyncTaskService {
    
    /**
     * 간단한 비동기 작업 (void 반환)
     * 
     * @param taskName 작업명
     */
    @Async
    public void simpleAsyncTask(String taskName) {
        log.info("[{}] 비동기 작업 시작: {}", taskName, DataUtil.getCurrentThreadInfo());
        
        // I/O 작업 시뮬레이션
        String result = DataUtil.fetchFromDatabase(taskName);
        
        log.info("[{}] 작업 결과: {}", taskName, result);
    }
    
    /**
     * CompletableFuture를 반환하는 비동기 작업
     * 
     * @param userId 사용자 ID
     * @return CompletableFuture - 비동기 작업 결과
     */
    @Async
    public CompletableFuture<String> asyncTaskWithResult(String userId) {
        log.info("[{}] CompletableFuture 작업 시작", userId);
        
        try {
            // I/O 작업
            String result = DataUtil.callExternalAPI("/api/users/" + userId);
            
            log.info("[{}] 작업 완료: {}", userId, result);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("[{}] 작업 중 오류 발생", userId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * AsyncResult를 반환하는 비동기 작업 (Spring 전용)
     * 
     * @param orderId 주문 ID
     * @return AsyncResult - 비동기 작업 결과
     */
    @Async
    public AsyncResult<String> asyncTaskWithAsyncResult(String orderId) {
        log.info("[{}] AsyncResult 작업 시작", orderId);
        
        try {
            String result = DataUtil.fetchFromDatabase(orderId);
            
            log.info("[{}] 작업 완료", orderId);
            // AsyncResult로 래핑하여 반환
            return new AsyncResult<>(result);
        } catch (Exception e) {
            log.error("[{}] 작업 중 오류 발생", orderId, e);
            return new AsyncResult<>(null);
        }
    }
    
    /**
     * ListenableFuture를 반환하는 비동기 작업
     * 
     * @param productId 상품 ID
     * @return ListenableFuture - 리스너 기반 비동기 결과
     */
    @Async
    public ListenableFuture<String> asyncTaskWithListenableFuture(String productId) {
        log.info("[{}] ListenableFuture 작업 시작", productId);
        
        try {
            // 데이터 조회
            String result = DataUtil.callExternalAPI("/api/products/" + productId);
            
            log.info("[{}] 작업 완료", productId);
            // CompletableFuture는 ListenableFuture를 구현하지 않으므로 변환 필요
            return new org.springframework.util.concurrent.ListenableFutureTask<>(
                    () -> result
            );
        } catch (Exception e) {
            log.error("[{}] 작업 중 오류 발생", productId, e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 여러 비동기 작업을 병렬로 수행
     * 
     * @param userIds 사용자 ID 리스트
     * @return CompletableFuture - 모든 작업의 결과
     */
    @Async
    public CompletableFuture<List<String>> parallelAsyncTasks(List<String> userIds) {
        log.info("병렬 비동기 작업 시작: {} 개", userIds.size());
        
        try {
            List<CompletableFuture<String>> futures = new ArrayList<>();
            
            // 각 사용자에 대해 비동기 작업 생성
            for (String userId : userIds) {
                CompletableFuture<String> future = asyncTaskWithResult(userId);
                futures.add(future);
            }
            
            // 모든 작업 완료 대기
            CompletableFuture<Void> allComplete = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            
            // 모든 결과 수집
            return allComplete.thenApply(v -> {
                List<String> results = new ArrayList<>();
                for (CompletableFuture<String> future : futures) {
                    results.add(future.join());
                }
                return results;
            });
        } catch (Exception e) {
            log.error("병렬 작업 중 오류 발생", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 예외 처리를 포함한 비동기 작업
     * 
     * @param paymentId 결제 ID
     * @return CompletableFuture - 비동기 작업 결과
     */
    @Async
    public CompletableFuture<String> asyncTaskWithErrorHandling(String paymentId) {
        log.info("[{}] 예외 처리 포함 비동기 작업 시작", paymentId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // API 호출 (실패 가능)
                String result = DataUtil.callExternalAPI("/api/payments/" + paymentId);
                log.info("[{}] 작업 완료", paymentId);
                return result;
            } catch (Exception e) {
                log.error("[{}] 작업 실패: {}", paymentId, e.getMessage());
                throw new RuntimeException("결제 처리 실패: " + paymentId, e);
            }
        })
        .exceptionally(ex -> {
            log.error("예외 처리: {}", ex.getMessage());
            return "기본값 - 처리 실패";
        });
    }
    
    /**
     * 체이닝된 비동기 작업
     * 
     * @param orderId 주문 ID
     * @return CompletableFuture - 최종 결과
     */
    @Async
    public CompletableFuture<String> chainedAsyncTasks(String orderId) {
        log.info("[{}] 체이닝 비동기 작업 시작", orderId);
        
        return CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Step 1: 주문 조회", orderId);
            return "주문 정보: " + orderId;
        })
        .thenApply(orderInfo -> {
            log.info("[{}] Step 2: 재고 확인", orderId);
            DataUtil.simulateNetworkIO(500);
            return orderInfo + " -> 재고 확인 완료";
        })
        .thenApply(info -> {
            log.info("[{}] Step 3: 결제 처리", orderId);
            DataUtil.simulateNetworkIO(500);
            return info + " -> 결제 완료";
        })
        .thenApply(info -> {
            log.info("[{}] Step 4: 배송 준비", orderId);
            DataUtil.simulateNetworkIO(500);
            return info + " -> 배송 준비 완료";
        })
        .whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[{}] 체이닝 작업 완료", orderId);
            } else {
                log.error("[{}] 체이닝 작업 실패", orderId, ex);
            }
        });
    }
}
