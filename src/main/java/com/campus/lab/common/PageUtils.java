package com.campus.lab.common;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页页码工具：最多展示 5 个页码，避免页数过多
 */
public final class PageUtils {

    private PageUtils() {
    }

    public static List<Integer> numbers(Page<?> page) {
        int totalPages = page.getTotalPages();
        if (totalPages <= 0) {
            return List.of();
        }
        int start = Math.max(0, page.getNumber() - 2);
        int end = Math.min(totalPages - 1, start + 4);
        start = Math.max(0, end - 4);
        List<Integer> numbers = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            numbers.add(i);
        }
        return numbers;
    }
}
