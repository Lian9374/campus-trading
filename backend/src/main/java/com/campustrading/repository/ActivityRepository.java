package com.campustrading.repository;

import com.campustrading.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Page<Activity> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    Page<Activity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
