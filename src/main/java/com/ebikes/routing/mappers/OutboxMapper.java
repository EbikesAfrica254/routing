package com.ebikes.routing.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.ebikes.routing.database.entities.Outbox;
import com.ebikes.routing.dtos.responses.outbox.OutboxResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OutboxMapper {

  OutboxResponse toResponse(Outbox outbox);
}
