package com.mt.mtsocket.distribute;

import com.mt.mtsocket.distribute.exception.MethodArgumentNotValidException;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Resolves method arguments annotated with {@code @RequestBody} and handles return
 * values from methods annotated with {@code @ResponseBody} by reading and writing
 * to the body of the request or response with an { HttpMessageConverter}.
 * 解析用@RequestBody标注的方法参数，并通过使用{HttpMessageConverter}读写请求或响应的主体，处理用@ResponseBody标注的方法的返回值。
 * <p>An {@code @RequestBody} method argument is also validated if it is annotated
 * with {@code @javax.validation.Valid}. In case of validation failure,
 * { MethodArgumentNotValidException} is raised and results in an HTTP 400
 * response status code if { DefaultHandlerExceptionResolver} is configured.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public class RequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    /**
     * Throws MethodArgumentNotValidException if validation fails.
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ServiceRequestInfo webRequest) throws Exception {

        parameter = parameter.nestedIfOptional();
        Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
        String name = Conventions.getVariableNameForParameter(parameter);
        if (arg != null) {
            DataBinder binder = new DataBinder(arg, name);
            validateIfApplicable(binder, parameter);
            if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
            }
        }

        return adaptArgumentIfNecessary(arg, parameter);
    }

}

