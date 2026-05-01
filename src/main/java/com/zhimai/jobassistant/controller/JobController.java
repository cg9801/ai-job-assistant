package com.zhimai.jobassistant.controller;

import com.zhimai.jobassistant.model.ApiResponse;
import com.zhimai.jobassistant.model.JobPosting;
import com.zhimai.jobassistant.repository.JobPostingRepository;
import com.zhimai.jobassistant.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理接口
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobPostingRepository jobPostingRepository;
    private final FileService fileService;

    /**
     * 获取所有职位
     */
    @GetMapping
    public ApiResponse<List<JobPosting>> getAllJobs() {
        return ApiResponse.success(jobPostingRepository.findAll());
    }

    /**
     * 按关键词搜索职位
     */
    @GetMapping("/search")
    public ApiResponse<List<JobPosting>> searchJobs(@RequestParam String keyword) {
        List<JobPosting> results = jobPostingRepository.findByTitleContaining(keyword);
        results.addAll(jobPostingRepository.findByTechStackContaining(keyword));
        return ApiResponse.success(results);
    }

    /**
     * 添加单个职位
     */
    @PostMapping
    public ApiResponse<JobPosting> addJob(@RequestBody JobPosting job) {
        JobPosting saved = jobPostingRepository.save(job);
        return ApiResponse.success(saved);
    }

    /**
     * 批量导入职位
     */
    @PostMapping("/batch")
    public ApiResponse<List<JobPosting>> importJobs(@RequestBody List<JobPosting> jobs) {
        List<JobPosting> saved = fileService.importJobs(jobs);
        return ApiResponse.success(saved);
    }
}
