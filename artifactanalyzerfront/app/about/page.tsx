export default function About() {
  return (
    <main className="container mx-auto px-4 py-16 flex-grow">
      <h2 className="text-2xl font-bold mb-6 text-blue-700">Sobre o Projeto</h2>

      <p className="text-lg mb-4">
        Este sistema foi desenvolvido por{" "}
        <span className="font-semibold">Hugo Santos</span> na disciplina de{" "}
        <span className="font-semibold">Construção de Software</span> ministrada
        pelo professor <span className="font-semibold">Fábio Nogueira</span>.
      </p>

      <p className="text-lg mb-4">
        O <span className="font-semibold">FHIR Artifact Analyzer</span> é uma
        ferramenta que visa identificar, validar e facilitar a consulta de
        artefatos do padrão FHIR. Seu objetivo é apoiar tanto profissionais que
        integram sistemas de saúde quanto aqueles que desenvolvem guias de
        implementação FHIR, permitindo uma navegação mais clara e organizada
        entre recursos como <em>StructureDefinitions</em>, <em>ValueSets</em>,{" "}
        <em>CodeSystems</em>, entre outros.
      </p>

      <p className="text-lg">
        Com ele, é possível importar pacotes .tgz contendo artefatos FHIR,
        visualizar e filtrar seus dados, validar URLs canônicas, explorar
        relações em formato de grafo, e exportar resultados em diversos
        formatos.
      </p>
    </main>
  );
}
