package com.mt.mtuser.controller

import com.mt.mtuser.common.SmsSample
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.User
import com.mt.mtuser.service.RedisUtil
import com.mt.mtuser.service.RoleService
import com.mt.mtuser.service.UserService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

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

    /**
     * @api {get} /register 注册一个用户
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
    @Transactional  // 由于事务不支持挂起函数，所以注解只能打在普通函数上面并且一定要让报错抛出去，不然事务不会回退
    fun register(@RequestBody map: Map<String, String>): Mono<ResponseInfo<Unit>> {
        return mono {
            val code = map["code"] ?: return@mono ResponseInfo<Unit>(1, "请输入验证码")
            val user = User()
            user.phone = map["phone"] ?: return@mono ResponseInfo<Unit>(1, "请输入手机号")
            user.password = map["password"] ?: return@mono ResponseInfo<Unit>(1, "请输入密码")
            logger.info(code)
            val localCode = redisUtil.getCode(user.phone!!)
            if (localCode != null && code == localCode) {
                redisUtil.deleteCode(user.phone!!)
                userService.register(user)
                ResponseInfo<Unit>(0, "成功")
            } else {
                ResponseInfo<Unit>(1, "验证码错误")
            }
        }
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
     * @apiSuccessExample {json} 用户不存在:
     * {"code":1,"msg":"用户不存在","data":null}
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/sendCode")
    fun sendCode(@RequestParam phone: String): Mono<ResponseInfo<String>> {
        return ResponseInfo.ok(mono {
            if (userService.existsUserByPhone(phone)) {
                val smsCode = Util.getRandomInt(4)
                val (code, msg) = SmsSample.send(phone, smsCode)
                if (code == 0) {
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
     * {"code":0,"msg":"成功","data":[{"nameZh": "超级管理员","name": "ROLE_SUPER_ADMIN","id": 1},
     * {"nameZh": "企业管理员","name": "ROLE_ADMIN","id": 2},{"nameZh": "股东","name": "ROLE_USER","id": 3}]}
     * @apiGroup Common
     * @apiPermission none
     */
    @GetMapping("/common/getRoles")
    fun getRoles(): Mono<ResponseInfo<List<MtRole>>> {
        return ResponseInfo.ok(mono { roleService.findAll().toList() })
    }
}