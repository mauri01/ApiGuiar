package com.example.service;

import com.example.model.Location;
import com.example.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("locationService")
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public void saveData(String lugar,String lng, String lat, String desc){
        Location location = new Location();
        location.setLatitud(lat);
        location.setLongitud(lng);
        location.setNombdescripcionre(desc);
        location.setNombre(lugar);
        locationRepository.save(location);

    }

    public List<Location> getLocationAll(){
        return locationRepository.findAll();
    }


    public Location findLocationById(int id) {
        return locationRepository.findById(id);
    }

    public List<Location> findLocationAll(){
        return locationRepository.findAll();
    }

    public void remove(Location location) {
        locationRepository.delete(location);
    }

    public void update(Location location){
        locationRepository.save(location);
    }

    public Location findLocationByLongAndLat(String longitud, String latitud) {
        return locationRepository.findByLongitudAndLatitud(longitud,latitud);
    }
}
