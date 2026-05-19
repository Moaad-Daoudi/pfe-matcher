import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import { Link, useLocation } from "react-router-dom";
import Navbar from "../components/Navbar";
import { useEffect, useState } from "react";

const API_BASE = "http://localhost:8080/pfe-matcher";

type PdfFile = {
  name: string;
  url: string;
};

type PlanningResult = {
  pdfFileName?: string;
};

type ImportState = {
  affectation?: {
    assignments?: unknown[];
  };
  planning?: PlanningResult;
};

function Pages2() {
  const { state } = useLocation();
  const routeState = state as ImportState | null;
  const [pdfs, setPdfs] = useState<PdfFile[]>([]);
  const [pvsByProf, setPvsByProf] = useState<Record<string, string[]>>({});
  const [loading, setLoading] = useState(true);
  const [expandedProf, setExpandedProf] = useState<string | null>(null);
  const [showSearch, setShowSearch] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const assignments = routeState?.affectation?.assignments;

  async function fetchData() {
    try {
      const [pdfsRes, pvsRes] = await Promise.all([
        fetch(`${API_BASE}/api/soutenances/list-pdfs`),
        fetch(`${API_BASE}/api/soutenances/list-pvs`),
      ]);

      let fileNames: string[] = [];
      if (pdfsRes.ok) {
        fileNames = await pdfsRes.json();
      }

      if (fileNames.length === 0) {
        fileNames = await getGeneratedPdfFallbacks();
      }

      setPdfs(fileNames.map((fileName) => ({
        name: fileName,
        url: getPdfUrl(fileName),
      })));

      if (pvsRes.ok) {
        setPvsByProf(await pvsRes.json());
      }
    } catch (error) {
      console.error("Error fetching data:", error);
    } finally {
      setLoading(false);
    }
  }

  function viewPdf(url: string) {
    window.open(url, "_blank");
  }

  async function getGeneratedPdfFallbacks() {
    const fileNames = new Set<string>();

    if (routeState?.affectation) {
      fileNames.add("affectation_final.pdf");
    }
    if (routeState?.planning?.pdfFileName) {
      fileNames.add(routeState.planning.pdfFileName);
    }

    try {
      const [affectationRes, planningRes] = await Promise.all([
        fetch(`${API_BASE}/api/affectations/current`),
        fetch(`${API_BASE}/api/soutenances/current`),
      ]);

      if (affectationRes.ok) {
        fileNames.add("affectation_final.pdf");
      }
      if (planningRes.ok) {
        const planning: PlanningResult = await planningRes.json();
        if (planning.pdfFileName) {
          fileNames.add(planning.pdfFileName);
        }
      }
    } catch (error) {
      console.error("Error fetching generated PDF fallbacks:", error);
    }

    await Promise.all([
      addPdfIfAvailable(fileNames, "affectation_final.pdf"),
      addPdfIfAvailable(fileNames, "planning_soutenances.pdf"),
    ]);

    return Array.from(fileNames);
  }

  async function addPdfIfAvailable(fileNames: Set<string>, fileName: string) {
    try {
      const response = await fetch(getPdfUrl(fileName), { method: "HEAD" });
      if (response.ok) {
        fileNames.add(fileName);
      }
    } catch (error) {
      console.error(`Error checking PDF ${fileName}:`, error);
    }
  }

  function getPdfUrl(fileName: string) {
    const encodedName = encodeURIComponent(fileName);
    const endpoint = fileName === "affectation_final.pdf" ? "affectations" : "soutenances";
    return `${API_BASE}/api/${endpoint}/view/${encodedName}`;
  }

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    console.log("Structure des données reçues :", assignments);
  }, [assignments]);

  function downloadPv(profName: string, fileName: string) {
    const link = document.createElement("a");
    link.href = `${API_BASE}/api/soutenances/download-pv/${encodeURIComponent(profName)}/${encodeURIComponent(fileName)}`;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (loading) {
    return (
      <>
        <Navbar activePage="planing" dashboardState={routeState} />
        <div className="container my-5 text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      {/* NAVBAR */}
      <Navbar activePage="planing" dashboardState={routeState} />

      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">Gestion des Projets de Fin d'Etudes</h2>
          <p className="text-white-50 mb-0">Verification des donnees importees.</p>
        </div>
      </div>

      <div className="container my-5">

        {/* PDFS SECTION */}
        <div className="mt-5">
          <h5 className="section-title mb-3">
            Fichiers PDF Generes ({pdfs.length})
          </h5>

          {pdfs.length > 0 ? (
            <div className="row">
              {pdfs.map((pdf) => (
                <div key={pdf.name} className="col-md-6 mb-3">
                  <div className="card">
                    <div className="card-body">
                      <h6 className="card-title">{pdf.name}</h6>
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={() => viewPdf(pdf.url)}
                      >
                        <i className="bi bi-eye"></i> Voir le PDF
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="alert alert-info">Aucun PDF trouve</div>
          )}
        </div>

        {/* PVs SECTION */}
        <div className="mt-5">
          <div className="d-flex align-items-center justify-content-between mb-3 pb-2" style={{ borderBottom: "1px solid rgba(255,255,255,0.1)" }}>
            <h5 className="section-title mb-0" style={{ borderLeft: "4px solid #54a659", paddingLeft: "10px" }}>
              Fichiers des Professeurs
            </h5>
            <div className="d-flex align-items-center gap-2">
              {showSearch && (
                <input
                  type="text"
                  className="form-control form-control-sm"
                  placeholder="Rechercher un encadrant..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  style={{ width: "220px", backgroundColor: "#1e2a38", borderColor: "#54a659", color: "#e0e0e0" }}
                  autoFocus
                />
              )}
              <button
                className="btn btn-sm d-flex align-items-center gap-1 text-white"
                onClick={() => {
                  setShowSearch(!showSearch);
                  if (showSearch) setSearchQuery("");
                }}
                style={{ backgroundColor: "#54a659", borderColor: "#6fcf7f", fontWeight: "600" }}
              >
                <span>{showSearch ? "❌" : "🔍"}</span>
                <span>{showSearch ? "Fermer" : "Rechercher"}</span>
              </button>
            </div>
          </div>

          {Object.keys(pvsByProf).length > 0 ? (
            <div className="accordion" id="pvsAccordion">
              {Object.entries(pvsByProf)
                .filter(([profName]) => profName.toLowerCase().includes(searchQuery.toLowerCase()))
                .map(([profName, pvFiles], idx) => (
                <div key={profName} className="accordion-item">
                  <h2 className="accordion-header">
                    <button
                      className={`accordion-button ${expandedProf !== profName ? 'collapsed' : ''}`}
                      type="button"
                      data-bs-toggle="collapse"
                      data-bs-target={`#accordion-${idx}`}
                      aria-expanded={expandedProf === profName}
                      onClick={() => setExpandedProf(expandedProf === profName ? null : profName)}
                    >
                      <strong>{profName}</strong>
                      {pvFiles.length > 0 && (
                        <span className="badge bg-primary ms-2">{pvFiles.length} Fichier(s)</span>
                      )}
                    </button>
                  </h2>
                  <div
                    id={`accordion-${idx}`}
                    className={`accordion-collapse collapse ${expandedProf === profName ? 'show' : ''}`}
                    data-bs-parent="#pvsAccordion"
                  >
                    <div className="accordion-body">
                      {pvFiles.length > 0 ? (
                        <ul className="list-group">
                          {pvFiles.map((pvFile) => (
                            <li key={pvFile} className="list-group-item d-flex justify-content-between align-items-center">
                              <span>{pvFile}</span>
                              <button
                                className="btn btn-sm btn-success"
                                onClick={() => downloadPv(profName, pvFile)}
                              >
                                <i className="bi bi-download"></i> Telecharger
                              </button>
                            </li>
                          ))}
                        </ul>
                      ) : (
                        <p className="text-muted">Aucun fichier pour ce professeur</p>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="alert alert-info">Aucun fichier PV trouve</div>
          )}
        </div>

        {/* BACK BUTTON */}
        <div className="mt-5">
          <Link className="btn custom-btn px-4" to="/">Retour</Link>
        </div>
      </div>
    </>
  );
}

export default Pages2;
