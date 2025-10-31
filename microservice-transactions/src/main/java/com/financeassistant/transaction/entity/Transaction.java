package com.financeassistant.transaction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity{

    @Getter @Setter
    @Column(nullable = false)
    private BigDecimal amount;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDate date;

    @Getter @Setter
    private String description;

    @Getter @Setter
    @Column(nullable = false)
    private Long userId;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Getter @Setter
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
