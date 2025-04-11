package com.ufg.artifactanalyzer.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtractedFilesService {

    private final FhirContext fhirContext = FhirContext.forR4();
    private final IParser parser = fhirContext.newJsonParser();
    private final String extractedPath = new File("uploads/extracted/package/").getAbsolutePath();

    /**
     * Lista todos os recursos FHIR extraídos
     */
    public List<Resource> listAllResources() throws IOException {
        return readJsonFiles().stream()
                .map(this::parseJsonToResource)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Lista os recursos FHIR filtrados por resourceType
     */
    public List<Resource> listResourcesByType(String resourceType) throws IOException {
        return listAllResources().stream()
                .filter(resource -> resource.getResourceType().name().equalsIgnoreCase(resourceType))
                .collect(Collectors.toList());
    }

    /**
     * Busca um recurso FHIR pelo campo "id"
     */
    public Resource getResourceById(String id) throws IOException {
        return listAllResources().stream()
                .filter(resource -> id.equals(resource.getIdElement().getIdPart()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Conta os recursos por resourceType e inclui o total geral
     */
    public Map<String, Long> countResourcesByType() throws IOException {
        List<Resource> resources = listAllResources();

        Map<String, Long> counts = resources.stream()
                .collect(Collectors.groupingBy(
                        resource -> Optional.ofNullable(resource.getResourceType().name()).orElse("UNKNOWN"),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        counts.put("total", (long) resources.size());

        // Ordena colocando o total por último
        Map<String, Long> orderedCounts = new LinkedHashMap<>();
        counts.entrySet().stream()
                .filter(entry -> !"total".equals(entry.getKey()))
                .forEach(entry -> orderedCounts.put(entry.getKey(), entry.getValue()));
        orderedCounts.put("total", counts.get("total"));

        return orderedCounts;
    }

    /**
     * Lê todos os arquivos .json da pasta extraída
     */
    private List<String> readJsonFiles() throws IOException {
        File folder = new File(extractedPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null) {
            return Collections.emptyList();
        }

        List<String> jsonContents = new ArrayList<>();
        for (File file : files) {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            jsonContents.add(content);
        }
        return jsonContents;
    }

    /**
     * Converte conteúdo JSON para Resource FHIR
     */
    private Resource parseJsonToResource(String jsonContent) {
        try {
            return (Resource) parser.parseResource(jsonContent);
        } catch (Exception e) {
            System.err.println("Erro ao parsear recurso FHIR: " + e.getMessage());
            return null;
        }
    }


}
