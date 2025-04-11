package com.ufg.artifactanalyzer.services;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//teste

@Service
public class TgzExtractorService {

    public void extractTgz(File tgzFile, String outputDir) throws IOException {
        try (
                InputStream fi = new FileInputStream(tgzFile);
                InputStream gzi = new GzipCompressorInputStream(fi);
                TarArchiveInputStream tarInput = new TarArchiveInputStream(gzi)
        ) {
            ArchiveEntry entry;
            while ((entry = tarInput.getNextEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + outputFile);
                    }
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    try (FileOutputStream out = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = tarInput.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                    }
                }
            }
        }
    }
}
