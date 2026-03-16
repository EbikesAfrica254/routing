package com.ebikes.routing.database.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ebikes.routing.database.entities.VehicleRoutingProfile;
import com.ebikes.routing.enums.VehicleClass;

public interface VehicleRoutingProfileRepository
    extends JpaRepository<VehicleRoutingProfile, UUID> {

  Optional<VehicleRoutingProfile> findByVehicleClass(VehicleClass vehicleClass);
}
