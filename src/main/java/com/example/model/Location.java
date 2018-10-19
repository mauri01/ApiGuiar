package com.example.model;

import org.hibernate.annotations.IndexColumn;

import javax.persistence.*;
import java.util.List;

/**
 * Created by mauri on 23/08/2018.
 */
@Entity
@Table(name = "location")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "location_id")
    private int id;

    @Column(name = "longitud")
    String longitud;

    @Column(name = "latitud")
    String latitud;

    @Column(name = "nombre")
    String  nombre;

    @Column(name = "descripcion")
    String  nombdescripcionre;

    @OneToMany(cascade= CascadeType.ALL)
    @JoinColumn(name="location")
    private List<TargetManager> targetManagers;

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombdescripcionre() {
        return nombdescripcionre;
    }

    public void setNombdescripcionre(String nombdescripcionre) {
        this.nombdescripcionre = nombdescripcionre;
    }

    public List<TargetManager> getTargetManagers() {
        return targetManagers;
    }

    public void setTargetManagers(List<TargetManager> targetManagers) {
        this.targetManagers = targetManagers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
