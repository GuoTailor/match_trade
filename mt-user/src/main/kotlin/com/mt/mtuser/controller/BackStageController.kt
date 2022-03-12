package com.mt.mtuser.controller

import com.mt.mtcommon.firstDay
import com.mt.mtcommon.lastDay
import com.mt.mtcommon.toLocalDateTime
import com.mt.mtuser.entity.AppUpdate
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.*
import com.mt.mtuser.service.kline.KlineService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
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

    @Autowired
    lateinit var analysisService: AnalysisService

    @Autowired
    lateinit var appUpdateService: AppUpdateService

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
     * @apiParam {Integer} companyId 公司id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":[]}
     * @apiSuccess (返回) {Integer} dayOpeningNumber 今天的开盘数
     * @apiSuccess (返回) {Integer} dayTradesCapacity 今天的交易量
     * @apiSuccess (返回) {Integer} monthTradesCapacity 本月的交易量
     * @apiSuccess (返回) {Decimal} dayTradesVolume 今天的交易金额
     * @apiSuccess (返回) {Decimal} monthTradesVolume 本月的交易金额
     * @apiSuccess (返回) {Integer} activeUser 今日参与人数
     * @apiSuccess (返回) {Integer} stockholderNum 股东数量
     * @apiSuccess (返回) {Integer} reportCount 分析报告数
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @GetMapping("/company/info")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ANALYST')")
    fun getCompanyTraderInfo(companyId: Int): Mono<ResponseInfo<Map<String, Any>>> {
        val startTime = LocalTime.MIN.toLocalDateTime()
        val endTime = LocalDateTime.now()
        return ResponseInfo.ok(mono {
            val roleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
            mapOf("dayOpeningNumber" to roomRecordService.countByStartTimeAndCompanyId(startTime, companyId),
                    "dayTradesCapacity" to tradeInfoService.countStockByTradeTimeAndCompanyId(startTime, endTime, companyId),
                    "monthTradesCapacity" to tradeInfoService.countStockByTradeTimeAndCompanyId(firstDay(), lastDay(), companyId),
                    "dayTradesVolume" to tradeInfoService.countMoneyByTradeTimeAndCompanyId(startTime, endTime, companyId),
                    "monthTradesVolume" to tradeInfoService.countMoneyByTradeTimeAndCompanyId(firstDay(), lastDay(), companyId),
                    "activeUser" to tradeInfoService.countUserByTradeTime(),
                    "stockholderNum" to roleService.countByCompanyIdAndRoleId(companyId, roleId),
                    "reportCount" to analysisService.countByCompanyId(companyId))
        })
    }

    /**
     * @api {get} /system/company/active 获取活跃公司top
     * @apiDescription  获取活跃公司top
     * @apiName getTopCompany
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParam {String} [type] 时间类型：day:代表获取天的活跃公司，month:代表月的活跃公司，默认为天
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
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ANALYST')")
    fun getTopCompany(query: PageQuery, @RequestParam(required = false) type: String?): Mono<ResponseInfo<List<Map<String, Any?>>>> {
        return ResponseInfo.ok(mono { roomRecordService.countTopCompany(query, type ?: "day") })
    }

    /**
     * @api {post} /system/wgt 上传wgt文件
     * @apiDescription  上传wgt文件
     * @apiName uploadWgt
     * @apiParam {File} file wgt文件
     * @apiUse AppUpdate
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": true}
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @PostMapping("/wgt")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun uploadWgt(@RequestPart("file") filePart: FilePart, appUpdate: AppUpdate): Mono<ResponseInfo<AppUpdate>> {
        return ResponseInfo.ok(appUpdateService.uploadWgt(filePart, appUpdate))
    }

    /**
     * @api {get} /system/wgt 获取所有更新文件
     * @apiDescription  获取所有更新文件
     * @apiName findAllWgt
     * @apiUse PageQuery
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": true}
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @GetMapping("/wgt")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun findAllWgt(query: PageQuery): Mono<ResponseInfo<PageView<AppUpdate>>> {
        return ResponseInfo.ok(mono { appUpdateService.findAll(query) })
    }

    /**
     * @api {delete} /system/wgt 删除指定更新文件
     * @apiDescription  删除指定更新文件
     * @apiName deleteWgt
     * @apiParam {String} id 更新日志id
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": true}
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @DeleteMapping("/wgt")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun deleteWgt(@RequestParam id: Int): Mono<ResponseInfo<Void>> {
        return ResponseInfo.ok(appUpdateService.deleteById(id))
    }

    /**
     * @api {put} /system/wgt 修改更新日志
     * @apiDescription  修改更新日志，不支持修改更新文件
     * @apiName updateWgt
     * @apiUse AppUpdate
     * @apiParam {String} id 更新日志id
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": true}
     * @apiGroup BackStage
     * @apiUse tokenMsg
     * @apiPermission superAdmin
     */
    @PutMapping("/wgt")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun updateWgt(appUpdate: AppUpdate): Mono<ResponseInfo<AppUpdate>> {
        return ResponseInfo.ok(appUpdateService.update(appUpdate))
    }
}