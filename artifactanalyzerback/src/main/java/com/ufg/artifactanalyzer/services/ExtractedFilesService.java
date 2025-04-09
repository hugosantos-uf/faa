package com.ufg.artifactanalyzer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtractedFilesService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String extractedPath = new File("uploads/extracted/package/").getAbsolutePath();

    public List<Map<String, Object>> listAllFiles() throws IOException {
        File folder = new File(extractedPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null) return Collections.emptyList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (File file : files) {
            Map<String, Object> json = objectMapper.readValue(file, Map.class);
            result.add(json);
        }
        return result;
    }

    public List<Map<String, Object>> listFilesByResourceType(String resourceType) throws IOException {
        return listAllFiles().stream()
                .filter(json -> resourceType.equalsIgnoreCase((String) json.get("resourceType")))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getFileById(String id) throws IOException {
        return listAllFiles().stream()
                .filter(json -> id.equals(json.get("id")))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Long> countFilesByResourceType() throws IOException {
        List<Map<String, Object>> allFiles = listAllFiles();

        Map<String, Long> counts = allFiles.stream()
                .collect(Collectors.groupingBy(
                        json -> {
                            String resourceType = (String) json.get("resourceType");
                            return resourceType != null ? resourceType : "UNKNOWN";
                        },
                        Collectors.counting()
                ));

        counts.put("total", (long) allFiles.size());

        // Ordena e coloca "total" por último
        Map<String, Long> orderedCounts = new LinkedHashMap<>();

        counts.entrySet().stream()
                .filter(entry -> !"total".equals(entry.getKey()))
                .forEach(entry -> orderedCounts.put(entry.getKey(), entry.getValue()));

        orderedCounts.put("total", counts.get("total"));

        return orderedCounts;
    }
}
