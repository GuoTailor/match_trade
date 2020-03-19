package com.mt.mtgateway

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.function.BiConsumer

/**
 * Created by gyh on 2018/10/25.
 * 统一返回
 */
class RespBody<T> {
    /**
     * 成功：0； 失败：1
     */
    var code: Int
    @JsonIgnore
    var function: BiConsumer<Int, T?>? = null
    var msg: String
        get() {
            function?.accept(code, data)
            return field
        }
    var data: T? = null

    constructor(code: Int, msg: String) {
        this.code = code
        this.msg = msg
    }

    constructor(code: Int, msg: String, data: T) {
        this.code = code
        this.data = data
        this.msg = msg
    }

    /**
     * 后续操作
     * 注意，注册的方法不会立即被执行。只有在用 json 序列化时或调用[.getMsg] 方法才会被调用
     * @param function 要做的操作
     */
    fun processing(function: BiConsumer<Int, T?>?): RespBody<T> {
        this.function = function
        return this
    }

    fun setCode(code: Int): RespBody<T> {
        this.code = code
        return this
    }

    fun setMsg(msg: String): RespBody<T> {
        this.msg = msg
        return this
    }

    fun setData(data: T): RespBody<T> {
        this.data = data
        return this
    }

    class Build(private val code: Int, private val msg: String) {
        fun <T> setData(data: T): RespBody<T> {
            return RespBody(code, msg, data)
        }

        fun build(): RespBody<Void> {
            return RespBody(code, msg)
        }

    }

    companion object {
        const val CODE_SUCCESS = 0
        const val CODE_ERROR = 1
        fun SUCCESS(): Build {
            return Build(CODE_SUCCESS, "成功")
        }

        fun SUCCESS(msg: String): Build {
            return Build(CODE_SUCCESS, msg)
        }

        fun ERROR(): Build {
            return Build(CODE_ERROR, "失败")
        }

        fun ERROR(msg: String): Build {
            return Build(CODE_ERROR, msg)
        }
    }
}