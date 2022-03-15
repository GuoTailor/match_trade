package com.mt.mtsocket.distribute;

import com.mt.mtsocket.distribute.ServiceRequestInfo;import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.beans.PropertyEditor;
import java.util.Map;

/**
 * Resolves method arguments annotated with @{@link RequestParam}, arguments of
 * type { MultipartFile} in conjunction with Spring's { MultipartResolver}
 * abstraction, and arguments of type {@code javax.servlet.http.Part} in conjunction
 * with Servlet 3.0 multipart requests. This resolver can also be created in default
 * resolution mode in which simple types (int, long, etc.) not annotated with
 * {@link RequestParam @RequestParam} are also treated as request parameters with
 * the parameter name derived from the argument name.
 *
 * <p>If the method parameter type is {@link Map}, the name specified in the
 * annotation is used to resolve the request parameter String value. The value is
 * then converted to a {@link Map} via type conversion assuming a suitable
 * {@link Converter} or {@link PropertyEditor} has been registered.
 * Or if a request parameter name is not specified the
 * { RequestParamMapMethodArgumentResolver} is used instead to provide
 * access to all request parameters in the form of a map.
 *
 * <p>A { WebDataBinder} is invoked to apply type conversion to resolved request
 * header values that don't yet match the method parameter type.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @since 3.1
 */
public class RequestParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    /**
     * Create a new {@link RequestParamMethodArgumentResolver} instance.
     */
    public RequestParamMethodArgumentResolver() { }

    /**
     * 支持以下:
     * <ul>
     * <li>@RequestParam-annotated 方法参数。
     * 这排除了注释没有指定名称的{@link Map}参数。有关此类参数，请参见{ RequestParamMapMethodArgumentResolver} instead for such params.
     * <li>Arguments of type { MultipartFile} unless annotated with @{ RequestPart}.
     * <li>Arguments of type {@code Part} unless annotated with @{ RequestPart}.
     * <li>In default resolution mode, simple type arguments even if not with @{@link RequestParam}.
     * </ul>
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(RequestParam.class)) {
            if (Map.class.isAssignableFrom(parameter.nestedIfOptional().getNestedParameterType())) {
                RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
                return (requestParam != null && StringUtils.hasText(requestParam.name()));
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
        return (ann != null ? new RequestParamNamedValueInfo(ann) : new RequestParamNamedValueInfo());
    }

    @Override
    @Nullable
    protected Object resolveName(String name, MethodParameter parameter, ServiceRequestInfo request) throws Exception {
        Object arg = null;
        Object[] paramValues = request.getParameterValues(name);
        if (paramValues != null) {
            arg = (paramValues.length == 1 ? paramValues[0] : paramValues);
        }
        return arg;
    }

    private static class RequestParamNamedValueInfo extends NamedValueInfo {

        public RequestParamNamedValueInfo() {
            super("", false, "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n");
        }

        public RequestParamNamedValueInfo(RequestParam annotation) {
            super(annotation.name(), annotation.required(), annotation.defaultValue());
        }
    }

}

