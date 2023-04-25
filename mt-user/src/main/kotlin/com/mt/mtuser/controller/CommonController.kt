package com.mt.mtuser.controller

import com.mt.mtuser.common.SendSms
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.AppUpdate
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.User
import com.mt.mtuser.service.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * Created by gyh on 2020/3/18.
 */
@RestController
class CommonController {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    lateinit var roleService: RoleService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var redisUtil: RedisUtil

    @Autowired
    lateinit var fileService: FileService

    @Autowired
    lateinit var appUpdateService: AppUpdateService

    /**
     * @api {get} /common/check 检查用户是否存在
     * @apiDescription  检查用户是否存在,当companyId参数不为空是同时检查用户是否为公司股东
     * @apiName checkingPhone
     * @apiVersion 0.0.1
     * @apiParam {String} phone 用户手机号
     * @apiParam {String} [companyId] 公司Id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":{"user": true, "stockholder": true}}
     * @apiSuccess {String} user 用户是否存在 true：存在; false：不存在
     * @apiSuccess {String} stockholder 用户是否为公司股东 true：是; false：不是
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/check")
    suspend fun checkingPhone(
        @RequestParam phone: String,
        @RequestParam(required = false) companyId: Int?
    ): ResponseInfo<Map<String, Boolean>> {
        val user = userService.findByPhone(phone)
        var role = 0
        if (user != null && companyId != null) {
            val roleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
            role = roleService.exists(user.id!!, roleId, companyId)
        }
        return ResponseInfo.ok(mapOf("user" to (user != null), "stockholder" to (role == 1)))
    }

    /**
     * @api {post} /register 注册一个用户
     * @apiDescription  注册用户
     * @apiName register
     * @apiVersion 0.0.1
     * @apiParam {String} phone 用户的手机号
     * @apiParam {String} password 用户密码
     * @apiParam {String} code 短信验证码
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiSuccessExample {json} 用户已存在:
     * {"code": 1,"msg": "用户已存在","data": null}
     * @apiGroup Common
     * @apiPermission none
     */
    @PostMapping("/register")
    fun register(@RequestBody map: Mono<Map<String, String>>): Mono<ResponseInfo<Unit>> {
        val result = map.flatMap {
            val code = it["code"] ?: return@flatMap Mono.error<Unit>(IllegalStateException("请输入验证码"))
            val user = User()
            user.phone = it["phone"] ?: return@flatMap Mono.error<Unit>(IllegalStateException("请输入手机号"))
            user.password = it["password"] ?: return@flatMap Mono.error<Unit>(IllegalStateException("请输入密码"))
            logger.info(code)
            mono {
                if (!userService.existsUserByPhone(user.phone!!)) {
                    redisUtil.getCode(user.phone!!)
                } else error("用户已存在")
            }.filter { localCode -> localCode != null && code == localCode }
                .switchIfEmpty(Mono.error(IllegalStateException("验证码错误")))
                .flatMap { mono { redisUtil.deleteCode(user.phone!!) } }
                .flatMap { mono { userService.register(user) } }
        }
        return ResponseInfo.ok(result)
    }

