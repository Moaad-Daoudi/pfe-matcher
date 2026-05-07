import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import { useLocation, Link } from "react-router-dom";
import Navbar from "../components/Navbar";

type ImportState = {
  file1?: File | null;
  file2?: File | null;
  startDate?: string;
  endDate?: string;
};

function Pages2() {
  const { state } = useLocation();
  const { file1, file2, startDate, endDate } = (state ?? {}) as ImportState;

  return (
    <>
      {/* NAVBAR */}
      <Navbar activePage="home" />

      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">Gestion des Projets de Fin d'Etudes</h2>
          <p className="text-white-50 mb-0">Verification des donnees importees.</p>
        </div>
      </div>

      <div className="container my-5">
        <h5 className="section-title mb-3">Recapitulatif</h5>

        <div className="card custom-card mb-4">
          <div className="card-body">
            <p className="mb-2">
              <strong>Liste des etudiants :</strong> {file1?.name ?? "Aucun fichier selectionne"}
            </p>
            <p className="mb-2">
              <strong>Liste des encadrants :</strong> {file2?.name ?? "Aucun fichier selectionne"}
            </p>
            <p className="mb-0">
              <strong>Periode :</strong>{" "}
              {startDate && endDate ? `${startDate} -> ${endDate}` : "Dates non configurees"}
            </p>
          </div>
        </div>

        <Link className="btn custom-btn px-4" to="/">Retour</Link>
      </div>
    </>
  );
}

export default Pages2;
