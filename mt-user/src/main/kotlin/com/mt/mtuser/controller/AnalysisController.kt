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
     * @apiDescription  添加分析报告
     * @apiName addAnalysis
     * @apiVersion 0.0.1
     * @apiUse tokenMsg
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
     * @apiDescription  获取分析报告
     * @apiName findAllAnalysis
     * @apiVersion 0.0.1
     * @apiParam {Integer} userId 分析员id
     * @apiUse PageQuery
     * @apiUse tokenMsg
     * @apiSuccessExample {json} 成功返回:
     * {"code":0,"msg":"成功","data":null}
     * @apiGroup Analysis
     * @apiPermission analyst
     */
    @GetMapping
    @PreAuthorize("hasRole('ANALYST')")
    fun findAllAnalysis(query: Mono<PageQuery>, userId : Int): Mono<ResponseInfo<PageView<Analysis>>> {
        return ResponseInfo.ok(mono { analysisService.findAllAnalysis(query.awaitSingle(), userId) })
    }

}