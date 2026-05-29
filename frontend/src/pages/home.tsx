import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Logo from "../assets/t1.png";
import Navbar from "../components/Navbar";

interface Student {
  id?: string;
  name: string;
}

interface Professor {
  id?: string;
  name: string;
  students: Student[];
}

interface Assignment {
  professor: Professor;
  students: Student[];
  binomes?: Array<[Student, Student]>;
}

function Home() {
  const API_BASE = "http://localhost:8080/pfe-matcher";
  const tomorrowObj = new Date();
  tomorrowObj.setDate(tomorrowObj.getDate() + 1);
  const tomorrow = tomorrowObj.toISOString().split("T")[0];

  // Step state
  const [currentStep, setCurrentStep] = useState(1);
  const [file1, setFile1] = useState<File | null>(null);
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [binomePairs, setBinomePairs] = useState<Map<string, string>>(new Map());
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("18:00");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Step 1: Process CSV import
  const handleImportFile = async () => {
    if (!file1) {
      alert("Veuillez sélectionner un fichier.");
      return;
    }

    setLoading(true);
    try {
      const formData = new FormData();
      formData.append("studentFile", file1);
      formData.append("profFile", file1);

      const affectationRes = await axios.post(`${API_BASE}/api/affectations/process`, formData);

      if (!affectationRes.data || !affectationRes.data.assignments) {
        throw new Error("Le backend n'a retourné aucune affectation.");
      }

      setAssignments(affectationRes.data.assignments);
      setCurrentStep(2);
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message;
      alert("Erreur: " + msg);
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Add binome pair
  const addBinomePair = (student1Id: string, student2Id: string) => {
    if (student1Id && student2Id && student1Id !== student2Id) {
      const newPairs = new Map(binomePairs);
      newPairs.set(student1Id, student2Id);
      newPairs.set(student2Id, student1Id);
      setBinomePairs(newPairs);
    }
  };

  // Step 2: Remove binome pair
  const removeBinomePair = (studentId: string) => {
    const newPairs = new Map(binomePairs);
    const paired = newPairs.get(studentId);
    if (paired) {
      newPairs.delete(studentId);
      newPairs.delete(paired);
      setBinomePairs(newPairs);
    }
  };

  // Move to step 3
  const goToDateConfiguration = () => {
    setCurrentStep(3);
  };

  // Step 4: Final import and generate planning
  const handleFinalImport = async () => {
    if (!startDate || !endDate) {
      alert("Veuillez sélectionner les dates.");
      return;
    }

    setLoading(true);
    try {
      const planningPayload = {
        startDate: startDate,
        endDate: endDate,
        startTime: startTime,
        endTime: endTime,
        binomes: Array.from(binomePairs.entries())
      };
      const planningRes = await axios.post(`${API_BASE}/api/soutenances/generate`, planningPayload);

      navigate("/planing", {
        state: {
          affectation: { assignments },
          planning: planningRes.data
        }
      });
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message;
      alert("Erreur: " + msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Navbar activePage="home" />
      <div className="custom-hero py-5" style={{ background: "none", borderBottom: "none", backdropFilter: "none" }}>
        <div className="container">
          <h1 className="text-white fw-bold mb-2">Gestion des Projets de Fin d'Études</h1>
          <p className="text-white-50">Suivez les étapes pour configurer votre planning de soutenances</p>
        </div>
      </div>

      <div className="container my-5">


        {/* Step 1: Import File */}
        {currentStep === 1 && (
          <div className="row">
            <div className="col-lg-7 mx-auto">
              <div className="card custom-card shadow-lg">
                <div className="card-body p-5">
                  <div className="mb-5 text-center">
                    <h3 className="text-white fw-bold mb-2">Importer votre Fichier</h3>
                    <p className="text-muted mb-0">Commençons par charger votre fichier Excel</p>
                  </div>

                  <div
                    className="upload-area p-5 rounded text-center"
                    style={{
                      border: "2px dashed rgba(84, 166, 89, 0.5)",
                      backgroundColor: "rgba(84, 166, 89, 0.02)",
                      cursor: "pointer"
                    }}
                  >
                    <input
                      type="file"
                      className="form-control d-none"
                      id="fileInput"
                      onChange={(e) => setFile1(e.target.files?.[0] ?? null)}
                    />
                    <label htmlFor="fileInput" className="text-center w-100 mb-0" style={{ cursor: "pointer" }}>
                      <p className="text-light fw-600 mb-1" style={{ fontSize: "1.1rem" }}>
                        {file1 ? `✅ ${file1.name}` : "Cliquez ou glissez votre fichier"}
                      </p>
                      <small className="text-muted">Fichier Excel (.xlsx, .xls)</small>
                    </label>
                  </div>

                  {file1 && (
                    <div className="alert alert-success mt-4 mb-0 border-0 bg-success bg-opacity-10">
                      <strong className="text-success">✓ Fichier prêt:</strong>
                      <span className="text-light ms-2">{file1.name}</span>
                    </div>
                  )}

                  <div className="mt-5 text-center">
                    <button
                      className="btn custom-btn px-5 py-3 fw-600"
                      onClick={handleImportFile}
                      disabled={!file1 || loading}
                      style={{ minWidth: "250px", fontSize: "1rem" }}
                    >
                      {loading ? "⏳ Traitement..." : "➜ Continuer"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Step 2: View & Manage Assignments */}
        {currentStep === 2 && (
          <div>
            <div className="mb-5 text-center">
              <h3 className="text-white fw-bold mb-2">👥 Gérer les Affectations</h3>
              <p className="text-muted">Vérifiez et créez des binômes si nécessaire</p>
            </div>

            {assignments.map((assignment, idx) => (
              <div key={idx} className="card custom-card shadow-lg mb-4">
                <div className="card-body p-4">
                  <div className="d-flex align-items-center mb-4">
                    <div className="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                         style={{ width: "44px", height: "44px", background: "linear-gradient(135deg, #00c6ff, #54a659)" }}>
                      👨‍🏫
                    </div>
                    <div className="ms-3">
                      <h6 className="text-light mb-0 fw-600">{assignment.professor.name}</h6>
                      <small className="text-muted">{assignment.students.length} étudiant(s)</small>
                    </div>
                  </div>

                  <div className="table-responsive">
                    <table className="table table-dark mb-0">
                      <thead>
                        <tr>
                          <th className="text-success fw-600">👤 Étudiant</th>
                          <th className="text-success fw-600">🤝 Binôme</th>
                          <th className="text-success fw-600">⚙️ Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {assignment.students.map((student, sidx) => (
                          <tr key={sidx}>
                            <td>
                              <span className="text-light fw-500">{student.name}</span>
                            </td>
                            <td>
                              {binomePairs.get(student.id || "") ? (
                                <span className="badge bg-info">
                                  {assignment.students.find(s => s.id === binomePairs.get(student.id || ""))?.name || "N/A"}
                                </span>
                              ) : (
                                <select
                                  className="form-select form-select-sm bg-dark text-light border-secondary"
                                  onChange={(e) => {
                                    if (e.target.value) {
                                      addBinomePair(student.id || "", e.target.value);
                                      e.target.value = "";
                                    }
                                  }}
                                  defaultValue=""
                                >
                                  <option value="">-- Sélectionner --</option>
                                  {assignment.students
                                    .filter((s) => s.id !== student.id && !binomePairs.has(s.id || ""))
                                    .map((s, i) => (
                                      <option key={i} value={s.id || ""}>
                                        {s.name}
                                      </option>
                                    ))}
                                </select>
                              )}
                            </td>
                            <td>
                              {binomePairs.get(student.id || "") && (
                                <button
                                  className="btn btn-sm btn-outline-danger"
                                  onClick={() => removeBinomePair(student.id || "")}
                                  style={{ fontSize: "12px" }}
                                >
                                  ✕ Supprimer
                                </button>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            ))}

            <div className="mt-5 text-center">
              <button
                className="btn custom-btn px-5 py-3 fw-600"
                onClick={goToDateConfiguration}
                style={{ minWidth: "300px", fontSize: "1rem" }}
              >
                ➜ Configuration des Dates & Heures
              </button>
            </div>
          </div>
        )}

        {/* Step 3: Date & Time Configuration */}
        {currentStep === 3 && (
          <div className="row">
            <div className="col-lg-7 mx-auto">
              <div className="card custom-card shadow-lg">
                <div className="card-body p-5">
                  <div className="mb-5 text-center">
                    <h3 className="text-white fw-bold mb-2">⏰ Configuration des Soutenances</h3>
                    <p className="text-muted">Définissez les dates et horaires</p>
                  </div>

                  <div className="row g-4">
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">📅 Date Début</label>
                      <input
                        type="date"
                        className="form-control"
                        min={tomorrow}
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">📅 Date Fin</label>
                      <input
                        type="date"
                        className="form-control"
                        min={startDate || tomorrow}
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">🕐 Heure Début</label>
                      <input
                        type="time"
                        className="form-control"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                      />
                      <small className="text-muted d-block mt-2">Par défaut: 09:00</small>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">🕐 Heure Fin</label>
                      <input
                        type="time"
                        className="form-control"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                      />
                      <small className="text-muted d-block mt-2">Par défaut: 18:00</small>
                    </div>
                  </div>

                  <div className="mt-5 text-center">
                    <button
                      className="btn custom-btn px-5 py-3 fw-600"
                      onClick={() => setCurrentStep(4)}
                      style={{ minWidth: "250px", fontSize: "1rem" }}
                    >
                      ➜ Vérifier & Générer
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Step 4: Review & Generate */}
        {currentStep === 4 && (
          <div className="row">
            <div className="col-lg-7 mx-auto">
              <div className="card custom-card shadow-lg">
                <div className="card-body p-5">
                  <div className="mb-5 text-center">
                    <h3 className="text-white fw-bold mb-2">✅ Résumé & Génération</h3>
                    <p className="text-muted">Vérifiez votre configuration</p>
                  </div>

                  <div className="p-4 rounded mb-4" style={{ background: "rgba(0, 198, 255, 0.08)", border: "1px solid rgba(0, 198, 255, 0.2)" }}>
                    <div className="row g-3">
                      <div className="col-md-6">
                        <p className="text-muted small mb-1">📋 Affectations</p>
                        <p className="text-light fw-600">{assignments.length} encadrants</p>
                      </div>
                      <div className="col-md-6">
                        <p className="text-muted small mb-1">🤝 Binômes Créés</p>
                        <p className="text-light fw-600">{binomePairs.size / 2} paires</p>
                      </div>
                      <div className="col-md-6">
                        <p className="text-muted small mb-1">📅 Période</p>
                        <p className="text-light fw-600">{startDate} → {endDate}</p>
                      </div>
                      <div className="col-md-6">
                        <p className="text-muted small mb-1">🕐 Horaires</p>
                        <p className="text-light fw-600">{startTime} - {endTime}</p>
                      </div>
                    </div>
                  </div>

                  <div className="alert alert-info border-0 mb-4" style={{ background: "rgba(0, 198, 255, 0.1)" }}>
                    <p className="text-light mb-0">
                      <strong className="text-info">ℹ️ Info:</strong> Vous êtes sur le point de générer le planning complet avec toutes les configurations.
                    </p>
                  </div>

                  <div className="mt-5 text-center">
                    <button
                      className="btn custom-btn px-5 py-3 fw-600"
                      onClick={handleFinalImport}
                      disabled={loading}
                      style={{ minWidth: "280px", fontSize: "1rem" }}
                    >
                      {loading ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                          Génération...
                        </>
                      ) : (
                        "🚀 Générer le Planning"
                      )}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
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
