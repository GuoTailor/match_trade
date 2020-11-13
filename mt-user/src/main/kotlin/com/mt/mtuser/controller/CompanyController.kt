package com.mt.mtuser.controller

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.*
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.CompanyService
import com.mt.mtuser.service.RoomRecordService
import com.mt.mtuser.service.TradeInfoService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.HashMap

/**
 * Created by gyh on 2020/3/18.
 */
@RestController
@RequestMapping("/company")
class CompanyController {
    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var tradeInfoService: TradeInfoService

    @Autowired
    lateinit var roomRecordService: RoomRecordService

    /**
     * @api {post} /company 注册一个公司
     * @apiDescription  注册公司
     * @apiName registerCompany
     * @apiVersion 0.0.1
     * @apiParam {String} analystId 观察员id
     * @apiParam {String} adminPhone 管理员电话
     * @apiParam {String} adminName 管理员名字
     * @apiParamExample {json} 请求-例子:
     * {"name":"15306科技有限公司", "roomCount":2, "mode": "2"}
     * @apiUse Company
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":1,"data":null}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun register(@RequestBody company: Company): Mono<ResponseInfo<Company>> {
        return ResponseInfo.ok(mono {
            if (!Util.isEmpty(company)) {
                companyService.registerCompany(company)
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
     * @apiUse tokenMsg
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
     * {"id": 1,"name": "6105","roomCount": 1, "modes": ["C","B","D"]}
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"id": 1,"name": "6105","roomCount": 1,"mode": "[\"C\",\"B\",\"D\"]",
     * "createTime": "2020-03-18 15:35:45","licenseUrl": null,"creditUnionCode": null,"legalPerson": null,
     * "unitAddress": null,"unitContactName": null,"unitContactPhone": null,"modes": ["C","B","D"]}}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping
    fun update(@RequestBody company: Mono<Company>): Mono<ResponseInfo<Company>> {
        return ResponseInfo.ok(mono { companyService.update(company.awaitSingle()) })
    }

    /**
     * @api {post} /company/stockholder 添加一个股东
     * @apiDescription  为公司添加一个股东
     * @apiName addStockholder
     * @apiVersion 0.0.1
     * @apiUse StockholderInfo
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/stockholder")
    fun addStockholder(@RequestBody stockholderInfo: Mono<StockholderInfo>): Mono<ResponseInfo<Stockholder>> {
        return ResponseInfo.ok(mono { companyService.addStockholder(stockholderInfo.awaitSingle()) })
    }

    /**
     * @api {delete} /company/stockholder 删除一个股东
     * @apiDescription  删除一个股东
     * @apiName deleteStockholder
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 股东id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/stockholder")
    fun deleteStockholder(id: Int): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { companyService.deleteStockholder(id) })
    }

    /**
     * @api {post} /company/admin 绑定一个公司管理员
     * @apiDescription  为公司添加一个管理员，如果公司已存在管理员就更新
     * @apiName addCompanyAdmin
     * @apiVersion 0.0.1
     * @apiParam {Integer} companyId 公司id
     * @apiParam {String} phone 用户手机号
     * @apiParam {String} realName 真实姓名
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/admin")
    fun addCompanyAdmin(@RequestBody stockholderInfo: Mono<StockholderInfo>): Mono<ResponseInfo<Stockholder>> {
        return ResponseInfo.ok(mono { companyService.addCompanyAdmin(stockholderInfo.awaitSingle()) })
    }

    /**
     * @api {post} /company/analyst 绑定一个公司分析员
     * @apiDescription  为公司绑定一个分析员，如果公司已存在分析员就更新
     * @apiName addCompanyAnalyst
     * @apiVersion 0.0.1
     * @apiParam {Integer} companyId 公司id
     * @apiParam {Integer} userId 用户id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/analyst")
    fun addCompanyAnalyst(userId: Int, companyId: Int): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { companyService.addCompanyAnalyst(userId, companyId) })
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
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/{id}")
    fun getCompany(@PathVariable id: Int): Mono<ResponseInfo<Company>> {
        return ResponseInfo.ok(mono { companyService.findCompany(id) })
    }

    /**
     * @api {get} /company/self 获取自己加入的公司信息
     * @apiDescription  获取自己加入的公司信息
     * @apiName getCompanys
     * @apiUse PageQuery
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 30,"total": 2,"item": [{"id": 1,"name": "6105",
     * "roomCount": 1,"mode": "[\"C\",\"B\",\"D\"]","createTime": "2020-03-18 15:35:45","licenseUrl": null,
     * "creditUnionCode": null,"legalPerson": null,"unitAddress": null,"unitContactName": null,"unitContactPhone": null,
     * "stock": 0,"money": 0.0000,"modes": ["C","B","D"]},{"id": 2,"name": "15-304","roomCount": 1,"mode": "[]",
     * "createTime": "2020-04-23 10:43:42","licenseUrl": null,"creditUnionCode": null,"legalPerson": null,"unitAddress": null,
     * "unitContactName": null,"unitContactPhone": null,"stock": 0,"money": 0.0000,"modes": []}]}}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/self")
    fun getCompanys(query: PageQuery): Mono<ResponseInfo<PageView<Company>>> {
        return ResponseInfo.ok(mono { companyService.findCompany(query) })
    }

    /**
     * @api {get} /company/stockholder 获取公司所有的股东
     * @apiDescription  获取公司所有的股东
     * @apiName getAllShareholder
     * @apiUse PageQuery
     * @apiParam {Integer} [companyId] 公司id,不传默认获取自己管理的公司
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 30,"total": 4,"item": [{"id": 1,"companyId": 1,"userId":
     * 5,"amount": 1800,"realName": "账务","department": null,"position": null,"phone": null,"money": 87500.0000},{"id": 7,
     * "companyId": 1,"userId": 1,"amount": null,"realName": null,"department": null,"position": null,"phone": null,"money":
     * 0.0000},{"id": 16,"companyId": 1,"userId": 10,"amount": 1300,"realName": "nmka","department": null,"position": null,
     * "phone": null,"money": -87499.7700},{"id": 18,"companyId": 1,"userId": 18,"amount": 100,"realName": "刘能","department":
     * "测试","position": "跳舞","phone": null,"money": 10.0000}]}}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @GetMapping("/stockholder")
    fun getAllShareholder(query: PageQuery, @RequestParam(required = false) companyId: Int?): Mono<ResponseInfo<PageView<StockholderInfo>>> {
        return ResponseInfo.ok(mono { companyService.getAllShareholder(query, companyId) })
    }

    /**
     * @api {get} /company/stockholder/department 按部门获取公司所有的股东
     * @apiDescription  按部门获取公司所有的股东
     * @apiName getShareholderByDepartment
     * @apiUse PageQuery
     * @apiParam {Integer} companyId 公司id,不传默认获取自己管理的公司
     * @apiParam {String} name 部门名称
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 30,"total": 4,"item": [{"id": 1,"companyId": 1,"userId":
     * 5,"amount": 1800,"realName": "账务","department": null,"position": null,"phone": null,"money": 87500.0000},{"id": 7,
     * "companyId": 1,"userId": 1,"amount": null,"realName": null,"department": null,"position": null,"phone": null,"money":
     * 0.0000},{"id": 16,"companyId": 1,"userId": 10,"amount": 1300,"realName": "nmka","department": null,"position": null,
     * "phone": null,"money": -87499.7700},{"id": 18,"companyId": 1,"userId": 18,"amount": 100,"realName": "刘能","department":
     * "测试","position": "跳舞","phone": null,"money": 10.0000}]}}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @GetMapping("/stockholder/department")
    fun getShareholderByDepartment(query: PageQuery, companyId: Int, name: String): Mono<ResponseInfo<PageView<StockholderInfo>>> {
        return ResponseInfo.ok(mono { companyService.getShareholderByDepartment(query, companyId, name) })
    }

    /**
     * @api {put} /company/stockholder 修改公司一个股东的信息
     * @apiDescription  修改公司一个股东的信息
     * @apiName updateStockholder
     * @apiUse StockholderInfo
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": true}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/stockholder")
    fun updateStockholder(@RequestBody info: Mono<StockholderInfo>): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(mono { companyService.updateStockholder(info.awaitSingle()) })
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
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    fun getAllCompany(query: PageQuery): Mono<ResponseInfo<PageView<Company>>> {
        return ResponseInfo.ok(mono { companyService.findAllByQuery(query) })
    }

    /**
     * @api {get} /company/analyst 分析员获取自己管理的公司
     * @apiDescription  分析员获取自己管理的公司
     * @apiName findByUser
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 10,"total": 1,"item": [{"id": 1,"name": "6105","roomCount": 1,"mode": "4","createTime": "2020-03-18T07:35:45.000+0000"}]}}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission analyst
     */
    @PreAuthorize("hasRole('ANALYST')")
    @GetMapping("/analyst")
    fun findByUser(query: PageQuery): Mono<ResponseInfo<PageView<Company>>> {
        return ResponseInfo.ok(mono { companyService.findByUser(query) })
    }

