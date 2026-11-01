package br.checker.monitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//DTO para atualizacao de usuario (UserUpdate)

public record UserUpdateRequest(

        @NotBlank(message = "O email é obrigatório") //email nao pode ser nulo
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotNull(message = "O perfil é obrigatório")
        Long role

) {
}