package com.ufg.artifactanalyzer.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.*;
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

    public List<Resource> listAllResources() throws IOException {
        return readJsonFiles().stream()
                .map(this::parseJsonToResource)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Resource> listResourcesByType(String resourceType) throws IOException {
        return listAllResources().stream()
                .filter(resource -> resource.getResourceType().name().equalsIgnoreCase(resourceType))
                .collect(Collectors.toList());
    }

    public Resource getResourceById(String id) throws IOException {
        return listAllResources().stream()
                .filter(resource -> id.equals(resource.getIdElement().getIdPart()))
                .findFirst()
                .orElse(null);
    }

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

    private Resource parseJsonToResource(String jsonContent) {
        try {
            return (Resource) parser.parseResource(jsonContent);
        } catch (Exception e) {
            System.err.println("Erro ao parsear recurso FHIR: " + e.getMessage());
            return null;
        }
    }

    public List<Resource> searchResources(String query) throws IOException {
        return listAllResources().stream()
                .filter(resource -> {
                    String name = extractName(resource);
                    String url = extractUrl(resource);
                    String description = extractDescription(resource);

                    return containsIgnoreCase(name, query) ||
                            containsIgnoreCase(url, query) ||
                            containsIgnoreCase(description, query);
                })
                .collect(Collectors.toList());
    }


    private String extractName(Resource resource) {
        switch (resource.getResourceType()) {
            case StructureDefinition:
                return ((StructureDefinition) resource).getName();
            case CodeSystem:
                return ((CodeSystem) resource).getName();
            case ValueSet:
                return ((ValueSet) resource).getName();
            case ImplementationGuide:
                return ((ImplementationGuide) resource).getName();
            case CapabilityStatement:
                return ((CapabilityStatement) resource).getName();
            case OperationDefinition:
                return ((OperationDefinition) resource).getName();
            case SearchParameter:
                return ((SearchParameter) resource).getName();
            default:
                return null;
        }
    }

    private String extractUrl(Resource resource) {
        switch (resource.getResourceType()) {
            case StructureDefinition:
                return ((StructureDefinition) resource).getUrl();
            case CodeSystem:
                return ((CodeSystem) resource).getUrl();
            case ValueSet:
                return ((ValueSet) resource).getUrl();
            case ImplementationGuide:
                return ((ImplementationGuide) resource).getUrl();
            case CapabilityStatement:
                return ((CapabilityStatement) resource).getUrl();
            case OperationDefinition:
                return ((OperationDefinition) resource).getUrl();
            case SearchParameter:
                return ((SearchParameter) resource).getUrl();
            default:
                return null;
        }
    }

    private String extractDescription(Resource resource) {
        switch (resource.getResourceType()) {
            case StructureDefinition:
                return ((StructureDefinition) resource).getDescription();
            case CodeSystem:
                return ((CodeSystem) resource).getDescription();
            case ValueSet:
                return ((ValueSet) resource).getDescription();
            case ImplementationGuide:
                return ((ImplementationGuide) resource).getDescription();
            case CapabilityStatement:
                return ((CapabilityStatement) resource).getDescription();
            case OperationDefinition:
                return ((OperationDefinition) resource).getDescription();
            case SearchParameter:
                return ((SearchParameter) resource).getDescription();
            default:
                return null;
        }
    }

    private boolean containsIgnoreCase(String field, String query) {
        return field != null && field.toLowerCase().contains(query.toLowerCase());
    }

}
