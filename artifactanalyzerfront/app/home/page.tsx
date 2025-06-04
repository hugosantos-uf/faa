"use client";

import React, { useState, useCallback, useMemo } from "react";
import { useDropzone, FileRejection } from "react-dropzone";
import {
  UploadCloud,
  Search,
  Loader,
  ServerCrash,
  FileJson,
  Download,
  CheckCircle,
  XCircle,
  FileCog,
} from "lucide-react";

interface FhirResource {
  id: string;
  resourceType: string;
  url?: string;
  name?: string | { text: string }[];
  [key: string]: any; // Para outras propriedades
}

const API_BASE_URL = "http://localhost:8080";

export default function HomePage() {
  const [isUploading, setIsUploading] = useState<boolean>(false);
  const [isLoadingData, setIsLoadingData] = useState<boolean>(false);

  const [statusMessage, setStatusMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);

  const [allResources, setAllResources] = useState<FhirResource[]>([]);
  const [resourceCounts, setResourceCounts] = useState<Record<
    string,
    number
  > | null>(null);

  const [selectedType, setSelectedType] = useState<string>("all");
  const [searchTerm, setSearchTerm] = useState<string>("");

  const [isValidationModalOpen, setIsValidationModalOpen] =
    useState<boolean>(false);
  const [validationResults, setValidationResults] = useState<Record<
    string,
    string
  > | null>(null);
  const [isValidating, setIsValidating] = useState<boolean>(false);

  const fetchFhirData = useCallback(async () => {
    setIsLoadingData(true);
    setStatusMessage(null);
    try {
      const countResponse = await fetch(`${API_BASE_URL}/fhir/resources/count`);
      if (!countResponse.ok)
        throw new Error(
          `Falha ao buscar contagem: ${countResponse.statusText}`
        );
      const counts = await countResponse.json();
      setResourceCounts(counts);

      const resourcesResponse = await fetch(`${API_BASE_URL}/fhir/resources`);
      if (!resourcesResponse.ok)
        throw new Error(
          `Falha ao buscar recursos: ${resourcesResponse.statusText}`
        );
      const resources = await resourcesResponse.json();
      setAllResources(resources);

      setStatusMessage({
        type: "success",
        text: "Recursos carregados com sucesso!",
      });
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Um erro desconhecido ocorreu";
      console.error("Erro ao buscar dados FHIR:", errorMessage);
      setStatusMessage({
        type: "error",
        text: `Não foi possível carregar os dados: ${errorMessage}`,
      });
      setAllResources([]);
      setResourceCounts(null);
    } finally {
      setIsLoadingData(false);
    }
  }, []);

  const onDrop = useCallback(
    async (acceptedFiles: File[], fileRejections: FileRejection[]) => {
      if (fileRejections.length > 0) {
        setStatusMessage({
          type: "error",
          text: "Arquivo inválido. Por favor, envie apenas um arquivo .tgz.",
        });
        return;
      }

      if (acceptedFiles.length > 0) {
        const file = acceptedFiles[0];
        const formData = new FormData();
        formData.append("file", file);

        setIsUploading(true);
        setStatusMessage(null);
        setAllResources([]);
        setResourceCounts(null);

        try {
          const response = await fetch(`${API_BASE_URL}/extract`, {
            method: "POST",
            body: formData,
          });

          if (!response.ok) {
            const errorText = await response.text();
            throw new Error(
              errorText || `Erro no servidor: ${response.status}`
            );
          }

          const resultText = await response.text();
          setStatusMessage({ type: "success", text: resultText });

          await fetchFhirData();
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Um erro desconhecido ocorreu";
          console.error("Erro ao fazer upload:", error);
          setStatusMessage({
            type: "error",
            text: `Erro no upload: ${errorMessage}`,
          });
        } finally {
          setIsUploading(false);
        }
      }
    },
    [fetchFhirData]
  );

  const handleValidateUrls = async () => {
    if (allResources.length === 0) {
      setStatusMessage({
        type: "error",
        text: "Carregue um pacote antes de validar as URLs.",
      });
      return;
    }
    setIsValidating(true);
    setValidationResults(null);
    try {
      const response = await fetch(
        `${API_BASE_URL}/fhir/resources/validate-urls`
      );
      if (!response.ok)
        throw new Error(`Falha na validação: ${response.statusText}`);
      const results = await response.json();
      setValidationResults(results);
      setIsValidationModalOpen(true);
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Erro desconhecido";
      setStatusMessage({
        type: "error",
        text: `Erro ao validar URLs: ${errorMessage}`,
      });
    } finally {
      setIsValidating(false);
    }
  };

  const downloadJson = () => {
    if (allResources.length === 0) {
      setStatusMessage({
        type: "error",
        text: "Não há recursos para exportar.",
      });
      return;
    }
    const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
      JSON.stringify(allResources, null, 2)
    )}`;
    const link = document.createElement("a");
    link.href = jsonString;
    link.download = "fhir_resources.json";
    link.click();
  };

  const filteredResources = useMemo(() => {
    return allResources.filter((resource) => {
      const typeMatch =
        selectedType === "all" || resource.resourceType === selectedType;
      const termMatch =
        searchTerm === "" ||
        JSON.stringify(resource)
          .toLowerCase()
          .includes(searchTerm.toLowerCase());
      return typeMatch && termMatch;
    });
  }, [allResources, selectedType, searchTerm]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { "application/gzip": [".tgz", ".gz"] },
    maxFiles: 1,
  });

  return (
    <main className="flex-grow container mx-auto px-4 py-10">
      <div
        {...getRootProps()}
        className={`border-4 border-dashed rounded-2xl p-12 flex flex-col items-center justify-center transition-colors duration-300 cursor-pointer text-center
            ${
              isDragActive
                ? "border-blue-500 bg-blue-50"
                : "border-gray-300 bg-white hover:bg-gray-50"
            }`}
      >
        <input {...getInputProps()} />
        <UploadCloud className="w-16 h-16 text-blue-500 mb-4" />
        {isUploading ? (
          <p className="text-lg font-semibold text-blue-600">
            Enviando e Processando...
          </p>
        ) : isDragActive ? (
          <p className="text-lg font-semibold text-blue-500">
            Solte o arquivo .tgz aqui...
          </p>
        ) : (
          <>
            <p className="text-xl font-semibold">
              Arraste e solte um arquivo .tgz aqui
            </p>
            <p className="text-gray-500 mt-2">
              ou clique para selecionar o arquivo
            </p>
            <p className="text-sm text-gray-400 mt-2">
              O arquivo deve conter artefatos FHIR
            </p>
          </>
        )}
      </div>

      {statusMessage && (
        <div
          className={`mt-4 p-4 rounded-md text-center font-medium ${
            statusMessage.type === "error"
              ? "bg-red-100 text-red-800"
              : "bg-green-100 text-green-800"
          }`}
        >
          {statusMessage.text}
        </div>
      )}

      {resourceCounts && (
        <div className="mt-8 p-4 bg-white border rounded-lg shadow">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div className="flex-shrink-0">
              <h3 className="text-xl font-bold text-gray-800">
                Total de Recursos:{" "}
                <span className="text-blue-600">{resourceCounts.total}</span>
              </h3>
            </div>

            <div className="flex items-center gap-2">
              <label htmlFor="resourceTypeFilter" className="font-semibold">
                Filtrar por tipo:
              </label>
              <select
                id="resourceTypeFilter"
                value={selectedType}
                onChange={(e) => setSelectedType(e.target.value)}
                className="p-2 border rounded-md bg-gray-50"
              >
                <option value="all">Todos os Tipos</option>
                {Object.keys(resourceCounts)
                  .filter((key) => key !== "total")
                  .sort()
                  .map((type) => (
                    <option key={type} value={type}>
                      {type} ({resourceCounts[type]})
                    </option>
                  ))}
              </select>
            </div>

            <div className="relative flex-grow max-w-xs">
              <input
                type="text"
                placeholder="Buscar em todos os campos..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full p-2 pl-10 border rounded-md"
              />
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            </div>
          </div>

          <div className="mt-4 pt-4 border-t flex items-center gap-3 flex-wrap">
            <h4 className="font-semibold">Ações:</h4>
            <button
              onClick={downloadJson}
              className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors text-sm inline-flex items-center gap-2"
            >
              <Download size={16} /> Exportar para JSON
            </button>
            <a
              href={`${API_BASE_URL}/fhir/resources/export/csv`}
              download
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-sm inline-flex items-center gap-2"
            >
              <Download size={16} /> Exportar para CSV
            </a>
            <button
              onClick={handleValidateUrls}
              disabled={isValidating}
              className="px-4 py-2 bg-yellow-500 text-white rounded-md hover:bg-yellow-600 transition-colors text-sm inline-flex items-center gap-2 disabled:bg-gray-400"
            >
              {isValidating ? (
                <Loader size={16} className="animate-spin" />
              ) : (
                <FileCog size={16} />
              )}
              {isValidating ? "Validando..." : "Validar URLs"}
            </button>
          </div>
        </div>
      )}

      <div className="mt-8">
        {isLoadingData ? (
          <div className="flex flex-col items-center justify-center p-10">
            <Loader className="w-12 h-12 text-blue-500 animate-spin" />
            <p className="mt-4 text-lg font-semibold text-gray-600">
              Carregando recursos...
            </p>
          </div>
        ) : allResources.length > 0 ? (
          filteredResources.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredResources.map((resource, index) => (
                <div
                  key={`${resource.resourceType}-${resource.id}-${index}`}
                  className="border p-4 rounded-lg shadow-md bg-white transition-transform hover:scale-105"
                >
                  <div className="flex items-center gap-3 mb-2">
                    <FileJson className="w-6 h-6 text-blue-500" />
                    <h4
                      className="text-lg font-bold text-blue-700 truncate"
                      title={`${resource.resourceType}/${resource.id}`}
                    >
                      {resource.resourceType}/{resource.id}
                    </h4>
                  </div>
                  {resource.name && (
                    <p className="text-sm truncate">
                      <strong>Nome:</strong>{" "}
                      {typeof resource.name === "string"
                        ? resource.name
                        : JSON.stringify(resource.name)}
                    </p>
                  )}
                  {resource.url && (
                    <p className="text-sm truncate">
                      <strong>URL:</strong> {resource.url}
                    </p>
                  )}

                  <details className="mt-3">
                    <summary className="cursor-pointer text-sm text-gray-500 hover:text-gray-800">
                      Ver JSON completo
                    </summary>
                    <pre className="mt-2 p-2 bg-gray-100 rounded text-xs overflow-auto max-h-60 border">
                      {JSON.stringify(resource, null, 2)}
                    </pre>
                  </details>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center p-10 bg-white rounded-lg shadow">
              <Search className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <p className="text-lg font-semibold text-gray-700">
                Nenhum recurso encontrado.
              </p>
              <p className="text-gray-500">
                Tente ajustar os filtros ou o termo de busca.
              </p>
            </div>
          )
        ) : statusMessage && statusMessage.type === "error" ? (
          <div className="text-center p-10 bg-red-50 rounded-lg shadow">
            <ServerCrash className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <p className="text-lg font-semibold text-red-700">
              Ocorreu um erro ao carregar os dados.
            </p>
            <p className="text-red-600">
              Verifique a conexão com o backend ou tente fazer um novo upload.
            </p>
          </div>
        ) : (
          <div className="text-center p-10 bg-white rounded-lg shadow">
            <p className="text-lg font-semibold text-gray-700">
              Nenhum pacote carregado.
            </p>
            <p className="text-gray-500">
              Por favor, faça o upload de um arquivo .tgz para começar a
              análise.
            </p>
          </div>
        )}
      </div>

      {isValidationModalOpen && validationResults && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-2xl max-w-2xl w-full max-h-[80vh] flex flex-col">
            <header className="p-4 border-b flex justify-between items-center">
              <h3 className="text-xl font-bold">
                Resultados da Validação de URL
              </h3>
              <button
                onClick={() => setIsValidationModalOpen(false)}
                className="text-gray-500 hover:text-gray-800"
              >
                <XCircle size={24} />
              </button>
            </header>
            <main className="p-6 overflow-y-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr>
                    <th className="border-b p-2 bg-gray-100 font-semibold">
                      Recurso
                    </th>
                    <th className="border-b p-2 bg-gray-100 font-semibold">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(validationResults).map(([key, value]) => (
                    <tr key={key} className="hover:bg-gray-50">
                      <td className="border-b p-2 font-mono text-sm">{key}</td>
                      <td
                        className={`border-b p-2 text-sm font-semibold inline-flex items-center gap-2 ${
                          value === "Válida" ? "text-green-600" : "text-red-600"
                        }`}
                      >
                        {value === "Válida" ? (
                          <CheckCircle size={16} />
                        ) : (
                          <XCircle size={16} />
                        )}
                        {value}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </main>
            <footer className="p-4 border-t text-right bg-gray-50">
              <button
                onClick={() => setIsValidationModalOpen(false)}
                className="px-6 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
              >
                Fechar
              </button>
            </footer>
          </div>
        </div>
      )}
    </main>
  );
}
