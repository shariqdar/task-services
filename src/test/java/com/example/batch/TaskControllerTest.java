package com.example.batch;


import com.example.batch.controller.TaskController;
import com.example.batch.dto.TaskDTO;
import com.example.batch.dto.TaskResponse;
import com.example.batch.service.TaskService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

public class TaskControllerTest {

    private TaskService taskService;
    private static final String DESCRIPTION_ONE = "Description 1";
    private static final String DESCRIPTION_TWO = "Description 2";

    private static final String URL = "/api/tasks/";

    private static final String TASK_ONE = "Task 1";
    private static final String TASK_TWO = "Task 2";
    private static final String NEW_TASK = "New Task";


    @BeforeEach
    public void setUp() {
        taskService = mock(TaskService.class);
        TaskController controller = new TaskController(taskService);
        RestAssuredMockMvc.standaloneSetup(controller);
    }

    @Test
    void testGetAllTasks() {
        List<TaskDTO> tasks = List.of(
                new TaskDTO(1L, TASK_ONE, DESCRIPTION_ONE, true),
                new TaskDTO(2L, TASK_TWO, DESCRIPTION_TWO, true)
        );
        TaskResponse taskResponse = new TaskResponse(tasks,11,1, 2);
        when(taskService.getAllTasks(1,2)).thenReturn(taskResponse);

        RestAssuredMockMvc.given()
                .when().get("/api/tasks?page=1&size=2")
                .then().statusCode(200)
                .body("totalRecords", is(11))
                .body("page", is(1))
                .body("size", is(2))
                .body("tasks[0].id", equalTo(1))
                .body("tasks[0].title", equalTo(TASK_ONE))
                .body("tasks[1].id", equalTo(2))
                .body("tasks[1].title", equalTo(TASK_TWO))
                .body("tasks[1].description", equalTo(DESCRIPTION_TWO))
                .body("tasks[0].description", equalTo(DESCRIPTION_ONE));
    }

    @Test
    void testGetTaskByIdFound() {
        TaskDTO task = new TaskDTO(1L, TASK_ONE, DESCRIPTION_ONE, true);
        when(taskService.getTaskById(1L)).thenReturn(Optional.of(task));

        RestAssuredMockMvc.given()
                .when().get(URL+"1")
                .then().statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo(TASK_ONE))
                .body("description", equalTo(DESCRIPTION_ONE))
                .body("completed", equalTo(true));
    }

    @Test
    void testGetTaskByIdNotFound() {
        when(taskService.getTaskById(999L)).thenReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .when().get(URL+"999")
                .then().statusCode(404);
    }

    @Test
    void testCreateTask() {
        TaskDTO input = new TaskDTO(null, NEW_TASK, "New Desc", true);
        TaskDTO saved = new TaskDTO(3L, NEW_TASK, "New Desc", true);
        when(taskService.createTask(any())).thenReturn(saved);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(input)
                .when().post("/api/tasks")
                .then().statusCode(201)
                .body("id", equalTo(3))
                .body("title", equalTo(NEW_TASK));
    }

    @Test
    void testUpdateTaskSuccess() {
        when(taskService.updateTask(eq(1L), any())).thenReturn(1);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new TaskDTO(1L, "Updated", "Updated Desc", true))
                .when().put(URL+"1")
                .then().statusCode(204);
    }

    @Test
    void testUpdateTaskNotFound() {
        when(taskService.updateTask(eq(999L), any())).thenReturn(0);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new TaskDTO(999L, "Updated", "Updated Desc", true))
                .when().put(URL+"999")
                .then().statusCode(404);
    }

    @Test
    void testDeleteTaskSuccess() {
        when(taskService.deleteTask(1L)).thenReturn(true);

        RestAssuredMockMvc.given()
                .when().delete(URL+"1")
                .then().statusCode(204);
    }

    @Test
    void testDeleteTaskNotFound() {
        when(taskService.deleteTask(999L)).thenReturn(false);

        RestAssuredMockMvc.given()
                .when().delete(URL+"999")
                .then().statusCode(404);
    }
}
