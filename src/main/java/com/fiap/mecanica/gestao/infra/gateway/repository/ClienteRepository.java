package com.fiap.mecanica.gestao.infra.gateway.repository;

import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
	Optional<ClienteEntity> findByCpf(String cpf);
	boolean existsByCpf(String cpf);
	boolean existsByCnpj(String cnpj);
	boolean existsByCpfAndIdNot(String cpf, Long id);
	boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
