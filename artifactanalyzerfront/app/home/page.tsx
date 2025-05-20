"use client";

import React, { useCallback } from "react";
import { useDropzone } from "react-dropzone";
import { UploadCloud } from "lucide-react";

export default function HomePage() {
  const onDrop = useCallback((acceptedFiles: any) => {
    console.log(acceptedFiles);
    // Aqui você pode fazer o upload para o backend
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { "application/gzip": [".tgz"] },
    maxFiles: 1,
  });

  return (
    <main className="flex-grow container mx-auto px-4 py-16">
      <div
        {...getRootProps()}
        className={`border-4 border-dashed rounded-2xl p-12 flex flex-col items-center justify-center transition-colors duration-300 cursor-pointer
            ${
              isDragActive
                ? "border-blue-500 bg-blue-50"
                : "border-gray-300 bg-white"
            }`}
      >
        <input {...getInputProps()} />
        <UploadCloud className="w-16 h-16 text-blue-500 mb-4" />
        {isDragActive ? (
          <p className="text-blue-500 font-medium">
            Solte o arquivo .tgz aqui...
          </p>
        ) : (
          <>
            <p className="text-lg font-semibold">
              Selecione ou arraste um arquivo .tgz
            </p>
            <p className="text-sm text-gray-500 mt-2">
              O arquivo deve conter artefatos FHIR
            </p>
          </>
        )}
      </div>
    </main>
  );
}
