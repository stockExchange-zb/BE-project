package com.stockexchange.domain.stock.repository;

import com.stockexchange.domain.stock.dto.StockResDTO;
import com.stockexchange.domain.stock.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, Long> {

//    종목 전체 조회
    @Query("""
                SELECT new com.stockexchange.domain.stock.dto.StockResDTO(
                stockId,
                stockName,
                stockNumber,
                stockIpo,
                stockPrice
                )
                FROM StockEntity
            """
    )
    List<StockResDTO> findAllStocks();

//    종목 상세 조회
    @Query("""
            SELECT new com.stockexchange.domain.stock.dto.StockResDTO(
                stockId,
                stockName,
                stockNumber,
                stockIpo,
                stockPrice
            )
            FROM StockEntity
            WHERE stockId = :stockId
            """)
    StockResDTO findByStockId(@Param("stockId") Long stockId);
}
