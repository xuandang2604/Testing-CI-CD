package com.example.xuandangwebbanhang.repository;

import com.example.xuandangwebbanhang.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Danh mục gốc (không có cha) */
    List<Category> findByParentIsNull();

    /** Danh mục con theo parent id */
    List<Category> findByParentId(Long parentId);
}