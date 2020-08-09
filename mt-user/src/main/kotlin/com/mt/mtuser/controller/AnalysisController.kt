package com.mt.mtuser.controller

import com.mt.mtuser.entity.Analysis
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.AnalysisService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/7/19
 */
@RestController
@RequestMapping("/analysis")
class AnalysisController {

    @Autowired
    lateinit var analysisService: AnalysisService

    /**
     * @api {post} /analysis 添加分析报告
     * @apiDescription 添加分析报告
     * @apiName addAnalysis
     * @apiVersion 0.0.1
     * @apiUse Analysis
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @PostMapping
    @PreAuthorize("hasRole('ANALYST')")
    fun addAnalysis(@RequestBody analysis: Mono<Analysis>): Mono<ResponseInfo<Analysis>> {
        return ResponseInfo.ok(mono { analysisService.addAnalysis(analysis.awaitSingle()) })
    }

    /**
     * @api {get} /analysis 获取分析报告
     * @apiDescription 获取分析报告
     * @apiName findAllAnalysis
     * @apiVersion 0.0.1
     * @apiParam {Integer} [userId] 分析员id
     * @apiUse PageQuery
     * @apiUse tokenMsg
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @GetMapping
    @PreAuthorize("hasRole('ANALYST') or hasRole('SUPER_ADMIN')")
    fun findAllAnalysis(query: Mono<PageQuery>, @RequestParam(required = false) userId: Int?): Mono<ResponseInfo<PageView<Analysis>>> {
        return ResponseInfo.ok(mono { analysisService.findAllAnalysis(query.awaitSingle(), userId) })
    }

    /**
     * @api {get} /analysis/company 公司获取分析报告
     * @apiDescription 公司获取分析报告
     * @apiName getAllAnalysisByCompany
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiUse tokenMsg
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @GetMapping("/company")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    fun getAllAnalysisByCompany(query: Mono<PageQuery>): Mono<ResponseInfo<PageView<Analysis>>> {
        return ResponseInfo.ok(mono { analysisService.getAllAnalysisByCompany(query.awaitSingle()) })
    }

    /**
     * @api {get} /analysis/{id} 获取指定分析报告
     * @apiDescription 获取指定分析报告
     * @apiName findAnalysisById
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 分析报告id
     * @apiUse tokenMsg
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    fun findAnalysisById(@PathVariable id: Int): Mono<ResponseInfo<Analysis>> {
        return ResponseInfo.ok(mono { analysisService.findAnalysisById(id) })
    }

    /**
     * @api {get} /analysis/unread 获取未读分析报告数量
     * @apiDescription 获取未读分析报告数量
     * @apiName findUnreadCount
     * @apiVersion 0.0.1
     * @apiUse tokenMsg
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":1}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    fun findUnreadCount(): Mono<ResponseInfo<Long>> {
        return ResponseInfo.ok(mono { analysisService.findUnreadCount() })
    }

    /**
     * @api {put} /analysis 修改分析报告
     * @apiDescription 修改分析报告
     * @apiName updateAnalysis
     * @apiVersion 0.0.1
     * @apiUse Analysis
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @PutMapping
    @PreAuthorize("hasRole('ANALYST') or hasRole('SUPER_ADMIN')")
    fun updateAnalysis(@RequestBody analysis: Mono<Analysis>): Mono<ResponseInfo<Int>> {
        return ResponseInfo.ok(mono { analysisService.updateAnalysis(analysis.awaitSingle()) })
    }

    /**
     * @api {delete} /analysis 删除分析报告
     * @apiDescription 删除分析报告
     * @apiName deleteAnalysis
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 分析报告id
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ANALYST') or hasRole('SUPER_ADMIN')")
    fun deleteAnalysis(@RequestParam id: Int): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { analysisService.deleteAnalysis(id) })
    }

}