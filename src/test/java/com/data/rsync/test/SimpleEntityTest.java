package com.data.rsync.test;

import com.data.rsync.task.entity.SimpleTaskEntity;
import java.time.LocalDateTime;

public class SimpleEntityTest {
    public static void main(String[] args) {
        SimpleTaskEntity task = new SimpleTaskEntity();
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
        
        System.out.println("SimpleTaskEntity test passed!");
    }
}