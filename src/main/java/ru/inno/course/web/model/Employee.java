package ru.inno.course.web.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Employee(int id, String firstName, String lastName, int companyId, String email, String phone) {
}
