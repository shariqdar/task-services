package com.example.batch;


import com.example.batch.dto.TaskDTO;
import com.example.batch.service.TaskService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

class TaskControllerIntegrationTest {

    @Value("${app.task.path}")
    private String path;

    @LocalServerPort
    private int port;

    @Autowired
    private TaskService taskService;

    private List<Long> taskIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        TaskDTO task3 = new TaskDTO(null, "Sample Task3", "For description3", true);
        taskIds.add(taskService.createTask(task3).id());
    }

    @AfterEach
    void tearDown() {
        taskService.getAllTasks(1,2).tasks().forEach(task -> taskService.deleteTask(task.id()));
    }

    @Test
    @Order(1)
    void shouldGetAllTasks() {
        given()
                .when()
                .get("/api/tasks?page=1&size=5")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page", equalTo(1))
               // .body("size", equalTo(5))
                .body("tasks.size()", greaterThan(0));
                //.body("size()", greaterThan(0));
    }

    @Test
    @Order(2)
    void shouldGetTaskById() {
        given()
                .when()
                .get(path, taskIds.get(0))
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("Sample Task3"))
                .body("description", equalTo("For description3"))
                .body("completed", equalTo(true));
    }

    @Test
    @Order(3)
    void shouldCreateTask() {
        TaskDTO task = new TaskDTO(null, "New Task", "Created via test", true);
        Integer createdTaskId =
        given()
                .contentType(ContentType.JSON)
                .body(task)
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("title", equalTo("New Task"))
                .body("description", equalTo("Created via test"))
                .body("completed", equalTo(true))
                .extract()
                .path("id");
        taskIds.add(createdTaskId.longValue());
    }

    @Test
    @Order(4)
    void shouldUpdateTask() {
        TaskDTO update = new TaskDTO(taskIds.get(0), "Updated Task2", "Updated Desc2", true);

        given()
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put(path, taskIds.get(0))
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @Order(5)
    void shouldDeleteTask() {
        given()
                .when()
                .delete(path, taskIds.get(0))
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}

