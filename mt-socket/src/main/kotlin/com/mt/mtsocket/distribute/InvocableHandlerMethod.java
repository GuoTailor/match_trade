package com.mt.mtsocket.distribute;

import com.mt.mtsocket.distribute.ServiceRequestInfo;import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * {@link HandlerMethod}的扩展，它调用具有参数值的底层方法，
 * 这些参数值通过{@link HandlerMethodArgumentResolver}列表从当前NETTY请求解析。
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public class InvocableHandlerMethod extends HandlerMethod {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * Create an instance from a {@code HandlerMethod}.
     */
    public InvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }


    /**
     * Set { HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} to use to use for resolving method argument values.
     */
    public void setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.resolvers = argumentResolvers;
    }

    /**
     * Set the ParameterNameDiscoverer for resolving parameter names when needed
     * (e.g. default request attribute name).
     * <p>Default is a {@link DefaultParameterNameDiscoverer}.
     */
    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    /**
     * 在给定请求的上下文中解析方法的参数值后调用该方法。
     * <p>参数值通常通过
     * { HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}.
     * 但是{@code providedArgs}参数可以提供直接使用的参数值，即没有参数解析。
     * 提供的参数值示例包括{WebDataBinder}、{SessionStatus}或抛出异常实例。
     * 在参数解析器之前检查提供的参数值。<p>
     * 委托{@link #getMethodArgumentValues}并使用已解析的参数调用{@link #doInvoke}。
     *
     * @param request the current request
     * @param providedArgs "given" arguments matched by type, not resolved
     * @return the raw value returned by the invoked method
     * @throws Exception raised if no suitable argument resolver can be found,
     * or if the method raised an exception
     * @see #getMethodArgumentValues
     * @see #doInvoke
     */
    @Nullable
    public Object invokeForRequest(ServiceRequestInfo request, Object... providedArgs) throws Exception {
        Object[] args = getMethodArgumentValues(request, providedArgs);
        if (logger.isTraceEnabled()) {
            logger.trace("Arguments: " + Arrays.toString(args));
        }
        return doInvoke(args);
    }

    /**
     * 获取当前请求的方法参数值，检查提供的参数值并退回到配置的参数解析器。
     * <p>得到的数组将被传递到 {@link #doInvoke}.
     * @since 5.1.2
     */
    protected Object[] getMethodArgumentValues(ServiceRequestInfo request, Object... providedArgs) throws Exception {

        if (ObjectUtils.isEmpty(getMethodParameters())) {
            return EMPTY_ARGS;
        }
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args[i] = findProvidedArgument(parameter, providedArgs);
            if (args[i] != null) {
                continue;
            }
            if (!this.resolvers.supportsParameter(parameter)) {
                throw new IllegalStateException(formatArgumentError(parameter, "没有合适的解析器"));
            }
            try {
                args[i] = this.resolvers.resolveArgument(parameter, request);
            }
            catch (Exception ex) {
                // Leave stack trace for later, exception may actually be resolved and handled..
                if (logger.isDebugEnabled()) {
                    String error = ex.getMessage();
                    if (error != null && !error.contains(parameter.getExecutable().toGenericString())) {
                        logger.debug(formatArgumentError(parameter, error));
                    }
                }
                throw ex;
            }
        }
        return args;
    }

    /**
     * 使用给定的参数值调用处理程序方法。
     */
    @Nullable
    protected Object doInvoke(Object... args) throws Exception {
        ReflectionUtils.makeAccessible(getBridgedMethod());
        try {
            return getBridgedMethod().invoke(getBean(), args);
        }
        catch (IllegalArgumentException ex) {
            assertTargetBean(getBridgedMethod(), getBean(), args);
            String text = (ex.getMessage() != null ? ex.getMessage() : "Illegal argument");
            throw new IllegalStateException(formatInvokeError(text, args), ex);
        }
        catch (InvocationTargetException ex) {
            // Unwrap for HandlerExceptionResolvers ...
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            else if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            else if (targetException instanceof Exception) {
                throw (Exception) targetException;
            }
            else {
                throw new IllegalStateException(formatInvokeError("Invocation failure", args), targetException);
            }
        }
    }

}

