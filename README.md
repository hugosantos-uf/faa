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

---

## 🚀 Como Rodar o Projeto

Para executar o **FHIR Artifact Analyzer** localmente, você precisará clonar o repositório e rodar o backend (servidor Java) e o frontend (aplicação Next.js) separadamente.

### ✅ Pré-requisitos

Antes de começar, garanta que você tenha as seguintes ferramentas instaladas:

* **Java 17 ou superior**: O backend foi desenvolvido com Java 17.
* **Maven**: Para gerenciar as dependências e compilar o projeto Java.
* **Node.js 20.x ou superior**: Para o ambiente de execução do frontend.
* **npm, yarn, ou pnpm**: Para gerenciar as dependências do frontend.

---

### 1. Configurando o Backend (Servidor Java)

O backend é responsável por extrair os arquivos `.tgz` e fornecer os recursos FHIR através de uma API REST.

1.  **Navegue até o diretório do backend:**
    ```bash
    cd artifactanalyzerback
    ```

2.  **Compile o projeto com o Maven:**
    Isso irá baixar todas as dependências necessárias.
    ```bash
    mvn clean install
    ```

3.  **Inicie o servidor Spring Boot:**
    ```bash
    mvn spring-boot:run
    ```
    O servidor estará rodando e escutando na porta `8080`. Você verá as mensagens de log do Spring no seu terminal.

---

### 2. Configurando o Frontend (Aplicação Next.js)

O frontend é a interface web onde o usuário interage com a aplicação.

1.  **Abra um novo terminal**. Não feche o terminal onde o backend está rodando.

2.  **Navegue até o diretório do frontend:**
    ```bash
    cd artifactanalyzerfront
    ```

3.  **Instale as dependências do Node.js:**
    Escolha o gerenciador de pacotes de sua preferência.
    ```bash
    npm install
    # ou
    yarn install
    # ou
    pnpm install
    ```

4.  **Inicie o servidor de desenvolvimento do Next.js:**
    ```bash
    npm run dev
    ```
    O servidor de desenvolvimento iniciará e estará acessível na porta `3000`.

---

### 3. Usando a Aplicação

🎉 Pronto! Com os dois servidores rodando, você pode usar a aplicação:

1.  Abra seu navegador e acesse [**http://localhost:3000**](http://localhost:3000).
2.  Arraste e solte um arquivo de pacote FHIR no formato `.tgz` na área indicada ou clique para selecioná-lo.
3.  Após o upload, os recursos serão extraídos pelo backend e exibidos na tela, onde você poderá filtrar, buscar e analisar os artefatos.

---
