import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Logo from "../assets/t1.png";
import Navbar from "../components/Navbar";
import { Calendar, Rocket } from "lucide-react";

interface Student {
  id?: string;
  name?: string;
  firstname?: string;
  lastname?: string;
}

interface Professor {
  id?: string;
  name?: string;
  firstname?: string;
  lastname?: string;
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
  const [binomesList, setBinomesList] = useState<Array<[string, string]>>([]);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  // Time overrides — empty string means "use server default from application.properties"
  const [morningStart, setMorningStart] = useState("");
  const [morningEnd, setMorningEnd] = useState("");
  const [afternoonStart, setAfternoonStart] = useState("");
  const [afternoonEnd, setAfternoonEnd] = useState("");
  const [loading, setLoading] = useState(false);
  const [rawAffectation, setRawAffectation] = useState<any>(null);
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
      formData.append("file", file1);

      const affectationRes = await axios.post(`${API_BASE}/api/affectations/process`, formData);

      if (!affectationRes.data || !affectationRes.data.assignments) {
        throw new Error("Le backend n'a retourné aucune affectation.");
      }

      setRawAffectation(affectationRes.data);

      // Group backend strictly flat 'Assignment' { student: {...}, professor: {...} } into frontend UI array structure
      const rawAssignments: any[] = affectationRes.data.assignments;
      const groupedMap = new Map<string, Assignment>();

      rawAssignments.forEach((item) => {
        const profId = item.professor?.id || item.professor?.name;
        if (!groupedMap.has(profId)) {
          groupedMap.set(profId, {
            professor: item.professor,
            students: []
          });
        }
        groupedMap.get(profId)!.students.push(item.student);
      });

      setAssignments(Array.from(groupedMap.values()));
      setCurrentStep(2);
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message;
      alert("Erreur: " + msg);
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Manage binomes
  const addEmptyBinome = () => {
    setBinomesList([...binomesList, ["", ""]]);
  };

  const removeBinome = (index: number) => {
    const newList = [...binomesList];
    newList.splice(index, 1);
    setBinomesList(newList);
  };

  const updateBinome = (index: number, pos: 0 | 1, value: string) => {
    const newList = [...binomesList];
    newList[index][pos] = value;
    setBinomesList(newList);
  };

  const getStudentName = (s: Student) => {
    return s.name || [s.firstname, s.lastname].filter(Boolean).join(" ") || "Sans nom";
  };

  const getProfFullName = (p: Professor) => {
    return p.name || [p.firstname, p.lastname].filter(Boolean).join(" ") || "Inconnu";
  };

  const getAllStudents = () => {
    return assignments.flatMap(a => a.students).sort((a, b) => getStudentName(a).localeCompare(getStudentName(b)));
  };

  const getAvailableStudents = (currentValue: string) => {
    const usedIds = new Set(binomesList.flat().filter(id => id !== ""));
    return getAllStudents().filter(s => s.id === currentValue || !usedIds.has(s.id || ""));
  };

  const getProfName = (studentId?: string) => {
    const a = assignments.find(assign => assign.students.some(s => s.id === studentId));
    return a ? getProfFullName(a.professor) : "";
  };

  // Move to step 3
  const goToDateConfiguration = () => {
    // Clean up empty binomes
    setBinomesList(binomesList.filter(b => b[0] !== "" && b[1] !== ""));
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
        binomes: binomesList.filter(b => b[0] !== "" && b[1] !== ""),
        // Only send when the user changed the value; empty string = use server default
        morningStart:   morningStart   || undefined,
        morningEnd:     morningEnd     || undefined,
        afternoonStart: afternoonStart || undefined,
        afternoonEnd:   afternoonEnd   || undefined,
      };
      const planningRes = await axios.post(`${API_BASE}/api/soutenances/generate`, planningPayload);

