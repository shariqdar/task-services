package com.example.batch.dto;

import java.util.List;

public record TaskResponse(List<TaskDTO> tasks, long totalRecords, int page, int size) {
}
