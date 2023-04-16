package com.mt.mtuser.controller

import com.mt.mtcommon.TradeInfo
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.room.*
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * Created by gyh on 2020/3/25.
 */
@RestController
@RequestMapping("/room")
class RoomController {
    @Autowired
    lateinit var roomService: RoomService

    /**
     * @api {post} /room/click 创建一个点选撮合的房间
     * @apiDescription  创建一个点选撮合的房间
     * @apiName createClickRoom
     * @apiVersion 0.0.1
     * @apiUse ClickMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/click")
    @PreAuthorize("hasRole('ADMIN')")
    // @Valid 和 @Validated 注解均不起作用，不知道为什么 也许现在可以了
    suspend fun createClickRoom(@RequestBody clickRoom: Mono<ClickMatch>): ResponseInfo<ClickMatch> {
        return ResponseInfo.ok(roomService.createRoom(clickRoom.awaitSingle()))
    }

    /**
     * @api {post} /room/bicker 创建一个抬杠撮合的房间
     * @apiDescription  创建一个点抬杠撮合的房间
     * @apiName createBickerRoom
     * @apiVersion 0.0.1
     * @apiUse BickerMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/bicker")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun createBickerRoom(@RequestBody bickerRoom: Mono<BickerMatch>): ResponseInfo<BickerMatch> {
        return ResponseInfo.ok(roomService.createRoom(bickerRoom.awaitSingle()))
    }

    /**
     * @api {post} /room/double 创建一个两两撮合的房间
     * @apiDescription  创建一个两两撮合的房间
     * @apiName createDoubleRoom
     * @apiVersion 0.0.1
     * @apiUse DoubleMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/double")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun createDoubleRoom(@RequestBody doubleRoom: Mono<DoubleMatch>): ResponseInfo<DoubleMatch> {
        return ResponseInfo.ok(roomService.createRoom(doubleRoom.awaitSingle()))
    }

    /**
     * @api {post} /room/continue 创建一个连续撮合的房间
     * @apiDescription  创建一个连续撮合的房间
     * @apiName createTimelyMatch
     * @apiVersion 0.0.1
     * @apiUse TimelyMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/continue")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun createTimelyMatch(@RequestBody continueRoom: Mono<ContinueMatch>): ResponseInfo<ContinueMatch> {
        return ResponseInfo.ok(roomService.createRoom(continueRoom.awaitSingle()))
    }

    /**
     * @api {post} /room/timing 创建一个定时撮合的房间
     * @apiDescription  创建一个定时撮合的房间
     * @apiName createTimingMatch
     * @apiVersion 0.0.1
     * @apiUse TimingMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/timing")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun createTimingMatch(@RequestBody timingRoom: Mono<TimingMatch>): ResponseInfo<TimingMatch> {
        return ResponseInfo.ok(roomService.createRoom(timingRoom.awaitSingle()))
    }

    /**
     * @api {put} /room/click 更新一个点选撮合的房间
     * @apiDescription  更新一个点选撮合的房间
     * @apiName updateClickRoom
     * @apiVersion 0.0.1
     * @apiUse ClickMatch
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/click")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateClickRoom(@RequestBody clickRoom: Mono<ClickMatch>): ResponseInfo<ClickMatch> {
        val room = clickRoom.awaitSingle()
        return ResponseInfo.ok(roomService.updateRoomByRoomId(room, room.oldFlag ?: room.flag))
    }

    /**
     * @api {put} /room/bicker 更新一个抬杠撮合的房间
     * @apiDescription  更新一个抬杠撮合的房间
     * @apiName updateBickerRoom
     * @apiVersion 0.0.1
     * @apiUse BickerMatch
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/bicker")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateBickerRoom(@RequestBody bickerRoom: Mono<BickerMatch>): ResponseInfo<BickerMatch> {
        val room = bickerRoom.awaitSingle()
        return ResponseInfo.ok(roomService.updateRoomByRoomId(room, room.oldFlag ?: room.flag))
    }

    /**
     * @api {put} /room/double 更新一个两两撮合的房间
     * @apiDescription  更新一个两两撮合的房间
     * @apiName updateDoubleRoom
     * @apiVersion 0.0.1
     * @apiUse DoubleMatch
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/double")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateDoubleRoom(@RequestBody doubleRoom: Mono<DoubleMatch>): ResponseInfo<DoubleMatch> {
        val room = doubleRoom.awaitSingle()
        return ResponseInfo.ok(roomService.updateRoomByRoomId(room, room.oldFlag ?: room.flag))
    }

    /**
     * @api {put} /room/continue 更新一个连续撮合的房间
     * @apiDescription  更新一个连续撮合的房间
     * @apiName updateTimelyMatch
     * @apiVersion 0.0.1
     * @apiUse TimelyMatch
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/continue")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateTimelyMatch(@RequestBody continueRoom: Mono<ContinueMatch>): ResponseInfo<ContinueMatch> {
        val room = continueRoom.awaitSingle()
        return ResponseInfo.ok(roomService.updateRoomByRoomId(room, room.oldFlag ?: room.flag))
    }

    /**
     * @api {put} /room/timing 更新一个定时撮合的房间
     * @apiDescription  更新一个定时撮合的房间
     * @apiName updateTimingMatch
     * @apiVersion 0.0.1
     * @apiUse TimingMatch
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/timing")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateTimingMatch(@RequestBody timingRoom: Mono<TimingMatch>): ResponseInfo<TimingMatch> {
        val room = timingRoom.awaitSingle()
        return ResponseInfo.ok(roomService.updateRoomByRoomId(room, room.oldFlag ?: room.flag))
    }

    /**
     * @api {get} /room/editable 获取全部可编辑的房间
     * @apiDescription  获取全部可编辑的房间，就是自己管理的房间
     * @apiName getEditableRoomList
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":[]}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping("/editable")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getEditableRoomList(): ResponseInfo<LinkedList<BaseRoom>> {
        return ResponseInfo.ok(roomService.getEditableRoomList())
    }

    //--------------------------------不需要管理员权限-------------------------------------

    /**
     * @api {get} /room/scope 获取指定房间报价范围
     * @apiDescription  获取指定房间报价范围
     * @apiName getRoomScope
     * @apiVersion 0.0.1
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":[]}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/scope")
    suspend fun getRoomScope(roomId: String): ResponseInfo<Map<String, String>> {
        return ResponseInfo.ok(roomService.getRoomScope(roomId))
    }

    /**
     * @api {get} /room 获取全部的房间
     * @apiDescription  获取全部的房间，就是自己能加入的房间
     * @apiName getAllRoomList
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": [{"roomId": "28","companyId": 1,"stockId": 1,"name": "6105","people": 0,"quoteTime":
     * "14:05:00","secondStage": "00:05:00","time": "14:10:00","startTime": "09:30:00","numberTrades": 100,"lowScope": 0.1,
     * "highScope": 10.0,"enable": "1","createTime": "2020-04-29 15:03:20","rival": 5,"flag": "C","oldFlag": null,"id": "28",
     * "new": true,"delayEndTIme": "23:40:59"},{"roomId": "27","companyId": 1,"stockId": 1,"name": "6105","people": 0,"time":
     * "07:55:00","startTime": "09:00:00","numberTrades": 100,"lowScope": 14.0,"highScope": 100.0,"enable": "0","createTime":
     * "2020-05-16 11:10:26","flag": "I","oldFlag": null,"id": "27","new": true,"delayEndTIme": "16:55:59"}]}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping
    suspend fun getAllRoomList(): ResponseInfo<LinkedList<BaseRoom>> {
        return ResponseInfo.ok(roomService.getAllRoomList())
    }

    /**
     * @api {get} /room/homepage 获取房间的主页数据
     * @apiDescription 获取房间的成交最高价和最低价， 最新收盘价，相对昨日收盘价的涨跌幅和开盘次数
     * @apiName getHomepageData
     * @apiVersion 0.0.1
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"closePrice": 0,"tradesNumber": 199,"difference": 0,"minPrice": 0,"maxPrice": 0}}
     * @apiSuccess (返回) {Decimal} closePrice 收盘价
     * @apiSuccess (返回) {Long} tradesNumber 开盘次数
     * @apiSuccess (返回) {Decimal} difference 涨跌幅（用收盘价 - 昨日收盘价 可能为负数）
     * @apiSuccess (返回) {Decimal} minPrice 最低价
     * @apiSuccess (返回) {Decimal} maxPrice 最高价
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/homepage")
    suspend fun getHomepageData(roomId: String): ResponseInfo<Map<String, Any>> {
        return ResponseInfo.ok(roomService.getHomepageData(roomId))
    }

    /**
     * @api {get} /room/order 查找指定房间最近一次开放的历史订单
     * @apiDescription  查找指定房间的历史订单,只能查询房间最近一次开放的交易记录
     * @apiName findOrder
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParam {String} roomId 房间id
     * @apiParamExample {url} Request-Example:
     * /room/order?roomId=1&pageSize=2
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": []}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/order")
    suspend fun findOrder(roomId: String, query: PageQuery): ResponseInfo<PageView<TradeInfo>> {
        return ResponseInfo.ok(roomService.findOrder(roomId, query))
    }

}
