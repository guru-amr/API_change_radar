package com.apichangeradar.diff.domain;

import com.apichangeradar.common.domain.ChangeSeverity;
import com.apichangeradar.common.domain.ChangeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "change_items", indexes = {
        @Index(name = "idx_item_report", columnList = "change_report_id")
})
@Getter
@Setter
public class ChangeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_report_id", nullable = false, foreignKey = @ForeignKey(name = "fk_items_report"))
    private ChangeReport changeReport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ChangeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeSeverity severity;

    @Column(length = 500)
    private String path;

    @Column(nullable = false, length = 500)
    private String description;
}

