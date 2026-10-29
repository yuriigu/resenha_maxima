package br.checker.monitor.controller;

import br.checker.monitor.dto.PlaceAddressResponse;
import br.checker.monitor.dto.PlaceRequest;
import br.checker.monitor.dto.PlaceResponse;
import br.checker.monitor.exception.BusinessRuleException;
import br.checker.monitor.model.Place;
import br.checker.monitor.service.PlaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de lugares (places).
 *
 * <p>Endpoints:
 *   GET    /api/places                    - lista todos os lugares
 *   GET    /api/places/{id}               - busca um lugar pelo ID
 *   POST   /api/places                    - cadastra um novo lugar
 *   PUT    /api/places/{id}               - atualiza um lugar existente
 *   DELETE /api/places/{id}               - remove um lugar
 *   POST   /api/places/{id}/validate-address - valida se o lugar pode ter endereco (regra de negocio)
 *
 * <p>Anotacao {@link RestController}: controller REST que retorna JSON.
 * Anotacao {@link RequestMapping("/api/places")}: caminho base dos endpoints.
 */
@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    /**
     * Lista todos os lugares cadastrados.
     */
    @GetMapping
    public ResponseEntity<List<PlaceResponse>> listPlaces() {
        List<Place> places = placeService.findAll();
        List<PlaceResponse> responses = places.stream()
                .map(p -> new PlaceResponse(
                        p.getId(),
                        p.getName(),
                        p.getParent() != null ? p.getParent().getId() : null,
                        p.getAddress() != null ?
                                new PlaceAddressResponse(
                                        p.getAddress().getStreet(),
                                        p.getAddress().getNumber(),
                                        p.getAddress().getCity(),
                                        p.getAddress().getState(),
                                        p.getAddress().getZipCode())
                                : null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca um lugar pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlace(@PathVariable Long id) {
        Place place = placeService.findById(id);
        PlaceResponse response = new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getParent() != null ? place.getParent().getId() : null,
                place.getAddress() != null ?
                        new PlaceAddressResponse(
                                place.getAddress().getStreet(),
                                place.getAddress().getNumber(),
                                place.getAddress().getCity(),
                                place.getAddress().getState(),
                                place.getAddress().getZipCode())
                        : null);
        return ResponseEntity.ok(response);
    }

    /**
     * Cadastra um novo lugar.
     */
    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody PlaceRequest request) {
        Place place = placeService.create(request);
        PlaceResponse response = new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getParent() != null ? place.getParent().getId() : null,
                null);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um lugar existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlaceResponse> updatePlace(@PathVariable Long id,
                                                      @Valid @RequestBody PlaceRequest request) {
        Place place = placeService.update(id, request);
        PlaceResponse response = new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getParent() != null ? place.getParent().getId() : null,
                null);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove um lugar pelo ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        placeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Valida se um lugar pode ter endereco fisico (apenas lugares raiz).
     * Lança BusinessRuleException se o lugar nao for raiz.
     */
    @PostMapping("/{id}/validate-address")
    public ResponseEntity<Void> validateAddress(@PathVariable Long id) {
        placeService.validateAddressRule(id);
        return ResponseEntity.ok().build();
    }
}