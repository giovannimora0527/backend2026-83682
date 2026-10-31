package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.entity.PerfilMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfilMenuRepository extends JpaRepository<PerfilMenu, Integer> {
    /**
     * Busca todos los registros de PerfilMenu asociados a un perfil específico.
     * @param perfil perfil.
     * @return Lista de perfil menu asociados.
     */
    List<PerfilMenu> findByPerfil(Perfil perfil);
}
