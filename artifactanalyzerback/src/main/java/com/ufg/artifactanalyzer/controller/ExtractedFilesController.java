package com.ufg.artifactanalyzer.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.ufg.artifactanalyzer.services.ExtractedFilesService;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/fhir")
public class ExtractedFilesController {

    private final ExtractedFilesService extractedFilesService;
    private final FhirContext fhirContext = FhirContext.forR4();
    private final IParser fhirParser = fhirContext.newJsonParser().setPrettyPrint(true);

    public ExtractedFilesController(ExtractedFilesService extractedFilesService) {
        this.extractedFilesService = extractedFilesService;
    }

    /**
     * Lista todos os recursos FHIR extraídos
     */
    @GetMapping(value = "/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllExtractedResources() throws IOException {
        List<Resource> resources = extractedFilesService.listAllResources();
        return ResponseEntity.ok(convertResourcesToJson(resources));
    }

    /**
     * Lista recursos FHIR filtrados por resourceType
     */
    @GetMapping(value = "/resources/{resourceType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getResourcesByType(@PathVariable String resourceType) throws IOException {
        List<Resource> resources = extractedFilesService.listResourcesByType(resourceType);
        return ResponseEntity.ok(convertResourcesToJson(resources));
    }

    /**
     * Busca recurso FHIR pelo id
     */
    @GetMapping(value = "/resources/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getResourceById(@PathVariable String id) throws IOException {
        Resource resource = extractedFilesService.getResourceById(id);
        if (resource == null) {
            throw new ResponseStatusException(NOT_FOUND, "Recurso com id '" + id + "' não encontrado");
        }
        return ResponseEntity.ok(fhirParser.encodeResourceToString(resource));
    }

    /**
     * Conta recursos FHIR por tipo
     */
    @GetMapping(value = "/resources/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> countResourcesByType() throws IOException {
        Map<String, Long> counts = extractedFilesService.countResourcesByType();
        return ResponseEntity.ok(counts);
    }

    /**
     * Utilitário para converter lista de recursos para JSON FHIR
     */
    private String convertResourcesToJson(List<Resource> resources) {
        StringBuilder responseJson = new StringBuilder("[");
        for (int i = 0; i < resources.size(); i++) {
            responseJson.append(fhirParser.encodeResourceToString(resources.get(i)));
            if (i < resources.size() - 1) {
                responseJson.append(",");
            }
        }
        responseJson.append("]");
        return responseJson.toString();
    }
}
