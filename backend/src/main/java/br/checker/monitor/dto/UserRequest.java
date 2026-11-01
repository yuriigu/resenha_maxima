package br.checker.monitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//DTO para cadastro de usuario(User)

public record UserRequest(

        @NotBlank(message = "O nome do usuario é obrigatório") //nome nao pode ser nulo
        String name,

        @NotBlank(message = "O email é obrigatório") //email nao pode ser nulo
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória") //senha nao pode ser nula
        @Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$") //minimo 8 digitos, pelomenos 1 caracter minusculo e maiusculo, 1 numero e 1 simbolo especial
        @Size(min = 8, max = 20) //minimo 8 maximo 20
        String password,

        @NotNull(message = "O perfil do usuario é obrigatório") //perfil (role) nao pode ser nulo
        Long role

) {
}