package com.example.repository;

import com.example.model.TargetManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("targetManagerRepository")
public interface TargetManagerRepository extends JpaRepository<TargetManager, Long> {
    List<TargetManager> findAll();
    TargetManager findById(int id);
}
