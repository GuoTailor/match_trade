package com.mt.mtsocket.controller

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.TradeInfo
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.PeekSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/5/19.
 */
@Controller
class PeekSocketController {
    @Autowired
    private lateinit var peekService: PeekSocketService

    /**
     * @api {connect} /peek/tradeInfo 获取全部历史订单
     * @apiDescription  获取全部历史订单
     * @apiName getTradeInfo
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":{}},"req":12}
     * @apiGroup Socket
     * @apiPermission admin
     */
    @RequestMapping("/peek/tradeInfo")
    fun getTradeInfo(): Mono<ResponseInfo<MutableList<TradeInfo>>> {
        return ResponseInfo.ok(peekService.getTradeInfo())
    }

    /**
     * @api {connect} /peek/order 获取全部报价
     * @apiDescription  获取全部报价
     * @apiName getOrder
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":{}},"req":12}
     * @apiGroup Socket
     * @apiPermission admin
     */
    @RequestMapping("/peek/order")
    fun getOrder(): Mono<ResponseInfo<MutableList<OrderParam>>> {
        return ResponseInfo.ok(peekService.getOrder())
    }
}