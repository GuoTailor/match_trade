package com.mt.mtuser.entity

import java.math.BigDecimal

/**
 * Created by gyh on 2020/4/23.
 * @apiDefine StockholderInfo
 * @apiParam {Integer} companyId 股票所属公司id
 * @apiParam {Integer} amount 股票数量
 * @apiParam {String} realName 真实姓名
 * @apiParam {String} department 所在部门
 * @apiParam {String} position 职位
 * @apiParam {String} phone 用户手机号
 * @apiParam {Decimal} money 资金
 */
class StockholderInfo(
        var id: Int? = null,
        var companyId: Int? = null,     // 股票所属公司id
        var amount: Int? = null,        // 数量, 该字段用于绑定股东时添加默认股票
        var realName: String? = null,   // 真实姓名
        var department: String? = null, // 所在部门
        var position: String? = null,   // 职位
        val phone: String? = null,      // 手机号, 该字段用于绑定股东时查找股东
        val money: BigDecimal? = null   // 资金
) {
    fun toStockholder() = Stockholder(
            companyId = companyId,
            realName = realName,
            department = department,
            position = position,
            money = money
    )
}