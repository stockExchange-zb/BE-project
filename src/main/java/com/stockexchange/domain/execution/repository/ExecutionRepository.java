package com.stockexchange.domain.execution.repository;

import com.stockexchange.domain.execution.entity.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<ExecutionEntity, Long> {

//    사용자 체결 전체 내역 조회
    List<ExecutionEntity> findAllById(@Param("userId") Long userId);
}
