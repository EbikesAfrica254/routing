package com.ebikes.routing.exceptions;

import java.io.Serial;

import com.ebikes.routing.enums.ResponseCode;

public class ResourceNotFoundException extends BaseException {

  @Serial private static final long serialVersionUID = 1L;

  public ResourceNotFoundException(ResponseCode code, String developerMessage) {
    super(code, developerMessage);
  }

  public ResourceNotFoundException(ResponseCode code, String developerMessage, Throwable cause) {
    super(code, developerMessage, cause);
  }
}
