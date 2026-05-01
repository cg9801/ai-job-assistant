package com.zhimai.jobassistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户简历实体
 */
@Entity
@Table(name = "resume")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名 */
    private String username;

    /** 原始文件名 */
    private String fileName;

    /** 简历文本内容（解析后） */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 技能标签（逗号分隔） */
    @Column(columnDefinition = "TEXT")
    private String skills;

    /** 上传时间 */
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
