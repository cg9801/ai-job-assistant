package com.zhimai.jobassistant.repository;

import com.zhimai.jobassistant.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByTitleContaining(String title);
    List<JobPosting> findByCompanyContaining(String company);
    List<JobPosting> findByTechStackContaining(String tech);
}
