package com.dockerplatform.backend.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtils {

    public static Pageable convertToNativePageable(Pageable pageable) {
        Sort nativeSort = Sort.unsorted();

        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                String property = order.getProperty();
                String column = camelToSnake(property);
                nativeSort = nativeSort.and(Sort.by(order.getDirection(), column));
            }
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), nativeSort);
    }

    public static String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
