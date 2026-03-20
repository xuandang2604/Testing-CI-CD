package com.example.xuandangwebbanhang.repository;

import com.example.xuandangwebbanhang.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByUsernameOrderByCreatedAtDesc(String username);
    Optional<Voucher> findByCode(String code);
}

