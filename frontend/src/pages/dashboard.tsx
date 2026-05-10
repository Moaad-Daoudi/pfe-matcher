import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import Navbar from "../components/Navbar";
import { useLocation, Link } from "react-router-dom";

type CountMap = Record<string, number>;

type DashboardState = {
  affectation?: {
    assignments?: any[];
    stats?: {
      studentsPerProf?: CountMap;
      fieldStats?: CountMap;
    };
  };
  planning?: {
    soutenances?: any[];
    stats?: {
      totalSoutenances?: number;
      totalViolations?: number;
      soutenancesPerDate?: CountMap;
      soutenancesPerRoom?: CountMap;
    };
  };
};

function toNumberMap(value: unknown): CountMap {
  if (!value || typeof value !== "object") {
    return {};
  }

  return Object.entries(value as Record<string, unknown>).reduce<CountMap>((acc, [key, raw]) => {
    const count = Number(raw);
    acc[key] = Number.isFinite(count) ? count : 0;
    return acc;
  }, {});
}

function Dashboard() {
  const { state } = useLocation();
  
  // Try state first, then fallback to localStorage
  const affectation = state?.affectation || JSON.parse(localStorage.getItem("affectationData") || "null");
  const planning = state?.planning || JSON.parse(localStorage.getItem("planningData") || "null");

  if (!affectation || !planning) {
    return (
      <div className="pg text-center text-white py-5">
        <Navbar activePage="dashboard" />
        <h3 className="mt-5">Aucune donnee disponible.</h3>
        <p>Veuillez revenir a la page d'accueil pour importer les fichiers.</p>
        <Link to="/" className="btn btn-primary mt-3">Retour a l'import</Link>
      </div>
    );
  }

  const assignments = affectation.assignments ?? [];
  const soutenances = planning.soutenances ?? [];

  const studentsPerProf = Object.keys(affectation.stats?.studentsPerProf ?? {}).length > 0
    ? toNumberMap(affectation.stats?.studentsPerProf)
    : assignments.reduce<CountMap>((acc, assignment) => {
        const name = assignment.professor?.lastname ?? "N/A";
        acc[name] = (acc[name] ?? 0) + 1;
        return acc;
      }, {});

  const fieldStats = Object.keys(affectation.stats?.fieldStats ?? {}).length > 0
    ? toNumberMap(affectation.stats?.fieldStats)
    : assignments.reduce<CountMap>((acc, assignment) => {
        const field = assignment.student?.field ?? "N/A";
        acc[field] = (acc[field] ?? 0) + 1;
        return acc;
      }, {});

  const soutenancesPerRoom = toNumberMap(planning.stats?.soutenancesPerRoom);
  const soutenancesPerDate = toNumberMap(planning.stats?.soutenancesPerDate);
  const totalStudents = assignments.length;
  const totalDefenses = Number(planning.stats?.totalSoutenances ?? soutenances.length);
  const totalViolations = Number(planning.stats?.totalViolations ?? 0);
  const totalSpecialties = Object.keys(fieldStats).length;
  const totalProfessors = Object.keys(studentsPerProf).length;
  const maxStudentsPerProf = Math.max(1, ...Object.values(studentsPerProf));
  const maxFieldCount = Math.max(1, ...Object.values(fieldStats));
  const maxRoomCount = Math.max(1, ...Object.values(soutenancesPerRoom));

  return (
    <>
      <Navbar activePage="dashboard" />

      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">Tableau de Bord</h2>
          <p className="text-white-50 mb-0">Statistiques calculees par le backend</p>
        </div>
      </div>

      <div className="container my-5">
        <div className="row g-4 mb-5">
          <div className="col-md-3">
            <div className="card custom-card h-100 border-success">
              <div className="card-body text-center">
                <h5 className="card-title text-success mb-2">Total Etudiants</h5>
                <h2 className="text-white fw-bold">{totalStudents}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card custom-card h-100 border-info">
              <div className="card-body text-center">
                <h5 className="card-title text-info mb-2">Total Soutenances</h5>
                <h2 className="text-white fw-bold">{totalDefenses}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card custom-card h-100 border-warning">
              <div className="card-body text-center">
                <h5 className="card-title text-warning mb-2">Filieres</h5>
                <h2 className="text-white fw-bold">{totalSpecialties}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card custom-card h-100 border-danger">
              <div className="card-body text-center">
                <h5 className="card-title text-danger mb-2">Violations</h5>
                <h2 className="text-white fw-bold">{totalViolations}</h2>
              </div>
            </div>
          </div>
        </div>

        <div className="row g-4 mb-5">
          <div className="col-lg-6">
            <div className="card custom-card">
              <div className="card-header bg-primary bg-opacity-10 border-bottom border-primary">
                <h5 className="card-title text-primary mb-0">Etudiants par Professeur ({totalProfessors})</h5>
              </div>
              <div className="card-body">
                {Object.entries(studentsPerProf).map(([name, count]) => (
                  <div key={name} className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <span className="text-white fw-500">{name}</span>
                      <span className="badge bg-primary">{count}</span>
                    </div>
                    <div className="progress" style={{ height: "8px" }}>
                      <div className="progress-bar bg-primary" style={{ width: `${(count / maxStudentsPerProf) * 100}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="col-lg-6">
            <div className="card custom-card">
              <div className="card-header bg-success bg-opacity-10 border-bottom border-success">
                <h5 className="card-title text-success mb-0">Soutenances par Salle</h5>
              </div>
              <div className="card-body">
                {Object.entries(soutenancesPerRoom).map(([room, count]) => (
                  <div key={room} className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <span className="text-white fw-500">{room}</span>
                      <span className="badge bg-success">{count}</span>
                    </div>
                    <div className="progress" style={{ height: "8px" }}>
                      <div className="progress-bar bg-success" style={{ width: `${(count / maxRoomCount) * 100}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="row g-4 mb-5">
          <div className="col-lg-6">
            <div className="card custom-card">
              <div className="card-header bg-warning bg-opacity-10 border-bottom border-warning">
                <h5 className="card-title text-warning mb-0">Etudiants par Filiere</h5>
              </div>
              <div className="card-body">
                {Object.entries(fieldStats).map(([field, count]) => (
                  <div key={field} className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <span className="text-white fw-500">{field}</span>
                      <span className="badge bg-warning text-dark">{count}</span>
                    </div>
                    <div className="progress" style={{ height: "8px" }}>
                      <div className="progress-bar bg-warning" style={{ width: `${(count / maxFieldCount) * 100}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="col-lg-6">
            <div className="card custom-card">
              <div className="card-header bg-info bg-opacity-10 border-bottom border-info">
                <h5 className="card-title text-info mb-0">Soutenances par Date</h5>
              </div>
              <div className="card-body">
                <table className="table table-hover text-white">
                  <thead>
                    <tr className="border-bottom border-secondary">
                      <th className="text-info">Date</th>
                      <th className="text-info text-center">Soutenances</th>
                    </tr>
                  </thead>
                  <tbody>
                    {Object.entries(soutenancesPerDate).map(([date, count]) => (
                      <tr key={date} className="border-bottom border-secondary-subtle">
                        <td>{date}</td>
                        <td className="text-center">{count}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default Dashboard;