    /**
     * @api {get} /common/sendCode 发生验证码
     * @apiDescription  发生验证码
     * @apiName sendCode
     * @apiVersion 0.0.1
     * @apiParam {String} phone 用户的手机号
     * @apiParamExample {url} Request-Example:
     * /common/sendCode?phone=12459874125
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/sendCode")
    suspend fun sendCode(@RequestParam phone: String): ResponseInfo<String?> {
        val smsCode = Util.getRandomInt(4)
        val (code, msg) = SendSms.send(phone, smsCode, 5)
        if (code == "0") {
            redisUtil.saveCode(phone, smsCode)
        } else throw IllegalStateException(msg)
        return ResponseInfo.ok(msg)
    }

    /**
     * @api {get} /common/verifyCode 验证验证码
     * @apiDescription  验证验证码
     * @apiName verifyCode
     * @apiVersion 0.0.1
     * @apiParam {String} phone 用户的手机号
     * @apiParam {String} code 验证码
     * @apiParamExample {url} Request-Example:
     * /common/sendCode?phone=12459874125&code=1234
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiSuccessExample {json} 验证码错误:
     * {"code":1,"msg":"验证码错误","data":null}
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/verifyCode")
    suspend fun verifyCode(@RequestParam phone: String, @RequestParam code: String): ResponseInfo<Unit> {
        val localCode = redisUtil.getCode(phone)
        if (localCode == null || code != localCode) {
            throw IllegalStateException("验证码错误")
        }
        redisUtil.deleteCode(phone)
        redisUtil.saveVerifyResult(phone)
        return ResponseInfo.ok()
    }

    /**
     * @api {put} /common/password 忘记密码
     * @apiDescription  忘记密码，需先调用发送验证码获取验证码
     * @apiName forgetPassword
     * @apiVersion 0.0.1
     * @apiParam {String} phone 用户的手机号
     * @apiParam {String} password 用户密码
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":true}
     * @apiSuccessExample {json} 过期:
     * {"code": 1,"msg": "过期","data": null}
     * @apiGroup Common
     * @apiPermission none
     */
    @PutMapping("/common/password")
    suspend fun forgetPassword(@RequestBody map: Map<String, String>): ResponseInfo<Boolean> {
        val user = User()
        user.phone = map["phone"] ?: throw IllegalStateException("请输入手机号")
        user.password = map["password"] ?: throw IllegalStateException("请输入密码")
        user.passwordEncoder()
        redisUtil.getVerify(user.phone!!) ?: throw IllegalStateException("验证码错误")
        redisUtil.deleteCode(user.phone!!)
        redisUtil.deleteVerify(user.phone!!)
        val result = userService.forgetPassword(user)
        return ResponseInfo.ok(result)
    }

    /**
     * @api {get} /common/getRoles 获取所有的角色
     * @apiDescription  获取所有的角色
     * @apiName getRoles
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": [{"id": 1,"name": "ROLE_SUPER_ADMIN","nameZh": "超级管理员"},{"id": 10,"name":
     * "ROLE_USER","nameZh": "股东"},{"id": 2,"name": "ROLE_ANALYST","nameZh": "企业分析员"},{"id": 3,"name": "ROLE_ADMIN"
     * ,"nameZh": "企业管理员"}]}
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/getRoles")
    suspend fun getRoles(): ResponseInfo<List<MtRole>> {
        return ResponseInfo.ok(roleService.findAll().toList())
    }

    /**
     * @api {post} /file 上传文件
     * @apiDescription  上传文件
     * @apiName uploadFile
     * @apiVersion 0.0.1
     * @apiParam {File} img 文件
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": "\\2020\\05\\pictures\\5-661ab0ad-acfe-4c5c-a1b6-a7c419edc961"}
     * @apiGroup Common
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @PostMapping("/file")
    fun uploadFile(@RequestPart("img") filePart: FilePart): Mono<ResponseInfo<String>> {
        logger.info("{}", filePart.filename())
        return ResponseInfo.ok(fileService.uploadImg(filePart))
    }

    /**
     * @api {delete} /file 删除文件
     * @apiDescription  删除文件
     * @apiName deleteFile
     * @apiParam {String} path 文件路径
     * @apiVersion 0.0.1
     * @apiParamExample {url} Request-Example:
     * /file?path=\\2020\\05\\pictures\\5-7d2be6ae-def9-46f9-8078-97a29a788f8b.jpg
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": true}
     * @apiGroup Common
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @DeleteMapping("/file")
    fun deleteFile(@RequestParam path: String): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(Mono.just(fileService.deleteFile(path)))
    }

    /**
     * @api {get} /common/wgt 查询更新信息
     * @apiDescription  查询更新信息
     * @apiName appVersion
     * @apiParam {String} version 当前版本
     * @apiParam {String} type 类型：android，ios
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": true}
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/wgt")
    fun appVersion(@RequestParam version: String, @RequestParam type: String): Mono<ResponseInfo<AppUpdate>> {
        return ResponseInfo.ok(appUpdateService.appVersion(version, type))
    }
}