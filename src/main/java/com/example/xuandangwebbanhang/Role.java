package com.example.xuandangwebbanhang;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public enum Role {
    ADMIN(1),    // Vai trò quản trị viên, có quyền cao nhất trong hệ thống.
    MANAGER(2),  // Vai trò quản lý, chỉ có quyền quản lý sản phẩm và danh mục.
    USER(3);     // Vai trò người dùng bình thường, có quyền hạn giới hạn.
    public final long value;
}