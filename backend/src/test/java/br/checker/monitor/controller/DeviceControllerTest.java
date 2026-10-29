package br.checker.monitor.controller;

import br.checker.monitor.dto.DeviceDetailResponse;
import br.checker.monitor.dto.DeviceRequest;
import br.checker.monitor.dto.DeviceRoleResponse;
import br.checker.monitor.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste automatizado do DeviceController (HU001 - Cadastrar dispositivo).
 *
 * <p>Usa {@link WebMvcTest} para testar apenas a camada de controller (REST),
 * mockando o {@link DeviceService} com {@link MockitoBean}. Assim nao e necessario
 * subir o banco de dados nem o contexto completo da aplicacao.
 *
 * <p>Cenarios testados:
 * <ul>
 *   <li>Cadastro com dados validos -> deve retornar HTTP 201 Created.</li>
 *   <li>Cadastro com IP duplicado (service lancando excecao) -> deve retornar HTTP 400 Bad Request.</li>
 *   <li>Cadastro sem roles (DTO invalido) -> deve retornar HTTP 400 Bad Request.</li>
 *   <li>Busca de dispositivo existente -> deve retornar HTTP 200 OK.</li>
 *   <li>Busca de dispositivo inexistente -> deve retornar HTTP 404 Not Found.</li>
 * </ul>
 *
 * <p>Anotacoes:
 * <ul>
 *   <li>{@link WebMvcTest}: configura apenas o contexto MVC (controllers, exception handlers),
 *       ideal para teste unitario de camada web.</li>
 *   <li>{@link MockitoBean}: substitui o bean real por um mock (Mockito), permitindo
 *       controlar o comportamento do servico em cada teste.</li>
 *   <li>{@link MockMvc}: simula requisicoes HTTP para o controller sem iniciar um servidor real.</li>
 * </ul>
 */
