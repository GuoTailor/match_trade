package com.mt.mtuser.controller

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.*
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.RoleService
import com.mt.mtuser.service.UserService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
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
     * @api {get} /user/info 获取用户的信息
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
    suspend fun getUserInfo(@RequestParam(required = false) id: Int?): ResponseInfo<User> {
        val userId = id ?: BaseUser.getcurrentUser().awaitSingle().id!!
        val user = userService.findById(userId) ?: return ResponseInfo(1, "用户不存在")
        val roles = roleService.selectRolesByUserId(user.id!!).toList()
        user.roles = roles
        return ResponseInfo(0, "成功", user)

    }

    /**
     * @api {put} /user 修改用户的信息
     * @apiDescription  修改用户的信息,不会修改用户的`id`，`phone`，`password`
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
    suspend fun alter(@RequestBody monoUser: Mono<User>): ResponseInfo<Int> {
        val user = monoUser.awaitSingle()
        return if (!Util.isEmpty(user)) {
            val currentUser = BaseUser.getcurrentUser().awaitSingle()
            user.id = currentUser.id
            user.phone = null
            user.password = null
            ResponseInfo(userService.save(user), "修改成功")
        } else {
            ResponseInfo(userService.save(user), "请至少更新一个属性")
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
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 10,"total": 1,"item": [{"id": 1,"name": "6105",
     * "roomCount": 1,"mode": "4","createTime": "2020-03-18T07:35:45.000+0000"}]}}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping
    suspend fun getAllUser(query: PageQuery): ResponseInfo<PageView<User>> {
        return ResponseInfo.ok(userService.findAllUser(query))
    }

    /**
     * @api {post} /user/role/analyst 添加一个观察员
     * @apiDescription  添加一个观察员
     * @apiName addAnalystRole
     * @apiVersion 0.0.1
     * @apiParam {String} phone 电话
     * @apiParam {String} nickName 用户名
     * @apiParam {String} idNum 身份证号码
     * @apiParam {String} password 密码
     * @apiParam {String} userPhoto 头像url地址
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"修改成功","data":null}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/role/analyst")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun addAnalystRoleTest(@RequestBody user: Mono<User>): ResponseInfo<Unit> {
        return ResponseInfo.ok(userService.addAnalystRole(user.awaitSingle()))
    }

    /**
     * @api {put} /user/password 修改用户的密码
     * @apiDescription  修改用户的密码
     * @apiName updatePassword
     * @apiVersion 0.0.1
     * @apiParam {String} oldPassword 老密码
     * @apiParam {String} newPassword 新密码
     * @apiParamExample {json} 请求-例子:
     * {"oldPassword":"nmka", "newPassword":"admin"}
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"修改成功","data":true}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @PutMapping("/password")
    suspend fun updatePassword(@RequestBody data: Mono<Map<String, String>>): ResponseInfo<Boolean> {
        val map = data.awaitSingle()
        return ResponseInfo.ok(
            userService.updatePassword(
                map["oldPassword"] ?: error("oldPassword 不能为空"),
                map["newPassword"] ?: error("newPassword 不能为空")
            )
        )
    }

    /**
     * @api {get} /user/role/analyst 取全部的观察员
     * @apiDescription  取全部的观察员
     * @apiName getAllAnalystRole
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiSuccess (返回) {Integer} id 用户id
     * @apiSuccess (返回) {String} phone 电话
     * @apiSuccess (返回) {String} nickName 用户名
     * @apiSuccess (返回) {String} idNum 身份证号码
     * @apiSuccess (返回) {String} password 密码
     * @apiSuccess (返回) {String} userPhoto 头像url地址
     * @apiSuccess (返回) {List} roles 角色信息
     * @apiSuccess (返回) {Date} createTime 注册日期
     * @apiSuccess (返回) {Date} lastTime 注册日期
     * @apiSuccess (返回) {Date} updateTime 最后修改日期
     * @apiSuccess (返回) {Integer} companyCount 公司数
     * @apiSuccess (返回) {Integer} reportCount 报告数
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping("/role/analyst")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun getAllAnalystRole(query: PageQuery): ResponseInfo<PageView<Analyst>> {
        return ResponseInfo.ok(userService.getAllAnalystRole(query))
    }

    /**
     * @api {get} /user/role/analyst/info 获取观察员信息
     * @apiDescription  获取观察员信息
     * @apiName getAnalystInfo
     * @apiVersion 0.0.1
     * @apiParam {Integer} [id] 用户id,不传默认获取自己的信息
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiSuccess (返回) {Integer} id 用户id
     * @apiSuccess (返回) {String} phone 电话
     * @apiSuccess (返回) {String} nickName 用户名
     * @apiSuccess (返回) {String} idNum 身份证号码
     * @apiSuccess (返回) {String} password 密码
     * @apiSuccess (返回) {String} userPhoto 头像url地址
     * @apiSuccess (返回) {List} roles 角色信息
     * @apiSuccess (返回) {Date} createTime 注册日期
     * @apiSuccess (返回) {Date} lastTime 注册日期
     * @apiSuccess (返回) {Date} updateTime 最后修改日期
     * @apiSuccess (返回) {Integer} companyCount 公司数
     * @apiSuccess (返回) {Integer} reportCount 报告数
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission analyst
     */
    @GetMapping("/role/analyst/info")
    @PreAuthorize("hasRole('ANALYST')")
    suspend fun getAnalystInfo(@RequestParam(required = false) id: Int?): ResponseInfo<Analyst> {
        return ResponseInfo.ok(userService.getAnalystInfo(id))
    }

    /**
     * @api {delete} /user/role/analyst 删除一个观察员
     * @apiDescription  删除一个观察员
     * @apiName deleteAnalyst
     * @apiVersion 0.0.1
     * @apiParam {Integer} stockholderId 观察员id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @DeleteMapping("/role/analyst")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun deleteAnalyst(stockholderId: Int): ResponseInfo<Unit> {
        return ResponseInfo.ok(userService.deleteAnalyst(stockholderId))
    }

    /**
     * @api {put} /user/role/analyst 修改一个观察员
     * @apiDescription  修改一个观察员
     * @apiName updateAnalyst
     * @apiVersion 0.0.1
     * @apiParamExample {json} 请求-例子:
     * {"phone":"110"}
     * @apiParam {String} [phone] 电话
     * @apiParam {String} [nickName] 用户名
     * @apiParam {String} [idNum] 身份证号码
     * @apiParam {String} [password] 密码
     * @apiParam {String} [userPhoto] 头像url地址
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"修改成功","data":null}
     * @apiGroup User
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/role/analyst")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun updateAnalyst(@RequestBody user: Mono<User>): ResponseInfo<Unit> {
        return ResponseInfo.ok(userService.updateAnalyst(user.awaitSingle()))
    }
}