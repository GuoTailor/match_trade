package com.mt.mtuser.controller

import com.mt.mtuser.common.SmsSample
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.User
import com.mt.mtuser.service.RedisUtil
import com.mt.mtuser.service.RoleService
import com.mt.mtuser.service.UserService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.lang.IllegalStateException
import java.util.*

/**
 * Created by gyh on 2020/3/18.
 */
@RestController
class CommonController {
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
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[{"nameZh": "超级管理员","name": "ROLE_SUPER_ADMIN","id": 1},
     * {"nameZh": "企业管理员","name": "ROLE_ADMIN","id": 2},{"nameZh": "股东","name": "ROLE_USER","id": 3}]}
     * @apiSuccessExample {json} 用户已存在:
     * {"code": 1,"msg": "用户已存在","data": null}
     * @apiGroup Common
     * @apiPermission none
     */
    @PostMapping("/register")
    fun register(@RequestBody user: User): Mono<ResponseInfo<Unit>> {
        return userService.register(user)
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
    @GetMapping("/sendCode")
    fun sendCode(@RequestParam phone: String): Mono<ResponseInfo<Unit>> {
        /*ResponseInfo.ok(userService.existsUserByPhone(phone)
                .filter { it }  // 过滤是否不存在
                .map { Util.getRandomInt(4) }   // 构建验证码
                .flatMap { mono { SmsSample.send(phone, it) } } // 发送验证码
                .map { (code, msg) ->
                    if (code == 0) {
                        redisUtil.saveCode()
                    }
                    msg
                }
                .defaultIfEmpty(Mono.error(RuntimeException()))
        )

        userService.existsUserByPhone(phone).map {
            if (it) {
                val code = Util.getRandomInt(4)
                SmsSample.send(phone, code)
            }
        }*/


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
        return ResponseInfo.ok(roleService.findAll().collectList())
    }
}