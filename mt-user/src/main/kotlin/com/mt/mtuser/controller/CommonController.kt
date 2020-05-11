package com.mt.mtuser.controller

import com.mt.mtuser.common.SendSms
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.User
import com.mt.mtuser.schedule.QuartzManager
import com.mt.mtuser.schedule.RoomStartJobInfo
import com.mt.mtuser.schedule.RoomTask
import com.mt.mtuser.service.*
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.mono
import org.quartz.JobDataMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalTime
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
    lateinit var roomService: RoomService

    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var redisUtil: RedisUtil

    @Autowired
    lateinit var tradeInfoService: TradeInfoService

    @Autowired
    lateinit var roomRecordService: RoomRecordService

    /**
     * @api {gut} /common/check 检查用户是否存在
     * @apiDescription  检查用户是否存在
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
    fun checkingPhone(@RequestParam phone: String, @RequestParam(required = false) companyId: Int?): Mono<ResponseInfo<Map<String, Boolean>>> {
        return ResponseInfo.ok(mono {
            val user = userService.findByPhone(phone)
            var role = 0
            if (user != null && companyId != null) {
                val roleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
                role = roleService.exists(user.id!!, roleId, companyId)
            }
            mapOf("user" to (user != null), "stockholder" to (role == 1))
        })
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
            val code = it["code"] ?: return@flatMap Mono.error<User>(IllegalStateException("请输入验证码"))
            val user = User()
            user.phone = it["phone"] ?: return@flatMap Mono.error<User>(IllegalStateException("请输入手机号"))
            user.password = it["password"] ?: return@flatMap Mono.error<User>(IllegalStateException("请输入密码"))
            logger.info(code)
            mono { redisUtil.getCode(user.phone!!) }
                    .filter { localCode -> localCode != null && code == localCode }
                    .switchIfEmpty(Mono.error(IllegalStateException("验证码错误")))
                    .map { user }
        }.flatMap { user ->
            mono { redisUtil.deleteCode(user.phone!!) }
                    .flatMap { userService.register(user) }
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
     * @apiSuccessExample {json} 用户已存在:
     * {"code":1,"msg":"用户已存在","data":null}
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/sendCode")
    fun sendCode(@RequestParam phone: String): Mono<ResponseInfo<String>> {
        return ResponseInfo.ok(mono {
            if (!userService.existsUserByPhone(phone)) {
                val smsCode = Util.getRandomInt(4)
                val (code, msg) = SendSms.send(phone, smsCode, 5)
                if (code == "0") {
                    redisUtil.saveCode(phone, smsCode)
                    msg
                } else throw IllegalStateException(msg)
            } else throw IllegalStateException("用户已存在")
        })
    }

    /**
     * @api {put} /common/password 忘记密码
     * @apiDescription  忘记密码，需先调用发送验证码获取验证码
     * @apiName forgetPassword
     * @apiVersion 0.0.1
     * @apiParam {String} phone 用户的手机号
     * @apiParam {String} password 用户密码
     * @apiParam {String} code 短信验证码
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":true}
     * @apiSuccessExample {json} 验证码错误:
     * {"code": 1,"msg": "验证码错误","data": null}
     * @apiGroup Common
     * @apiPermission none
     */
    @PutMapping("/common/password")
    fun forgetPassword(@RequestBody map: Mono<Map<String, String>>): Mono<ResponseInfo<Boolean>> {
        val result = map.flatMap {
            val code = it["code"] ?: return@flatMap Mono.error<User>(IllegalStateException("请输入验证码"))
            val user = User()
            user.phone = it["phone"] ?: return@flatMap Mono.error<User>(IllegalStateException("请输入手机号"))
            user.password = it["password"] ?: return@flatMap Mono.error<User>(IllegalStateException("请输入密码"))
            logger.info(code)
            mono { redisUtil.getCode(user.phone!!) }
                    .filter { localCode -> localCode != null && code == localCode }
                    .switchIfEmpty(Mono.error(IllegalStateException("验证码错误")))
                    .map { user }
        }.flatMap { user ->
            mono {
                redisUtil.deleteCode(user.phone!!)
                userService.forgetPassword(user)
            }
        }
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
    fun getRoles(): Mono<ResponseInfo<List<MtRole>>> {
        return ResponseInfo.ok(mono { roleService.findAll().toList() })
    }

    /**
     * @api {get} /system/info 获取系统信息
     * @apiDescription  获取系统信息
     * @apiName getSystemInfo
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess {Integer} companyCount 公司数量
     * @apiSuccess {Integer} userCount 用户数量
     * @apiSuccess {Integer} roomCount 房间数量
     * @apiSuccess {Long} tradesCapacity 交易量
     * @apiSuccess {Long} tradesVolume 交易金额
     * @apiSuccess {Integer} tradesNumber 交易次数
     * @apiGroup Common
     * @apiUse tokenMsg
     * @apiHeaderExample {json} 请求头例子:
     *     {
     *       "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwicm9sZXMiOiJbW1wiU1VQRVJfQURNSU5cIixudWxsXSxbXCJVU
     *       0VSXCIsMV1dIiwibmJmIjoxNTg3NTU5NTQ0LCJleHAiOjE1ODk2MzMxNDR9.zyppWBmaF0l6ezljR1bTWUkAon50KF-VTrge1-W2hsM"
     *     }
     * @apiPermission superAdmin
     */
    @GetMapping("/system/info")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Cacheable(cacheNames = ["getSystemInfo"])
    fun getSystemInfo(): Mono<ResponseInfo<MutableMap<String, Number>>> {
        return ResponseInfo.ok(mono {
            val data: MutableMap<String, Number> = HashMap()
            data["companyCount"] = companyService.count()
            data["userCount"] = userService.count()
            data["roomCount"] = roomService.getAllRoomCount()
            data["tradesCapacity"] = tradeInfoService.countStockByTradeTime() // 交易量
            data["tradesVolume"] = tradeInfoService.countMoneyByTradeTime() // 交易金额
            data["tradesNumber"] = roomRecordService.countByStartTime() // 交易次数
            data
        }.cache(Duration.ofMinutes(1)))
    }
}