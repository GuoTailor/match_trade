package com.mt.mtuser.controller

import com.mt.mtcommon.TradeInfo
import com.mt.mtuser.entity.BaseUser
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.TradeInfoService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/5/10.
 */
@RestController
@RequestMapping("/trade")
class TradeInfoController {
    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

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
     * @api {gut} /trade/order{roomId} 查找指定房间的全部历史订单
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
     * @api {gut} /trade/order 查询指定用户的历史订单
     * @apiDescription 查询指定用户的历史订单
     * @apiName findOrderByUserId
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiParam {String} [userId] 用户id，不传默认为获取自己的历史订单
     * @apiParamExample {url} Request-Example:
     * /room/order?pageSize=2
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": []}
     * @apiGroup Trade
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/order")
    fun findOrderByUserId(@RequestParam(required = false) userId: Int?, query: PageQuery): Mono<ResponseInfo<PageView<TradeInfo>>> {
        return ResponseInfo.ok(BaseUser.getcurrentUser().flatMap {
            mono { tradeInfoService.findOrderByUserId(userId ?: it.id!!, query) }
        })
    }

}