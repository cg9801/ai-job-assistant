package com.zhimai.jobassistant.service;

import com.zhimai.jobassistant.model.JobPosting;
import com.zhimai.jobassistant.model.MatchResult;
import com.zhimai.jobassistant.model.Resume;
import com.zhimai.jobassistant.repository.JobPostingRepository;
import com.zhimai.jobassistant.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG 核心服务 - 简历与职位的语义匹配
 *
 * 流程：
 * 1. 将 JD 文本 Embedding 后存入向量数据库
 * 2. 用简历内容做相似度检索
 * 3. 检索到的相似 JD 作为上下文，交给 LLM 分析
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final VectorStore vectorStore;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeRepository resumeRepository;

    /**
     * 将职位 JD 存入向量数据库
     * 把 JD 的关键信息拼成一段文本，Embedding 后存储
     */
    public void indexJobPostings(List<JobPosting> jobs) {
        List<Document> documents = jobs.stream()
                .map(job -> {
                    String content = buildJobContent(job);
                    Map<String, Object> metadata = Map.of(
                            "jobId", job.getId(),
                            "title", job.getTitle(),
                            "company", job.getCompany(),
                            "location", job.getLocation() != null ? job.getLocation() : "",
                            "salaryRange", job.getSalaryRange() != null ? job.getSalaryRange() : ""
                    );
                    return new Document(content, metadata);
                })
                .collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("已索引 {} 条职位到向量数据库", documents.size());
    }

    /**
     * 根据简历内容，检索最匹配的职位
     * 这就是 RAG 中的 "Retrieval" 步骤
     */
    public List<Document> retrieveMatchingJobs(String resumeContent, int topK) {
        SearchRequest request = SearchRequest.query(resumeContent)
                .withTopK(topK)
                .withSimilarityThreshold(0.5);

        return vectorStore.similaritySearch(request);
    }

    /**
     * 一键匹配：上传简历后，返回匹配结果列表
     */
    public List<MatchResult> matchResumeToJobs(String username, int topK) {
        // 1. 获取用户简历
        Resume resume = resumeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("未找到用户简历，请先上传"));

        // 2. RAG 检索相似职位
        List<Document> similarDocs = retrieveMatchingJobs(resume.getContent(), topK);

        // 3. 组装结果
        List<MatchResult> results = new ArrayList<>();
        for (Document doc : similarDocs) {
            Long jobId = (Long) doc.getMetadata().get("jobId");
            JobPosting job = jobPostingRepository.findById(jobId).orElse(null);
            if (job == null) continue;

            MatchResult result = MatchResult.builder()
                    .job(job)
                    .matchScore(calculateScore(doc.getScore()))
                    .build();
            results.add(result);
        }

        return results;
    }

    /**
     * 拼接 JD 关键信息为一段文本（用于 Embedding）
     */
    private String buildJobContent(JobPosting job) {
        StringBuilder sb = new StringBuilder();
        sb.append("岗位：").append(job.getTitle()).append("\n");
        sb.append("公司：").append(job.getCompany()).append("\n");
        if (job.getLocation() != null) sb.append("地点：").append(job.getLocation()).append("\n");
        if (job.getSalaryRange() != null) sb.append("薪资：").append(job.getSalaryRange()).append("\n");
        if (job.getEducation() != null) sb.append("学历：").append(job.getEducation()).append("\n");
        if (job.getExperience() != null) sb.append("经验：").append(job.getExperience()).append("\n");
        if (job.getTechStack() != null) sb.append("技术栈：").append(job.getTechStack()).append("\n");
        if (job.getDescription() != null) sb.append("描述：").append(job.getDescription());
        return sb.toString();
    }

    /**
     * 将向量相似度分数转为 0-100 的匹配度
     */
    private int calculateScore(Double score) {
        if (score == null) return 50;
        // PgVector 的 COSINE_DISTANCE 返回 0-2，越小越相似
        // 转换为 0-100 的匹配度
        return Math.max(0, Math.min(100, (int) ((1 - score / 2) * 100)));
    }
}
