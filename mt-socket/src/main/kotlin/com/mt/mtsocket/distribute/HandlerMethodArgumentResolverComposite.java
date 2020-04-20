package com.mt.mtsocket.distribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过委托给注册的{HandlerMethodArgumentResolver}列表来解析方法参数。以前解析的方法参数被缓存，以便更快地查找。
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public class HandlerMethodArgumentResolverComposite {

    protected final Log logger = LogFactory.getLog(getClass());

    private final List<HandlerMethodArgumentResolver> argumentResolvers = new LinkedList<>();

    private final Map<MethodParameter, HandlerMethodArgumentResolver> argumentResolverCache =
            new ConcurrentHashMap<>(128);


    /**
     * Add the given { HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}.
     * @since 4.3
     */
    public HandlerMethodArgumentResolverComposite addResolvers(@Nullable HandlerMethodArgumentResolver... resolvers) {
        if (resolvers != null) {
            Collections.addAll(this.argumentResolvers, resolvers);
        }
        return this;
    }


    /**
     * Whether the given {@linkplain MethodParameter method parameter} is
     * supported by any registered { HandlerMethodArgumentResolver}.
     */
    public boolean supportsParameter(MethodParameter parameter) {
        return getArgumentResolver(parameter) != null;
    }

    /**
     * Iterate over registered
     * { HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} and
     * invoke the one that supports it.
     * @throws IllegalStateException if no suitable
     * { HandlerMethodArgumentResolver} is found.
     */
    @Nullable
    public Object resolveArgument(MethodParameter parameter, ServiceRequestInfo webRequest) throws Exception {

        HandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
        if (resolver == null) {
            throw new IllegalArgumentException(
                    "Unsupported parameter type [" + parameter.getParameterType().getName() + "]." +
                            " supportsParameter should be called first.");
        }
        return resolver.resolveArgument(parameter, webRequest);
    }

    /**
     * Find a registered { HandlerMethodArgumentResolver} that supports
     * the given method parameter.
     */
    @Nullable
    private HandlerMethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
        HandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
        if (result == null) {
            for (HandlerMethodArgumentResolver methodArgumentResolver : this.argumentResolvers) {
                if (methodArgumentResolver.supportsParameter(parameter)) {
                    result = methodArgumentResolver;
                    this.argumentResolverCache.put(parameter, result);
                    break;
                }
            }
        }
        return result;
    }

}

