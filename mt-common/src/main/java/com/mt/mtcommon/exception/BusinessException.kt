package com.mt.mtcommon.exception

/**
 * @author zqj
 */
class BusinessException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(e: Throwable) : super(e)
}
