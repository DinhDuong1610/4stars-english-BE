package com.fourstars.FourStars.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.projection.DateCountProjection;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Override
    @EntityGraph(attributePaths = { "role", "badge" })
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "role", "badge", "role.permissions" })
    Optional<User> findByEmail(String email);

    List<User> findByEmailIn(Collection<String> emails);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRoleId(Long roleId);

    boolean existsByBadgeId(Long badgeId);

    Page<User> findAllByOrderByPointDesc(Pageable pageable);

    long countByCreatedAtAfter(Instant date);

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(id) as count FROM users WHERE created_at >= :startDate GROUP BY CAST(created_at AS DATE) ORDER BY date ASC", nativeQuery = true)
    List<DateCountProjection> findNewUserCountByDate(Instant startDate);

}
