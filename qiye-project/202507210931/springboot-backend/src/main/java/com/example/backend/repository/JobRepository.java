package com.example.backend.repository;

import com.example.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    @Query(value = "SELECT * FROM jobs WHERE (:title IS NULL OR title LIKE %:title%) AND (:company IS NULL OR company LIKE %:company%) AND (:city IS NULL OR city LIKE %:city%) AND (:description IS NULL OR description LIKE %:description%) AND (:queryType IS NULL OR query_type LIKE %:queryType%) AND ((:startDate IS NULL) OR (created_at >= :startDate)) AND ((:endDate IS NULL) OR (created_at <= :endDate)) ORDER BY created_at DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<Job> findJobsByConditions(@Param("title") String title, @Param("company") String company, @Param("city") String city, @Param("description") String description, @Param("queryType") String queryType, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM jobs WHERE (:title IS NULL OR title LIKE %:title%) AND (:company IS NULL OR company LIKE %:company%) AND (:city IS NULL OR city LIKE %:city%) AND (:description IS NULL OR description LIKE %:description%) AND (:queryType IS NULL OR query_type  LIKE %:queryType%) AND ((:startDate IS NULL) OR (created_at >= :startDate)) AND ((:endDate IS NULL) OR (created_at <= :endDate))", nativeQuery = true)
    int countJobsByConditions(@Param("title") String title, @Param("company") String company, @Param("city") String city, @Param("description") String description, @Param("queryType") String queryType, @Param("startDate") String startDate, @Param("endDate") String endDate);
}
