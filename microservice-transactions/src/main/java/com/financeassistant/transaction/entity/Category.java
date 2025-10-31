package com.financeassistant.transaction.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity{

    @Getter @Setter
    @Column(nullable = false, unique = true)
    private String name;

    @Getter @Setter
    @OneToMany(mappedBy = "category")
    private Set<Transaction> transactions;
}
