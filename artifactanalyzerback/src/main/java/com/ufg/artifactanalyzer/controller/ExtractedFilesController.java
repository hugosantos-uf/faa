package com.ufg.artifactanalyzer.controller;

import com.ufg.artifactanalyzer.services.ExtractedFilesService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ExtractedFilesController {

    private final ExtractedFilesService extractedFilesService;

    public ExtractedFilesController(ExtractedFilesService extractedFilesService) {
        this.extractedFilesService = extractedFilesService;
    }

    @GetMapping("/files")
    public List<Map<String, Object>> getAllExtractedFiles() throws IOException {
        return extractedFilesService.listAllFiles();
    }

    @GetMapping("/files/{resourceType}")
    public List<Map<String, Object>> getFilesByResourceType(@PathVariable String resourceType) throws IOException {
        return extractedFilesService.listFilesByResourceType(resourceType);
    }

    @GetMapping("/files/id/{id}")
    public Map<String, Object> getFileById(@PathVariable String id) throws IOException {
        Map<String, Object> result = extractedFilesService.getFileById(id);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo com id '" + id + "' não encontrado");
        }
        return result;
    }

    @GetMapping("/files/count")
    public Map<String, Long> countFilesByResourceType() throws IOException {
        return extractedFilesService.countFilesByResourceType();
    }
}
