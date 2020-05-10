package com.mt.mtuser.entity

import com.mt.mtcommon.TradeInfo
import org.springframework.data.relational.core.mapping.Table

/**
 * Created by gyh on 2020/5/10.
 */
@Table("mt_trade_info")
class TradeDetails : TradeInfo() {
    var sellerName: String? = null
}