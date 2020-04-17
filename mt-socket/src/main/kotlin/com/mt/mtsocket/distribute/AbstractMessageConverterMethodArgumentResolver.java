package com.mt.mtsocket.distribute;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * A base class for resolving method argument values by reading from the body of
 * a request with { HttpMessageConverter HttpMessageConverters}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractMessageConverterMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Object NO_VALUE = new Object();

    protected final Log logger = LogFactory.getLog(getClass());

    protected final AbstractJackson2NettyMessageConverter messageConverter = new AbstractJackson2NettyMessageConverter(new ObjectMapper());

    /**
     * Create the method argument value of the expected parameter type by reading
     * from the given HttpInputMessage.
     *
     * @param inputMessage the HTTP input message representing the current request
     * @param parameter    the method parameter descriptor
     * @param targetType   the target type, not necessarily the same as the method
     *                     parameter type, e.g. for {@code HttpEntity<String>}.
     * @return the created method argument value
     * @throws IOException if the reading from the request fails
     */
    @Nullable
    protected Object readWithMessageConverters(ServiceRequestInfo inputMessage, MethodParameter parameter,
                                               Type targetType) throws IOException {

        Class<?> contextClass = parameter.getContainingClass();
        Object body = NO_VALUE;
        try {
            if (messageConverter.canRead(targetType, contextClass)) {
                if (!StringUtils.isEmpty(inputMessage.getBody())) {
                    body = messageConverter.read(targetType, contextClass, inputMessage);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IOException("I/O error while reading input message");
        }

        if (body == NO_VALUE) {
            return null;
        }

        Object theBody = body;
        LogFormatUtils.traceDebug(logger, traceOn -> {
            String formatted = LogFormatUtils.formatValue(theBody, !traceOn);
            return "Read \"" + "\" to [" + formatted + "]";
        });

        return body;
    }

    /**
     * Validate the binding target if applicable.
     * <p>The default implementation checks for {@code @javax.validation.Valid},
     * Spring's {@link Validated},
     * and custom annotations whose name starts with "Valid".
     *
     * @param binder    the DataBinder to be used
     * @param parameter the method parameter descriptor
     * @see #isBindExceptionRequired
     * @since 4.1.5
     */
    protected void validateIfApplicable(DataBinder binder, MethodParameter parameter) {
        Annotation[] annotations = parameter.getParameterAnnotations();
        for (Annotation ann : annotations) {
            Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
            if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[]{hints});
                binder.validate(validationHints);
                break;
            }
        }
    }

    /**
     * 是否在验证错误时引发致命的绑定异常。
     *
     * @param binder    the data binder used to perform data binding
     * @param parameter the method parameter descriptor
     * @return {@code true} if the next method argument is not of type {@link Errors}
     * @since 4.1.5
     */
    protected boolean isBindExceptionRequired(DataBinder binder, MethodParameter parameter) {
        int i = parameter.getParameterIndex();
        Class<?>[] paramTypes = parameter.getExecutable().getParameterTypes();
        boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
        return !hasBindingResult;
    }

    /**
     * 如果需要，根据方法参数调整给定的参数。
     *
     * @param arg       the resolved argument
     * @param parameter the method parameter descriptor
     * @return the adapted argument, or the original resolved argument as-is
     * @since 4.3.5
     */
    @Nullable
    protected Object adaptArgumentIfNecessary(@Nullable Object arg, MethodParameter parameter) {
        if (parameter.getParameterType() == Optional.class) {
            if (arg == null || (arg instanceof Collection && ((Collection<?>) arg).isEmpty()) ||
                    (arg instanceof Object[] && ((Object[]) arg).length == 0)) {
                return Optional.empty();
            } else {
                return Optional.of(arg);
            }
        }
        return arg;
    }


}
