package com.mt.mtengine.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*


/**
 * Created by gyh on 2020/3/22.
 * @apiDefine Stock
 * @apiParam {Integer} id 股票id
 * @apiParam {Integer} companyId 公司id
 * @apiParam {String} name 股票名字
 * @apiParam {Double} price 股票价格
 * @apiParam {Date} createTime 创建时间
 */
@Table("mt_stock")
data class Stock(@Id var id: Int? = null,
                 var companyId: Int? = null,
                 var name: String? = null,
                 var price: Double? = null,
                 var createTime: Date? = null
)