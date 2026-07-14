package com.campustrading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    private Long productId; // 可选：通过商品发起时有值，直接私信时为null

    @NotNull(message = "收件人ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
