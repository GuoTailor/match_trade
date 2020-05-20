package com.mt.mtsocket.controller

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo
import com.mt.mtcommon.TopThree
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.RoomSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/17.
 */
@Controller
class RoomSocketController {
    @Autowired
    private lateinit var roomSocketService: RoomSocketService

    /**
     * @api {connect} /echo 测试接口
     * @apiDescription  测试接口，该接口的[value]字段传什么就放回什么
     * @apiName echo
     * @apiParam {String} value 任意字符
     * @apiVersion 0.0.1
     * @apiParamExample {json} 请求-例子:
     * {"order":"/echo", "data": {"value": "123"}, "req":12}
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":{"value":"123"}},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/echo")
    fun echo(@RequestParam value: String): Mono<ResponseInfo<Map<String, String>>> {
        return ResponseInfo.ok(Mono.just(mapOf("value" to value)))
    }

    /**
     * @api {connect} /offer 报价
     * @apiDescription 报价
     * @apiName offer
     * @apiVersion 0.0.1
     * @apiUse OrderParam
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/offer")
    fun offer(@RequestBody orderParam: OrderParam): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(roomSocketService.addOrder(orderParam))
    }

    /**
     * @api {connect} /rival 选择对手
     * @apiDescription 报价
     * @apiName addRival
     * @apiVersion 0.0.1
     * @apiUse RivalInfo
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/rival")
    fun addRival(@RequestBody rival: RivalInfo): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(roomSocketService.addRival(rival))
    }

    /**
     * @api {connect} /cancel 撤销订单
     * @apiDescription 报价
     * @apiName cancel
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/cancel")
    fun cancel(): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(roomSocketService.cancelOrder())
    }

    /**
     * @api {connect} /order 获取自己的全部报价
     * @apiDescription 获取自己的全部报价，不支持分页
     * @apiName getOrderRecord
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/order")
    fun getOrderRecord(): Mono<ResponseInfo<List<OrderParam>>> {
        return ResponseInfo.ok(roomSocketService.getOrderRecord())
    }

    /**
     * @api {connect} /getRival 获取自己选择的对手
     * @apiDescription 获取自己选择的对手，不支持分页
     * @apiName getRival
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/getRival")
    fun getRival(): Mono<ResponseInfo<RivalInfo>> {
        return ResponseInfo.ok(roomSocketService.getRival())
    }

    /**
     * @api {connect} /getAllRival 获取对手
     * @apiDescription 获取对手，不支持分页
     * @apiName getAllRival
     * @apiParam {Boolean} isBuy true：获取买家；false：获取卖家
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/getAllRival")
    fun getAllRival(@RequestParam isBuy: Boolean): Mono<ResponseInfo<List<OrderParam>>> {
        return ResponseInfo.ok(roomSocketService.getAllRival(isBuy))
    }

    /**
     * @api {connect} /getTopThree 获取自己房间的报价前三档
     * @apiDescription 获取自己房间的报价前三档
     * @apiName getTopThree
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/getTopThree")
    fun getTopThree(): Mono<ResponseInfo<TopThree>> {
        return ResponseInfo.ok(roomSocketService.getTopThree())
    }

    /**
     * @api {connect} /number 获取自己当前房间的在线人数
     * @apiDescription 获取自己当前房间的在线人数
     * @apiName getRoomNumber
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":1},"req":12}
     * @apiGroup Socket
     * @apiPermission user
     */
    @RequestMapping("/number")
    fun getRoomNumber(): Mono<ResponseInfo<Int>> {
        return ResponseInfo.ok(roomSocketService.getOnLineSize())
    }

}
