package com.example.vibelet.repository;

import com.example.vibelet.model.Vibe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VibeRepository extends JpaRepository<Vibe, Long> {
    // TO DO (in the future)
}