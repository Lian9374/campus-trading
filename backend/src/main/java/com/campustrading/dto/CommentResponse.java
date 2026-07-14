package com.campustrading.dto;

import com.campustrading.entity.ProductComment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private int replyCount;
    private List<CommentResponse> replies = new ArrayList<>();

    public static CommentResponse fromEntity(ProductComment c) {
        CommentResponse r = new CommentResponse();
        r.setId(c.getId());
        r.setProductId(c.getProductId());
        r.setUserId(c.getUserId());
        r.setParentId(c.getParentId());
        r.setContent(c.getContent());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
