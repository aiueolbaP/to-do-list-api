package com.flores.cicd.controller;

import com.flores.cicd.model.Task;
import com.flores.cicd.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Aprender CI/CD")
                .description("Configurar pipeline en GitLab CI")
                .completed(false)
                .build();
    }

    @Test
    @DisplayName("GET /api/tasks/health - debe retornar status UP")
    void testHealthEndpoint() {
        ResponseEntity<Map<String, String>> response = taskController.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("service")).isEqualTo("To-Do List API");
    }

    @Test
    @DisplayName("GET /api/tasks - debe retornar lista de tareas")
    void testGetAllTasks() {
        when(taskService.getAllTasks(null)).thenReturn(Arrays.asList(sampleTask));

        ResponseEntity<List<Task>> response = taskController.getAllTasks(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTitle()).isEqualTo("Aprender CI/CD");
        verify(taskService, times(1)).getAllTasks(null);
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - debe retornar tarea por ID")
    void testGetTaskById() {
        when(taskService.getTaskById(1L)).thenReturn(sampleTask);

        ResponseEntity<Task> response = taskController.getTaskById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    @DisplayName("POST /api/tasks - debe crear tarea y retornar 201 CREATED")
    void testCreateTask() {
        when(taskService.createTask(any(Task.class))).thenReturn(sampleTask);

        ResponseEntity<Task> response = taskController.createTask(sampleTask);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Aprender CI/CD");
        verify(taskService, times(1)).createTask(sampleTask);
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - debe actualizar tarea existente")
    void testUpdateTask() {
        Task updateDetails = Task.builder()
                .title("Aprender CI/CD con AWS")
                .description("Actualizado")
                .completed(true)
                .build();

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updateDetails);

        ResponseEntity<Task> response = taskController.updateTask(1L, updateDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Aprender CI/CD con AWS");
        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/toggle - debe alternar estado de completado")
    void testToggleTask() {
        sampleTask.setCompleted(true);
        when(taskService.toggleTaskCompleted(1L)).thenReturn(sampleTask);

        ResponseEntity<Task> response = taskController.toggleTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isCompleted()).isTrue();
        verify(taskService, times(1)).toggleTaskCompleted(1L);
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - debe eliminar tarea")
    void testDeleteTask() {
        doNothing().when(taskService).deleteTask(1L);

        ResponseEntity<Map<String, Object>> response = taskController.deleteTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("deleted")).isEqualTo(true);
        verify(taskService, times(1)).deleteTask(1L);
    }
}
