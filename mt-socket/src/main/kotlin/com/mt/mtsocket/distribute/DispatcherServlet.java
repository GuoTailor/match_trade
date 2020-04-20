package com.mt.mtsocket.distribute;

import com.mt.mtsocket.distribute.mapping.AbstractWebSocketHandlerMethodMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by GYH on 2018/12/6.
 */
@Component
public class DispatcherServlet {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HandlerMethodArgumentResolverComposite argumentResolvers = new HandlerMethodArgumentResolverComposite();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 此servlet使用的处理程序映射的列表。
     */
    @Nullable
    private List<AbstractWebSocketHandlerMethodMapping> handlerMappings;

    /**
     * 初始化该类使用的HandlerMappings。
     * <p>如果在BeanFactory中没有为该名称空间定义HandlerMapping bean，
     * 我们默认为BeanNameUrlHandlerMapping。
     */
    public void initHandlerMappings(ApplicationContext context) {
        Map<String, AbstractWebSocketHandlerMethodMapping> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, AbstractWebSocketHandlerMethodMapping.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerMappings = new ArrayList<>(matchingBeans.values());
            // 我们保持处理器映射的排序。
            AnnotationAwareOrderComparator.sort(this.handlerMappings);
        }
        //handlerInterceptor = BeanFactoryUtils.beanOfTypeIncludingAncestors(context, HandlerInterceptor.class, true, false);
        argumentResolvers.addResolvers(new RequestResponseBodyMethodProcessor());
        argumentResolvers.addResolvers(new RequestParamMethodArgumentResolver());
    }

    public void doDispatch(ServiceRequestInfo request, ServiceResponseInfo response) {
        InvocableHandlerMethod handlerMethod = getHandler(request);
        if (handlerMethod == null) {
            response.setData(Mono.error(new IllegalStateException("没有该order:'" + request.getOrder() + "'")));
        } else {
            invokeHandlerMethod(request, response, handlerMethod);
        }
    }

    /**
     * 返回此请求的HandlerMethod.
     * <p>按顺序尝试所有处理程序映射。
     *
     * @param request 当前NETTY请求
     * @return the HandlerMethod, 如果找不到处理程序，则为{@code null}
     */
    @Nullable
    private InvocableHandlerMethod getHandler(ServiceRequestInfo request) {
        if (this.handlerMappings != null) {
            for (AbstractWebSocketHandlerMethodMapping mapping : this.handlerMappings) {
                HandlerMethod handler = mapping.getHandlerInternal(request);
                if (handler != null) {
                    return new InvocableHandlerMethod(handler);
                }
            }
        }
        return null;
    }

    /**
     * 调用 {@link InvocableHandlerMethod} 处理程序方法，在需要视图解析时准备{@link Object}。
     *
     * @since 4.2
     */
    private void invokeHandlerMethod(ServiceRequestInfo request, ServiceResponseInfo response, InvocableHandlerMethod invocableMethod) {
        invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        try {
            Object publisher = invocableMethod.invokeForRequest(request);
            if (publisher instanceof Mono) {
                response.setData((Mono<?>) publisher);
            } else if (publisher == null) {
                response.setData(Mono.empty());
            } else {
                response.setData(Mono.just(publisher));
            }
        } catch (Exception e) {
            logger.error("内部服务器出错", e);
            e.printStackTrace();
            response.setData(Mono.just(e.getMessage()));
        }
    }

}
