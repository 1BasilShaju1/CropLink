package com.croplink.service;

import com.croplink.entity.Category;
import com.croplink.exception.AppException;
import com.croplink.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getAllActive() {
        return categoryRepository.findByStatus("ACTIVE");
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category create(String name, String description) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new AppException("Category already exists", 400);
        }
        Category cat = Category.builder().name(name).description(description).status("ACTIVE").build();
        return categoryRepository.save(cat);
    }

    @Transactional
    public Category update(Long id, String name, String description, String status) {
        Category cat = categoryRepository.findById(id).orElseThrow(() -> new AppException("Category not found", 404));
        if (name != null) cat.setName(name);
        if (description != null) cat.setDescription(description);
        if (status != null) cat.setStatus(status);
        return categoryRepository.save(cat);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
