package com.financeassistant.transaction.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "categories")
@Getter @Setter
public class Category extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @OneToMany(mappedBy = "category")
    private Set<Transaction> transactions;
}
