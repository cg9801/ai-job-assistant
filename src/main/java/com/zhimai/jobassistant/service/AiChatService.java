package com.zhimai.jobassistant.service;

import com.zhimai.jobassistant.model.JobPosting;
import com.zhimai.jobassistant.model.MatchResult;
import com.zhimai.jobassistant.model.Resume;
import com.zhimai.jobassistant.repository.JobPostingRepository;
import com.zhimai.jobassistant.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 对话服务 - 负责和 LLM 交互
 *
 * 这里是 Spring AI 的核心用法：ChatClient
 */
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final RagService ragService;

    private ChatClient chatClient;

    /**
     * 懒初始化 ChatClient
     */
    private ChatClient getChatClient() {
        if (chatClient == null) {
            chatClient = chatClientBuilder.build();
        }
        return chatClient;
    }

    /**
     * 核心功能：分析简历与某个职位的匹配度
     * 使用 RAG 检索 + LLM 分析
     */
    public MatchResult analyzeMatch(String username, Long jobId) {
        Resume resume = resumeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("未找到简历"));
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("未找到职位"));

        // 构造 Prompt：把简历和 JD 都喂给 LLM
        String prompt = """
                你是一个专业的求职顾问。请分析以下简历和职位的匹配情况。

                === 简历内容 ===
                %s

                === 目标职位 ===
                岗位：%s
                公司：%s
                技术要求：%s
                岗位描述：%s

                请按以下格式返回：
                1. 匹配度评分（0-100）
                2. 匹配的技术栈（简历中有且岗位要求的）
                3. 缺失的技术栈（岗位要求但简历中没有的）
                4. 具体改进建议（如何提升匹配度）
                """.formatted(
                        resume.getContent(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getTechStack(),
                        job.getDescription()
                );

        String response = getChatClient().prompt()
                .user(prompt)
                .call()
                .content();

        return MatchResult.builder()
                .job(job)
                .advice(response)
                .build();
    }

    /**
     * 自由问答：根据简历上下文回答用户问题
     * 这是 RAG 的完整流程：检索 → 注入上下文 → 生成
     */
    public String chat(String username, String question) {
        Resume resume = resumeRepository.findByUsername(username)
                .orElse(null);

        // RAG 检索相关职位作为上下文
        String resumeContext = resume != null ? resume.getContent() : "暂无简历";
        List<JobPosting> allJobs = jobPostingRepository.findAll();
        String jobContext = allJobs.stream()
                .limit(10)  // 限制上下文长度
                .map(j -> j.getTitle() + " @ " + j.getCompany() + " - " + j.getTechStack())
                .reduce("", (a, b) -> a + "\n" + b);

        String systemPrompt = """
                你是一个专业的 AI 求职顾问。基于以下上下文回答用户问题。

                === 用户简历 ===
                %s

                === 已收录职位 ===
                %s

                请给出专业、具体、可操作的建议。
                """.formatted(resumeContext, jobContext);

        return getChatClient().prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();
    }
}
