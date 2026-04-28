package com.parag.campuspulse.repository;

import com.parag.campuspulse.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByCode(String code);

    List<Program> findBySchoolId(Long schoolId);
}