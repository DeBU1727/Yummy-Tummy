package com.yummytummy.backend.repository;

import com.yummytummy.backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    /**
     * Fetches all MenuItems with their associated Category in a single query
     * to prevent N+1 issues.
     * @return A list of MenuItems.
     */
    @Query("SELECT m FROM MenuItem m JOIN FETCH m.category")
    List<MenuItem> findAllWithCategory();
}
