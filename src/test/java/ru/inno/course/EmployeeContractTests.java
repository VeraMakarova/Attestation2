package ru.inno.course;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import ru.inno.course.db.EmployeeRepository;
import ru.inno.course.db.EmployeeRepositoryJDBC;

import java.sql.SQLException;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class EmployeeContractTests {
    public static final String URL_EMPLOYEE_LIST = "https://x-clients-be.onrender.com/employee?company={id}";
    public static final String URL_AUTH = "https://x-clients-be.onrender.com/auth/login";
    public static final String URL_EMPLOYEE_ID = "https://x-clients-be.onrender.com/employee/{id}";
    public static final String URL_EMPLOYEE = "https://x-clients-be.onrender.com/employee";

    private int companyIdToDelete;
    private int employeeIdToDelete;
    private String token;
    private XClientsWebClient client;
    private EmployeeRepository repository;

    @BeforeAll
    public void setUp3() {
        client = new XClientsWebClient();
        token = client.getToken("flora", "nature-fairy");
    }

    @BeforeEach
    public void setUp2() throws SQLException {
        String connectionString = "jdbc:postgresql://dpg-cn1542en7f5s73fdrigg-a.frankfurt-postgres.render.com/x_clients_xxet";
        String user = "x_clients_user";
        String pass = "x7ngHjC1h08a85bELNifgKmqZa8KIR40";
        repository = new EmployeeRepositoryJDBC(connectionString, user, pass);
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = client.createEmployee("Marta", "Smith", companyIdToDelete, "casebat359@dovesilo.com", "123456789", token);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        repository.deleteEmployeeDBById(employeeIdToDelete);
        repository.deleteCompanyById(companyIdToDelete);
        repository.close();
    }

    @Test
    @DisplayName("1. Получение списка сотрудников")
    public void shouldReturnListOfEmployees() {
        given().log().all()
                .pathParams("id", companyIdToDelete)
                .get(URL_EMPLOYEE_LIST)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .log().all();
    }

    @Test
    @DisplayName("2. Получение информации о сотруднике")
    public void shouldReturnEmployeeInfo() {
        given().log().all()
                .pathParams("id", employeeIdToDelete)
                .get(URL_EMPLOYEE_ID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .body("companyId", equalTo(companyIdToDelete))
                .log().all();
    }

    @Test
    @DisplayName("3. Создание сотрудника")
    public void shouldCreateEmployee() throws SQLException {
        String requestBody = """
                {
                    "firstName": "Jack",
                    "lastName": "Smith",
                    "companyId": """ + companyIdToDelete + """ 
                  , "email": "casebat359@dovesilo.com",
                  "phone": "1111"
                }""";

        int testEmployeeId = given().log().all()
                .body(requestBody)
                .header("x-client-token", token)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(1))
                .extract().path("id");

        given().log().all()
                .pathParams("id", testEmployeeId)
                .get(URL_EMPLOYEE_ID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .body("companyId", equalTo(companyIdToDelete))
                .body("firstName", equalTo("Jack"))
                .log().all();

        repository.deleteEmployeeDBById(testEmployeeId);
    }

    @Test
    @DisplayName("4. Изменение информации о сотруднике")
    public void shouldChangeInfo() {
        String requestBody2 = """
                {
                   "lastName": "Adams",
                   "email": "12345@dovesilo.com",
                   "phone": "6789"
                }""";

        given().log().all()
                .body(requestBody2)
                .contentType(ContentType.JSON)
                .header("x-client-token", token)
                .header("id", employeeIdToDelete)
                .when().patch(URL_EMPLOYEE + "/" + employeeIdToDelete)
                .then().log().all()
                .statusCode(200);

        given()
                .get(URL_EMPLOYEE + "/" + employeeIdToDelete)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .body("lastName", equalTo("Adams"));
    }

}
