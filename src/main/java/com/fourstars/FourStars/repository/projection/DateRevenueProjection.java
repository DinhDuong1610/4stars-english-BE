package com.fourstars.FourStars.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DateRevenueProjection {
    LocalDate getDate();

    BigDecimal getTotal();
}