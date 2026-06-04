package ru.practicum.annotation;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Аспект для логирования выполнения всех методов классов, помеченных аннотацией {@link LogAllMethods}.
 * <p>
 * Этот аспект обеспечивает сквозное логирование, включая:
 * <ul>
 *   <li>Запись входа в метод с аргументами</li>
 *   <li>Измерение времени выполнения</li>
 *   <li>Логирование результата или исключения</li>
 *   <li>Запись времени выполнения метода</li>
 * </ul>
 * <p>
 * <b>Формат логируемых сообщений:</b>
 * <pre>
 * [ClassName.methodName] Starting execution
 * Arguments: [arg1, arg2, ...]
 * Exiting method [ClassName.methodName] with result {result}
 * Execution time: {time} ms
 * Exception in method [ClassName.methodName] after {time} ms
 * </pre>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * // Конфигурация Spring
 * @Configuration
 * @EnableAspectJAutoProxy
 * public class AopConfig {
 *     @Bean
 *     public LogAllMethodsAspect logAllMethodsAspect() {
 *         return new LogAllMethodsAspect();
 *     }
 * }
 *
 * // Помеченный класс будет логироваться
 * @Service
 * @LogAllMethods
 * public class UserService {
 *     public User getUser(Long id) { ... }
 * }
 * }</pre>
 * <p>
 * <b>Особенности реализации:</b>
 * <ul>
 *   <li>Использует advice типа {@code @Around} для полного контроля над выполнением</li>
 *   <li>Логирует как успешные выполнения, так и исключения</li>
 *   <li>Измеряет время выполнения с точностью до миллисекунд</li>
 *   <li>Извлекает имя класса и метода через reflection</li>
 * </ul>
 *
 * @see LogAllMethods аннотация, которую обрабатывает этот аспект
 * @see Around advice, используемый для перехвата вызовов
 */
@Slf4j
@Aspect
public class LogAllMethodsAspect {

    /**
     * Advice, перехватывающий выполнение методов в классах с аннотацией {@link LogAllMethods}.
     * <p>
     * Выполняет следующие действия для каждого перехваченного метода:
     * <ol>
     *   <li>Извлекает имя класса и метода</li>
     *   <li>Замеряет время начала выполнения</li>
     *   <li>Логирует вход в метод с аргументами</li>
     *   <li>Выполняет оригинальный метод</li>
     *   <li>При успехе - логирует результат и время выполнения</li>
     *   <li>При исключении - логирует ошибку и время до сбоя</li>
     * </ol>
     *
     * @param joinPoint точка соединения, содержащая информацию о вызываемом методе
     * @return результат выполнения оригинального метода
     * @throws Throwable если оригинальный метод выбрасывает исключение
     */
    @Around("@within(ru.practicum.annotation.LogAllMethods)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();

        Object[] args = joinPoint.getArgs();

        log.info("[{}] Starting execution", fullMethodName);
        log.info("Arguments: {}", args);

        try {
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("Exiting method [{}] with result {}", fullMethodName, result);
            log.info("Execution time: {} ms", executionTime);

            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.error("Exception in method [{}] after {} ms", fullMethodName, executionTime, throwable);
            throw throwable;
        }
    }
}