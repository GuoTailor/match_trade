package com.mt.mtsocket.common

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import java.beans.IntrospectionException
import java.beans.Introspector
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Util {
    private val logger = LoggerFactory.getLogger(Util::class.java)
    private val random = Random()

    /**
     * 将json格式化为map
     *
     * @param json
     * @return
     */
    fun getParameterMap(json: String?): Map<String, Any> {
        var map: Map<String, Any> = HashMap()
        if (!StringUtils.isEmpty(json)) {
            try {
                map = jacksonObjectMapper().readValue(json!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return map
    }

    fun createDate(time: Long): String {
        return createDate("yyyy-MM-dd HH:mm:ss", time)
    }

    /**
     * 按当前时间，按`yyyy-MM-dd HH:mm:ss`格式格式化一个时间字符串
     *
     * @return 格式化后的时间字符串
     */
    @JvmOverloads
    fun createDate(pattern: String? = "yyyy-MM-dd HH:mm:ss", time: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat(pattern).format(time)
    }

    /**
     * 把格式化后的时间字符串解码成时间毫秒值
     *
     * @param time 格式化后的时间字符串
     * @return 时间毫秒值
     */
    fun encoderDate(time: String?): Long? {
        try {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 判断一个（bean）实体类对象是否为空
     *
     *
     * 判断为空的标准为：<P>
     *  1. 如果实体类的属性为[String]那么字符串长度为0或为null就认为为空
     *  1. 如果属性为[Collection]的子类那么集合的长度为0或为null就认为为空
     *  1. 如果属性不为上述的就为null才认为为空
     *
     *
     * @param obj 一个实体类（bean）对象
     * @return true：如果该实体类的所有属性都为空，false：其中的任意一个属性不为空
     */
    fun isEmpty(obj: Any): Boolean {
        return isEmpty(obj, true)
    }

    /**
     * 判断一个（bean）实体类对象是否为空
     *
     *
     * 非严格模式下判断为空的标准为：
     * 对象的属性是否为null<P>
     * 严格模式下判断为空的标准为：<P>
     *  1. 如果实体类的属性为[String]那么字符串长度为0或为null就认为为空
     *  1. 如果属性为[Collection]的子类那么集合的长度为0或为null就认为为空
     *  1. 如果属性不为上述的就为null才认为为空
     *
     *
     * @param obj    一个实体类（bean）对象
     * @param strict 是否使用严格模式
     * @return true：如果该实体类的所有属性都为空，false：其中的任意一个属性不为空
     */
    fun isEmpty(obj: Any, strict: Boolean): Boolean {
        try {
            val beanInfo = Introspector.getBeanInfo(obj.javaClass)
            val proDescriptors = beanInfo?.propertyDescriptors
            if (proDescriptors != null && proDescriptors.isNotEmpty()) {
                for (propDesc in proDescriptors) {
                    val o = propDesc.readMethod.invoke(obj)
                    if (o == null || o == obj.javaClass) {
                        continue
                    }
                    if (!strict) {
                        return false
                    }
                    if (o is String) {
                        return if (o.isNotEmpty()) {
                            false
                        } else {
                            continue
                        }
                    }
                    return if (o is Collection<*>) {
                        if (!o.isEmpty()) {
                            false
                        } else {
                            continue
                        }
                    } else false
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            logger.error(e.message)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            logger.error(e.message)
        } catch (e: IntrospectionException) {
            e.printStackTrace()
            logger.error(e.message)
        }
        return true
    }

    /**
     * 随机生成指定长度的数字验证码<br></br>
     * 考虑使用[RandomStringUtils.randomNumeric]
     * @param length 数字验证码长度
     * @return 数字验证码
     */
    fun getRandomInt(length: Int): String {
        var max = 1
        for (i in 0 until length) {
            max *= 10
        }
        val nextInt = StringBuilder(random.nextInt(max).toString())
        while (nextInt.length < length) {
            nextInt.insert(0, '0')
        }
        return nextInt.toString()
    }

    /**
     * 构建一个把当前`roomNumber`加一的房间号<br></br>
     * 注意可能把当前房间号加一后出现进位，如99加一后为100，长度从两位变成了三位，这是返回一个全0的字符串
     * @param roomNumber 当前的房间号
     * @return 一个新的房间号
     */
    fun createNewNumber(roomNumber: String?): String {
        if (roomNumber == null) {
            return "0000"
        }
        val matcher = Pattern.compile("[1-9][\\d]*").matcher(roomNumber)
        return if (matcher.find()) {
            val start = matcher.start()
            var length = matcher.end() - start
            val ss = roomNumber.substring(0, start)
            val num = matcher.group().toInt() + 1
            var es = Integer.toString(num)
            if (es.length > length) {
                val temp = StringBuilder()
                while (length-- > 0) {
                    temp.append("0")
                }
                es = temp.toString()
            }
            ss + es
        } else {
            throw IllegalStateException("不支持的房间号$roomNumber")
        }
    }

    fun getQueryMap(queryStr: String): Map<String, String> {
        val queryMap: MutableMap<String, String> = HashMap()
        if (!StringUtils.isEmpty(queryStr)) {
            val queryParam = queryStr.split("&")
            queryParam.forEach { s: String ->
                val kv = s.split("=".toRegex(), 2)
                val value = if (kv.size == 2) kv[1] else ""
                queryMap[kv[0]] = URLDecoder.decode(value, "utf-8")
            }
        }
        return queryMap
    }
}