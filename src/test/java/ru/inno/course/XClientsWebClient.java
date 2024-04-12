package ru.inno.course;

import io.restassured.http.ContentType;
import ru.inno.course.web.model.CreateCompany;
import ru.inno.course.web.model.CreateEmployee;
import ru.inno.course.web.model.Employee;

import java.util.List;

import static io.restassured.RestAssured.given;


public class XClientsWebClient {
    public static final String URL = "https://x-clients-be.onrender.com/company";
    public static final String URL_AUTH = "https://x-clients-be.onrender.com/auth/login";
    public static final String URL_EMPLOYEE = "https://x-clients-be.onrender.com/employee";
    public static final String URL_EMPLOYEE_ID = "https://x-clients-be.onrender.com/employee/{id}";
    public static final String URL_EMPLOYEE_LIST = "https://x-clients-be.onrender.com/employee?company={id}";

    public String getToken(String login, String pass) {
        String creds = "{\"username\": \"" + login + "\",\"password\": \"" + pass + "\"}";

        return given().log().all()
                .body(creds)
                .contentType(ContentType.JSON)
                .when().post(URL_AUTH)
                .then().log().all()
                .extract().path("userToken");
    }

    public int createCompany(String name, String desc, String token) {
        CreateCompany createCompany = new CreateCompany(name, desc);

        return given()
                .log().all()
                .body(createCompany)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when()
                .post(URL)
                .then()
                .log().all()
                .extract().path("id");
    }

    public int createEmployee(String firstName, String lastName, int companyId, String email, String phone, String token) {
        CreateEmployee createEmployee = new CreateEmployee(firstName, lastName, companyId, email, phone);

        return given()
                .log().all()
                .body(createEmployee)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when()
                .post(URL_EMPLOYEE)
                .then()
                .log().all()
                .extract().path("id");

    }

    public Employee getEmployeeInfo(int employeeId) {
        return given()
                .log().all()
                .pathParams("id", employeeId)
                .get(URL_EMPLOYEE_ID)
                .then()
                .log().all()
                .extract().as(Employee.class);
    }

    public Employee changeEmployee(int employeeId, String lastName, String firstName, String email, String phone, boolean isActive, String token) {

        Employee oldEmp = given()
                .log().all()
                .pathParams("id", employeeId)
                .get(URL_EMPLOYEE_ID)
                .then()
                .log().all()
                .extract().as(Employee.class);
        String requestBodyPath =
                "{\"lastName\": \"" + lastName + "\", \"firstName\": \"" + firstName + "\", \"email\": \"" + email + "\", \"phone\": \"" + phone + "\", \"isActive\": \"" + isActive + "\"}";

        return given().log().all()
                .body(requestBodyPath)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", employeeId)
                .when().patch(URL_EMPLOYEE + "/" + employeeId)
                .then().log().all()
                .extract().as(Employee.class);

    }

    public List<Employee> getEmployeeList(int companyId) {
        return given()
                .log().all()
                .pathParams("id", companyId)
                .get(URL_EMPLOYEE_LIST)
                .then()
                .log().all()
                .extract().body().jsonPath().getList("", Employee.class);
    }

}
