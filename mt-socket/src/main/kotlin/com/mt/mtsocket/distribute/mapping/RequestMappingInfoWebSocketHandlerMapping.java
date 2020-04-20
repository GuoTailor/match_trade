package com.mt.mtsocket.distribute.mapping;

import com.mt.mtsocket.distribute.ServiceRequestInfo;
import org.springframework.web.method.HandlerMethod;

import java.util.Comparator;
import java.util.Set;

/**
 * {@link RequestMappingInfo}定义请求和处理程序方法之间映射的类的抽象基类。
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public abstract class RequestMappingInfoWebSocketHandlerMapping extends AbstractWebSocketHandlerMethodMapping<RequestMappingInfo> {

    protected RequestMappingInfoWebSocketHandlerMapping() {
        setHandlerMethodMappingNamingStrategy((HandlerMethod handlerMethod, RequestMappingInfo mapping) -> {
            if (mapping.getName() != null) {
                return mapping.getName();
            }
            StringBuilder sb = new StringBuilder();
            String simpleTypeName = handlerMethod.getBeanType().getSimpleName();
            for (int i = 0; i < simpleTypeName.length(); i++) {
                if (Character.isUpperCase(simpleTypeName.charAt(i))) {
                    sb.append(simpleTypeName.charAt(i));
                }
            }
            sb.append("#").append(handlerMethod.getMethod().getName());
            return sb.toString();
        });
    }


    /**
     * Get the URL path patterns associated with this {@link RequestMappingInfo}.
     */
    @Override
    protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
        return info.getPatternsCondition().getPatterns();
    }

    /**
     * Check if the given RequestMappingInfo matches the current request and
     * return a (potentially new) instance with conditions that match the
     * current request -- for example with a subset of URL patterns.
     *
     * @return an info in case of a match; or {@code null} otherwise.
     */
    @Override
    protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, ServiceRequestInfo request) {
        return info.getMatchingCondition(request);
    }

    /**
     * Provide a Comparator to sort RequestMappingInfos matched to a request.
     */
    @Override
    protected Comparator<RequestMappingInfo> getMappingComparator(final ServiceRequestInfo request) {
        return (info1, info2) -> info1.compareTo(info2, request);
    }

    /**
     * 在请求中公开URI模板变量、矩阵变量和可生成的媒体类型。
     */
    @Override
    protected void handleMatch(RequestMappingInfo info, String lookupPath, ServiceRequestInfo request) {
        super.handleMatch(info, lookupPath, request);

        //request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern);

        //request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, decodedUriVariables);

    }

}

