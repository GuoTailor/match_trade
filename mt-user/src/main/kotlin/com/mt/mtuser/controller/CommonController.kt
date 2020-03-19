package com.mt.mtuser.controller

import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Role
import com.mt.mtuser.entity.User
import com.mt.mtuser.service.RoleService
import com.mt.mtuser.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/18.
 */
@RestController
class CommonController {
    @Autowired
    lateinit var roleService: RoleService
    @Autowired
    lateinit var userService: UserService

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