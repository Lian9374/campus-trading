package com.campustrading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private Long parentId; // 回复某条留言时传此字段

    @NotBlank(message = "内容不能为空")
    private String content;
}
