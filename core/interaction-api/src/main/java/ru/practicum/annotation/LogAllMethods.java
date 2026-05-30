package ru.practicum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматического логирования вызовов всех методов помеченного класса.
 * <p>
 * Применяется на уровне класса для включения аспектно-ориентированного логирования
 * выполнения всех его публичных методов. Обычно используется в сочетании с Spring AOP
 * для добавления сквозной функциональности логирования без модификации бизнес-кода.
 * <p>
 * <b>Типичное применение:</b>
 * <ul>
 *   <li>Логирование входа/выхода из методов</li>
 *   <li>Замер времени выполнения методов</li>
 *   <li>Логирование параметров и возвращаемых значений</li>
 *   <li>Отладка и мониторинг работы сервисов</li>
 * </ul>
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * @Service
 * @LogAllMethods
 * public class UserService {
 *     public User createUser(UserDto userDto) {
 *         // создание пользователя
 *     }
 *
 *     public User getUserById(Long id) {
 *         // получение пользователя
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Требования для работы:</b>
 * <ul>
 *   <li>Наличие настроенного аспекта, обрабатывающего данную аннотацию</li>
 *   <li>Включенная поддержка AOP в конфигурации Spring</li>
 *   <li>Соответствующие зависимости для аспектного программирования</li>
 * </ul>
 *
 * @see org.aspectj.lang.annotation.Aspect
 * @see org.springframework.context.annotation.EnableAspectJAutoProxy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAllMethods {
    // Аннотация-маркер, не содержит дополнительных параметров
}