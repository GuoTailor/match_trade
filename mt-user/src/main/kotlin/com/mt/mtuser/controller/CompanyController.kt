package com.mt.mtuser.controller

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.Company
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.CompanyService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/18.
 */
@RestController
@RequestMapping("/company")
class CompanyController {
    @Autowired
    lateinit var companyService: CompanyService

    /**
     * @api {post} /company 注册一个公司
     * @apiDescription  注册公司
     * @apiName registerCompany
     * @apiVersion 0.0.1
     * @apiParamExample {json} 请求-例子:
     * {"name":"15306科技有限公司", "roomCount":2, "mode": "2"}
     * @apiUse Company
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":1,"data":null}
     * @apiGroup Company
     * @apiPermission admin
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun register(@RequestBody company: Company): Mono<ResponseInfo<Company>> {
        return ResponseInfo.ok(mono {
            if (!Util.isEmpty(company)) {
                companyService.save(company)
            } else {
                throw IllegalStateException("请填写属性")
            }
        })
    }

    /**
     * @api {delete} /company/{id} 删除一个公司
     * @apiDescription  删除公司
     * @apiName deleteCompany
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 公司id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Company
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { companyService.deleteById(id) })
    }

    /**
     * @api {put} /company 更新公司信息
     * @apiDescription  更新公司信息
     * @apiName updateCompany
     * @apiVersion 0.0.1
     * @apiUse Company
     * @apiParamExample {json} 请求-例子:
     * {"id":2,"name":"15306科技有限公司", "roomCount":2, "mode": "2"}
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Company
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping
    fun update(@RequestBody company: Company): Mono<ResponseInfo<Company>> {
        return ResponseInfo.ok(mono { companyService.update(company) })
    }

    /**
     * @api {get} /company/{id} 获取一个公司信息
     * @apiDescription  获取公司信息
     * @apiName getCompany
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 公司id
     * @apiParamExample {json} 请求-例子:
     * {"id":2}
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Company
     * @apiPermission user
     */
    @GetMapping("/{id}")
    fun getCompany(@PathVariable id: Int): Mono<ResponseInfo<Company>> {
        return ResponseInfo.ok(mono { companyService.findById(id) })
    }

    /**
     * @api {get} /company 获取所有公司信息
     * @apiDescription  获取所有公司信息
     * @apiName getAllCompany
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParamExample {url} 请求-例子:
     * /company?pageSize=10&pageNum=1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 10,"total": 1,"item": [{"id": 1,"name": "6105","roomCount": 1,"mode": "4","createTime": "2020-03-18T07:35:45.000+0000"}]}}
     * @apiGroup Company
     * @apiPermission user
     */
    @GetMapping
    fun getAllCompany(query: PageQuery): Mono<ResponseInfo<PageView<Company>>> {
        return ResponseInfo.ok(mono { companyService.findAllByQuery(query) })
    }

}