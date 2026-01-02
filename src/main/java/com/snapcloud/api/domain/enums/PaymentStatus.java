package com.snapcloud.api.domain.enums;

public enum PaymentStatus {
  REQUIRES_PAYMENT_METHOD,
  REQUIRES_CONFIRMATION,
  PROCESSING,
  SUCCEEDED,
  FAILED,
  CANCELED
}
