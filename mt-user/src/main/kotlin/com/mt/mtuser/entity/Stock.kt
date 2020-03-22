package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*


/**
 * Created by gyh on 2020/3/22.
 */
@Table("mt_stock")
data class Stock(@Id var id: Int,
                 var companyId: Int,
                 var name: String? = null,
                 var price: String,
                 var create_time: Date
)