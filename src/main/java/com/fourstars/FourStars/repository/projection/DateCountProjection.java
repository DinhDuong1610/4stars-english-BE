package com.fourstars.FourStars.repository.projection;

import java.time.LocalDate;

public interface DateCountProjection {
    LocalDate getDate();

    Long getCount();
}