@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    // =========================================================================
    // JSON auxiliares (strings brutas, evitando dependencia de Jackson)
    // =========================================================================

    private static final String VALID_DEVICE_JSON = """
        {
          "name": "Servidor Principal",
          "idBrand": 10,
          "idModel": 20,
          "ipAddress": "192.168.1.100",
          "macAddress": "00-1A-2B-3C-4D-5E",
          "idPlace": 1,
          "deviceRoles": [
            {
              "idRole": 1,
              "timeStart": "08:00:00",
              "timeEnd": "18:00:00",
              "active": true
            }
          ]
        }
        """;

    private static final String EMPTY_ROLES_JSON = """
        {
          "name": "Dispositivo Sem Role",
          "idBrand": 10,
          "idModel": null,
          "ipAddress": "10.0.0.1",
          "macAddress": null,
          "idPlace": null,
          "deviceRoles": []
        }
        """;

    // =========================================================================
    // Testes de cadastro (POST)
    // =========================================================================

    /**
     * Cenario: cadastro com dados validos.
     * Expectativa: o service retorna um DeviceDetailResponse e o controller
     * responde com HTTP 201 Created contendo o dispositivo cadastrado.
     */
    @Test
    void testCreateDevice_Success() throws Exception {
        // Dado: um response valido do service mockado
        DeviceDetailResponse response = createValidResponse();
        when(deviceService.create(any(DeviceRequest.class))).thenReturn(response);

        // Quando: envia POST /api/devices com JSON valido
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_DEVICE_JSON))

                // Entao: deve retornar HTTP 201 Created
                .andExpect(status().isCreated())

                // E o corpo deve conter o ID e nome do dispositivo
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Servidor Principal"))
                .andExpect(jsonPath("$.ipAddress").value("192.168.1.100"))
                .andExpect(jsonPath("$.macAddress").value("001A2B3C4D5E"));

        // Verifica que o service foi chamado exatamente uma vez
        verify(deviceService, times(1)).create(any(DeviceRequest.class));
    }

    /**
     * Cenario: cadastro com IP duplicado.
     * Expectativa: o service lanca BusinessRuleException, capturada pelo
     * GlobalExceptionHandler, que retorna HTTP 400 Bad Request.
     */
    @Test
    void testCreateDevice_DuplicateIp() throws Exception {
        // Dado: o service lancara excecao de IP duplicado
        when(deviceService.create(any(DeviceRequest.class)))
                .thenThrow(new br.checker.monitor.exception.BusinessRuleException(
                        "Ja existe um dispositivo cadastrado com o IP: 192.168.1.100"));

        // Quando: envia POST /api/devices
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_DEVICE_JSON))

                // Entao: deve retornar HTTP 400 Bad Request
                .andExpect(status().isBadRequest())

                // E o corpo deve conter a mensagem de erro
                .andExpect(jsonPath("$.message").value(
                        "Ja existe um dispositivo cadastrado com o IP: 192.168.1.100"));

        verify(deviceService, times(1)).create(any(DeviceRequest.class));
    }

    /**
     * Cenario: cadastro sem informar nenhuma role.
     * Expectativa: a validacao Bean Validation (@NotEmpty na lista de deviceRoles)
     * falha antes mesmo de chamar o service, retornando HTTP 400 Bad Request.
     */
    @Test
    void testCreateDevice_MissingRoles() throws Exception {
        // Quando: envia POST /api/devices com lista de roles vazia
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMPTY_ROLES_JSON))

                // Entao: deve retornar HTTP 400 Bad Request (erro de validacao)
                .andExpect(status().isBadRequest());

        // Verifica que o service NAO foi chamado (validacao falhou antes)
        verify(deviceService, never()).create(any(DeviceRequest.class));
    }

    // =========================================================================
    // Testes de busca (GET)
    // =========================================================================

    /**
     * Cenario: busca de dispositivo existente.
     * Expectativa: retorna HTTP 200 OK com os detalhes do dispositivo.
     */
    @Test
    void testGetDevice_Found() throws Exception {
        // Dado: o service retorna um dispositivo para o id 1L
        DeviceDetailResponse response = createValidResponse();
        when(deviceService.findById(1L)).thenReturn(response);

        // Quando: envia GET /api/devices/1
        mockMvc.perform(get("/api/devices/1"))

                // Entao: deve retornar HTTP 200 OK
                .andExpect(status().isOk())

                // E o corpo deve conter os dados do dispositivo
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Servidor Principal"));

        verify(deviceService, times(1)).findById(1L);
    }

    /**
     * Cenario: busca de dispositivo inexistente.
     * Expectativa: o service lanca ResourceNotFoundException, retornando HTTP 404 Not Found.
     */
    @Test
    void testGetDevice_NotFound() throws Exception {
        // Dado: o service lancara excecao de nao encontrado
        when(deviceService.findById(999L))
                .thenThrow(new br.checker.monitor.exception.ResourceNotFoundException(
                        "Dispositivo nao encontrado com id: 999"));

        // Quando: envia GET /api/devices/999
        mockMvc.perform(get("/api/devices/999"))

                // Entao: deve retornar HTTP 404 Not Found
                .andExpect(status().isNotFound())

                // E o corpo deve conter a mensagem de erro
                .andExpect(jsonPath("$.message").value(
                        "Dispositivo nao encontrado com id: 999"));

        verify(deviceService, times(1)).findById(999L);
    }

    // =========================================================================
    // Teste de listagem (GET all)
    // =========================================================================

    /**
     * Cenario: listagem de dispositivos.
     * Expectativa: retorna HTTP 200 OK com lista (pode estar vazia).
     */
    @Test
    void testListDevices() throws Exception {
        // Dado: o service retorna uma lista vazia
        when(deviceService.findAll()).thenReturn(java.util.Collections.emptyList());

        // Quando: envia GET /api/devices
        mockMvc.perform(get("/api/devices"))

                // Entao: deve retornar HTTP 200 OK com lista vazia
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(deviceService, times(1)).findAll();
    }

    // =========================================================================
    // Metodos privados auxiliares
    // =========================================================================

    private DeviceDetailResponse createValidResponse() {
        DeviceRoleResponse roleResp = new DeviceRoleResponse(
                1L, "ADMIN", LocalTime.of(8, 0), LocalTime.of(18, 0), true);
        return new DeviceDetailResponse(
                1L, "Servidor Principal", 10L, "Dell", 20L, "PowerEdge R740",
                "192.168.1.100", "001A2B3C4D5E", null, null,
                List.of(roleResp));
    }
}