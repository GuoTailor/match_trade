package com.mt.mtuser.entity.department

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("mt_department_post")
class DepartmentPost (
    /**
     *
     */
    @Id
    var id: Int? = null,

    /**
     * 部门id
     */
    var departmentId: Int? = null,

    /**
     * 职位id
     */
    var postId: Int? = null,

    /**
     * 公司id
     */
    var companyId: Int? = null

)