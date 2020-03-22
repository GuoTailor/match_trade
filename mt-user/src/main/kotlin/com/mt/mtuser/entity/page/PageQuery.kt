package com.mt.mtuser.entity.page

import com.mt.mtuser.common.page.PageQuery
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.util.StringUtils

/**
 * Created by gyh on 2020/3/20.
 * @apiDefine PageQuery
 * @apiParam {String} [searchField] 查找字段,如'name'
 * @apiParam {String} [searchOper] 查找方式,可用参数与说明: [{'cn':包含'},{'eq':'等于'},{'ne':'不等于'},{'gt':'大于'},
 * {'gt;=':'大于等于'},{'lt':'小于'},{'lt;=':'小于等于'},{'bw':'开始于'},{'ew':'结束于'}]
 * @apiParam {String} [searchString] 查找的字符,如'张'
 * @apiParam {Int} [pageNum] 当前页号,页从0开始,默认第0页
 * @apiParam {Int} [pageSize] 每页显示的数量,默认30条
 * @apiParam {String} [order] 排序字段,如: id
 * @apiParam {String} [direction] 排序规则,如: DESC, ASC
 */
class PageQuery(val pageNum: Int = 0,
                val pageSize: Int = 30,
                private val searchField: String? = null,    // 查找字段 :name
                private val searchOper: String? = null,     // 查找方式 :"[['cn', '包含'], ['eq', '等于'], ['nc', '不包含'], ['ne', '不等于'], ['gt', '大于'], ['lt', '小于'], ['bw', '开始于'], ['bn', '不开始于'], ['ew', '结束于'], ['en', '不结束于']]")
                private val searchString: String? = null,   // 查找的字符 :张
                private val order: String? = null,          // 排序字段 :id
                private val direction: String? = null       // 排序方向 :asc
) {
    private val oper = arrayOf(arrayOf("cn", "like", "%%%s%%"), arrayOf("eq", "=", "%s"),
            arrayOf("nc", "not like", "%%%s%%"), arrayOf("ne", "<>", "%s"), arrayOf("gt", ">", "%s"),
            arrayOf("lt", "<", "%s"), arrayOf("bw", "like", "%s%%"), arrayOf("bn", "not like", "%s%%"),
            arrayOf("ew", "like", "%%%s"), arrayOf("en", "not like", "%%%s"))
    fun where(): Criteria {
        var criteria = Criteria.empty()
        if (searchField != null && searchOper != null && searchString != null) {
            with(Criteria.where(searchField)) {
                criteria = when (searchOper) {
                    "eq" -> `is`(searchString)
                    "ne" -> not(searchString)
                    "gt" -> greaterThan(searchString)
                    "gt;=" -> greaterThanOrEquals(searchString)
                    "it" -> lessThan(searchString)
                    "it;=" -> lessThanOrEquals(searchString)
                    "cn", "bw", "ew" -> buildSubSql()
                    "nc", "bn", "en" -> throw IllegalStateException("暂不支持的查找方式$searchOper")
                    else -> throw IllegalStateException("不支持的查找方式$searchOper")
                }
            }
        }
        return criteria
    }

    fun getWhere(): String {
        if (!StringUtils.isEmpty(searchField) && !StringUtils.isEmpty(searchOper) && !StringUtils.isEmpty(searchString)) {
            for (strings in oper) { //判断指令是否在允许范围内
                if (strings[0] == searchOper) {
                    return " \"$searchField\" ${strings[1]} '${String.format(strings[2], searchString)}'"
                }
            }
        }
        return ""
    }

    fun page(): Pageable {
        var pageable = PageRequest.of(pageNum, pageSize)
        if (order != null) {
            if (direction.equals("asc", true)) {
                pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, order))
            } else if (direction.equals("desc", true)) {
                pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, order))
            }
        }
        return pageable
    }

    private fun buildSubSql(): Criteria {
        if (searchField != null && searchOper != null && searchString != null) {
            for (strings in oper) { //判断指令是否在允许范围内
                if (strings[0] == searchOper) {
                    return Criteria.where(searchField).like(String.format(strings[2], searchString))
                }
            }
        }
        return Criteria.empty()
    }
}