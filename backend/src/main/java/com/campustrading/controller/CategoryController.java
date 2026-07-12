package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.entity.Category;
import com.campustrading.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public Result<List<Category>> listCategories() {
        return Result.success(categoryRepository.findAll());
    }
}
