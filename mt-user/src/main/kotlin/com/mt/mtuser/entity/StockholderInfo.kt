package com.mt.mtuser.entity

import java.math.BigDecimal

/**
 * Created by gyh on 2020/4/23.
 * @apiDefine StockholderInfo
 * @apiParam {Integer} id 股东id
 * @apiParam {Integer} companyId 股票所属公司id
 * @apiParam {Integer} amount 股票数量
 * @apiParam {String} realName 真实姓名
 * @apiParam {Integer} dpId 职位id
 * @apiParam {String} phone 用户手机号
 * @apiParam {Decimal} money 资金
 */
class StockholderInfo(
        var id: Int? = null,
        var companyId: Int? = null,     // 股票所属公司id
        var userId: Int? = null,        // 用户id
        var amount: Int? = null,        // 数量, 该字段用于绑定股东时添加默认股票
        var realName: String? = null,   // 真实姓名
        var dpId: Int? = null,
        var department: String? = null, // 所在部门
        var position: String? = null,   // 职位
        val phone: String? = null,      // 手机号, 该字段用于绑定股东时查找股东
        val money: BigDecimal? = null   // 资金
) {
    fun toStockholder() = Stockholder(
            companyId = companyId,
            realName = realName,
            dpId = dpId,
            money = money
    )

    fun toStockholder(stockholder: Stockholder) {
        if (realName != null) {
            stockholder.realName = realName
        }
        if (dpId != null) {
            stockholder.dpId = dpId
        }
        if (money != null) {
            stockholder.money = money
        }
    }
}