package com.campustrading.repository;

import com.campustrading.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    Page<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId, Pageable pageable);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);
}
