package com.snapcloud.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Column(nullable = false, length = 120)
  private String passwordHash;

  @Column(nullable = false)
  private boolean emailVerified = false;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<ApiKey> apiKeys = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Image> images = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Subscription> subscriptions = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Payment> payments = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Usage> usageEntries = new ArrayList<>();
}
