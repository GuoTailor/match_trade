package com.mt.mtuser.config

import com.mt.mtcommon.exception.BusinessException
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.ResponseInfo.Companion.failed
import org.slf4j.LoggerFactory
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import java.util.*

/**
 * 全局异常处理，处理可预见的异常
 *
 * @author gyh
 */
@RestControllerAdvice
class RestExceptionTranslator {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleError(e: BusinessException): ResponseInfo<Unit> {
        log.error("业务异常:{}", e.message)
        return failed(e.message!!)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleError(ex: WebExchangeBindException): ResponseInfo<Unit> {
        val list = ex.allErrors
        val errorMsg = StringJoiner(",")
        for (objectError in list) {
            var defaultMessage = objectError.defaultMessage
            if (objectError is FieldError) {
                val filed = objectError.field
                defaultMessage = if (defaultMessage.contains("%s")) {
                    String.format(defaultMessage, filed)
                } else {
                    filed + defaultMessage
                }
            }
            errorMsg.add(defaultMessage)
        }
        log.error("表单绑定或校验失败：{} ", errorMsg)
        return failed(errorMsg.toString())
    }
}
