package com.example.async.util;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 유틸리티 클래스 - 비동기 예제에서 사용하는 공통 함수들
 */
public class DataUtil {
    
    private static final Random random = new Random();
    
    /**
     * 네트워크 I/O를 시뮬레이션하는 지연 처리
     * 
     * @param delayMs 지연 시간 (밀리초)
     * @return 처리 결과
     */
    public static String simulateNetworkIO(long delayMs) {
        try {
            Thread.sleep(delayMs);
            return "네트워크 요청 완료 - " + Instant.now();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "중단됨";
        }
    }
    
    /**
     * 데이터베이스 조회를 시뮬레이션
     * 
     * @param id 조회 ID
     * @return 조회 결과
     */
    public static String fetchFromDatabase(String id) {
        try {
            // 100~500ms의 임의 지연
            long delay = 100 + random.nextInt(400);
            Thread.sleep(delay);
            return "DB 결과: " + id + " (" + delay + "ms)";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "DB 조회 중단";
        }
    }
    
    /**
     * CPU 바운드 작업 시뮬레이션
     * 
     * @param iterations 반복 횟수
     * @return 계산 결과
     */
    public static long performCalculation(int iterations) {
        long result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sqrt(i * 123.456);
        }
        return result;
    }
    
    /**
     * 외부 API 호출 시뮬레이션
     * 
     * @param endpoint API 엔드포인트
     * @return 응답 데이터
     */
    public static String callExternalAPI(String endpoint) {
        try {
            // 200~800ms의 임의 지연 (네트워크 불안정성 시뮬레이션)
            long delay = 200 + random.nextInt(600);
            Thread.sleep(delay);
            
            // 10% 확률로 실패
            if (random.nextInt(10) == 0) {
                throw new RuntimeException("API 호출 실패: " + endpoint);
            }
            
            return "API 응답: " + endpoint + " (" + delay + "ms)";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "API 호출 중단";
        }
    }
    
    /**
     * 현재 스레드 정보 출력
     * 
     * @return 스레드 정보 문자열
     */
    public static String getCurrentThreadInfo() {
        Thread current = Thread.currentThread();
        String threadType = current.isVirtual() ? "[Virtual]" : "[Platform]";
        return threadType + " " + current.getName() + " (ID: " + current.threadId() + ")";
    }
    
    /**
     * 시간 측정 (단순 래퍼)
     * 
     * @param task 실행할 작업
     * @return 실행 시간 (나노초)
     */
    public static long measureTime(Runnable task) {
        long startNano = System.nanoTime();
        task.run();
        return System.nanoTime() - startNano;
    }
    
    /**
     * 밀리초를 시간 형식으로 변환
     * 
     * @param millis 밀리초
     * @return 시간 형식 문자열
     */
    public static String formatTime(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        long seconds = millis / 1000;
        long remainingMillis = millis % 1000;
        return seconds + "." + String.format("%03d", remainingMillis) + "s";
    }
}
