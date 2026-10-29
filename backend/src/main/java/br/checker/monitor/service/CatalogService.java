package br.checker.monitor.service;

import br.checker.monitor.dto.BrandResponse;
import br.checker.monitor.dto.ModelResponse;
import br.checker.monitor.dto.RoleResponse;
import br.checker.monitor.exception.ResourceNotFoundException;
import br.checker.monitor.model.Brand;
import br.checker.monitor.model.DeviceModel;
import br.checker.monitor.model.Role;
import br.checker.monitor.repository.BrandRepository;
import br.checker.monitor.repository.DeviceModelRepository;
import br.checker.monitor.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servico de catalogo: gerencia buscas de Marcas, Modelos e Roles.
 *
 * <p>Fornece metodos para listar entidades do catalogo e converter para DTOs de resposta.
 *
 * <p>Anotacao {@link Service}: camada de servico gerenciada pelo Spring.
 * Anotacao {@link Transactional(readOnly = true)}: otimizacao para operacoes de leitura
 * (nao bloqueia transacoes desnecessariamente).
 */
@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final BrandRepository brandRepository;
    private final DeviceModelRepository modelRepository;
    private final RoleRepository roleRepository;

    public CatalogService(BrandRepository brandRepository,
                          DeviceModelRepository modelRepository,
                          RoleRepository roleRepository) {
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Lista todas as marcas cadastradas.
     */
    public List<BrandResponse> findAllBrands() {
        return brandRepository.findAll().stream()
                .map(b -> new BrandResponse(b.getId(), b.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Busca uma marca pelo ID ou lança ResourceNotFoundException.
     */
    public Brand findBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Marca nao encontrada com id: " + id));
    }

    /**
     * Lista todos os modelos cadastrados.
     */
    public List<ModelResponse> findAllModels() {
        return modelRepository.findAll().stream()
                .map(m -> new ModelResponse(m.getId(), m.getBrand().getId(), m.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Busca um modelo pelo ID ou lança ResourceNotFoundException.
     */
    public DeviceModel findModelById(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Modelo nao encontrado com id: " + id));
    }

    /**
     * Lista todas as roles cadastradas.
     */
    public List<RoleResponse> findAllRoles() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getName(), r.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * Busca uma role pelo ID ou lança ResourceNotFoundException.
     */
    public Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role nao encontrada com id: " + id));
    }
}