package com.fiap.mecanica.shared.seguranca.infra.gateway.repository;

import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByCpf(String cpf);
}
