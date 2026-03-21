package com.yummytummy.backend.service;

import com.yummytummy.backend.dto.MenuItemDto;
import com.yummytummy.backend.entity.MenuItem;
import com.yummytummy.backend.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yummytummy.backend.dto.ProductResponseDto;
import com.yummytummy.backend.repository.CategoryRepository;
import com.yummytummy.backend.entity.Category;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public MenuService(MenuItemRepository menuItemRepository, CategoryRepository categoryRepository, FileStorageService fileStorageService) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<ProductResponseDto> getAllProducts() {
        return menuItemRepository.findAll().stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDto saveMenuItem(MenuItem item, Integer categoryId, MultipartFile file) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        item.setCategory(category);

        if (file != null && !file.isEmpty()) {
            String filePath = fileStorageService.storeFile(file);
            item.setImage(filePath);
        }

        MenuItem saved = menuItemRepository.save(item);
        return convertToProductDto(saved);
    }

    @Transactional
    public ProductResponseDto updateMenuItem(Integer id, MenuItem details, Integer categoryId, MultipartFile file) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        item.setName(details.getName());
        item.setPrice(details.getPrice());
        item.setDescription(details.getDescription());
        
        if (file != null && !file.isEmpty()) {
            String filePath = fileStorageService.storeFile(file);
            item.setImage(filePath);
        } else if (details.getImage() != null) {
            // Keep existing or update if text path provided
            item.setImage(details.getImage());
        }

        item.setCategory(category);
        
        MenuItem saved = menuItemRepository.save(item);
        return convertToProductDto(saved);
    }

    @Transactional
    public void deleteMenuItem(Integer id) {
        menuItemRepository.deleteById(id);
    }

    // --- Category Management ---

    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Integer id, Category details) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(details.getName());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // If there are menu items in this category, we might want to prevent deletion or handle it.
        // For now, let's allow it if the DB constraint allows (it has @OneToMany but check cascade).
        // The SQL script for categories table didn't have special constraints shown but 
        // usually we'd check if it's empty.
        if (category.getMenuItems() != null && !category.getMenuItems().isEmpty()) {
            throw new RuntimeException("Cannot delete category with associated menu items. Please reassign or delete the items first.");
        }
        
        categoryRepository.deleteById(id);
    }

    private ProductResponseDto convertToProductDto(MenuItem item) {
        return new ProductResponseDto(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getDescription(),
                item.getImage(),
                item.getCategory() != null ? item.getCategory().getId() : null,
                item.getCategory() != null ? item.getCategory().getName() : "No Category"
        );
    }

    public Map<String, List<MenuItemDto>> getFullMenu() {
        List<MenuItem> menuItems = menuItemRepository.findAllWithCategory();

        // Group by category name and map MenuItem entities to MenuItemDto
        return menuItems.stream()
                .collect(Collectors.groupingBy(
                        menuItem -> menuItem.getCategory().getName(),
                        Collectors.mapping(
                                this::convertToDto,
                                Collectors.toList()
                        )
                ));
    }

    private MenuItemDto convertToDto(MenuItem menuItem) {
        return new MenuItemDto(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getPrice(),
                menuItem.getDescription(),
                menuItem.getImage()
        );
    }
}
