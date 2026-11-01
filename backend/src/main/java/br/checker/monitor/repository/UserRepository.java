package br.checker.monitor.repository;

import br.checker.monitor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
//repositorio para a entidade User

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    Optional<User> findByName(String name); //
}