package com.ebikes.routing.exceptions;

import java.io.Serial;

import com.ebikes.routing.enums.ResponseCode;

import lombok.Getter;

@Getter
public class ExternalServiceException extends BaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final String endpoint;

  public ExternalServiceException(
      String endpoint, String developerMessage, ResponseCode responseCode) {
    super(responseCode, developerMessage);
    this.endpoint = endpoint;
  }

  public ExternalServiceException(
      String endpoint, String developerMessage, ResponseCode responseCode, Throwable cause) {
    super(responseCode, developerMessage, cause);
    this.endpoint = endpoint;
  }
}
