package com.campustrading.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private String remark;
}
