package com.mt.mtuser.controller

import com.mt.mtuser.common.SendSms
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.User
import com.mt.mtuser.schedule.QuartzManager
import com.mt.mtuser.schedule.RoomStartJobInfo
import com.mt.mtuser.schedule.RoomTask
import com.mt.mtuser.service.CompanyService
import com.mt.mtuser.service.RedisUtil
import com.mt.mtuser.service.RoleService
import com.mt.mtuser.service.UserService
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.mono
import org.quartz.JobDataMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
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
        }.flatMap {
            mono { redisUtil.deleteCode(it.phone!!) }
            userService.register(it)
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
                val (code, msg) = SendSms.send(phone, smsCode)
                if (code == "OK") {
                    redisUtil.saveCode(phone, smsCode)
                }
                msg
            } else {
                "用户已存在"
            }
        })
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
     * @apiSuccess {Integer} tradesCapacity=0 交易量
     * @apiSuccess {Integer} tradesVolume=0 交易金额
     * @apiSuccess {Integer} tradesNumber=0 交易次数
     * @apiGroup Common
     * @apiUse tokenMsg
     * @apiHeaderExample {json} 请求头例子:
     *     {
     *       "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwicm9sZXMiOiJbW1wiU1VQRVJfQURNSU5cIixudWxsXSxbXCJVU0VSXCIsMV1dIiwibmJmIjoxNTg3NTU5NTQ0LCJleHAiOjE1ODk2MzMxNDR9.zyppWBmaF0l6ezljR1bTWUkAon50KF-VTrge1-W2hsM"
     *     }
     * @apiPermission superAdmin
     */
    @GetMapping("/system/info")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun getSystemInfo(): Mono<ResponseInfo<MutableMap<String, Any>>> {
        return ResponseInfo.ok(mono {
            val data: MutableMap<String, Any> = HashMap()
            data["companyCount"] = companyService.count()
            data["userCount"] = userService.count()
            data["roomCount"] = roomService.getAllRoomCount()
            data["tradesCapacity"] = 0 // 交易量
            data["tradesVolume"] = 0 // 交易金额
            data["tradesNumber"] = 0 // 交易次数
            data
        })
    }
}