package com.mt.mtuser.controller

import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stock
import com.mt.mtuser.service.StockService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by gyh on 2020/3/22.
 */
@RestController
@RequestMapping("/stock")
class StockController {

    @Autowired
    private lateinit var stockService: StockService

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getStock(@PathVariable id: Int): ResponseInfo<Stock?> {
        return ResponseInfo(0, "成功", stockService.findById(id))
    }
}