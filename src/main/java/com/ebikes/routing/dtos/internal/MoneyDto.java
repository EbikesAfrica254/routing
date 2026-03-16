package com.ebikes.routing.dtos.internal;

import java.math.BigDecimal;

public record MoneyDto(BigDecimal amount, String currency) {}
