package com.mt.mtuser.controller

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.*
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.RoleService
import com.mt.mtuser.service.UserService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/19.
 */
@RestController
@RequestMapping("/user")
class UserController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var roleService: RoleService

    /**
     * @api {put} /user/info 获取用户的信息
     * @apiDescription  获取用户的信息
     * @apiName getUserInfo
     * @apiVersion 0.0.1
     * @apiParam {Integer} [id] 用户id,不传默认获取自己的信息
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0, "msg": "成功", "data": {"roles": [{"id": 33,"userid": 11,"roleid": 3,"companyid": null,"name":
     * "ROLE_USER","nameZh": "股东","authority": "ROLE_USER"}],"id": 11,"phone": "2222","nickName": null,"idNum": null,
     * "userPhoto": null,"createTime": "2020-03-07T20:54:35.000+0000","lastTime": null,"updateTime": "2020-03-07T20:54:35.000+0000",
     * "username": "2222"}}
     * @apiError UserNotFound The `id` of the User was not found.
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/info")
    fun getUserInfo(@RequestParam(required = false) id: Int?): Mono<ResponseInfo<User>> {
        return mono {
            val userid = id ?: BaseUser.getcurrentUser().awaitSingle().id!!
            val user = userService.findById(userid) ?: return@mono ResponseInfo<User>(1, "用户不存在")
            val role = roleService.selectRolesByUserId(user.id!!)
            user.roles = listOf(role)
            ResponseInfo(0, "成功", user)
        }
    }

    /**
     * @api {put} /user 修改用户的信息
     * @apiDescription  修改用户的信息,不会修改用户的`id`，`phone`
     * @apiName alter
     * @apiVersion 0.0.1
     * @apiParamExample {json} 请求-例子:
     * {"nickName":"nmka"}
     * @apiUse User
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"修改成功","data":null}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @PutMapping
    fun alter(@RequestBody monoUser: Mono<User>): Mono<ResponseInfo<Int>> {
        return mono {
            val user = monoUser.awaitSingle()
            if (!Util.isEmpty(user)) {
                val currentUser = BaseUser.getcurrentUser().awaitSingle()
                user.id = currentUser.id
                user.phone = null
                user.passwordEncoder()
                ResponseInfo<Int>(userService.save(user), "修改成功")
            } else {
                ResponseInfo<Int>(userService.save(user), "请至少更新一个属性")
            }
        }
    }

    /**
     * @api {get} /user 获取所有用户
     * @apiDescription  获取所有用户,支持查询
     * @apiName getAllUser
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParamExample {url} 请求-例子:
     * /user?pageSize=10&pageNum=1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 10,"total": 1,"item": [{"id": 1,"name": "6105","roomCount": 1,"mode": "4","createTime": "2020-03-18T07:35:45.000+0000"}]}}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping
    fun getAllUser(query: PageQuery): Mono<ResponseInfo<PageView<User>>> {
        return ResponseInfo.ok(mono { userService.findAllUser(query) })
    }

    /**
     * @api {put} /user/role 修改用户的角色和所属公司
     * @apiDescription  修改角色的信息
     * @apiName changeAuthority
     * @apiVersion 0.0.1
     * @apiParamExample {json} 请求-例子:
     * {"userid":1, "roleid":2, "companyid": 3}
     * @apiParam {Integer} userid 用户id
     * @apiParam {Integer} [roleid] 角色id
     * @apiParam {Integer} [companyid] 公司id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"修改成功","data":null}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    fun changeAuthority(@RequestBody role: Mono<Role>): Mono<ResponseInfo<Role>> {
        return ResponseInfo.ok(mono { roleService.save(role) }, "修改成功")
    }

    /**
     * @api {put} /user/role/analyst 修改用户的企业观察员角色
     * @apiDescription  修改用户为企业观察员角色
     * @apiName addAnalystRole
     * @apiVersion 0.0.1
     * @apiParamExample {json} 请求-例子:
     * {"phone":"110", "companyList": [1, 2, 3, 4]}
     * @apiParam {Integer} userId 用户id
     * @apiParam {List} companyList 公司列表
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"修改成功","data":null}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/role/analyst")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun addAnalystRole(@RequestBody map: Mono<Map<String, Any>>): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(map.flatMap {
            val userId = it["userId"] as Int
            val companyList = (it["companyList"] as List<*>).filterIsInstance<Int>()
            mono { userService.addAnalystRole(userId, companyList) }
        })
    }

}