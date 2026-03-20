package com.example.xuandangwebbanhang.repository;

import com.example.xuandangwebbanhang.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}

