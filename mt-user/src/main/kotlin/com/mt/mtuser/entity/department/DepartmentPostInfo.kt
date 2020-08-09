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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DepartmentPostInfo

        if (id != other.id) return false
        if (departmentName != other.departmentName) return false
        if (postName != other.postName) return false
        if (companyId != other.companyId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (departmentName?.hashCode() ?: 0)
        result = 31 * result + (postName?.hashCode() ?: 0)
        result = 31 * result + (companyId ?: 0)
        return result
    }
}