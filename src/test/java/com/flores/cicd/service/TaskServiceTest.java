package com.flores.cicd.service;

import com.flores.cicd.exception.ResourceNotFoundException;
import com.flores.cicd.model.Task;
import com.flores.cicd.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Estudiar CI/CD")
                .description("Aprender a configurar un pipeline con 5 etapas")
                .completed(false)
                .build();
    }

    @Test
    @DisplayName("Debe retornar todas las tareas")
    void testGetAllTasks() {
        when(taskRepository.findAll()).thenReturn(Arrays.asList(sampleTask));

        List<Task> tasks = taskService.getAllTasks(null);

        assertThat(tasks).isNotEmpty();
        assertThat(tasks.size()).isEqualTo(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Estudiar CI/CD");
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar tareas filtradas por completed")
    void testGetTasksByCompleted() {
        when(taskRepository.findByCompleted(true)).thenReturn(List.of());

        List<Task> tasks = taskService.getAllTasks(true);

        assertThat(tasks).isEmpty();
        verify(taskRepository, times(1)).findByCompleted(true);
    }

    @Test
    @DisplayName("Debe retornar una tarea por su ID si existe")
    void testGetTaskByIdSuccess() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task task = taskService.getTaskById(1L);

        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(1L);
        assertThat(task.getTitle()).isEqualTo("Estudiar CI/CD");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la tarea no existe")
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No se encontró la tarea con ID: 99");

        verify(taskRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe crear una nueva tarea exitosamente")
    void testCreateTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        Task created = taskService.createTask(sampleTask);

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Estudiar CI/CD");
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    @DisplayName("Debe actualizar una tarea existente")
    void testUpdateTask() {
        Task updateDetails = Task.builder()
                .title("Estudiar CI/CD avanzado")
                .description("Configurar GitLab CI y AWS EC2")
                .completed(true)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = taskService.updateTask(1L, updateDetails);

        assertThat(updated.getTitle()).isEqualTo("Estudiar CI/CD avanzado");
        assertThat(updated.getDescription()).isEqualTo("Configurar GitLab CI y AWS EC2");
        assertThat(updated.isCompleted()).isTrue();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Debe alternar (toggle) el estado completed de una tarea")
    void testToggleTaskCompleted() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task toggled = taskService.toggleTaskCompleted(1L);

        assertThat(toggled.isCompleted()).isTrue();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Debe eliminar una tarea existente")
    void testDeleteTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        doNothing().when(taskRepository).delete(sampleTask);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).delete(sampleTask);
    }
}
