package br.checker.monitor.dto;

// DTO de resposta para um usuario(User)

public record UserResponse(

        Long id,
        String name,
        String email,
        String roleName

) {
}