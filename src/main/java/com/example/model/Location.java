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
    long  longitud;

    @Column(name = "latitud")
    long  latitud;

    @Column(name = "nombre")
    String  nombre;

    @Column(name = "descripcion")
    String  nombdescripcionre;

    @OneToMany(cascade= CascadeType.ALL)
    @JoinColumn(name="location")
    private List<TargetManager> targetManagers;
}
