# FHIR Artifact Analyzer

![FHIR Logo](https://www.hl7.org/fhir/assets/images/fhir-logo-www.png)

**FHIR Artifact Analyzer** é uma ferramenta web desenvolvida para identificar, validar, explorar e exportar artefatos do padrão [FHIR (Fast Healthcare Interoperability Resources)](https://www.hl7.org/fhir/), versão 4.0.1.

Este sistema foi criado como parte da disciplina **Construção de Software**, ministrada pelo professor **Fábio Nogueira**, na Universidade Federal de Goiás.

---

## 🔍 Objetivo

Facilitar a navegação e análise de recursos FHIR contidos em arquivos `.tgz`, `.zip` ou diretórios locais, especialmente os utilizados em guias de implementação, como:

- `StructureDefinition`
- `ValueSet`
- `CodeSystem`
- `ImplementationGuide`
- `CapabilityStatement`
- `OperationDefinition`
- `SearchParameter`

---

## ⚙️ Funcionalidades

- Upload de arquivos `.tgz` com artefatos FHIR.
- Visualização dos recursos em formato JSON.
- Filtros por tipo de recurso (ex: apenas `ValueSet`).
- Busca por nome, descrição ou URL canônica.
- Contagem total e por tipo de recurso.
- Preparado para:
  - Validação de URLs canônicas.
  - Construção de grafo de relacionamentos.
  - Exportação dos dados em JSON, CSV e PNG.

---

## 🧱 Tecnologias Utilizadas

### 🔧 Backend

- **Java 17**
- **Spring Boot 3**
- [HAPI FHIR](https://hapifhir.io/) (validação e parse dos recursos)
- Jackson / Java IO para leitura de arquivos `.tgz` e `.json`

### 💻 Frontend

- **Next.js**
- **React**
- **Tailwind CSS**
- **React Dropzone** (upload por drag-and-drop)
- **Lucide-react** (ícones)
