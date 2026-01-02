package com.snapcloud.api.domain;

import com.snapcloud.api.domain.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

  @Column(length = 200)
  private String stripeCustomerId;

  @Column(length = 200)
  private String stripeSubscriptionId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  private Instant currentPeriodStart;

  private Instant currentPeriodEnd;

  private Instant canceledAt;
}
