package com.mt.mtuser.controller

import com.mt.mtcommon.TradeInfo
import com.mt.mtuser.entity.BaseUser
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.logger
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.RoleService
import com.mt.mtuser.service.TradeInfoService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/5/10.
 */
@RestController
@RequestMapping("/trade")
class TradeInfoController {
    @Autowired
    private lateinit var tradeInfoService: TradeInfoService
    @Autowired
    lateinit var roleService: RoleService

    /**
     * @api {get} /trade 获取一个交易详情
     * @apiDescription  获取一个交易详情
     * @apiName findDetailsById
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 订单id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": null}
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping
    fun findDetailsById(id: Int): Mono<ResponseInfo<TradeInfo>> {
        return ResponseInfo.ok(mono { tradeInfoService.findDetailsById(id) })
    }

    /**
     * @api {get} /trade/order/{roomId} 查找指定房间的全部历史订单
     * @apiDescription  查找指定房间的全部历史订单
     * @apiName findOrderDetails
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParam {String} roomId 房间id
     * @apiParamExample {url} Request-Example:
     * /room/order/101?pageSize=2
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": []}
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/order/{roomId}")
    fun findOrder(@PathVariable roomId: String, query: PageQuery): Mono<ResponseInfo<PageView<TradeInfo>>> {
        return ResponseInfo.ok(mono { tradeInfoService.findOrder(roomId, query) })
    }

    /**
     * @api {get} /trade/order/company/{companyId} 查找指定公司的全部历史订单
     * @apiDescription  查找指定公司的全部历史订单
     * @apiName findOrderByCompany
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParam {Integer} companyId 公司id
     * @apiParam {Date} date 时间，格式yyyy-MM-dd：
     * @apiParamExample {url} Request-Example:
     * /room/order/101?pageSize=2
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": []}
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/order/company/{companyId}")
    fun findOrderByCompany(@PathVariable companyId: Int, @DateTimeFormat(pattern = "yyyy-M-d") date: LocalDate, query: PageQuery): Mono<ResponseInfo<PageView<TradeInfo>>> {
        return ResponseInfo.ok(mono { tradeInfoService.findOrderByCompany(companyId, date, query) })
    }

    /**
     * @api {get} /trade/order 查询指定用户的历史订单
     * @apiDescription 查询指定用户的历史订单
     * @apiName findOrderByUserId
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParam {String} [userId] 用户id，不传默认为获取自己的历史订单
     * @apiParam {String} [isBuy] 买卖方向,不传为所有方向，true：买；false：卖方向
     * @apiParam {String} date 日期，查询指定日期的数据，格式 yyyy-MM-dd
     * @apiParamExample {url} Request-Example:
     * /room/order?pageSize=2&date=2020-05-21&isBuy=
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 2,"total": 13,"item": [{"id": 1,"companyId": 1,"stockId": 1,
     * "roomId": "27","model": "D","buyerId": 5,"buyerName": null,"buyerPrice": 12.0,"sellerId": 7,"sellerName": null,
     * "sellerPrice": 15.0,"tradePrice": 13.5,"tradeAmount": 1000,"tradeMoney": 13500.0,"tradeTime": "2020-05-10T09:30:19.000+00:00",
     * "tradeState": "success","stateDetails": null},{"id": 4,"companyId": 1,"stockId": 1,"roomId": "27","model": "I","buyerId": 5,
     * "buyerName": null,"buyerPrice": 110.0,"sellerId": 10,"sellerName": null,"sellerPrice": 100.0,"tradePrice": 105.0,"tradeAmount": 100,
     * "tradeMoney": 10500.0,"tradeTime": "2020-05-16T08:37:00.213+00:00","tradeState": "success","stateDetails": null}]}}
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/order")
    fun findOrderByUserId(@RequestParam(required = false) userId: Int?, query: PageQuery,
                          @RequestParam(required = false) isBuy: Boolean? = null,
                          @DateTimeFormat(pattern = "yyyy-MM-dd")
                          @RequestParam date: LocalDate): Mono<ResponseInfo<PageView<TradeInfo>>> {
        logger.info(" {} {}", isBuy, date)
        return ResponseInfo.ok(BaseUser.getcurrentUser().flatMap {
            mono { tradeInfoService.findOrderByUserId(userId ?: it.id!!, query, isBuy, date) }
        })
    }

    /**
     * @api {get} /trade/statistics/department 按部门统计交易详情
     * @apiDescription  按部门统计交易详情
     * @apiName statisticsOrderByDepartment
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParam {Int} companyId 公司id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 30,"total": 5,"item": [{"tradesNumber": 3,"tradesCapacity":
     * 0,"tradesVolume": 0,"avgPrice": 0,"minPrice": 0,"maxPrice": 0,"name": "技术部"},{"tradesNumber": 1,"tradesCapacity":
     * 0,"tradesVolume": 0,"avgPrice": 0,"minPrice": 0,"maxPrice": 0,"name": "123"},{"tradesNumber": 1,"tradesCapacity":
     * 0,"tradesVolume": 0,"avgPrice": 0,"minPrice": 0,"maxPrice": 0,"name": "1234"}]}}
     * @apiSuccess (返回) {Long} tradesCapacity 交易量
     * @apiSuccess (返回) {Decimal} tradesVolume 交易金额
     * @apiSuccess (返回) {Long} tradesNumber 交易次数
     * @apiSuccess (返回) {Decimal} avgPrice 平均价格
     * @apiSuccess (返回) {Decimal} maxPrice 最高价
     * @apiSuccess (返回) {Decimal} minPrice 最低价
     * @apiSuccess (返回) {String} name 部门名
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping("/statistics/department")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('ANALYST')")
    fun statisticsOrderByDepartment(page: PageQuery, companyId: Int): Mono<ResponseInfo<PageView<Map<String, Any?>>>> {
        return ResponseInfo.ok(mono { tradeInfoService.statisticsOrderByDepartment(page, companyId) })
    }

    /**
     * @api {get} /trade/statistics 查找房间的每日交易概述
     * @apiDescription  查找房间的每日交易概述
     * @apiName statisticsOrderByDay
     * @apiVersion 0.0.1
     * @apiParam {Int} [companyId] 公司id
     * @apiUse PageQuery
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 2,"total": 7,"item": [{"id": 3,"stockId": 1,"time":
     * "2020-05-19 00:00:00","tradesCapacity": 400,"tradesVolume": 63250,"tradesNumber": 4,"avgPrice": 153.41875,
     * "maxPrice": 189.5,"minPrice": 112.5,"openPrice": 186.5,"closePrice": 144,"companyId": 1,"openNumber": 20},{"id":
     * 5,"stockId": 1,"time": "2020-05-21 00:00:00","tradesCapacity": 200,"tradesVolume": 38650,"tradesNumber": 2,
     * "avgPrice": 193.25,"maxPrice": 220,"minPrice": 166.5,"openPrice": 220,"closePrice": 166.5,"companyId": 1,"openNumber": 3}]}}
     * @apiUse Kline
     * @apiSuccess (返回) {Integer} openNumber 开盘次数
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('ANALYST')")
    fun statisticsOrderByDay(page: PageQuery, @RequestParam(required = false) companyId: Int?): Mono<ResponseInfo<PageView<Map<String, Any?>>>> {
        return ResponseInfo.ok(mono {
            val cId = companyId ?: roleService.getCompanyList(Stockholder.ADMIN)[0]
            tradeInfoService.statisticsOrderByDay(page, cId)
        })
    }

    /**
     * @api {get} /trade/excel 导出excel
     * @apiDescription  导出excel
     * @apiName outExcel
     * @apiVersion 0.0.1
     * @apiParam {Integer} companyId 公司id
     * @apiParam {String} data 日期；格式：yyyy-M-d
     * @apiSuccessExample {json} 成功返回:
     * 文件
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission admin
     * @apiPermission supperAdmin
     */
    @GetMapping("/excel")
    //@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    fun outExcel(companyId: Int, @DateTimeFormat(pattern = "yyyy-M-d") data: LocalDate, response: ServerHttpResponse): Mono<Void> {
        return tradeInfoService.outExcel(companyId, data, response)
    }
}