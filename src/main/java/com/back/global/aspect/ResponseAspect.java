package com.back.global.aspect;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

// 컨트롤러가 RsData를 반환하면, 그 resultCode에 맞춰 실제 HTTP 상태코드를 바꿔 주는 후처리기예요
@Aspect
@Component
@RequiredArgsConstructor
public class ResponseAspect {
    // 💡 싱글톤 빈인데도 요청마다 "현재 요청의 response"를 가리키는 프록시가 주입돼요
    private final HttpServletResponse response;

    @Around("""
            execution(public com.back.global.rsData.RsData *(..)) &&
            within(@org.springframework.web.bind.annotation.RestController *)
            """)
    public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed(); // 원래 컨트롤러 메서드 실행

        RsData<?> rsData = (RsData<?>) proceed;
        response.setStatus(rsData.statusCode());

        return proceed;
    }
}
