package com.ufg.artifactanalyzer.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
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

    public String exportResourcesToCsv() throws IOException {
        loadResources();
        List<Resource> resources = listAllResources();
        StringWriter stringWriter = new StringWriter();
        String[] headers = {"resourceType", "id", "url", "name", "version"};

        try (CSVPrinter printer = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader(headers))) {
            for (Resource resource : resources) {
                String name = getPropertyValue(resource, "name");
                String url = getPropertyValue(resource, "url");
                String version = getPropertyValue(resource, "version");

                printer.printRecord(
                        resource.getResourceType().name(),
                        resource.getIdElement().getIdPart(),
                        url, name, version
                );
            }
        }
        return stringWriter.toString();
    }

    public Map<String, String> validateCanonicalUrls() throws IOException {
        loadResources();
        Map<String, String> validationResults = new LinkedHashMap<>();

        for (Resource resource : listAllResources()) {
            String resourceKey = resource.getResourceType().name() + "/" + resource.getIdElement().getIdPart();
            try {
                Property urlProp = resource.getNamedProperty("url");
                if (urlProp != null && urlProp.hasValues()) {
                    String url = urlProp.getValues().get(0).primitiveValue();
                    if (url == null || url.trim().isEmpty()) {
                        validationResults.put(resourceKey, "URL vazia");
                    } else {
                        new URI(url); // Tenta criar uma URI para validar a sintaxe
                        validationResults.put(resourceKey, "Válida");
                    }
                }
                // Se não tiver a propriedade 'url', simplesmente não adiciona ao mapa de resultados
            } catch (URISyntaxException e) {
                validationResults.put(resourceKey, "Inválida (Sintaxe incorreta)");
            } catch (Exception e) {
                // Captura outras exceções que possam ocorrer ao acessar a propriedade
                validationResults.put(resourceKey, "Erro ao ler URL");
            }
        }
        return validationResults;
    }


    // --- MÉTODOS PRIVADOS AUXILIARES ---

    private String getPropertyValue(Resource resource, String propertyName) {
        try {
            Property prop = resource.getNamedProperty(propertyName);
            if (prop != null && prop.hasValues()) {
                Base value = prop.getValues().get(0);
                return value.primitiveValue().replace("\"", ""); // Retorna o valor primitivo e remove aspas
            }
        } catch (Exception e) {
            // Ignora exceções se a propriedade não existir ou não puder ser lida
        }
        return "N/A";
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
            System.err.println("Diretório não encontrado ou vazio: " + extractedPath);
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