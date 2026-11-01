package br.checker.monitor.service;

import br.checker.monitor.dto.UserResponse;
import br.checker.monitor.dto.UserRequest;
import br.checker.monitor.dto.UserUpdateRequest;
import br.checker.monitor.dto.DeviceSummaryResponse;
import br.checker.monitor.exception.BusinessRuleException;
import br.checker.monitor.exception.ResourceNotFoundException;
import br.checker.monitor.model.User;
import br.checker.monitor.model.Role;
import br.checker.monitor.repository.RoleRepository;
import br.checker.monitor.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;          //declarando variaveis
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.roleRepository = roleRepository;
            this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(UserRequest request) { //funcao cujo cria um Usuario atraves de um contrato de dados (user request) e retorna ele criado com outro contrato de dados (user response)
        if (userRepository.existsByName(request.name())) {
            throw new BusinessRuleException( //jogando excessao de regra
                    "Já existe um usuário cadastrado com o nome: " + request.name());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException( //jogando excessao de regra
                    "Esse email já pertence á alguem: " + request.email());
        }

        Role role = roleRepository.findById(request.role()).orElseThrow(() -> new ResourceNotFoundException("Role não encontrada")); //busco a role atraves do ID obtido pelo metodo

        String passwordHash = passwordEncoder.encode(request.password()); //pega a senha em plain text e criptografa ela.

        User user = new User();                      //instanciando o novo user e atribuindo dados de entrada para seus atributos
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordHash);
        user.setRole(role);

        User savedUser = userRepository.save(user); //usando o service para salvar o usuario criado anteriormente

        return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole().getName());

    }

    @Transactional(readOnly = true) //como é só busca, nao é necessario escrever no db.
    public UserResponse findById(Long id) {

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().getName());
    }

    @Transactional(readOnly = true) //como é só busca, nao é necessario escrever no db.
    public List<UserResponse> findAll() { //por ser um findAll, nao precisa de parametro e o retorno vai ser uma lista de All usuarios.

        List<User> users = userRepository.findAll(); //nesse findAll, o repository retorna uma lista de instancias de usuarios.

        //portanto, para retornar os usuarios, precisamos deixar os dados no padrao do DTO UserResponse, para isso usamos o stream cujo
        //vai tratar cada instancia individualmente, o map ira servir como um loop, ou seja, ele vai pegar cada user e instanciar como new UserREsponse
        //ou seja, ele pegou uma instancia individual e instanciou ela novamente porem com os dados tratados para coincidirem com o DTO.
        //e o .toList recolhe todas instancias novas de UserResponde e coloca em uma lista.

        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().getName()
                ))
                .toList();

    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado")); // busca usuario pelo ID recebido do request.

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) { //aqui vai checar se o email e diferente do original e se ele tambem ja existe
            throw new BusinessRuleException("Esse email já pertence a outro usuário: " + request.email());
        }

        Role role = roleRepository.findById(request.role()).orElseThrow(() -> new ResourceNotFoundException("Role não encontrada")); //aqui ele vai obter a Role nova

        user.setEmail(request.email()); //atualizando email da instancia
        user.setRole(role); // atualizando role da instancia

        User updatedUser = userRepository.save(user); // salvando no db as atualizacoes do user.

        return new UserResponse(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail(), updatedUser.getRole().getName());

    }

    @Transactional
    public void delete(Long id) {

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        userRepository.delete(user);

    }


}
