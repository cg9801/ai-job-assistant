package com.zhimai.jobassistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 职位实体 - 存储 JD（岗位描述）
 */
@Entity
@Table(name = "job_posting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 岗位名称 */
    private String title;

    /** 公司名称 */
    private String company;

    /** 工作地点 */
    private String location;

    /** 薪资范围 */
    private String salaryRange;

    /** 学历要求 */
    private String education;

    /** 经验要求 */
    private String experience;

    /** 技术要求（逗号分隔） */
    @Column(columnDefinition = "TEXT")
    private String techStack;

    /** 岗位描述原文 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 来源平台 */
    private String source;

    /** 创建时间 */
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
