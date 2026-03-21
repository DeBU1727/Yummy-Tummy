package com.yummytummy.backend.controller;

import com.yummytummy.backend.dto.MenuItemDto;
import com.yummytummy.backend.dto.ProductResponseDto;
import com.yummytummy.backend.entity.MenuItem;
import com.yummytummy.backend.entity.Category;
import com.yummytummy.backend.repository.CategoryRepository;
import com.yummytummy.backend.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public MenuController(MenuService menuService, CategoryRepository categoryRepository) {
        this.menuService = menuService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/menu")
    public Map<String, List<MenuItemDto>> getMenu() {
        return menuService.getFullMenu();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        return ResponseEntity.ok(menuService.saveCategory(category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        return ResponseEntity.ok(menuService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            menuService.deleteCategory(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(menuService.getAllProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDto> addProduct(
            @RequestParam("name") String name,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setPrice(price);
        item.setDescription(description);
        
        return ResponseEntity.ok(menuService.saveMenuItem(item, categoryId, file));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Integer id,
            @RequestParam("name") String name,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setPrice(price);
        item.setDescription(description);
        
        return ResponseEntity.ok(menuService.updateMenuItem(id, item, categoryId, file));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok().build();
    }
}
