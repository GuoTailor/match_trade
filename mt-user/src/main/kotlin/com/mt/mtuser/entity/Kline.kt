package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * @apiDefine Kline
 * @apiSuccess (返回) {Long} id
 * @apiSuccess (返回) {Int} stockId 股票id
 * @apiSuccess (返回) {Int} companyId 公司id
 * @apiSuccess (返回) {Date} time 值的生成时间
 * @apiSuccess (返回) {Long} tradesCapacity 交易量
 * @apiSuccess (返回) {Decimal} tradesVolume 交易金额
 * @apiSuccess (返回) {Long} tradesNumber 交易次数
 * @apiSuccess (返回) {Decimal} avgPrice 平均价格
 * @apiSuccess (返回) {Decimal} maxPrice 最高价
 * @apiSuccess (返回) {Decimal} minPrice 最低价
 * @apiSuccess (返回) {Decimal} openPrice 开盘价
 * @apiSuccess (返回) {Decimal} closePrice 收盘价
 */
class Kline(
        @Id
        var id: Long? = null,
        /*** 股票id*/
        var stockId: Int? = null,
        /** 公司id */
        var companyId: Int? = null,
        /*** 值的生成时间*/
        var time: LocalDateTime? = null,
        /*** 交易量*/
        var tradesCapacity: Long? = null,
        /*** 交易金额*/
        var tradesVolume: BigDecimal? = null,
        /*** 交易次数*/
        var tradesNumber: Long? = null,
        /*** 平均价格*/
        var avgPrice: BigDecimal? = null,
        /*** 最高价*/
        var maxPrice: BigDecimal? = null,
        /*** 最低价*/
        var minPrice: BigDecimal? = null,
        /*** 开盘价*/
        var openPrice: BigDecimal? = null,
        /*** 收盘价*/
        var closePrice: BigDecimal? = null
) {
    fun isEmpty(): Boolean {
        return (tradesCapacity == null || tradesCapacity == 0L)
                && (tradesVolume == null || tradesVolume == BigDecimal.ZERO)
                && (tradesNumber == null || tradesNumber == 0L)
                && (avgPrice == null || avgPrice == BigDecimal.ZERO)
                && (maxPrice == null || maxPrice == BigDecimal.ZERO)
                && (minPrice == null || minPrice == BigDecimal.ZERO)
    }
}