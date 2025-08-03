package com.example.backend.controller;

import com.example.backend.auth.RequireLogin;
import com.example.backend.dto.JobStatisticsDTO;
import com.example.backend.entity.Job;
import com.example.backend.service.JobService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequireLogin
    @GetMapping
    public List<Job> list() {
        return jobService.findAll();
    }

    @RequireLogin
    @PostMapping
    public Job add(@RequestBody Job job) {
        return jobService.add(job);
    }

    @RequireLogin
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jobService.delete(id);
    }

    @Deprecated
    @RequireLogin
    @GetMapping("/statistics")
    public JobStatisticsDTO statistics() {
        JobStatisticsDTO dto = new JobStatisticsDTO();
        List<String> months = jdbcTemplate.query("SELECT strftime('%Y-%m', created_at) as month FROM jobs GROUP BY month ORDER BY month", (rs, rowNum) -> rs.getString("month"));
        List<Integer> counts = jdbcTemplate.query("SELECT COUNT(*) as cnt FROM jobs GROUP BY strftime('%Y-%m', created_at) ORDER BY strftime('%Y-%m', created_at)", (rs, rowNum) -> rs.getInt("cnt"));
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM jobs", Integer.class);
        dto.setMonths(months);
        dto.setCounts(counts);
        dto.setTotal(total == null ? 0 : total);
        return dto;
    }

    @Deprecated
    @RequireLogin
    @GetMapping("/page")
    public Page<Job> page(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String title, @RequestParam(required = false) String company, @RequestParam(required = false) String city) {
        Specification<Job> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(root.get("title"), "%" + title + "%"));
            }
            if (company != null && !company.isEmpty()) {
                predicates.add(cb.like(root.get("company"), "%" + company + "%"));
            }
            if (city != null && !city.isEmpty()) {
                predicates.add(cb.like(root.get("city"), "%" + city + "%"));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return jobService.findPage(spec, page, size);
    }

    @Deprecated
    @RequireLogin
    @GetMapping("/native-page")
    public Page<Job> nativePage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String title, @RequestParam(required = false) String company, @RequestParam(required = false) String city) {
        // 参数校验
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;
        return jobService.findPageByNativeSql(page, size, title, company, city);
    }

    /**
     * 查询仓库中的职位信息
     *
     * @param page 页码，从0开始
     * @return
     */
    @RequireLogin
    @GetMapping("/repo-page")
    public Page<Job> repoPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String title, @RequestParam(required = false) String company, @RequestParam(required = false) String city, @RequestParam(required = false) String description, @RequestParam(required = false) String query_type, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;
        return jobService.findPageByRepository(page, size, title, company, city, description, query_type, startDate, endDate);
    }

    /**
     * chart.html页面访问测试使用
     *
     * @return
     */
    @GetMapping("/chart-data")
    public List<Map<String, Object>> getChartData() {


        String sql = "SELECT city, query_type, DATE(created_at) as date, COUNT(*) as job_count " + "FROM jobs " + "WHERE created_at >= '2025-07-01' AND created_at < '2025-09-01' " + // 7月1日及以后，9月1日之前
                "GROUP BY city, query_type, DATE(created_at) " + "ORDER BY city, query_type, date";
        log.info("测试统计的sql语句：{}", sql);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("city", rs.getString("city"));
            row.put("query_type", rs.getString("query_type"));
            row.put("date", rs.getString("date"));
            row.put("job_count", rs.getInt("job_count"));
            return row;
        });
    }

    /**
     * 获取按年统计的职位数据，支持区间筛选
     *
     * @param startDate 开始日期（如2018-09-01）
     * @param endDate   结束日期（如2019-10-01）
     * @return List<Map < String, Object>>    职位数据
     */
    @GetMapping("/chart-data-year")
    public List<Map<String, Object>> getChartDataYear(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {


        StringBuilder sql = new StringBuilder("SELECT city, query_type, DATE(created_at) as date, COUNT(*) as job_count FROM jobs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND created_at >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND created_at < ?");
            params.add(endDate);
        }
        sql.append(" GROUP BY city, query_type, DATE(created_at) ORDER BY city, query_type, date");

        log.info("按年统计的sql语句：{},params:{}", sql, params.toArray());

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("city", rs.getString("city"));
            row.put("query_type", rs.getString("query_type"));
            row.put("date", rs.getString("date"));
            row.put("job_count", rs.getInt("job_count"));
            return row;
        });
    }
}
