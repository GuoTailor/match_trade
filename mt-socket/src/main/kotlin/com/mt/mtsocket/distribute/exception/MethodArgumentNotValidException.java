package com.mt.mtsocket.distribute.exception;

import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Exception to be thrown when validation on an argument annotated with {@code @Valid} fails.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
@SuppressWarnings("serial")
public class MethodArgumentNotValidException extends Exception {

    private final MethodParameter parameter;

    private final BindingResult bindingResult;


    /**
     * Constructor for {@link MethodArgumentNotValidException}.
     * @param parameter the parameter that failed validation
     * @param bindingResult the results of the validation
     */
    public MethodArgumentNotValidException(MethodParameter parameter, BindingResult bindingResult) {
        this.parameter = parameter;
        this.bindingResult = bindingResult;
    }

    /**
     * Return the method parameter that failed validation.
     */
    public MethodParameter getParameter() {
        return this.parameter;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Validation failed for argument [")
                .append(this.parameter.getParameterIndex()).append("] in ")
                .append(this.parameter.getExecutable().toGenericString());
        if (this.bindingResult.getErrorCount() > 1) {
            sb.append(" with ").append(this.bindingResult.getErrorCount()).append(" errors");
        }
        sb.append(": ");
        for (ObjectError error : this.bindingResult.getAllErrors()) {
            sb.append("[").append(error).append("] ");
        }
        return sb.toString();
    }

}

