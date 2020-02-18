package com.chinaway.http.client.model;


import java.io.Serializable;
import java.util.List;

/**
 * 分页数据
 *
 * @param <T>
 * @author manmao
 * @since 2019-03-12
 */
public class Page<T> implements Serializable {

    private static final long serialVersionUID = -8362552461766087046L;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总条数
     */
    private long totalCount;

    /**
     * 每页条数
     */
    private int pageSize;

    /**
     * 当前分页号
     */
    private int pageNo;

    public Page() {
        super();
    }

    public Page(List<T> list, long totalCount, int pageSize) {
        this.list = list;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    @Override
    public String toString() {
        return "Page [list=" + list + ", totalCount=" + totalCount
                + ", pageSize=" + pageSize + "]";
    }
}
