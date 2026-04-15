package com.fiap.mecanica.shared.seguranca.infra.gateway.database;

import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.RoleEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.UserEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserDatabaseGateway implements UserGateway {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            return userRepository.findByEmail(email)
                    .map(entity -> new User(
                            entity.getId(),
                            new Email(entity.getEmail()),
                            new PasswordHash(entity.getPassword()),
                            entity.getRoles().stream().map(roleEntity -> new Role(roleEntity.getId(), roleEntity.getName()))
                    .toList()));
        } catch (Exception e) {
            log.error("Erro ao buscar usuario por email: {}", email, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public User create(User user) {
        try {
            List<RoleEnum> roleEnums = user.getRoles().stream().map(Role::getName).toList();
            List<RoleEntity> roles = roleRepository.findByNameIn(roleEnums);

            UserEntity userEntity = new UserEntity(user.getEmail().value(), user.getPassword().getValue(),
                    user.getRoles().stream().map(role -> new RoleEntity(role.getName())).toList());
            userEntity.setRoles(roles);

            UserEntity entity = userRepository.save(userEntity);
            return new User(
                    entity.getId(),
                    new Email(entity.getEmail()),
                    new PasswordHash(entity.getPassword()),
                    entity.getRoles().stream().map(roleEntity -> new Role(roleEntity.getId(), roleEntity.getName())).toList());
        } catch (DataIntegrityViolationException e) {
            log.error("Já existe um usuario com o mesmo email", e);
            throw new ErroAcessoBaseDeDadosException(); //Tratar melhor
        } catch (Exception e) {
            log.error("Erro ao salvar usuario", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
