package ru.inno.course.db.model;

public record EmployeeDB(int id, String firstName, String lastName, int companyId, String email, String phone, boolean isActive) {
}
