package com.stockexchange.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_krw_price")
    private Long userKrwPrice;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
