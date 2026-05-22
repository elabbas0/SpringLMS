package com.springlms.backend.dto;

public record AdminDashboardResponse(
        long totalUsers,
        long totalStudents,
        long totalTeachers,
        long totalAdmins,
        long pendingUsers
) {
}