    /**
     * @api {put} /company/limit 修改股东的每日交易上限
     * @apiDescription  修改股东的每日交易上限
     * @apiName updateLimit
     * @apiVersion 0.0.1
     * @apiParam {List} userId 用户id
     * @apiParam {Integer} companyId 公司id
     * @apiParam {Integer} limit 限制
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": 1}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/limit")
    fun updateLimit(@RequestBody data: Map<String, Any>): Mono<ResponseInfo<Int>> {
        return ResponseInfo.ok(mono { companyService.updateLimit(data["userId"] as ArrayList<Int>, data["limit"] as Int, data["companyId"] as Int) })
    }

    /**
     * @api {put} /company/enable 启用/禁用公司
     * @apiDescription  使能公司；1:启用；0:禁用
     * @apiName updateEnable
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 公司id
     * @apiParam {String} enable 使能公司；1:启用；0:禁用
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": 1}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/enable")
    fun updateEnable(@RequestBody map: Map<String, Any>): Mono<ResponseInfo<Int>> {
        return ResponseInfo.ok(mono { companyService.updateEnable(map["id"] as Int, map["enable"] as String) })
    }

    /**
     * @api {get} /company/overview 获取交易概述
     * @apiDescription  获取交易概述 day 是今天的交易概述，month 是这个月的交易概述
     * @apiName getOverview
     * @apiVersion 0.0.1
     * @apiParam {Integer} companyId 公司id
     * @apiSuccess {Long} buyStock=0 买入股数
     * @apiSuccess {Long} sellStock=0 卖出股数
     * @apiSuccess {Decimal} buyMoney=0 买入金额
     * @apiSuccess {Decimal} sellMoney=0 卖出金额
     * @apiSuccess {Decimal} avgBuyMoney=0 平均买价
     * @apiSuccess {Decimal} avgSellMoney=0 平均卖价
     * @apiSuccess {Long} netBuyStock=0 净买入股数
     * @apiSuccess {Decimal} netBuyMoney=0 净买入金额
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"day": {"buyStock": 0,"sellStock": 0,"buyMoney": 0,"sellMoney": 0,"avgBuyMoney": 0,
     * "avgSellMoney": 0,"netBuyStock": 0,"netBuyMoney": 0},"month": {"buyStock": 0,"sellStock": 0,"buyMoney": 0,"sellMoney": 0,
     * "avgBuyMoney": 0,"avgSellMoney": 0,"netBuyStock": 0,"netBuyMoney": 0}}}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/overview")
    fun getOverview(companyId: Int): Mono<ResponseInfo<Map<String, Overview>>> {
        return ResponseInfo.ok(mono { companyService.getOverview(companyId) })
    }

    /**
     * @api {get} /company/data 获取今日数据
     * @apiDescription  获取今日数据
     * @apiName getTodayData
     * @apiVersion 0.0.1
     * @apiSuccess (返回) {Long} tradesCapacity 今日交易量
     * @apiSuccess (返回) {Long} tradesVolume 今日交易金额
     * @apiSuccess (返回) {Integer} tradesNumber 今日开盘次数
     * @apiSuccess (返回) {Decimal} closingPrice 收盘价
     * @apiSuccess (返回) {Decimal} openingPrice 开盘价
     * @apiSuccess (返回) {Decimal} avgPrice 平均价
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiGroup Company
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @GetMapping("/data")
    fun getTodayData(): Mono<ResponseInfo<HashMap<String, Any>>> {
        return ResponseInfo.ok(mono {
            val data = HashMap<String, Any>()   // TODO 想办法为每个公司加缓存，可以考虑替换协程为Mono
            // TODO 使用一次查询获取所有指标
            data["tradesCapacity"] = tradeInfoService.countStockByTradeTimeAndCompanyId()   // 交易量
            data["tradesVolume"] = tradeInfoService.countMoneyByTradeTimeAndCompanyId()     // 交易金额
            data["tradesNumber"] = roomRecordService.countByStartTimeAndCompanyId()         // 开盘次数
            data["closingPrice"] = tradeInfoService.getTodayOpeningPriceByCompanyId()       // 收盘价
            data["openingPrice"] = tradeInfoService.getYesterdayClosingPriceByCompanyId()   // 开盘价
            data["avgPrice"] = tradeInfoService.getAvgPriceByCompanyId()                    // 平均价
            data
        })
    }

}