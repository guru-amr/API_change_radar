package com.apichangeradar.alert.domain;

import com.apichangeradar.diff.domain.ChangeReport;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "alert_logs", indexes = {
        @Index(name = "idx_alertlog_report", columnList = "change_report_id"),
        @Index(name = "idx_alertlog_status", columnList = "status")
})
@Getter
@Setter
public class AlertLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_report_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alertlogs_report"))
    private ChangeReport changeReport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertChannelType channelType;

    @Column(nullable = false, length = 2048)
    private String target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private Instant sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = Instant.now();
    }
}

