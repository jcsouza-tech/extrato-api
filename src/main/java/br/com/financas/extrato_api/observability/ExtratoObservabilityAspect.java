package br.com.financas.extrato_api.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Aspect para rastreamento automático de métricas
 */
@Aspect
@Component
public class ExtratoObservabilityAspect {
    
    private final MeterRegistry meterRegistry;
    
    public ExtratoObservabilityAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @Around("execution(* br.com.financas.extrato_api.service.*Service.processarArquivo(..))")
    public Object rastrearProcessamentoArquivo(ProceedingJoinPoint joinPoint) throws Throwable {
        String serviceName = joinPoint.getTarget().getClass().getSimpleName();
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object result = joinPoint.proceed();
            
            // Registra métricas de sucesso
            Counter.builder("extrato.processamento.sucesso")
                    .tag("service", serviceName)
                    .register(meterRegistry)
                    .increment();
                    
            return result;
        } catch (Exception e) {
            // Registra métricas de erro
            Counter.builder("extrato.processamento.erro")
                    .tag("service", serviceName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("extrato.processamento.tempo")
                    .tag("service", serviceName)
                    .register(meterRegistry));
        }
    }
}
