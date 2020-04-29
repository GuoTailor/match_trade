package com.mt.mtuser.controller

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.Company
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stock
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.StockService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Created by gyh on 2020/3/22.
 */
@RestController
@RequestMapping("/stock")
class StockController {

    @Autowired
    private lateinit var stockService: StockService

    /**
     * @api {post} /stock/{id} 获取一支股票的详情
     * @apiDescription  获取股票的详情
     * @apiName getStock
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 公司id
     * @apiParamExample {url} 请求-例子:
     * /stock/1
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":1,"data":null}
     * @apiGroup Stock
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/{id}")
    fun getStock(@PathVariable id: Int): Mono<ResponseInfo<Stock?>> {
        return ResponseInfo.ok<Stock?>(mono { stockService.findById(id) })
    }

    /**
     * @api {get} /stock/company/{id} 获取公司所有股票信息
     * @apiDescription  获取公司所有的股票信息
     * @apiName getCompanyStock
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 公司id
     * @apiUse PageQuery
     * @apiParamExample {url} 请求-例子:
     * /stock/company/1?pageSize=10&pageNum=1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 10,"total": 1,"item": [{"id": 1,"name": "6105",
     * "roomCount": 1,"mode": "4","createTime": "2020-03-18T07:35:45.000+0000"}]}}
     * @apiGroup Stock
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/company/{id}")
    fun getCompanyStock(@PathVariable id: Int, @RequestBody query: PageQuery): Mono<ResponseInfo<PageView<Stock>>> {
        return ResponseInfo.ok(mono { stockService.findAllByQuery(query) })
    }

    /**
     * @api {post} /stock 为公司添加一支股票
     * @apiDescription  为公司添加一支股票
     * @apiName addStock
     * @apiVersion 0.0.1
     * @apiUse Stock
     * @apiParamExample {json} 请求-例子:
     * {"companyId":1,"name":"nmka"}
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Stock
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun addStock(@RequestBody stock: Stock): Mono<ResponseInfo<Stock>> {
        stock.id = null
        return ResponseInfo.ok(mono { stockService.save(stock) })
    }

    /**
     * @api {put} /stock 修改公司的一支股票
     * @apiDescription  修改公司的一支股票
     * @apiName addStock
     * @apiVersion 0.0.1
     * @apiUse Stock
     * @apiParamExample {json} 请求-例子:
     * {"id":1,"companyId":1,"name":"nmka"}
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": null}
     * @apiGroup Stock
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun updateStock(@RequestBody stock: Stock): Mono<ResponseInfo<Stock>> {
        return mono {
            if (!Util.isEmpty(stock)) {
                stock.id = null
                ResponseInfo(0, "成功", stockService.save(stock))
            } else {
                ResponseInfo<Stock>(1, "请填写id属性")
            }
        }
    }

    /**
     * @api {delete} /stock/{id} 删除公司的一支股票
     * @apiDescription  删除公司的一支股票
     * @apiName deleteStock
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 股票id
     * @apiParamExample {url} 请求-例子:
     * /stock/1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": null}
     * @apiGroup Stock
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    fun deleteStock(@PathVariable id: Int): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { stockService.deleteById(id) })
    }


}