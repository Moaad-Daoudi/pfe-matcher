import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import { useLocation, Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import { useEffect, useState } from "react";

const API_BASE = "http://localhost:8080/pfe-matcher";

type ImportState = {
  affectation?: {
    assignments?: Assignment[];
  };
  planning?: {
    pdfFileName?: string;
  };
};

type Student = {
  id: string;
  firstname: string;
  lastname: string;
  // email: string;
};

type Professor = {
  id: string;
  firstname: string;
  lastname: string;
  // email: string;
};

type Assignment = {
  id: string;
  student: Student;
  professor: Professor;
};

type PdfFile = {
  name: string;
  url: string;
};

function Pages2() {
  const { state } = useLocation();
  
  // Try state first, then fallback to localStorage
  const affectation = state?.affectation || JSON.parse(localStorage.getItem("affectationData") || "null");
  const planning = state?.planning || JSON.parse(localStorage.getItem("planningData") || "null");
  
  const [assignments] = useState<Assignment[]>(affectation?.assignments ?? []);
  const [pdfs] = useState<PdfFile[]>([
    {
      name: "affectation_final.pdf",
      url: `${API_BASE}/api/affectations/view/affectation_final.pdf`,
    },
    ...(planning?.pdfFileName
      ? [{
          name: planning.pdfFileName,
          url: `${API_BASE}/api/soutenances/view/${encodeURIComponent(planning.pdfFileName)}`,
        }]
      : []),
  ]);
  const [pvsByProf, setPvsByProf] = useState<Record<string, string[]>>({});
  const [loading, setLoading] = useState(true);
  const [expandedProf, setExpandedProf] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    console.log("Structure des données reçues :", assignments);
  }, [assignments]);

  const fetchData = async () => {
    try {
      const pvsRes = await fetch(`${API_BASE}/api/soutenances/list-pvs`);
      if (pvsRes.ok) {
        setPvsByProf(await pvsRes.json());
      }
    } catch (error) {
      console.error("Error fetching data:", error);
    } finally {
      setLoading(false);
    }
  };

  const viewPdf = (url: string) => {
    window.open(url, "_blank");
  };

  const downloadPv = (profName: string, fileName: string) => {
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
        <Navbar activePage="home" />
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
      <Navbar activePage="home" />

      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">Gestion des Projets de Fin d'Etudes</h2>
          <p className="text-white-50 mb-0">Verification des donnees importees.</p>
        </div>
      </div>

      <div className="container my-5">
        
        {/* AFFECTATIONS SECTION */}
        <div className="mt-5">
          <h5 className="section-title mb-3">
            Liste des Affectations ({assignments.length})
          </h5>

          {assignments.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead className="table-dark">
                  <tr>
                    <th>#</th>
                    <th>Etudiant</th>
                    {/* <th>Email Etudiant</th> */}
                    <th>Professeur</th>
                    {/* <th>Email Professeur</th> */}
                  </tr>
                </thead>
                <tbody>
                  {assignments.map((assign, idx) => (
                    <tr key={assign.id}>
                      <td>{idx + 1}</td>
                      <td>{assign.student? `${assign.student.firstname} ${assign.student.lastname}` : "N/A"}</td>
                      {/* <td>{assign.student?.email || "N/A"}</td> */}
                      <td>{assign.professor? `${assign.professor.firstname} ${assign.student.lastname}` : "N/A"}</td>
                      {/* <td>{assign.professor?.email || "N/A"}</td> */}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="alert alert-info">Aucune affectation trouvée</div>
          )}
        </div>

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
          <h5 className="section-title mb-3">
            Fichiers des Professeurs
          </h5>

          {Object.keys(pvsByProf).length > 0 ? (
            <div className="accordion" id="pvsAccordion">
              {Object.entries(pvsByProf).map(([ profName, pvFiles ], idx) => (
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
