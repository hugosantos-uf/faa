export default function Navbar() {
  return (
    <header className="bg-white shadow p-4">
      <div className="container mx-auto flex justify-between items-center">
        <h1 className="text-xl font-bold text-blue-600">
          FHIR Artifact Analyzer
        </h1>
        <nav className="space-x-4">
          <a href="#" className="text-sm hover:underline text-gray-600">
            Início
          </a>
          <a href="#" className="text-sm hover:underline text-gray-600">
            Sobre
          </a>
        </nav>
      </div>
    </header>
  );
}
