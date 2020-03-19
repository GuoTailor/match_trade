package com.mt.mtuser.common.page;

import java.util.List;

/**
 * Created by gyh on 2018/10/19.
 * 分页对象
 */
public class PageView<T> {
    private static final long serialVersionUID = -4426958360243585882L;

    // 当前页号
    private int pageNum;

    // 每页的数量
    private int pageSize;

    // 总记录数
    private long total;

    // 总页数
    private int pages;

    // 结果集
    private List<T> list;

    public PageView(PageQuery pageQuery) {
        this.pageNum = pageQuery.getPageNum();
        this.pageSize = pageQuery.getPageSize();
    }

    public PageView() {
    }

    public static int getPages(long total, int pageSize) {
        if (total == 0 || pageSize == 0) {
            return 0;
        }
        return (int) (total % pageSize == 0 ? (total / pageSize) : (total / pageSize + 1));
    }

    public int getPages() {
        return getPages(this.total, this.pageSize);
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
