package com.example.xuandangwebbanhang.repository;

import com.example.xuandangwebbanhang.model.RewardPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {

    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RewardPoint r WHERE r.userPhone = :userPhone")
    int sumPointsByUserPhone(String userPhone);

    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RewardPoint r WHERE r.username = :username")
    int sumPointsByUsername(String username);
}
