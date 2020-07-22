package com.mt.mtuser.entity.department

/**
 * Created by gyh on 2020/7/20
 * @apiDefine DepartmentPostInfo
 * @apiParam {Integer} id id
 * @apiParam {String} departmentName 部门名字
 * @apiParam {String} postName 职位名字
 * @apiParam {Integer} companyId companyId
 */
class DepartmentPostInfo (
        var id: Int? = null,
        var departmentName: String? = null,
        var postName: String? = null,
        var companyId: Int? = null
)