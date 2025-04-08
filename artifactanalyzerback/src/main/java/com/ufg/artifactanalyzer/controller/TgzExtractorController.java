package com.ufg.artifactanalyzer.controller;

import com.ufg.artifactanalyzer.services.TgzExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class TgzExtractorController {

    @Autowired
    private TgzExtractorService extractorService;

    @PostMapping("/extract")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        // Cria um arquivo temporário para armazenar o upload inicial
        File tempFile = File.createTempFile("upload", ".tgz");
        file.transferTo(tempFile);

        // Define a pasta de saída dentro do projeto
        String outputDir = new File("uploads/extracted/").getAbsolutePath();

        // Garante que a pasta existe
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Extrai o arquivo .tgz para a pasta definida
        extractorService.extractTgz(tempFile, outputDir);

        return "Arquivo extraído para: " + outputDir;
    }

}
