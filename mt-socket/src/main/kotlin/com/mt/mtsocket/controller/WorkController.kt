package com.mt.mtsocket.controller

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.WorkService
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
class WorkController {
    @Autowired
    private lateinit var workService: WorkService

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
     * @apiPermission none
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
     * @apiPermission none
     */
    @RequestMapping("/offer")
    fun offer(@RequestBody orderParam: OrderParam): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(workService.addOrder(orderParam))
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
     * @apiPermission none
     */
    @RequestMapping("/rival")
    fun addRival(@RequestBody rival: RivalInfo): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(workService.addRival(rival))
    }

    /**
     * @api {connect} /cancel 撤销订单
     * @apiDescription 报价
     * @apiName cancel
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission none
     */
    @RequestMapping("/cancel")
    fun cancel(): Mono<ResponseInfo<Boolean>> {
        return ResponseInfo.ok(workService.cancelOrder())
    }

    /**
     * @api {connect} /order 获取自己的全部报价
     * @apiDescription 获取自己的全部报价，不支持分页
     * @apiName getOrderRecord
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"data":{"code":0,"msg":"成功","data":null},"req":12}
     * @apiGroup Socket
     * @apiPermission none
     */
    @RequestMapping("/order")
    fun getOrderRecord(): Mono<ResponseInfo<List<OrderParam>>> {
        return ResponseInfo.ok(workService.getOrderRecord())
    }

}
