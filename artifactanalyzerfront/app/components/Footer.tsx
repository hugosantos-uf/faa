export default function Footer() {
  return (
    <footer className="bg-white border-t text-center text-sm py-4 text-gray-500">
      <p>
        &copy; {new Date().getFullYear()} FHIR Artifact Analyzer. Todos os
        direitos reservados.
      </p>
    </footer>
  );
}
