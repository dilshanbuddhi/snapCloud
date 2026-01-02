package com.snapcloud.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "usage_entries",
    indexes = {
      @Index(name = "idx_usage_user_id", columnList = "user_id"),
      @Index(name = "idx_usage_period_start", columnList = "period_start")
    })
@Getter
@Setter
public class Usage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "period_start", nullable = false)
  private Instant periodStart;

  @Column(name = "period_end", nullable = false)
  private Instant periodEnd;

  @Column(nullable = false)
  private long storageUsedBytes;

  @Column(nullable = false)
  private long uploadBytes;

  @Column(nullable = false)
  private long apiRequests;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
