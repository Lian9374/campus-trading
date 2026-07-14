package com.campustrading.repository;

import com.campustrading.entity.ProductComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {

    Page<ProductComment> findByProductIdAndParentIdIsNullOrderByCreatedAtDesc(Long productId, Pageable pageable);

    List<ProductComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByProductIdAndParentIdIsNull(Long productId);
}
