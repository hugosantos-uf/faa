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
    private final Map<String, List<Resource>> loadedResources = new HashMap<>();

    // Carrega e cacheia os recursos agrupados por tipo
    private void loadResources() throws IOException {
        if (!loadedResources.isEmpty()) {
            return;
        }

        System.out.println(">>>>> Carregando recursos da pasta: " + extractedPath);
        List<String> jsonContents = readJsonFiles();

        for (String json : jsonContents) {
            Resource resource = parseJsonToResource(json);
            if (resource != null) {
                String type = resource.getResourceType().name();
                loadedResources.computeIfAbsent(type, k -> new ArrayList<>()).add(resource);
            }
        }

        System.out.println(">>>>> Total de tipos de recursos carregados: " + loadedResources.keySet().size());
    }

    public List<Resource> listAllResources() throws IOException {
        loadResources();
        return loadedResources.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Resource> listResourcesByType(String resourceType) throws IOException {
        loadResources();
        return loadedResources.getOrDefault(resourceType, Collections.emptyList());
    }

    public Resource getResourceById(String id) throws IOException {
        loadResources();
        return loadedResources.values().stream()
                .flatMap(List::stream)
                .filter(resource -> id.equals(resource.getIdElement().getIdPart()))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Long> countResourcesByType() throws IOException {
        loadResources();
        Map<String, Long> counts = loadedResources.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (long) entry.getValue().size(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        counts.put("total", total);

        return counts;
    }

    public List<Resource> searchResources(String query) throws IOException {
        System.out.println(">>>>> Método searchResources() foi chamado");

        loadResources();

        Map<String, String> filters = parseQuery(query);
        List<Resource> allResources = loadedResources.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<Resource> filtered = allResources.stream()
                .filter(resource -> matchesAllFilters(resource, filters))
                .collect(Collectors.toList());

        System.out.println(">>>>> Recursos encontrados: " + filtered.size());
        return filtered;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> filters = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                filters.put(keyValue[0], keyValue[1]);
            }
        }
        return filters;
    }

    private boolean matchesAllFilters(Resource resource, Map<String, String> filters) {
        String json = fhirContext.newJsonParser().encodeResourceToString(resource);
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = "\"" + entry.getKey() + "\":";
            if (!json.contains(key) || !json.contains(entry.getValue())) {
                return false;
            }
        }
        return true;
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
}
