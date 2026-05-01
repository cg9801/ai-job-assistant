package com.zhimai.jobassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 匹配结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    /** 职位信息 */
    private JobPosting job;

    /** 匹配度分数 0-100 */
    private int matchScore;

    /** 匹配的技术栈 */
    private String matchedSkills;

    /** 缺失的技术栈 */
    private String missingSkills;

    /** AI 给出的建议 */
    private String advice;
}
