package com.xyz.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    // Getters and Setters
    private List<T> records;
    private long total;
    private int size;
    private long current;
    private long pages;
    private long prevPage; // 上一页页码（-1 表示没有）
    private long nextPage; // 下一页页码（-1 表示没有）

    // 构造函数
    public PageResult() {}

    public PageResult(IPage<T> page) {
        this.records = page.getRecords();
        this.total = page.getTotal();
        this.size = (int) page.getSize();
        this.current = page.getCurrent();
        this.pages = page.getPages();

        // 上一页逻辑
        if (this.current > 1) {
            this.prevPage = this.current - 1;
        } else {
            this.prevPage = -1;
        }

        // 下一页逻辑
        if (this.current < this.pages) {
            this.nextPage = this.current + 1;
        } else {
            this.nextPage = -1;
        }
    }
    // 静态工厂方法
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page);
    }
}
