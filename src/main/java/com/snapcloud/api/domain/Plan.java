package com.snapcloud.api.domain;

import com.snapcloud.api.domain.enums.PlanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
public class Plan {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true, length = 40)
  private PlanType type;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false)
  private long storageLimitBytes;

  @Column(nullable = false)
  private long monthlyUploadLimitBytes;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal monthlyPrice;

  @Column(length = 200)
  private String stripePriceId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
