package com.example.backend.service;

import com.example.backend.entity.Job;
import com.example.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * * 自定义sqllite方言无法处理分页
     */
    @Deprecated
    public Page<Job> findPage(Specification<Job> spec, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jobRepository.findAll(spec, pageable);
    }

    public Job add(Job job) {
        return jobRepository.save(job);
    }

    public void delete(Long id) {
        jobRepository.deleteById(id);
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public Page<Job> findPageByNativeSql(int page, int size, String title, String company, String city) {
        int offset = page * size;
        StringBuilder sql = new StringBuilder("SELECT * FROM jobs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (title != null && !title.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }
        if (company != null && !company.isEmpty()) {
            sql.append(" AND company LIKE ?");
            params.add("%" + company + "%");
        }
        if (city != null && !city.isEmpty()) {
            sql.append(" AND city LIKE ?");
            params.add("%" + city + "%");
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);
        // 使用 JdbcTemplate 查询
        List<Job> jobs = jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> {
                    Job job = new Job();
                    job.setId(rs.getLong("id"));
                    job.setTitle(rs.getString("title"));
                    job.setCompany(rs.getString("company"));
                    job.setLocation(rs.getString("location"));
                    job.setSalary(rs.getString("salary"));
                    job.setExperience(rs.getString("experience"));
                    job.setEducation(rs.getString("education"));
                    job.setDescription(rs.getString("description"));
                    job.setUrl(rs.getString("url"));
                    job.setQueryType(rs.getString("query_type"));
                    job.setCity(rs.getString("city"));
                    job.setCreatedAt(rs.getString("created_at"));
                    return job;
                }, params.toArray());
        // 查询总数
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM jobs WHERE 1=1");
        List<Object> countParams = new ArrayList<>();
        if (title != null && !title.isEmpty()) {
            countSql.append(" AND title LIKE ?");
            countParams.add("%" + title + "%");
        }
        if (company != null && !company.isEmpty()) {
            countSql.append(" AND company LIKE ?");
            countParams.add("%" + company + "%");
        }
        if (city != null && !city.isEmpty()) {
            countSql.append(" AND city LIKE ?");
            countParams.add("%" + city + "%");
        }
        int total = jdbcTemplate.queryForObject(countSql.toString(), countParams.toArray(), Integer.class);
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(jobs, pageable, total);
    }

 

    public Page<Job> findPageByRepository(int page, int size, String title, String company, String city, String description, String queryType, String startDate, String endDate) {
        int offset = page * size;
        List<Job> jobs = jobRepository.findJobsByConditions(title, company, city, description, queryType, startDate, endDate, size, offset);
        int total = jobRepository.countJobsByConditions(title, company, city, description, queryType, startDate, endDate);
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(jobs, pageable, total);
    }
}
