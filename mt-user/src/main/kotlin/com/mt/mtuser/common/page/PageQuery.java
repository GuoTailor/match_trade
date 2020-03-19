package com.mt.mtuser.common.page;

import org.hibernate.validator.constraints.Range;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gyh on 2019/1/24.
 *
 * @apiDefine PageQuery
 * @apiParam {String} [searchField] 查找字段,如'name'
 * @apiParam {String} [searchOper] 查找方式,可用参数与说明: [{'cn':包含'},{'eq':'等于'},{'nc':'不包含'},{'ne':'不等于'},{'gt':'大于'},{'gt;=':'大于等于'},{'lt':'小于'},{'lt;=':'小于等于'},{'bw':'开始于'},{'bn':'不开始于'},{'ew':'结束于'},{'en':'不结束于'}]
 * @apiParam {String} [searchString] 查找的字符,如'张'
 * @apiParam {Int} [pageNum] 当前页号,页从1开始,默认第一页
 * @apiParam {Int} [pageSize] 每页显示的数量,默认30条
 * @apiParam {String} [orderBy] 排序规则,如: create_time DESC,update_time ASC
 */
public class PageQuery {
    private static final Logger loggger = Logger.getLogger(PageQuery.class.getSimpleName());
    /**
     * 按id递增排序
     */
    public static final String ORDER_BY_CREATE_TIME_DESC = "id ASC";

    private static final Map<String, String> SearchType = new HashMap<>();
    private static final String[][] nmka = {
            {"cn", "like", "%%%s%%"}, {"eq", "=", "%s"},
            {"nc", "not like", "%%%s%%"}, {"ne", "<>", "%s"},
            {"gt", ">", "%s"}, {"lt", "<", "%s"},
            {"bw", "like", "%s%%"}, {"bn", "not like", "%s%%"},
            {"ew", "like", "%%%s"}, {"en", "not like", "%%%s"}
    };
    // "查找字段", example = "name")
    private String searchField;
    // "查找方式", allowableValues = "[['cn', '包含'], ['eq', '等于'], ['nc', '不包含'], ['ne', '不等于'], ['gt', '大于'], ['lt', '小于'], ['bw', '开始于'], ['bn', '不开始于'], ['ew', '结束于'], ['en', '不结束于']]")
    private String searchOper;
    // "查找的字符", example = "张")
    private String searchString;

    // "当前页号")
    @Range(min = 1, max = Integer.MAX_VALUE)
    private int pageNum = 1;

    // "一页数量")
    @Range(min = 1, max = Integer.MAX_VALUE)
    private int pageSize = 30;

    // "排序", notes = "例：create_time desc,update_time ASC")
    private String orderBy;

    public PageQuery(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public PageQuery() {
    }

    public int getOffset() {
        return (this.pageNum - 1) * this.pageSize;
    }

    private String buildSubSql() {
        StringBuilder sql = new StringBuilder();
        if (!StringUtils.isEmpty(searchField) && !StringUtils.isEmpty(searchOper) && !StringUtils.isEmpty(searchString)) {
            for (String[] strings : nmka) { //判断指令是否在允许范围内
                if (strings[0].equals(searchOper)) {
                    return sql
                            .append(" ")
                            .append(searchField).append(" ")
                            .append(strings[1]).append(" '")
                            .append(String.format(strings[2], searchString)).append("'")
                            .toString();
                }
            }
        }
        loggger.info(sql.toString());
        return sql.toString();
    }

    private String buildLimitSql() {
        return " limit " +
                pageSize +
                " OFFSET " +
                pageSize * (pageNum - 1);
    }

    public QueryBuild where() {
        return new QueryBuild(this).where();
    }

    public QueryBuild query() {
        return new QueryBuild(this).where();
    }

    public QueryBuild limit() {
        return new QueryBuild(this).limit();
    }

    public static class QueryBuild {
        private String where = " ";
        private String query = " ";
        private String limit = " ";
        private PageQuery pageQuery;

        public QueryBuild(PageQuery pageQuery) {
            this.pageQuery = pageQuery;
        }

        public QueryBuild where() {
            where = " where";
            return this;
        }

        public QueryBuild query() {
            query = pageQuery.buildSubSql();
            return this;
        }

        public QueryBuild limit() {
            limit = pageQuery.buildLimitSql();
            return this;
        }

        public String build() {
            String sql = StringUtils.isEmpty(query.trim())
                    ? query + limit
                    : where + query + limit;
            loggger.info(sql);
            return sql;
        }
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSearchField() {
        return searchField;
    }

    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    public String getSearchOper() {
        return searchOper;
    }

    public void setSearchOper(String searchOper) {
        this.searchOper = searchOper;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }


}
