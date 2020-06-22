package com.mt.mtuser.entity

import org.springframework.data.annotation.Id

/**
 * Created by gyh on 2020/6/16
 */
class Analyst(
        @Id
        var id: Int? = null,
        /*** 用户id */
        var userId: Int? = null,
        /*** 该用户在公司的角色id */
        var roleId: Int? = null,
        /*** 公司id */
        var companyList: List<Company> = listOf()
)