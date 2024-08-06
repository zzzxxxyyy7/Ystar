package com.ystar.common.VO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询所用的包装类
 */
@Data
public class PageWrapper<T> implements Serializable {

    private List<T> list;
    private boolean hasNext;
}