package com.data.rsync.test;

import com.data.rsync.task.entity.TaskEntity;
import java.time.LocalDateTime;

public class EntityTest {
    public static void main(String[] args) {
        TaskEntity task = new TaskEntity();
        task.setName("Test Task");
        task.setType("FULL_SYNC");
        task.setDataSourceId(1L);
        task.setStatus("PENDING");
        task.setEnabled(true);
        task.setUpdateTime(LocalDateTime.now());
        
        System.out.println("Task ID: " + task.getId());
        System.out.println("Task Name: " + task.getName());
        System.out.println("Task Status: " + task.getStatus());
        System.out.println("Task Enabled: " + task.getEnabled());
        System.out.println("Task Update Time: " + task.getUpdateTime());
    }
}