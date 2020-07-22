package com.mt.mtuser.controller

import com.mt.mtcommon.toLocalDateTime
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.service.*
import com.mt.mtuser.service.kline.KlineService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/7/8
 */
@RestController
@RequestMapping("/system")
class BackStageController {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var tradeInfoService: TradeInfoService

    @Autowired
    lateinit var roomRecordService: RoomRecordService

    @Autowired
    lateinit var roleService: RoleService

    @Autowired
    lateinit var klineService: KlineService

    /**
     * @api {get} /system/info 获取系统信息
     * @apiDescription  获取系统信息
     * @apiName getSystemInfo
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess (返回) {Integer} companyCount 公司数量
     * @apiSuccess (返回) {Integer} userCount 用户数量
     * @apiSuccess (返回) {Integer} activeCompany 今日活跃公司数
     * @apiSuccess (返回) {Long} tradesCapacity 今日交易量
     * @apiSuccess (返回) {Decimal} tradesVolume 今日交易金额
     * @apiSuccess (返回) {Decimal} totalVolume 总交易金额
     * @apiSuccess (返回) {Decimal} totalMoney 总交易金额
     * @apiSuccess (返回) {Integer} activeUser 参与交易总人数
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiHeaderExample {json} 请求头例子:
     *     {
     *       "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwicm9sZXMiOiJbW1wiU1VQRVJfQURNSU5cIixudWxsXSxbXCJVU
     *       0VSXCIsMV1dIiwibmJmIjoxNTg3NTU5NTQ0LCJleHAiOjE1ODk2MzMxNDR9.zyppWBmaF0l6ezljR1bTWUkAon50KF-VTrge1-W2hsM"
     *     }
     * @apiPermission superAdmin
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    //@Cacheable(cacheNames = ["getSystemInfo"])
    fun getSystemInfo(): Mono<ResponseInfo<MutableMap<String, Number>>> {
        return ResponseInfo.ok(mono {
            val data: MutableMap<String, Number> = HashMap()
            data["companyCount"] = companyService.count()
            data["userCount"] = userService.count()
            data["activeCompany"] = roomRecordService.countCompanyIdByStartTime() ?: 0// 今日活跃公司数
            data["tradesCapacity"] = tradeInfoService.countStockByTradeTime()       // 交易量
            data["tradesVolume"] = tradeInfoService.countMoneyByTradeTime()         // 交易金额
            data["totalVolume"] = tradeInfoService.countStock()                     // 总交易金额
            data["totalMoney"] = tradeInfoService.countMoney()                      // 总交易金额
            data["activeUser"] = tradeInfoService.countUserByTradeTime()            // 参与交易总人数
            data
        }.cache(Duration.ofMinutes(1)))
    }

    /**
     * @api {get} /system/top/amount 获取本月交易量
     * @apiDescription  获取本月交易量
     * @apiName getTradeAmountRank
     * @apiVersion 0.0.1
     * @apiParam {Integer} number top返回的个数
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess (返回) {Integer} amount 交易数量
     * @apiSuccess (返回) {Integer} companyId 公司id
     * @apiSuccess (返回) {String} name 公司名字
     * @apiSuccess (返回) {Long} openingNumber 开盘次数
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @GetMapping("/top/amount")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun getTradeAmountRank(number: Int): Mono<ResponseInfo<List<Map<String, Any?>>>> {
        return ResponseInfo.ok(tradeInfoService.getTradeAmountRank(number))
    }

    /**
     * @api {get} /system/top/money 获取本月交易金额
     * @apiDescription  获取本月交易金额
     * @apiName getTradeMoneyRank
     * @apiVersion 0.0.1
     * @apiParam {Integer} number top返回的个数
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess (返回) {Integer} money 交易金额
     * @apiSuccess (返回) {Integer} companyId 公司id
     * @apiSuccess (返回) {String} name 公司名字
     * @apiSuccess (返回) {Long} openingNumber 开盘次数
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @GetMapping("/top/money")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun getTradeMoneyRank(number: Int): Mono<ResponseInfo<List<Map<String, Any?>>>> {
        return ResponseInfo.ok(tradeInfoService.getTradeMoneyRank(number))
    }

    /**
     * @api {get} /system/week 获取最近7天交易统计
     * @apiDescription  获取最近7天交易统计
     * @apiName getWeekTraderInfo
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess (返回) {Integer} capacity 交易量
     * @apiSuccess (返回) {Decimal} volume 交易金额
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @GetMapping("/week")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun getWeekTraderInfo(): Mono<ResponseInfo<List<Map<String, Any?>>>> {
        return ResponseInfo.ok(klineService.getWeekTraderInfo())
    }

    /**
     * @api {get} /system/company/info 平台获取公司交易概述
     * @apiDescription  平台获取公司交易概述
     * @apiName getCompanyTraderInfo
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess (返回) {Integer} dayOpeningNumber 今天的开盘数
     * @apiSuccess (返回) {Integer} dayTradesCapacity 今天的交易量
     * @apiSuccess (返回) {Decimal} dayTradesVolume 今天的交易金额
     * @apiSuccess (返回) {Integer} activeUser 今日参与人数
     * @apiSuccess (返回) {Integer} stockholderNum 股东数量
     * @apiSuccess (返回) {Integer} reportCount 分析报告数
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @GetMapping("/company/info")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun getCompanyTraderInfo(companyId: Int) {
        val startTime = LocalTime.MIN.toLocalDateTime()
        val endTime = LocalDateTime.now()
        val roleId = roleService.roles!!.find { it.name == Stockholder.ANALYST }!!.id!!
        mono {
            mapOf("dayOpeningNumber" to roomRecordService.countByStartTimeAndCompanyId(startTime, companyId),
                    "dayTradesCapacity" to tradeInfoService.countStockByTradeTimeAndCompanyId(startTime, endTime, companyId),
                    "dayTradesVolume" to tradeInfoService.countMoneyByTradeTimeAndCompanyId(startTime, endTime, companyId),
                    "activeUser" to tradeInfoService.countUserByTradeTime(),
                    "stockholderNum" to roleService.countByCompanyIdAndRoleId(companyId, roleId),
                    "reportCount" to 0)
        }
    }

    /**
     * @api {get} /system/company/active 获取今日活跃公司top10
     * @apiDescription  获取今日活跃公司top10
     * @apiName getTopCompany
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess (返回) {Integer} companyId 公司id
     * @apiSuccess (返回) {Integer} openNumber 开盘次数
     * @apiSuccess (返回) {String} name 公司名字
     * @apiSuccess (返回) {Decimal} money 交易金额
     * @apiSuccess (返回) {Integer} amount 交易量
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @GetMapping("/company/active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun getTopCompany(): Mono<ResponseInfo<MutableList<Map<String, Any?>>>> {
        return ResponseInfo.ok(roomRecordService.countTopCompany())
    }
}