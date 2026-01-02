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
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "images",
    indexes = {
      @Index(name = "idx_images_user_id", columnList = "user_id"),
      @Index(name = "idx_images_public_id", columnList = "public_id", unique = true)
    })
@Getter
@Setter
public class Image {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "public_id", nullable = false, unique = true)
  private UUID publicId = UUID.randomUUID();

  @Column(nullable = false, length = 400)
  private String originalFilename;

  @Column(nullable = false, length = 120)
  private String contentType;

  @Column(nullable = false)
  private long sizeBytes;

  @Column(nullable = false, length = 600)
  private String s3Key;

  @Column(length = 600)
  private String s3Bucket;

  @Column(length = 600)
  private String cdnUrl;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
