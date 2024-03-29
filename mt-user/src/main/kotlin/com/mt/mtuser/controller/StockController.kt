package com.mt.mtuser.controller

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.Kline
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stock
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.StockService
import com.mt.mtuser.service.kline.KlineService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Created by gyh on 2020/3/22.
 */
@RestController
@RequestMapping("/stock")
class StockController {

    @Autowired
    private lateinit var stockService: StockService

    @Autowired
    private lateinit var klineService: KlineService

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
    suspend fun getStock(@PathVariable id: Int): ResponseInfo<Stock?> {
        return ResponseInfo.ok(stockService.findById(id))
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
    suspend fun getCompanyStock(@PathVariable id: Int, query: PageQuery): ResponseInfo<PageView<Stock>> {
        return ResponseInfo.ok(stockService.findAllByQuery(query, id))
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
    suspend fun addStock(@RequestBody stock: Stock): ResponseInfo<Stock> {
        stock.id = null
        return ResponseInfo.ok(stockService.save(stock))
    }

    /**
     * @api {put} /stock 修改公司的一支股票
     * @apiDescription  修改公司的一支股票
     * @apiName updateStock
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
    suspend fun updateStock(@RequestBody stock: Stock): ResponseInfo<Stock> {
        return if (!Util.isEmpty(stock)) {
            stock.id = null
            ResponseInfo(0, "成功", stockService.save(stock))
        } else {
            ResponseInfo(1, "请填写id属性")
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
    suspend fun deleteStock(@PathVariable id: Int): ResponseInfo<Unit> {
        return ResponseInfo.ok(stockService.deleteById(id))
    }

    /**
     * @api {get} /stock/kline 获取一只股票的k线
     * @apiDescription  获取一只股票的k线
     * @apiName findKline
     * @apiVersion 0.0.1
     * @apiParam {String} timeline 取值[1m:一分钟的k线；15m:十五分钟的k线；1h:一小时的k线；4h:四小时的k线；1d:日k]
     * @apiParam {String} roomId 房间id
     * @apiParam {String} mode 房间类型
     * @apiUse PageQuery
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 2,"total": 43,"item": [{"id": 395,"stockId": 1,
     * "companyId": 1,"time": "2020-05-13T11:23:00.000+00:00","tradesCapacity": 100,"tradesVolume": 25000.0,"tradesNumber"
     * : 1,"avgPrice": 250.0,"maxPrice": 250.0,"minPrice": 250.0,"openPrice": 250.0,"closePrice": 250.0,"empty": false}
     * ,{"id": 396,"stockId": 1,"companyId": 1,"time": "2020-05-14T05:11:00.000+00:00","tradesCapacity": 100,"tradesVolume":
     * 15250.0,"tradesNumber": 1,"avgPrice": 152.5,"maxPrice": 152.5,"minPrice": 152.5,"openPrice": 152.5,"closePrice":
     * 152.5,"empty": false}]}}
     * @apiUse Kline
     * @apiGroup Stock
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("kline")
    suspend fun findKline(
        @RequestParam roomId: String,
        @RequestParam mode: String,
        @RequestParam timeline: String,
        page: PageQuery
    ): ResponseInfo<PageView<Kline>> {
        return ResponseInfo.ok(klineService.findKline(roomId, mode, timeline, page))
    }

    /**
     * @api {get} /stock/kline/company 获取一只股票的k线通过公司id
     * @apiDescription  获取一只股票的k线通过公司id，获取公司的第一支股票
     * @apiName findKlineByCompanyId
     * @apiVersion 0.0.1
     * @apiParam {String} timeline 取值[1m:一分钟的k线；15m:十五分钟的k线；1h:一小时的k线；4h:四小时的k线；1d:日k]
     * @apiParam {Integer} companyId 房间id
     * @apiUse PageQuery
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 2,"total": 43,"item": [{"id": 395,"stockId": 1,
     * "companyId": 1,"time": "2020-05-13T11:23:00.000+00:00","tradesCapacity": 100,"tradesVolume": 25000.0,"tradesNumber"
     * : 1,"avgPrice": 250.0,"maxPrice": 250.0,"minPrice": 250.0,"openPrice": 250.0,"closePrice": 250.0,"empty": false}
     * ,{"id": 396,"stockId": 1,"companyId": 1,"time": "2020-05-14T05:11:00.000+00:00","tradesCapacity": 100,"tradesVolume":
     * 15250.0,"tradesNumber": 1,"avgPrice": 152.5,"maxPrice": 152.5,"minPrice": 152.5,"openPrice": 152.5,"closePrice":
     * 152.5,"empty": false}]}}
     * @apiUse Kline
     * @apiGroup Stock
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("kline/company")
    suspend fun findKlineByCompanyId(
        @RequestParam companyId: Int,
        @RequestParam timeline: String,
        page: PageQuery
    ): ResponseInfo<PageView<Kline>> {
        return ResponseInfo.ok(klineService.findKlineByCompanyId(companyId, timeline, page))
    }

}