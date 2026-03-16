package com.ebikes.routing.exceptions;

import java.io.Serial;

import com.ebikes.routing.enums.ResponseCode;

public class BusinessRuleException extends BaseException {

  @Serial private static final long serialVersionUID = 1L;

  public BusinessRuleException(ResponseCode code, String developerMessage) {
    super(code, developerMessage);
  }

  public BusinessRuleException(ResponseCode code, String developerMessage, Throwable cause) {
    super(code, developerMessage, cause);
  }
}
