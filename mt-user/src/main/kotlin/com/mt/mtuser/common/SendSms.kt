package com.mt.mtuser.common

import com.aliyuncs.CommonRequest
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.IAcsClient
import com.aliyuncs.exceptions.ClientException
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader

/**
 * Created by gyh on 2020/4/26.
 */
object SendSms {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    data class VerificationCode(val code: String, val msg: String?)

    private val codeMap = hashMapOf(
            "OK" to "OK",
            "isv.BLACK_KEY_CONTROL_LIMIT" to "黑名单管控 建议联系平台解除黑名单",
            "isv.MOBILE_NUMBER_ILLEGAL" to "非法手机号 建议使用正确的手机号",
            "VALVE:M_MC" to "重复过滤 建议减少每分钟发送数量",
            "VALVE:H_MC" to "重复过滤 建议减少每小时发送数量",
            "VALVE:D_MC" to "重复过滤 建议减少每天发送数量",
            "isv.ACCOUNT_ABNORMAL" to "账户异常 建议联系平台确认账号",
            "isv.AMOUNT_NOT_ENOUGH" to "账户余额不足 建议进行账户充值",
            "isv.ACCOUNT_NOT_EXISTS" to "账户不存在 建议开通账户",
            "isp.SYSTEM_ERROR" to "系统错误 建议联系平台核查原因",
            "isv.SMS_SIGNATURE_ILLEGAL" to "短信签名不合法 建议重新申请签名",
            "isv.SMS_TEMPLATE_ILLEGAL" to "短信模板不合法 建议重新申请模版",
            "isv.TEMPLATE_MISSING_PARAMETERS" to "模板缺少变量 建议修改模版",
            "isv.TEMPLATE_PARAMS_ILLEGAL" to "模板变量里包含非法关键字 建议修改模版",
            "isv.PRODUCT_UN_SUBSCRIPT" to "未开通云通信产品的阿里云客户 建议开通云通信产品",
            "isv.MOBILE_COUNT_OVER_LIMIT" to "手机号码数量超过限制 建议减少手机号码",
            "isv.PARAM_LENGTH_LIMIT" to "参数超出长度限制 建议修改参数长度",
            "isv.INVALID_PARAMETERS" to "参数异常 建议使用正确的参数",
            "FILTER" to "关键字拦截 建议修改短信内容",
            "isv.PRODUCT_UNSUBSCRIBE" to "产品未开通 建议订购产品",
            "isv.BUSINESS_LIMIT_CONTROL" to "业务限流 建议联系平台核查原因",
            "isv.OUT_OF_SERVICE" to "业务停机 建议联系平台核查原因",
            "isv.PARAM_NOT_SUPPORT_URL" to "不支持URL 建议删除内容中的URL",
            "MissingParameter.To" to "You must specify To. 确少To参数。",
            "MissingParameter.Message" to "You must specify Message. 参数Message缺失。",
            "Forbidden.Operation" to "You are not authorized to perform the operation. 无权限进行此操作！",
            "Account.Abnormal" to "The status of Alibaba Cloud account is invalid. 账号状态不正确。",
            "InvalidParameter.Type" to "The specified Type is invalid. 参数Type无效，请检查参数值。",
            "InvalidParameter.To" to "The specified To is invalid. 参数To无效，请检查参数值。",
            "InvalidParameter.SenderId" to "The specified SenderId is invalid. 参数SenderId无效，请检查参数值。",
            "PhoneNumber.Illegal" to "The specified phone number is invalid. 手机号码无效或者错误。",
            "InvalidParameter.From" to "The specified From is invalid. 参数From无效，请检查参数值。",
            "InvalidParameter.ExternalId" to "The specified ExternalId is invalid. 参数ExternalId无效，请检查参数值。",
            "Unsupport.CountryCode" to "The specified country code is not supported. 不支持的国家码。",
            "Unknown.CountryCode" to "The specified country code is invalid. 不能识别国家码。",
            "InvalidParameter.Channel" to "The specified Channel is invalid. 参数Channel无效",
            "MonthLimitControl" to "The monthly volume limit is exceeded. 发送量超过月限额。",
            "DayLimitControl" to "The daily volume limit is exceeded. 发送量超过日限额。",
            "OutOfService" to "The account is suspended due to an insufficient balance. 账号已停机。",
            "Amount.NotEnough" to "The account balance is insufficient. 余额不足。",
            "isp.RAM_PERMISSION_DENY" to "RAM权限DENY 建议联系平台核查原因",
            "isv.INVALID_JSON_PARAM" to "JSON参数不合法，只接受字符串值 建议修改JSON参数"
    )

    fun send(phone: String, code: String): VerificationCode {
        val idFile = ClassPathResource("AccessKeyId")
        val secretFile = ClassPathResource("AccessKeySecret")
        val keyId = FileCopyUtils.copyToString(InputStreamReader(idFile.inputStream))
        val keySecret = FileCopyUtils.copyToString(InputStreamReader(secretFile.inputStream))
        logger.info("AccessKeyId : $keyId")
        logger.info("AccessKeySecret : $keySecret")
        val profile = DefaultProfile.getProfile("cn-hangzhou", keyId, keySecret)
        val client: IAcsClient = DefaultAcsClient(profile)
        val request = CommonRequest()
        request.sysMethod = MethodType.POST
        request.sysDomain = "dysmsapi.aliyuncs.com"
        request.sysVersion = "2017-05-25"
        request.sysAction = "SendSms"
        request.putQueryParameter("RegionId", "cn-hangzhou")
        request.putQueryParameter("PhoneNumbers", phone)
        request.putQueryParameter("SignName", "企拍拍")
        request.putQueryParameter("TemplateCode", "SMS_189031868");
        request.putQueryParameter("TemplateParam", "{\"code\":\"$code\"}")

        val response = client.getCommonResponse(request)
        val msg = codeMap[response.data]
        logger.info("$phone : $code = ${response.data} + $msg")
        return VerificationCode(response.data, msg)
    }
}