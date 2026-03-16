package com.ebikes.routing.exceptions;

import java.io.Serial;

import com.ebikes.routing.enums.ResponseCode;

public class RateLimitException extends BaseException {

  @Serial private static final long serialVersionUID = 1L;

  public RateLimitException(ResponseCode code, String developerMessage) {
    super(code, developerMessage);
  }

  public RateLimitException(ResponseCode code, String developerMessage, Throwable cause) {
    super(code, developerMessage, cause);
  }
}
