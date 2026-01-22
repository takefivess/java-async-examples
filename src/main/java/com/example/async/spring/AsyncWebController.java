package com.example.async.spring;

import com.example.async.util.DataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Spring MVC @Async와 DeferredResult를 이용한 비동기 웹 요청 처리
 * 
 * DeferredResult는 요청 스레드를 블로킹하지 않고 나중에 결과를 설정할 수 있는 기능입니다.
 * @Async와 함께 사용하면 매우 효율적인 비동기 웹 애플리케이션을 만들 수 있습니다.
 */
@Slf4j
@Controller
@RequestMapping("/api/async")
public class AsyncWebController {
    
    /**
     * DeferredResult를 이용한 비동기 데이터 조회
     * 
     * HTTP 요청 스레드는 즉시 반환되고,
     * 별도의 스레드에서 데이터를 처리한 후 결과를 응답합니다.
     * 
     * @param userId 사용자 ID
     * @return DeferredResult - 지연된 결과
     */
    @GetMapping("/user/{userId}")
    @ResponseBody
    public DeferredResult<ResponseEntity<Map<String, Object>>> getUserAsync(@PathVariable String userId) {
        log.info("비동기 사용자 조회 요청: {}", userId);
        
        // DeferredResult 생성 (3초 타임아웃)
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = 
                new DeferredResult<>(3000L);
        
        // 별도의 스레드에서 비동기 처리
        CompletableFuture.runAsync(() -> {
            try {
                log.info("[{}] 사용자 데이터 조회 시작", userId);
                
                // 외부 API 호출 (시뮬레이션)
                String userData = DataUtil.callExternalAPI("/api/users/" + userId);
                
                log.info("[{}] 사용자 데이터 조회 완료", userId);
                
                // 결과 설정
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("userId", userId);
                response.put("data", userData);
                response.put("timestamp", System.currentTimeMillis());
                
                deferredResult.setResult(ResponseEntity.ok(response));
            } catch (Exception e) {
                log.error("[{}] 사용자 조회 중 오류", userId, e);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", e.getMessage());
                
                deferredResult.setErrorResult(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorResponse));
            }
        });
        
        // 타임아웃 처리
        deferredResult.onTimeout(() -> {
            log.warn("[{}] 요청 타임아웃", userId);
            
            Map<String, Object> timeoutResponse = new HashMap<>();
            timeoutResponse.put("status", "timeout");
            timeoutResponse.put("message", "요청 처리 중 타임아웃이 발생했습니다.");
            
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.REQUEST_TIMEOUT)
                    .body(timeoutResponse));
        });
        
        return deferredResult;
    }
    
    /**
     * Callable을 반환하는 비동기 요청 처리
     * 
     * Spring은 Callable을 반환하는 메서드를 자동으로 스레드 풀에서 실행합니다.
     * 
     * @param orderId 주문 ID
     * @return Callable - 비동기 작업
     */
    @GetMapping("/order/{orderId}")
    @ResponseBody
    public java.util.concurrent.Callable<ResponseEntity<Map<String, Object>>> getOrderAsync(
            @PathVariable String orderId) {
        log.info("비동기 주문 조회 요청: {}", orderId);
        
        // Callable을 반환 (Spring이 스레드 풀에서 실행)
        return () -> {
            try {
                log.info("[{}] 주문 데이터 조회", orderId);
                
                // 외부 API 호출
                String orderData = DataUtil.callExternalAPI("/api/orders/" + orderId);
                
                log.info("[{}] 주문 조회 완료", orderId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("orderId", orderId);
                response.put("data", orderData);
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                log.error("[{}] 주문 조회 중 오류", orderId, e);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", e.getMessage());
                
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorResponse);
            }
        };
    }
    
    /**
     * CompletableFuture를 반환하는 비동기 처리
     * 
     * Java 8의 CompletableFuture를 직접 반환할 수도 있습니다.
     * 
     * @param productId 상품 ID
     * @return CompletableFuture - 비동기 작업 결과
     */
    @GetMapping("/product/{productId}")
    @ResponseBody
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getProductAsync(
            @PathVariable String productId) {
        log.info("비동기 상품 조회 요청: {}", productId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[{}] 상품 데이터 조회", productId);
                
                // 외부 API 호출
                String productData = DataUtil.callExternalAPI("/api/products/" + productId);
                
                log.info("[{}] 상품 조회 완료", productId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("productId", productId);
                response.put("data", productData);
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                log.error("[{}] 상품 조회 중 오류", productId, e);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", e.getMessage());
                
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorResponse);
            }
        });
    }
    
    /**
     * WebAsyncTask를 이용한 타임아웃 및 콜백 처리
     * 
     * @param paymentId 결제 ID
     * @return WebAsyncTask - 타임아웃 설정이 가능한 비동기 작업
     */
    @GetMapping("/payment/{paymentId}")
    @ResponseBody
    public org.springframework.web.context.request.async.WebAsyncTask<ResponseEntity<Map<String, Object>>> 
            processPaymentAsync(@PathVariable String paymentId) {
        log.info("비동기 결제 처리 요청: {}", paymentId);
        
        // WebAsyncTask 생성 (5초 타임아웃)
        return new org.springframework.web.context.request.async.WebAsyncTask<>(
                5000,
                java.util.concurrent.Executors.newSingleThreadExecutor(),
                () -> {
                    try {
                        log.info("[{}] 결제 처리 시작", paymentId);
                        
                        // 결제 처리 시뮬레이션
                        String paymentResult = DataUtil.callExternalAPI("/api/payments/" + paymentId);
                        
                        log.info("[{}] 결제 완료", paymentId);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("paymentId", paymentId);
                        response.put("result", paymentResult);
                        response.put("timestamp", System.currentTimeMillis());
                        
                        return ResponseEntity.ok(response);
                    } catch (Exception e) {
                        log.error("[{}] 결제 처리 중 오류", paymentId, e);
                        
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("status", "error");
                        errorResponse.put("message", e.getMessage());
                        
                        return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(errorResponse);
                    }
                }
        );
    }
    
    /**
     * 건강 상태 확인
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
