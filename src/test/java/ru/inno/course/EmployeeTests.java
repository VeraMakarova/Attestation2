package ru.inno.course;

import org.junit.jupiter.api.*;
import ru.inno.course.db.EmployeeRepository;
import ru.inno.course.db.EmployeeRepositoryJDBC;
import ru.inno.course.db.model.EmployeeDB;
import ru.inno.course.web.model.Employee;

import java.sql.SQLException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class EmployeeTests {

    private XClientsWebClient client;
    private int employeeIdToDelete;
    private int companyIdToDelete;
    private EmployeeRepository repository;
    private String token;


    @BeforeAll
    public void setUp1() {
        client = new XClientsWebClient();
        token = client.getToken("flora", "nature-fairy");
    }

    @BeforeEach
    public void setUp() throws SQLException {
        String connectionString = "jdbc:postgresql://dpg-cn1542en7f5s73fdrigg-a.frankfurt-postgres.render.com/x_clients_xxet";
        String user = "x_clients_user";
        String pass = "x7ngHjC1h08a85bELNifgKmqZa8KIR40";
        repository = new EmployeeRepositoryJDBC(connectionString, user, pass);
//        client = new XClientsWebClient();
//        token = client.getToken("flora", "nature-fairy");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        repository.deleteEmployeeDBById(employeeIdToDelete);
        repository.deleteCompanyById(companyIdToDelete);
        repository.close();
    }

    @Test
    @DisplayName("1. Получение списка пользователей")
    public void iCanGetEmployeeList() throws SQLException {

        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = repository.createEmployeeDB("Marta", "Smith", "123", companyIdToDelete);
        int annaId = repository.createEmployeeDB("Anna", "Adams", "456", companyIdToDelete);
        int tomId = repository.createEmployeeDB("Tom", "Jones", "789", companyIdToDelete);

        List<Employee> someList = client.getEmployeeList(companyIdToDelete);

        boolean hasMarta = someList
                .stream()
                .anyMatch(s -> s.firstName().equals("Marta"));
        boolean hasAnna = someList
                .stream()
                .anyMatch(s -> s.firstName().equals("Anna"));
        boolean hasTom = someList
                .stream()
                .anyMatch(s -> s.firstName().equals("Tom"));
        assertTrue(hasMarta);
        assertTrue(hasAnna);
        assertTrue(hasTom);

        repository.deleteEmployeeDBById(annaId);
        repository.deleteEmployeeDBById(tomId);
    }

    @Test
    @DisplayName("1.2 Получение пустого списка сотрудников работает корректно")
    public void iCanGetEmptyUsersList() {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        List<Employee> emptyList = client.getEmployeeList(companyIdToDelete);
        assertTrue(emptyList.isEmpty());
    }


    @Test
    @DisplayName("2.1. Создание пользователя в новую компанию успешно")
    public void iCanCreateEmployee() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = client.createEmployee("Marta", "Smith", companyIdToDelete, "casebat359@dovesilo.com", "123456789", token);

        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals(employeeIdToDelete, employeeDB.id());
        assertEquals(companyIdToDelete, employeeDB.companyId());
    }

    @Test
    @DisplayName("2.2. Создание пользователя, имя сохраняется правильно")
    public void whenCreateEmployeeNameIsSavedCorrectly() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "11111", token);
        employeeIdToDelete = client.createEmployee("Marta", "Smith", companyIdToDelete, "casebat359@dovesilo.com", "123456789", token);

        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals(employeeIdToDelete, employeeDB.id());
        assertEquals("Marta", employeeDB.firstName());
    }


    @Test
    @DisplayName("2.3. Создание пользователя, фамилия сохраняется правильно")
    public void whenCreateEmployeeLastNameIsSavedCorrectly() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = client.createEmployee("Marta", "Smith", companyIdToDelete, "casebat359@dovesilo.com", "123456789", token);

        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals(employeeIdToDelete, employeeDB.id());
        assertEquals("Smith", employeeDB.lastName());
    }

    @Test
    @DisplayName("2.4. Создание пользователя, email сохраняется правильно")
    public void whenCreateEmployeeEmailIsSavedCorrectly() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = client.createEmployee("Marta", "Smith", companyIdToDelete, "casebat359@dovesilo.com", "123456789", token);

        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals(employeeIdToDelete, employeeDB.id());
        assertEquals("casebat359@dovesilo.com", employeeDB.email());
    }

    @Test
    @DisplayName("2.5. Создание пользователя, телефон сохраняется правильно")
    public void whenCreateEmployeePhoneIsSavedCorrectly() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = client.createEmployee("Marta", "Smith", companyIdToDelete, "casebat359@dovesilo.com", "123456789", token);

        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals(employeeIdToDelete, employeeDB.id());
        assertEquals("123456789", employeeDB.phone());
    }

    @Test
    @DisplayName("3.1. Получение сведений о пользователе")
    public void iCanGetEmployeeInfo() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = repository.createEmployeeDB("Marta", "Smith", "12345678", companyIdToDelete);

        Employee employee = client.getEmployeeInfo(employeeIdToDelete);

        assertEquals("Marta", employee.firstName());
        assertEquals("Smith", employee.lastName());
        assertEquals("12345678", employee.phone());
        assertEquals(companyIdToDelete, employee.companyId());
    }

    @Test
    @DisplayName("3.2. Запрос несуществующего пользователя выдает код 404")
    public void iCannotGetNotExistingUser() {
        int incorrectId = -12;
        given()
                .log().all()
                .pathParams("id", incorrectId)
                .get("https://x-clients-be.onrender.com/employee/{id}")
                .then()
                .log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("4.1 Изменение фамилии успешно")
    public void iCanChangeEmployeeLastName2() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = repository.createEmployeeDB("Marta", "Smith", "12345678", companyIdToDelete);

        Employee updEmployee = client.changeEmployee(employeeIdToDelete, "Adams", "Jack", "someNewEmail@dovesilo.com", "87654321", false, token);
        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals("Adams", employeeDB.lastName());
        assertEquals(employeeIdToDelete, employeeDB.id());
    }

    @Test
    @DisplayName("4.2 Изменение email успешно")
    public void iCanChangeEmployeeEmail() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = repository.createEmployeeDB("Marta", "Smith", "12345678", companyIdToDelete);

        Employee updEmployee = client.changeEmployee(employeeIdToDelete, "Adams", "Jack", "555555@dovesilo.com", "87654321", false, token);
        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals("555555@dovesilo.com", employeeDB.email());
        assertEquals(employeeIdToDelete, employeeDB.id());
    }

    @Test
    @DisplayName("4.3 Изменение телефона успешно")
    public void iCanChangeEmployeePhone() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = repository.createEmployeeDB("Marta", "Smith", "12345678", companyIdToDelete);

        Employee updEmployee = client.changeEmployee(employeeIdToDelete, "Adams", "Jack", "someNewEmail@dovesilo.com", "87654321", false, token);
        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertEquals("87654321", employeeDB.phone());
        assertEquals(employeeIdToDelete, employeeDB.id());
    }

    @Test
    @DisplayName("4.4 Изменение активности успешно")
    public void iCanChangeEmployeeIsActive() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = repository.createEmployeeDB("Marta", "Smith", "12345678", companyIdToDelete);

        Employee updEmployee = client.changeEmployee(employeeIdToDelete, "Adams", "Jack", "someNewEmail@dovesilo.com", "87654321", false, token);
        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertFalse(employeeDB.isActive());
        assertEquals(employeeIdToDelete, employeeDB.id());
    }

    @Test
    @DisplayName("4.5 Нельзя изменить почту на некорректную")
    public void iCannotChangeEmployeeEmailToIncorrect() throws SQLException {
        companyIdToDelete = client.createCompany("Dark Side", "", token);
        employeeIdToDelete = repository.createEmployeeDB("Marta", "Smith", "12345678", companyIdToDelete);

        Employee updEmployee = client.changeEmployee(employeeIdToDelete, "Adams", "Jack", "0000", "87654321", false, token);
        EmployeeDB employeeDB = repository.getEmployeeDBById(employeeIdToDelete);

        assertNotEquals("0000", employeeDB.email());
        assertEquals(employeeIdToDelete, employeeDB.id());
    }

}
