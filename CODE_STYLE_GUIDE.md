# 代码风格指南

## 1. 概述

本文档定义了Data Rsync项目的代码风格规范，旨在确保代码的一致性、可读性和可维护性。

## 2. Lombok使用规范

### 2.1 实体类（Entity）

**所有实体类必须使用Lombok的@Data注解**，以自动生成setter、getter、toString、equals和hashCode方法。

**示例：**

```java
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@TableName("task")
public class TaskEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "name", exist = true)
    private String name;
    
    // 其他字段...
}
```

### 2.2 值对象（VO/DTO）

**所有VO和DTO类必须使用Lombok的@Data注解**，以自动生成setter、getter、toString、equals和hashCode方法。

**示例：**

```java
import lombok.Data;

@Data
public class MilvusSyncResponse {
    private boolean success;
    private long totalCount;
    private long syncedCount;
    // 其他字段...
}
```

### 2.3 构造方法

- 对于需要自定义初始化逻辑的类，可以添加无参构造方法
- 对于需要Builder模式的类，可以使用@Builder注解

**示例：**

```java
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataSourceEntity {
    private Long id;
    private String name;
    // 其他字段...
    
    public DataSourceEntity() {
        // 初始化逻辑
    }
    
    // Builder构造方法（当使用@Builder时需要）
    public DataSourceEntity(Long id, String name, /* 其他参数 */) {
        this.id = id;
        this.name = name;
        // 其他赋值
    }
}
```

## 3. Maven配置规范

### 3.1 Lombok依赖

在pom.xml中，Lombok依赖应配置为：

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

### 3.2 编译器插件配置

Maven编译器插件应配置annotationProcessorPaths，确保Lombok作为注解处理器：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.10.1</version>
    <configuration>
        <source>${java.version}</source>
        <target>${java.version}</target>
        <encoding>UTF-8</encoding>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## 4. 代码质量检查

- **禁止手动添加setter/getter方法**：所有实体类和VO类必须使用Lombok的@Data注解
- **禁止重复代码**：避免在多个类中重复相同的代码逻辑
- **保持代码简洁**：使用Lombok注解减少样板代码

## 5. 最佳实践

1. **统一使用@Data注解**：确保所有实体类和VO类都使用@Data注解
2. **合理使用其他Lombok注解**：如@Builder、@ToString等，根据需要选择合适的注解
3. **遵循命名规范**：类名使用大驼峰命名法，字段名使用小驼峰命名法
4. **保持注释清晰**：为重要字段和方法添加适当的注释

## 6. 总结

通过统一使用Lombok注解，可以：

1. **减少样板代码**：自动生成setter、getter等方法，减少代码量
2. **提高代码质量**：避免手动编写setter/getter方法时可能出现的错误
3. **增强代码可读性**：使代码更加简洁明了
4. **便于维护**：当字段发生变化时，不需要手动更新setter/getter方法

所有开发人员必须严格遵守本规范，确保项目代码的一致性和可维护性。