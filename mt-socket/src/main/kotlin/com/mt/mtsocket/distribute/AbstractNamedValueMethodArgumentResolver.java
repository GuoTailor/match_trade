package com.mt.mtsocket.distribute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt.mtsocket.distribute.exception.ServletRequestBindingException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.validation.DataBinder;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for resolving method arguments from a named value.
 * Request parameters, request headers, and path variables are examples of named
 * values. Each may have a name, a required flag, and a default value.
 *
 * <p>Subclasses define how to do the following:
 * <ul>
 * <li>Obtain named value information for a method parameter
 * <li>Resolve names into argument values
 * <li>Handle missing argument values when argument values are required
 * <li>Optionally handle a resolved value
 * </ul>
 *
 * <p>A default value string can contain ${...} placeholders and Spring Expression
 * Language #{...} expressions. For this to work a
 * {@link ConfigurableBeanFactory} must be supplied to the class constructor.
 *
 * <p>A {@link DataBinder} is created to apply type conversion to the resolved
 * argument value if it doesn't match the method parameter type.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractNamedValueMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final Map<MethodParameter, NamedValueInfo> namedValueInfoCache = new ConcurrentHashMap<>(256);
    private final AbstractJackson2NettyMessageConverter messageConverter = new AbstractJackson2NettyMessageConverter(new ObjectMapper());

    public AbstractNamedValueMethodArgumentResolver() { }

    @Override
    @Nullable
    public final Object resolveArgument(MethodParameter parameter, ServiceRequestInfo webRequest) throws Exception {

        NamedValueInfo namedValueInfo = getNamedValueInfo(parameter);
        MethodParameter nestedParameter = parameter.nestedIfOptional();

        Object resolvedName = namedValueInfo.name;
        if (resolvedName == null) {
            throw new IllegalArgumentException(
                    "Specified name must not resolve to null: [" + namedValueInfo.name + "]");
        }

        Object arg = resolveName(resolvedName.toString(), nestedParameter, webRequest);
        if (arg == null) {
            if (namedValueInfo.required && !nestedParameter.isOptional()) {
                handleMissingValue(namedValueInfo.name, nestedParameter, webRequest);
            }
            arg = handleNullValue(namedValueInfo.name, arg, nestedParameter.getNestedParameterType());
        }
        DataBinder binder = new DataBinder(null, namedValueInfo.name);
        try {
            arg = binder.convertIfNecessary(arg, parameter.getParameterType(), parameter);
        } catch (ConversionNotSupportedException ex) {
            if (arg instanceof String) {
                arg = messageConverter.objectMapper.readValue((String)arg, parameter.getParameterType());
            } else
            throw new MethodArgumentConversionNotSupportedException(arg, ex.getRequiredType(),
                    namedValueInfo.name, parameter, ex.getCause());
        } catch (TypeMismatchException ex) {
            throw new MethodArgumentTypeMismatchException(arg, ex.getRequiredType(),
                    namedValueInfo.name, parameter, ex.getCause());

        }

        handleResolvedValue(arg, namedValueInfo.name, parameter, webRequest);

        return arg;
    }

    /**
     * 获取给定方法参数的命名值。
     */
    private NamedValueInfo getNamedValueInfo(MethodParameter parameter) {
        NamedValueInfo namedValueInfo = this.namedValueInfoCache.get(parameter);
        if (namedValueInfo == null) {
            namedValueInfo = createNamedValueInfo(parameter);
            namedValueInfo = updateNamedValueInfo(parameter, namedValueInfo);
            this.namedValueInfoCache.put(parameter, namedValueInfo);
        }
        return namedValueInfo;
    }

    /**
     * Create the {@link NamedValueInfo} object for the given method parameter. Implementations typically
     * retrieve the method annotation by means of {@link MethodParameter#getParameterAnnotation(Class)}.
     *
     * @param parameter the method parameter
     * @return the named value information
     */
    protected abstract NamedValueInfo createNamedValueInfo(MethodParameter parameter);

    /**
     * Create a new NamedValueInfo based on the given NamedValueInfo with sanitized values.
     */
    private NamedValueInfo updateNamedValueInfo(MethodParameter parameter, NamedValueInfo info) {
        String name = info.name;
        if (info.name.isEmpty()) {
            name = parameter.getParameterName();
            if (name == null) {
                throw new IllegalArgumentException(
                        "Name for argument type [" + parameter.getNestedParameterType().getName() +
                                "] not available, and parameter name information not found in class file either.");
            }
        }
        String defaultValue = ("\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n".equals(info.defaultValue) ? null : info.defaultValue);
        return new NamedValueInfo(name, info.required, defaultValue);
    }

    /**
     * Resolve the given parameter type and value name into an argument value.
     *
     * @param name      the name of the value being resolved
     * @param parameter the method parameter to resolve to an argument value
     *                  (pre-nested in case of a {@link java.util.Optional} declaration)
     * @param request   the current request
     * @return the resolved argument (may be {@code null})
     * @throws Exception in case of errors
     */
    @Nullable
    protected abstract Object resolveName(String name, MethodParameter parameter, ServiceRequestInfo request)
            throws Exception;

    /**
     * Invoked when a named value is required, but {@link #resolveName(String, MethodParameter, ServiceRequestInfo)}
     * returned {@code null} and there is no default value. Subclasses typically throw an exception in this case.
     *
     * @param name      the name for the value
     * @param parameter the method parameter
     * @param request   the current request
     * @since 4.3
     */
    protected void handleMissingValue(String name, MethodParameter parameter, ServiceRequestInfo request)
            throws Exception {

        handleMissingValue(name, parameter);
    }

    /**
     * Invoked when a named value is required, but {@link #resolveName(String, MethodParameter, ServiceRequestInfo)}
     * returned {@code null} and there is no default value. Subclasses typically throw an exception in this case.
     *
     * @param name      the name for the value
     * @param parameter the method parameter
     */
    protected void handleMissingValue(String name, MethodParameter parameter) throws ServletRequestBindingException {
        throw new ServletRequestBindingException("Missing argument '" + name +
                "' for method parameter of type " + parameter.getNestedParameterType().getSimpleName());
    }

    /**
     * A {@code null} results in a {@code false} value for {@code boolean}s or an exception for other primitives.
     */
    @Nullable
    private Object handleNullValue(String name, @Nullable Object value, Class<?> paramType) {
        if (value == null) {
            if (Boolean.TYPE.equals(paramType)) {
                return Boolean.FALSE;
            } else if (paramType.isPrimitive()) {
                throw new IllegalStateException("Optional " + paramType.getSimpleName() + " parameter '" + name +
                        "' is present but cannot be translated into a null value due to being declared as a " +
                        "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.");
            }
        }
        return value;
    }

    /**
     * 解析值后调用。
     *
     * @param arg        已解析参数值
     * @param name       参数名称
     * @param parameter  参数类型
     * @param webRequest 当前请求
     */
    protected void handleResolvedValue(@Nullable Object arg, String name, MethodParameter parameter, ServiceRequestInfo webRequest) {
    }


    /**
     * 表示关于已命名值的信息，包括名称、是否需要以及默认值。
     */
    protected static class NamedValueInfo {

        private final String name;

        private final boolean required;

        @Nullable
        private final String defaultValue;

        public NamedValueInfo(String name, boolean required, @Nullable String defaultValue) {
            this.name = name;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }

}

