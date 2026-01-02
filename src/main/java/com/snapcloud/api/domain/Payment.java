package com.snapcloud.api.domain;

import com.snapcloud.api.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "payments",
    indexes = {
      @Index(name = "idx_payments_user_id", columnList = "user_id"),
      @Index(name = "idx_payments_stripe_payment_intent", columnList = "stripe_payment_intent_id", unique = true)
    })
@Getter
@Setter
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  private Subscription subscription;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency = "usd";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 60)
  private PaymentStatus status;

  @Column(name = "stripe_payment_intent_id", length = 200)
  private String stripePaymentIntentId;

  @Column(name = "stripe_charge_id", length = 200)
  private String stripeChargeId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
