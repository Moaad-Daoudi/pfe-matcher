import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "../assets/t1.png";
import Navbar from "../components/Navbar";
function Home() {
  const [file1, setFile1] = useState<File | null>(null);
  const [file2, setFile2] = useState<File | null>(null);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  return (
    <>
      {/* NAVBAR */}
      <Navbar activePage="home" />

      {/* HERO */}
      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">Gestion des Projets de Fin d'Études</h2>
          <p className="text-white-50 mb-0">Importez les listes et configurez le planning des soutenances.</p>
        </div>
      </div>

      {/* MAIN */}
      <div className="container my-5">

        <h5 className="section-title mb-3">Importer les fichiers Excel</h5>

        <div className="row g-4 mb-4">
          {/* FILE 1 */}
          <div className="col-md-6">
            <div className="card custom-card h-100">
              <div className="card-body">
                <h6 className="card-title">Liste des étudiants</h6>
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  className="form-control"
                  onChange={(e) => setFile1(e.target.files?.[0] ?? null)}
                />
                {file1 && (
                  <p className="mt-2 mb-0 small text-success fw-semibold">✔ {file1.name}</p>
                )}
              </div>
            </div>
          </div>

          {/* FILE 2 */}
          <div className="col-md-6">
            <div className="card custom-card h-100">
              <div className="card-body">
                <h6 className="card-title">Liste des encadrants</h6>
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  className="form-control"
                  onChange={(e) => setFile2(e.target.files?.[0] ?? null)}
                />
                {file2 && (
                  <p className="mt-2 mb-0 small text-success fw-semibold">✔ {file2.name}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* PLANNING */}
        <h5 className="section-title mb-3">Planning PFE</h5>
        <div className="card custom-card mb-4">
          <div className="card-body">
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Date début</label>
                <input
                  type="date"
                  className="form-control"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Date fin</label>
                <input
                  type="date"
                  className="form-control"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                />
              </div>
            </div>

            {startDate && endDate && (
              <div className="alert custom-alert mt-3 mb-0">
                📅 Période : <strong>{startDate}</strong> → <strong>{endDate}</strong>
              </div>
            )}
          </div>
        </div>

        {/* BUTTON */}
        <button
  className="btn custom-btn px-4"
  onClick={() => {
    setLoading(true);

    setTimeout(() => {
      setLoading(false);

      navigate("/pages2", {
        state: { file1, file2, startDate, endDate }
      });
    }, 2000); 
  }}
>
  Importer
</button>

      </div>
      {loading && (
  <div className="overlay">
    <div className="loader-box">
      <img src={Logo} className="logo-anim" alt="loading" />
      <p className="text-white mt-3">Traitement en cours...</p>
    </div>
  </div>
)}
    </>
  );
}

export default Home;
