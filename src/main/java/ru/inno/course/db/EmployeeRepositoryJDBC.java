package ru.inno.course.db;

import ru.inno.course.db.model.EmployeeDB;

import java.sql.*;

public class EmployeeRepositoryJDBC implements EmployeeRepository{

    private final Connection connection;
    private static final String  SQL_SELECT_EMP_BY_COMP_ID = "SELECT * FROM employee where companyId = ?";
    private static final String  SQL_SELECT_EMP_BY_ID = "SELECT * FROM employee where id = ?";
    private static final String SQL_DELETE_EMP_BY_ID = "DELETE FROM employee where id = ?";
    private static final String  SQL_DELETE_COMPANY_BY_ID = "DELETE FROM company WHERE id = ?";
    //private static final String  SQL_INSERT_COMPANY = "INSERT INTO company(\"name\") values (?)";
    private static final String  SQL_INSERT_EMPLOYEE = "INSERT INTO employee(\"first_name\",\"last_name\",\"phone\",\"company_id\") values (?, ?, ?, ?)";


    public EmployeeRepositoryJDBC(String connectionString, String user, String pass) throws SQLException {

        this.connection = DriverManager.getConnection(connectionString, user, pass);
    }

    @Override
    public EmployeeDB getEmployeeDBById(int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_EMP_BY_ID);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        EmployeeDB employeeDB = new EmployeeDB(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getInt("company_id"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getBoolean("is_active"));
        return employeeDB;
    }

    @Override
    public void deleteEmployeeDBById(int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_EMP_BY_ID);
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    @Override
    public void deleteCompanyById(int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_COMPANY_BY_ID);
        statement.setInt(1, id);
        statement.executeUpdate();
    }


    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public int createEmployeeDB(String first_name, String last_name, String phone, int company_id) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_EMPLOYEE, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, first_name);
        statement.setString(2, last_name);
        statement.setString(3, phone);
        statement.setInt(4, company_id);

        statement.executeUpdate();

        ResultSet keys = statement.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }



}
