package br.checker.monitor.service;

import br.checker.monitor.dto.DeviceDetailResponse;
import br.checker.monitor.dto.DeviceRequest;
import br.checker.monitor.dto.DeviceRoleRequest;
import br.checker.monitor.dto.DeviceRoleResponse;
import br.checker.monitor.dto.DeviceSummaryResponse;
import br.checker.monitor.exception.BusinessRuleException;
import br.checker.monitor.exception.ResourceNotFoundException;
import br.checker.monitor.model.*;
import br.checker.monitor.repository.DeviceRepository;
import br.checker.monitor.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servico de dispositivos (Device).
 *
 * <p>Implementa as regras de negocio da HU001 (Cadastrar dispositivo):
 * <ul>
 *   <li>Validacao de IP duplicado (unico no banco).</li>
 *   <li>Normalizacao de MAC Address (remove hifens/dois-pontos e converte para maiusculas).</li>
 *   <li>Validacao de que o modelo informado pertence a marca informada.</li>
 *   <li>Obrigatoriedade de pelo menos uma role vinculada.</li>
 * </ul>
 *
 * <p>Anotacao {@link Service}: camada de servico gerenciada pelo Spring.
 * Anotacao {@link Transactional}: cada metodo publico roda em transacao propria.
 */
@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final CatalogService catalogService;
    private final RoleRepository roleRepository;
    private final PlaceService placeService;

    public DeviceService(DeviceRepository deviceRepository,
                         CatalogService catalogService,
                         RoleRepository roleRepository, PlaceService placeService) {
        this.deviceRepository = deviceRepository;
        this.catalogService = catalogService;
        this.roleRepository = roleRepository;
        this.placeService = placeService;
    }

    /**
     * Cadastra um novo dispositivo aplicando todas as regras de negocio.
     */
    public DeviceDetailResponse create(DeviceRequest request) {
        if (deviceRepository.existsByIpAddress(request.ipAddress())) {
            throw new BusinessRuleException(
                    "Ja existe um dispositivo cadastrado com o IP: " + request.ipAddress());
        }

        Brand brand = catalogService.findBrandById(request.idBrand());

        DeviceModel model = null;
        if (request.idModel() != null) {
            model = catalogService.findModelById(request.idModel());
            if (!model.getBrand().getId().equals(brand.getId())) {
                throw new BusinessRuleException(
                        "O modelo id=" + model.getId() + " nao pertence a marca id=" + brand.getId());
            }
        }

        if (request.deviceRoles() == null || request.deviceRoles().isEmpty()) {
            throw new BusinessRuleException("Pelo menos uma role deve ser atribuida ao dispositivo.");
        }

        Place place = null;
        if (request.idPlace() != null) {
            place = placeService.findById(request.idPlace());
        }

        String normalizedMac = normalizeMacAddress(request.macAddress());

        Device device = new Device();
        device.setName(request.name());
        device.setBrand(brand);
        device.setModel(model);
        device.setIpAddress(request.ipAddress());
        device.setMacAddress(normalizedMac);
        device.setPlace(place);

        Device savedDevice = deviceRepository.save(device);

        Set<DeviceRole> deviceRoles = request.deviceRoles().stream()
                .map(roleReq -> createDeviceRole(savedDevice, roleReq))
                .collect(Collectors.toSet());
        savedDevice.setDeviceRoles(deviceRoles);

        Device finalDevice = deviceRepository.save(savedDevice);
        return toDetailResponse(finalDevice);
    }

    /**
     * Lista todos os dispositivos resumidos (para tabela/grid).
     */
    @Transactional(readOnly = true)
    public List<DeviceSummaryResponse> findAll() {
        return deviceRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca um dispositivo detalhado pelo ID.
     */
    @Transactional(readOnly = true)
    public DeviceDetailResponse findById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dispositivo nao encontrado com id: " + id));
        return toDetailResponse(device);
    }

    /**
     * Atualiza um dispositivo existente aplicando as mesmas regras de negocio do cadastro.
     */
    public DeviceDetailResponse update(Long id, DeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dispositivo nao encontrado com id: " + id));

        // Verifica se o IP ja e usado por OUTRO dispositivo (diferente do que esta sendo atualizado)
        Device existingByIp = deviceRepository.findByIpAddress(request.ipAddress());
        if (existingByIp != null && !existingByIp.getId().equals(id)) {
            throw new BusinessRuleException(
                    "Ja existe outro dispositivo cadastrado com o IP: " + request.ipAddress());
        }

        Brand brand = catalogService.findBrandById(request.idBrand());

        DeviceModel model = null;
        if (request.idModel() != null) {
            model = catalogService.findModelById(request.idModel());
            if (!model.getBrand().getId().equals(brand.getId())) {
                throw new BusinessRuleException(
                        "O modelo id=" + model.getId() + " nao pertence a marca id=" + brand.getId());
            }
        }

        if (request.deviceRoles() == null || request.deviceRoles().isEmpty()) {
            throw new BusinessRuleException("Pelo menos uma role deve ser atribuida ao dispositivo.");
        }

        Place place = null;
        if (request.idPlace() != null) {
            place = placeService.findById(request.idPlace());
        }

        device.setName(request.name());
        device.setBrand(brand);
        device.setModel(model);
        device.setIpAddress(request.ipAddress());
        device.setMacAddress(normalizeMacAddress(request.macAddress()));
        device.setPlace(place);

        device.getDeviceRoles().clear();
        Set<DeviceRole> newRoles = request.deviceRoles().stream()
                .map(roleReq -> createDeviceRole(device, roleReq))
                .collect(Collectors.toSet());
        device.setDeviceRoles(newRoles);

        Device updatedDevice = deviceRepository.save(device);
        return toDetailResponse(updatedDevice);
    }

    /**
     * Remove um dispositivo pelo ID.
     */
    public void delete(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dispositivo nao encontrado com id: " + id);
        }
        deviceRepository.deleteById(id);
    }

    // =========================================================================
    // Metodos privados auxiliares
    // =========================================================================

    /**
     * Normaliza o endereco MAC: remove hifens (-) e dois-pontos (:), converte para maiusculas.
     *
     * <p>Exemplos:
     * <ul>
     *   <li>"00-1A-2B-3C-4D-5E" -> "001A2B3C4D5E"</li>
     *   <li>"00:1a:2b:3c:4d:5e" -> "001A2B3C4D5E"</li>
     *   <li>null -> null</li>
     * </ul>
     *
     * @param mac endereco MAC bruto (pode ser nulo)
     * @return endereco MAC normalizado (ou null se o original for nulo)
     */
    private String normalizeMacAddress(String mac) {
        if (mac == null) {
            return null;
        }
        return mac.replace("-", "").replace(":", "").toUpperCase();
    }

    /**
     * Cria uma entidade DeviceRole a partir de um DeviceRoleRequest.
     *
     * <p>Busca a Role pelo ID, monta a chave composta DeviceRoleId e assina o dispositivo.
     */
    private DeviceRole createDeviceRole(Device device, DeviceRoleRequest roleReq) {
        Role role = roleRepository.findById(roleReq.idRole())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role nao encontrada com id: " + roleReq.idRole()));

        DeviceRoleId id = new DeviceRoleId(device.getId(), role.getId());

        DeviceRole deviceRole = new DeviceRole();
        deviceRole.setId(id);
        deviceRole.setDevice(device);
        deviceRole.setRole(role);
        deviceRole.setTimeStart(roleReq.timeStart());
        deviceRole.setTimeEnd(roleReq.timeEnd());
        deviceRole.setActive(roleReq.active() != null ? roleReq.active() : true);

        return deviceRole;
    }

    /**
     * Converte uma entidade Device para o DTO resumido (listagem).
     */
    private DeviceSummaryResponse toSummaryResponse(Device device) {
        return new DeviceSummaryResponse(
                device.getId(),
                device.getName(),
                device.getIpAddress(),
                device.getMacAddress(),
                device.getBrand() != null ? device.getBrand().getName() : null,
                device.getModel() != null ? device.getModel().getName() : null,
                device.getPlace() != null ? device.getPlace().getName() : null
        );
    }

    /**
     * Converte uma entidade Device para o DTO detalhado (tela de detalhes).
     */
    private DeviceDetailResponse toDetailResponse(Device device) {
        List<DeviceRoleResponse> roleResponses = device.getDeviceRoles().stream()
                .map(dr -> new DeviceRoleResponse(
                        dr.getRole().getId(),
                        dr.getRole().getName(),
                        dr.getTimeStart(),
                        dr.getTimeEnd(),
                        dr.getActive()))
                .collect(Collectors.toList());

        return new DeviceDetailResponse(
                device.getId(),
                device.getName(),
                device.getBrand() != null ? device.getBrand().getId() : null,
                device.getBrand() != null ? device.getBrand().getName() : null,
                device.getModel() != null ? device.getModel().getId() : null,
                device.getModel() != null ? device.getModel().getName() : null,
                device.getIpAddress(),
                device.getMacAddress(),
                device.getPlace() != null ? device.getPlace().getId() : null,
                device.getPlace() != null ? device.getPlace().getName() : null,
                roleResponses
        );
    }
}