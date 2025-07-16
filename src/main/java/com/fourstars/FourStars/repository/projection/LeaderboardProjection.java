package com.fourstars.FourStars.repository.projection;

public interface LeaderboardProjection {
    Long getUserId();

    String getName();

    String getEmail();

    Integer getWeeklyPoints();
}
