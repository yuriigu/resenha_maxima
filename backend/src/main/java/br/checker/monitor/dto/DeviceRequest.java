package br.checker.monitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * DTO de entrada (request) para cadastro e atualização de dispositivos.
 *
 * <p>Contém todos os campos necessários para criar ou alterar um {@link br.checker.monitor.model.Device},
 * incluindo a lista de roles a serem vinculadas.
 *
 * <p>Anotações de validação do Spring ({@link jakarta.validation}):
 * <ul>
 *   <li>{@link NotBlank}: o campo não pode ser nulo nem vazio (apenas espaços em branco conta como vazio).</li>
 *   <li>{@link NotNull}: o campo não pode ser nulo.</li>
 *   <li>{@link NotEmpty}: a coleção não pode ser nula nem vazia.</li>
 * </ul>
 *
 * <p>Estas validações são disparadas automaticamente pelo Spring MVC quando o controller
 * recebe o DTO anotado com {@code @Valid} ou {@code @Validated}.
 */
public record DeviceRequest(

    @NotBlank(message = "O nome do dispositivo é obrigatório")
    String name,

    @NotNull(message = "A marca (id_brand) é obrigatória")
    Long idBrand,

    Long idModel,

    @NotBlank(message = "O endereço IP é obrigatório")
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$",
            message = "Endereço IP inválido")
    String ipAddress,

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]?){5}[0-9A-Fa-f]{2}$",
            message = "Endereço MAC inválido")
    String macAddress,

    @NotNull(message = "O local (id_place) é obrigatório")
    Long idPlace,

    @NotEmpty(message = "Pelo menos uma role deve ser informada para o dispositivo")
    List<DeviceRoleRequest> deviceRoles
) {
}