      navigate("/planing", {
        state: {
          affectation: rawAffectation,
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

        {/* Step 2: Manage Binomes */}
        {currentStep === 2 && (
          <div>
            <div className="mb-5 text-center">
              <h3 className="text-white fw-bold mb-2">👥 Gérer les Binômes (Optionnel)</h3>
              <p className="text-muted">Créez des binômes d'étudiants si nécessaire.</p>
            </div>

            <div className="card custom-card shadow-lg mb-4">
              <div className="card-body p-5">
                {binomesList.map((pair, index) => (
                  <div key={index} className="row g-3 align-items-center mb-3">
                    <div className="col-md-5">
                      <select 
                        className="form-select bg-dark text-light border-secondary"
                        value={pair[0]}
                        onChange={(e) => updateBinome(index, 0, e.target.value)}
                      >
                        <option value="">-- Étudiant 1 --</option>
                        {getAvailableStudents(pair[0]).map(s => (
                          <option key={s.id} value={s.id || ""}>{getStudentName(s)} ({getProfName(s.id)})</option>
                        ))}
                      </select>
                    </div>
                    <div className="col-md-1 text-center text-white fw-bold">
                      &
                    </div>
                    <div className="col-md-5">
                      <select 
                        className="form-select bg-dark text-light border-secondary"
                        value={pair[1]}
                        onChange={(e) => updateBinome(index, 1, e.target.value)}
                      >
                        <option value="">-- Étudiant 2 --</option>
                        {getAvailableStudents(pair[1]).map(s => (
                          <option key={s.id} value={s.id || ""}>{getStudentName(s)} ({getProfName(s.id)})</option>
                        ))}
                      </select>
                    </div>
                    <div className="col-md-1">
                      <button 
                        className="btn btn-outline-danger w-100"
                        onClick={() => removeBinome(index)}
                      >
                        ✕
                      </button>
                    </div>
                  </div>
                ))}

                {binomesList.length === 0 && (
                  <p className="text-center text-muted my-3">
                    Aucun binôme créé. Chaque étudiant passera sa soutenance individuellement.
                  </p>
                )}

                <div className="text-center mt-4">
                  <button 
                    className="btn btn-outline-success px-4 py-2 fw-600"
                    onClick={addEmptyBinome}
                  >
                    + Ajouter un binôme
                  </button>
                </div>
              </div>
            </div>

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
                      <label className="form-label fw-600 text-light d-flex align-items-center gap-2">
                        <Calendar size={18} /> Date Début
                      </label>
                      <input
                        type="date"
                        className="form-control"
                        min={tomorrow}
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light d-flex align-items-center gap-2">
                        <Calendar size={18} /> Date Fin
                      </label>
                      <input
                        type="date"
                        className="form-control"
                        min={startDate || tomorrow}
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                      />
                    </div>

                    {/* Morning window */}
                    <div className="col-12">
                      <p className="text-muted small mb-2 fw-600">🌅 Créneau Matin <span className="text-secondary fw-normal">(défaut: 09:00 – 12:00)</span></p>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">Début Matin</label>
                      <input
                        type="time"
                        className="form-control"
                        placeholder="09:00"
                        value={morningStart}
                        onChange={(e) => setMorningStart(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">Fin Matin</label>
                      <input
                        type="time"
                        className="form-control"
                        placeholder="12:00"
                        value={morningEnd}
                        onChange={(e) => setMorningEnd(e.target.value)}
                      />
                    </div>

                    {/* Afternoon window */}
                    <div className="col-12">
                      <p className="text-muted small mb-2 fw-600">🌇 Créneau Après-midi <span className="text-secondary fw-normal">(défaut: 14:00 – 18:00)</span></p>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">Début Après-midi</label>
                      <input
                        type="time"
                        className="form-control"
                        placeholder="14:00"
                        value={afternoonStart}
                        onChange={(e) => setAfternoonStart(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label fw-600 text-light">Fin Après-midi</label>
                      <input
                        type="time"
                        className="form-control"
                        placeholder="18:00"
                        value={afternoonEnd}
                        onChange={(e) => setAfternoonEnd(e.target.value)}
                      />
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
                        <p className="text-light fw-600">{binomesList.filter(b => b[0] !== "" && b[1] !== "").length} paires</p>
                      </div>
                      <div className="col-md-6">
                        <p className="text-muted small mb-1 d-flex align-items-center gap-1">
                          <Calendar size={14} /> Période
                        </p>
                        <p className="text-light fw-600">{startDate} → {endDate}</p>
                      </div>
                      <div className="col-md-6">
                        <p className="text-muted small mb-1">🌅 Matin</p>
                        <p className="text-light fw-600">{morningStart || "09:00"} – {morningEnd || "12:00"}</p>
                      </div>
                      <div className="col-md-6">
                        <p className="text-muted small mb-1">🌇 Après-midi</p>
                        <p className="text-light fw-600">{afternoonStart || "14:00"} – {afternoonEnd || "18:00"}</p>
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
                        <div className="d-inline-flex align-items-center justify-content-center gap-2">
                          <Rocket size={18} /> Générer le Planning
                        </div>
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
