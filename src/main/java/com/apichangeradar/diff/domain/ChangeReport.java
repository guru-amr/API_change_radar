package com.apichangeradar.diff.domain;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "change_reports", indexes = {
        @Index(name = "idx_report_api_created", columnList = "monitored_api_id, createdAt DESC")
})
@Getter
@Setter
public class ChangeReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitored_api_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reports_api"))
    private MonitoredApi monitoredApi;

    @Column(nullable = false)
    private Long fromSnapshotId;

    @Column(nullable = false)
    private Long toSnapshotId;

    @Column(nullable = false)
    private Integer fromVersion;

    @Column(nullable = false)
    private Integer toVersion;

    @Column(nullable = false)
    private int breakingCount;

    @Column(nullable = false)
    private int nonBreakingCount;

    @Column(nullable = false)
    private int deprecationCount;

    @Column(nullable = false)
    private boolean hasBreakingChanges;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "changeReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChangeItem> changeItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

