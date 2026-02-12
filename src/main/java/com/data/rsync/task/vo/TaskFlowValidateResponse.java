package com.data.rsync.task.vo;

import lombok.Data;
import java.util.List;

/**
 * 任务流程验证响应对象
 */
@Data
public class TaskFlowValidateResponse {
    private boolean valid;
    private String message;
    private List<String> errors;
    private List<String> warnings;
}
