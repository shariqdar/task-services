package com.example.batch.service;

import com.example.batch.dto.TaskDTO;
import com.example.batch.dto.TaskResponse;
import com.example.batch.model.Task;
import com.example.batch.repository.TaskRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }


    private TaskDTO convertToDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted()
        );
    }

    private Task convertToTask(TaskDTO taskDTO) {
        Task task = new Task();
        task.setId(taskDTO.id());
        task.setTitle(taskDTO.title());
        task.setDescription(taskDTO.description());
        task.setCompleted(taskDTO.completed());
        return task;
    }

    public TaskResponse getAllTasks(int page, int size) {
        Pair<List<Task>, Long> result =  taskRepository.findAll(page, size);
        List<Task> tasks = result.getLeft();
        List<TaskDTO> tasksDTO = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new TaskResponse(tasksDTO, result.getRight(), page, tasks.size());
    }

    public Optional<TaskDTO> getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return Optional.ofNullable(task.map(this::convertToDTO).orElse(null));
    }

    public TaskDTO createTask(TaskDTO taskDTO) {
        Task task = taskRepository.save(convertToTask(taskDTO));
        return convertToDTO(task);
    }

    public int updateTask(Long id, TaskDTO updatedTask) {
        if (taskRepository.findById(id).isEmpty()) {
            return 0;
        }

        Task task = convertToTask(updatedTask);
        task.setId(id);
        return taskRepository.update(task);


    }

    public boolean deleteTask(Long id) {
        if (taskRepository.findById(id).isEmpty()) {
            return false;
        }

        taskRepository.delete(id);
        return true;
    }
}

