import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Logo from "../assets/t1.png";
import Navbar from "../components/Navbar";

function Home() {
  const API_BASE = "http://localhost:8080/pfe-matcher";
  const [file1, setFile1] = useState<File | null>(null);
  const [file2, setFile2] = useState<File | null>(null);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleImport = async () => {
    if (!file1 || !file2 || !startDate || !endDate) {
      alert("Veuillez sélectionner les deux fichiers et les dates.");
      return;
    }

    setLoading(true);
    try {
      // 1. Process Affectations
      const formData = new FormData();
      formData.append("studentFile", file1);
      formData.append("profFile", file2);
      
      const affectationRes = await axios.post(`${API_BASE}/api/affectations/process`, formData);

      if (!affectationRes.data || !affectationRes.data.assignments) {
            throw new Error("Le backend n'a retourné aucune affectation.");
        }

      // 2. Generate Planning
      const planningPayload = {
        startDate: startDate,
        endDate: endDate,
        durationMinutes: 60,
        salles: ["S4A", "S5A", "S16A", "S17A", "AMPHI A"]
      };
      const planningRes = await axios.post(`${API_BASE}/api/soutenances/generate`, planningPayload);

      // 3. Navigate to Dashboard with Data
      localStorage.setItem("affectationData", JSON.stringify(affectationRes.data));
      localStorage.setItem("planningData", JSON.stringify(planningRes.data));

      navigate("/pages2", { 
        state: { 
          affectation: affectationRes.data, 
          planning: planningRes.data 
        } 
      });

    } catch (err: any) {
        const msg = err.response?.data?.message || err.message;
        alert("Erreur: " + msg); 
        setLoading(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Navbar activePage="home" />
      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">Gestion des Projets de Fin d'Études</h2>
          <p className="text-white-50 mb-0">Importez les listes et configurez le planning des soutenances.</p>
        </div>
      </div>

      <div className="container my-5">
        <h5 className="section-title mb-3">Importer les fichiers Excel</h5>
        <div className="row g-4 mb-4">
          <div className="col-md-6">
            <div className="card custom-card h-100">
              <div className="card-body">
                <h6 className="card-title">Liste des étudiants</h6>
                <input type="file" className="form-control" onChange={(e) => setFile1(e.target.files?.[0] ?? null)} />
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card custom-card h-100">
              <div className="card-body">
                <h6 className="card-title">Liste des encadrants</h6>
                <input type="file" className="form-control" onChange={(e) => setFile2(e.target.files?.[0] ?? null)} />
              </div>
            </div>
          </div>
        </div>

        <h5 className="section-title mb-3">Planning PFE</h5>
        <div className="card custom-card mb-4">
          <div className="card-body">
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Date début</label>
                <input type="date" className="form-control" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Date fin</label>
                <input type="date" className="form-control" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
              </div>
            </div>
          </div>
        </div>

        <button className="btn custom-btn px-4" onClick={handleImport} disabled={loading}>
          {loading ? "Traitement en cours..." : "Importer et Générer"}
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