package com.croplink.service;

import com.croplink.entity.Category;
import com.croplink.exception.AppException;
import com.croplink.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getAllActive() {
        return categoryRepository.findByStatus("ACTIVE");
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category create(String name, String description) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new AppException("Category already exists", 400);
        }
        Category cat = Category.builder().name(name).description(description).status("ACTIVE").build();
        return categoryRepository.save(cat);
    }

    public Category update(Long id, String name, String description, String status) {
        Category cat = categoryRepository.findById(id).orElseThrow(() -> new AppException("Category not found", 404));
        if (name != null) cat.setName(name);
        if (description != null) cat.setDescription(description);
        if (status != null) cat.setStatus(status);
        return categoryRepository.save(cat);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
