package com.example.repository;

import com.example.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Override
    List<Location> findAll();

    Location findById(Integer id);

    Location findByLongitudAndLatitud(String lon, String lat);
}
