package com.mt.mtuser.controller

import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.department.Department
import com.mt.mtuser.entity.department.DepartmentPost
import com.mt.mtuser.entity.department.DepartmentPostInfo
import com.mt.mtuser.service.DepartmentPostService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * Created by gyh on 2020/7/20
 */
@RestController
@RequestMapping("/department")
class DepartmentPostController {

    @Autowired
    lateinit var departmentPostService: DepartmentPostService

    /**
     * @api {post} /department 绑定部门和职位
     * @apiDescription  绑定部门和职位
     * @apiName bindDepartment
     * @apiVersion 0.0.1
     * @apiParam {String} departmentName 部门名字
     * @apiParam {String} postName 职位名字
     * @apiParam {Integer} companyId companyId
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {null}
     * @apiGroup Department
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun bindDepartment(@RequestBody dpi: Mono<DepartmentPostInfo>): Mono<ResponseInfo<DepartmentPost>> {
        return ResponseInfo.ok(mono { departmentPostService.bindDepartment(dpi.awaitSingle()) })
    }

    /**
     * @api {put} /department 更新公司部门和职位
     * @apiDescription  更新公司部门和职位,支持增量更新
     * @apiName updateBind
     * @apiVersion 0.0.1
     * @apiParam {Integer} id id
     * @apiParam {String} [departmentName] 部门名字
     * @apiParam {String} [postName] 职位名字
     * @apiParamExample {json} 请求-例子:
     * {"id":3,"postName":"123"}
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": 1
     * @apiGroup Department
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun updateBind(@RequestBody dpi: Mono<DepartmentPostInfo>): Mono<ResponseInfo<Int>> {
        return ResponseInfo.ok(mono { departmentPostService.updateBind(dpi.awaitSingle()) })
    }

    /**
     * @api {delete} /department/{id} 删除公司部门和职位
     * @apiDescription  删除公司部门和职位
     * @apiName deleteBind
     * @apiVersion 0.0.1
     * @apiParam {Integer} id id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {null}
     * @apiGroup Department
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun deleteBind(@PathVariable id: Int): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { departmentPostService.deleteBind(id) })
    }

    /**
     * @api {get} /department/{id} 获取所有部门职位树
     * @apiDescription  获取所有部门职位树
     * @apiName findAllBind
     * @apiVersion 0.0.1
     * @apiParam {Integer} companyId 公司id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {null}
     * @apiGroup Department
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('ANALYST')")
    fun findAllBind(@PathVariable id: Int): Mono<ResponseInfo<LinkedList<Department>>> {
        return ResponseInfo.ok(mono { departmentPostService.findAllBind(id) })
    }
}