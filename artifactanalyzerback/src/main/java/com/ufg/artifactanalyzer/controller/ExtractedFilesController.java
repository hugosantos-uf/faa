package com.ufg.artifactanalyzer.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.ufg.artifactanalyzer.services.ExtractedFilesService;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/fhir")
public class ExtractedFilesController {

    private final ExtractedFilesService extractedFilesService;
    private final IParser fhirParser;

    public ExtractedFilesController(ExtractedFilesService extractedFilesService) {
        this.extractedFilesService = extractedFilesService;
        this.fhirParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
    }

    @GetMapping(value = "/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllExtractedResources() throws IOException {
        List<Resource> resources = extractedFilesService.listAllResources();
        return ResponseEntity.ok(convertResourcesToJson(resources));
    }

    @GetMapping(value = "/resources/{resourceType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getResourcesByType(@PathVariable String resourceType) throws IOException {
        List<Resource> resources = extractedFilesService.listResourcesByType(resourceType);
        return ResponseEntity.ok(convertResourcesToJson(resources));
    }

    @GetMapping(value = "/resources/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getResourceById(@PathVariable String id) throws IOException {
        Resource resource = extractedFilesService.getResourceById(id);
        if (resource == null) {
            throw new ResponseStatusException(NOT_FOUND, "Recurso com id '" + id + "' não encontrado");
        }
        return ResponseEntity.ok(fhirParser.encodeResourceToString(resource));
    }

    @GetMapping(value = "/resources/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> countResourcesByType() throws IOException {
        Map<String, Long> counts = extractedFilesService.countResourcesByType();
        return ResponseEntity.ok(counts);
    }

    @GetMapping(value = "/resources/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchResources(@RequestParam(required = false, defaultValue = "") String query) throws IOException {
        List<Resource> results = extractedFilesService.searchResources(query);
        return ResponseEntity.ok(convertResourcesToJson(results));
    }

    @GetMapping("/resources/export/csv")
    public ResponseEntity<String> exportResourcesAsCsv() throws IOException {
        String csvData = extractedFilesService.exportResourcesToCsv();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fhir_resources.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @GetMapping(value = "/resources/validate-urls", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> validateUrls() throws IOException {
        Map<String, String> results = extractedFilesService.validateCanonicalUrls();
        return ResponseEntity.ok(results);
    }

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
