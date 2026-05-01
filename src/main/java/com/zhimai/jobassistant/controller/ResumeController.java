package com.zhimai.jobassistant.controller;

import com.zhimai.jobassistant.model.ApiResponse;
import com.zhimai.jobassistant.model.MatchResult;
import com.zhimai.jobassistant.model.Resume;
import com.zhimai.jobassistant.service.AiChatService;
import com.zhimai.jobassistant.service.FileService;
import com.zhimai.jobassistant.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 简历与匹配相关接口
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResumeController {

    private final FileService fileService;
    private final RagService ragService;
    private final AiChatService aiChatService;

    /**
     * 上传简历
     * POST /api/resume/upload?username=zhimai
     */
    @PostMapping("/resume/upload")
    public ApiResponse<Resume> uploadResume(
            @RequestParam String username,
            @RequestParam("file") MultipartFile file) {
        try {
            Resume resume = fileService.uploadResume(username, file);
            return ApiResponse.success(resume);
        } catch (IOException e) {
            return ApiResponse.error("简历解析失败: " + e.getMessage());
        }
    }

    /**
     * 简历匹配职位（基于 RAG 语义检索）
     * GET /api/match?username=zhimai&topK=5
     */
    @GetMapping("/match")
    public ApiResponse<List<MatchResult>> matchJobs(
            @RequestParam String username,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            List<MatchResult> results = ragService.matchResumeToJobs(username, topK);
            return ApiResponse.success(results);
        } catch (Exception e) {
            return ApiResponse.error("匹配失败: " + e.getMessage());
        }
    }

    /**
     * 深度分析：简历 vs 具体职位
     * GET /api/analyze?username=zhimai&jobId=1
     */
    @GetMapping("/analyze")
    public ApiResponse<MatchResult> analyzeMatch(
            @RequestParam String username,
            @RequestParam Long jobId) {
        try {
            MatchResult result = aiChatService.analyzeMatch(username, jobId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * AI 求职问答（RAG 增强对话）
     * POST /api/chat
     */
    @PostMapping("/chat")
    public ApiResponse<String> chat(
            @RequestParam String username,
            @RequestParam String question) {
        try {
            String answer = aiChatService.chat(username, question);
            return ApiResponse.success(answer);
        } catch (Exception e) {
            return ApiResponse.error("对话失败: " + e.getMessage());
        }
    }
}
