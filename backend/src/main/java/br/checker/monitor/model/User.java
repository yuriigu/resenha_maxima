package br.checker.monitor.model;

import jakarta.persistence.*;
import java.util.Objects;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Identity deixa o BD lidar com o autoincremento do registro
    private Long id;

    @Column(name = "name", length = 255, unique = true)
    private String name;

    @Column(name = "email", length = 255, unique = true)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    public User() {

    } //construtor vazio para o JPA usar reflection

    //getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}