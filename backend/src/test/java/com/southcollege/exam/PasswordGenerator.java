package com.southcollege.exam;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Generate hash for "123456"
        String password = "123456";
        String hash = encoder.encode(password);

        System.out.println("BCrypt hash for '123456':");
        System.out.println(hash);

        // Test verification
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification result: " + matches);

        // Generate SQL statements
        System.out.println("\nSQL statements for init.sql:");
        System.out.println("INSERT INTO users (username, password, nickname, role, status) VALUES");
        System.out.println("('admin', '" + hash + "', '系统管理员', 'ADMIN', 'ACTIVE'),");
        System.out.println("('teacher1', '" + hash + "', '张老师', 'TEACHER', 'ACTIVE'),");
        System.out.println("('teacher2', '" + hash + "', '李老师', 'TEACHER', 'ACTIVE'),");
        System.out.println("('teacher3', '" + hash + "', '王老师', 'TEACHER', 'ACTIVE');");
        System.out.println("INSERT INTO users (username, password, nickname, role, status) VALUES");
        System.out.println("('student1', '" + hash + "', '张三', 'STUDENT', 'ACTIVE'),");
        System.out.println("('student2', '" + hash + "', '李四', 'STUDENT', 'ACTIVE'),");
        System.out.println("('student3', '" + hash + "', '王五', 'STUDENT', 'ACTIVE'),");
        System.out.println("('student4', '" + hash + "', '赵六', 'STUDENT', 'ACTIVE'),");
        System.out.println("('student5', '" + hash + "', '钱七', 'STUDENT', 'ACTIVE');");
    }
}
