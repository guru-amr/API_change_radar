package com.apichangeradar.snapshot.domain;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "spec_snapshots", indexes = {
        @Index(name = "idx_snapshot_api_fetched", columnList = "monitored_api_id, fetchedAt DESC"),
        @Index(name = "idx_snapshot_api_version", columnList = "monitored_api_id, version")
})
@Getter
@Setter
public class SpecSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitored_api_id", nullable = false, foreignKey = @ForeignKey(name = "fk_snapshots_api"))
    private MonitoredApi monitoredApi;

    @Column(nullable = false)
    private Integer version;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false)
    private Instant fetchedAt;

    @PrePersist
    protected void onCreate() {
        fetchedAt = Instant.now();
    }
}

