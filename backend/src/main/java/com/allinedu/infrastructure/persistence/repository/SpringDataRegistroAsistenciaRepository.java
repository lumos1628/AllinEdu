package com.allinedu.infrastructure.persistence.repository;

import com.allinedu.infrastructure.persistence.entity.RegistroAsistenciaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataRegistroAsistenciaRepository extends JpaRepository<RegistroAsistenciaEntity, Long> {

    @EntityGraph(attributePaths = {"ubicacionMatricula", "ubicacionMatricula.matricula",
            "ubicacionMatricula.matricula.estudiante", "ubicacionMatricula.matricula.estudiante.persona"})
    List<RegistroAsistenciaEntity> findByJornadaId(Long jornadaId);

    @Modifying
    @Query("delete from RegistroAsistenciaEntity r where r.jornada.id = :jornadaId")
    void deleteRegistrosDeJornada(@Param("jornadaId") Long jornadaId);
}