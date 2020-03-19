package com.mt.mtuser.dao.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Created by gyh on 2020/3/18.
 */
@Table("mt_role")
class MtRole {
    @Id
    var id: Int? = null
    var name: String? = null
    var nameZh: String? = null
}