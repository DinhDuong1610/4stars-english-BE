package com.fourstars.FourStars.domain.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultPaginationDTO<T> {
    private Meta meta;
    private List<T> result;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }
}
