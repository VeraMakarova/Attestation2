package ru.inno.course.db;

import ru.inno.course.db.model.EmployeeDB;

import java.sql.SQLException;

public interface EmployeeRepository {
    int createEmployeeDB(String first_name, String last_name, String phone, int company_id) throws SQLException;
    EmployeeDB getEmployeeDBById (int id) throws SQLException;

    //List<EmployeeDB>(int id);
    void deleteEmployeeDBById (int id) throws SQLException;
    void deleteCompanyById (int id) throws SQLException;
    void close() throws SQLException;
}
