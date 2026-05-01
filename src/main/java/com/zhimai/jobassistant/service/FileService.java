package com.zhimai.jobassistant.service;

import com.zhimai.jobassistant.model.JobPosting;
import com.zhimai.jobassistant.model.Resume;
import com.zhimai.jobassistant.repository.JobPostingRepository;
import com.zhimai.jobassistant.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * 文件处理服务 - 简历解析、JD 导入
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final RagService ragService;

    private final Tika tika = new Tika();

    /**
     * 上传并解析简历（支持 PDF、DOCX、TXT）
     */
    public Resume uploadResume(String username, MultipartFile file) throws IOException {
        // 1. 用 Tika 解析文件内容
        String content = tika.parseToString(file.getInputStream());

        // 2. 保存到数据库
        Resume resume = Resume.builder()
                .username(username)
                .fileName(file.getOriginalFilename())
                .content(content)
                .skills(extractSkills(content))
                .build();

        Resume saved = resumeRepository.save(resume);
        log.info("用户 {} 上传简历：{}", username, file.getOriginalFilename());
        return saved;
    }

    /**
     * 批量导入 JD（从 JSON 格式）
     */
    public List<JobPosting> importJobs(List<JobPosting> jobs) {
        List<JobPosting> saved = jobPostingRepository.saveAll(jobs);
        // 同时索引到向量数据库
        ragService.indexJobPostings(saved);
        log.info("导入 {} 条职位", saved.size());
        return saved;
    }

    /**
     * 简单的技能提取（基于关键词匹配）
     * 生产环境可以用 NLP 或 AI 来做
     */
    private String extractSkills(String content) {
        String[] keywords = {
                "Java", "Python", "Spring", "MyBatis", "MySQL", "Redis",
                "Docker", "Kubernetes", "Linux", "Git", "Maven", "Gradle",
                "React", "Vue", "JavaScript", "TypeScript", "HTML", "CSS",
                "微服务", "分布式", "高并发", "消息队列", "Kafka", "RabbitMQ",
                "Elasticsearch", "MongoDB", "PostgreSQL", "Nginx", "Jenkins"
        };

        StringBuilder skills = new StringBuilder();
        String lowerContent = content.toLowerCase();
        for (String keyword : keywords) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                if (skills.length() > 0) skills.append(",");
                skills.append(keyword);
            }
        }
        return skills.toString();
    }
}
