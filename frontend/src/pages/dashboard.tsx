import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import Navbar from "../components/Navbar";
import { Link, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";

const API_BASE = "http://localhost:8080/pfe-matcher";

type CountMap = Record<string, number>;

type Assignment = {
  professor?: {
    lastname?: string;
  };
  student?: {
    field?: string;
  };
};

type DashboardState = {
  affectation?: {
    assignments?: Assignment[];
    stats?: {
      studentsPerProf?: CountMap;
      fieldStats?: CountMap;
    };
  };
  planning?: {
    soutenances?: unknown[];
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

function PieChart({ data, label = "Étudiants" }: { data: Record<string, number>, label?: string }) {
  const total = Object.values(data).reduce((sum, val) => sum + val, 0);
  if (total === 0) return <div className="text-white-50 text-center py-4">Aucune donnée</div>;

  const colors = [
    "#00b4d8", // Blue
    "#ff9f1c", // Orange
    "#2ec4b6", // Teal
    "#e71d36", // Coral
    "#9b5de5", // Purple
    "#54a659", // Green
    "#f15bb5", // Pink
    "#fee440"  // Yellow
  ];

  let cumulativePercent = 0;
  const gradientParts = Object.entries(data).map(([, count], index) => {
    const percent = (count / total) * 100;
    const start = cumulativePercent;
    const end = cumulativePercent + percent;
    cumulativePercent = end;
    const color = colors[index % colors.length];
    return `${color} ${start.toFixed(1)}% ${end.toFixed(1)}%`;
  });

  const gradientString = `conic-gradient(${gradientParts.join(", ")})`;

  return (
    <div className="d-flex flex-column align-items-center justify-content-center p-2">
      <div
        style={{
          width: "160px",
          height: "160px",
          borderRadius: "50%",
          background: gradientString,
          position: "relative",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          boxShadow: "0 8px 32px 0 rgba(0, 0, 0, 0.3)",
          transition: "transform 0.3s ease"
        }}
      >
        <div
          style={{
            width: "110px",
            height: "110px",
            borderRadius: "50%",
            backgroundColor: "#16222f",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            boxShadow: "inset 0 0 10px rgba(0,0,0,0.5)"
          }}
        >
          <span className="text-white fw-bold" style={{ fontSize: "1.6rem" }}>{total}</span>
          <span className="text-white-50" style={{ fontSize: "0.7rem", textTransform: "uppercase", letterSpacing: "1px" }}>{label}</span>
        </div>
      </div>

      <div className="w-100 mt-4 d-flex flex-wrap justify-content-center gap-3">
        {Object.entries(data).map(([name, count], index) => {
          const percent = ((count / total) * 100).toFixed(0);
          const color = colors[index % colors.length];
          return (
            <div key={name} className="d-flex align-items-center gap-2" style={{ minWidth: "110px" }}>
              <div style={{ width: "12px", height: "12px", borderRadius: "3px", backgroundColor: color }} />
              <div className="d-flex flex-column">
                <span className="text-white-50" style={{ fontSize: "0.8rem", fontWeight: "500", lineHeight: "1.2" }}>{name}</span>
                <span className="text-white fw-bold" style={{ fontSize: "0.85rem" }}>{count} ({percent}%)</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function Dashboard() {
  const { state } = useLocation();
  const routeState = state as DashboardState | null;
  const [dashboardData, setDashboardData] = useState<DashboardState | null>(routeState);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      if (routeState?.affectation && routeState?.planning) {
        setDashboardData(routeState);
        setLoading(false);
        return;
      }

      try {
        const [affectationRes, planningRes] = await Promise.all([
          fetch(`${API_BASE}/api/affectations/current`),
          fetch(`${API_BASE}/api/soutenances/current`),
        ]);

        if (!affectationRes.ok || !planningRes.ok) {
          setDashboardData(null);
          return;
        }

        const [affectation, planning] = await Promise.all([
          affectationRes.json(),
          planningRes.json(),
        ]);

        setDashboardData({ affectation, planning });
      } catch (error) {
        console.error("Error fetching dashboard data:", error);
        setDashboardData(null);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [routeState]);

  if (loading) {
    return (
      <>
        <Navbar activePage="dashboard" />
        <div className="container my-5 text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </>
    );
  }

  const affectation = dashboardData?.affectation ?? null;
  const planning = dashboardData?.planning ?? null;

  if (!affectation || !planning) {
    return (
      <>
        <Navbar activePage="dashboard" />
        <div className="pg text-center text-white py-5">
          <h3 className="mt-5">Aucune donnee disponible.</h3>
          <p>Veuillez revenir a la page d'accueil pour importer les fichiers.</p>
          <Link to="/" className="btn btn-primary mt-3">Retour a l'import</Link>
        </div>
      </>
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

  // Count how many defenses a professor is in, either as encadrant, jury1, or jury2
  const defensesPerProf = soutenances.reduce<CountMap>((acc, soutenance: any) => {
    const roles = [
      soutenance.encadrant?.lastname,
      soutenance.jury1?.lastname,
      soutenance.jury2?.lastname
    ];
    const uniqueProfs = Array.from(new Set(roles.filter(Boolean)));
    uniqueProfs.forEach((name) => {
      acc[name] = (acc[name] ?? 0) + 1;
    });
    return acc;
  }, {});

  const maxDefensesPerProf = Math.max(1, ...Object.values(defensesPerProf));

  const totalStudents = assignments.length;
  const totalSpecialties = Object.keys(fieldStats).length;
  const totalProfessors = Object.keys(studentsPerProf).length;
  const maxStudentsPerProf = Math.max(1, ...Object.values(studentsPerProf));
  const maxRoomCount = Math.max(1, ...Object.values(soutenancesPerRoom));

  return (
    <>
      <Navbar activePage="dashboard" />

      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">Tableau de Bord</h2>
          <p className="text-white-50 mb-0">Statistiques</p>
        </div>
      </div>

      <div className="container my-5">
        <div className="row g-4 mb-5">
          <div className="col-md-4">
            <div className="card custom-card h-100 border-success">
              <div className="card-body text-center">
                <h5 className="card-title text-success mb-2">Total Etudiants</h5>
                <h2 className="text-white fw-bold">{totalStudents}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card custom-card h-100 border-info">
              <div className="card-body text-center">
                <h5 className="card-title text-info mb-2">Total Professeurs</h5>
                <h2 className="text-white fw-bold">{totalProfessors}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card custom-card h-100 border-warning">
              <div className="card-body text-center">
                <h5 className="card-title text-warning mb-2">Filieres</h5>
                <h2 className="text-white fw-bold">{totalSpecialties}</h2>
              </div>
            </div>
          </div>
        </div>

        <div className="row g-4 mb-5 align-items-start">
          <div className="col-lg-6">
            <div className="card custom-card mb-4">
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

            {/* Soutenances par Salle Card */}
            <div className="card custom-card mb-4">
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

          <div className="col-lg-6">
            {/* Soutenances par Professeur Card */}
            <div className="card custom-card mb-4">
              <div className="card-header bg-success bg-opacity-10 border-bottom border-success">
                <h5 className="card-title text-success mb-0">Soutenances par Professeur</h5>
              </div>
              <div className="card-body">
                {Object.entries(defensesPerProf).map(([name, count]) => (
                  <div key={name} className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <span className="text-white fw-500">{name}</span>
                      <span className="badge bg-success">{count} {count > 1 ? "soutenances" : "soutenance"}</span>
                    </div>
                    <div className="progress" style={{ height: "8px" }}>
                      <div className="progress-bar bg-success" style={{ width: `${(count / maxDefensesPerProf) * 100}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card custom-card mb-4">
              <div className="card-header bg-info bg-opacity-10 border-bottom border-info">
                <h5 className="card-title text-info mb-0">Soutenances par Date</h5>
              </div>
              <div className="card-body">
                <table className="table table-hover table-borderless text-white m-0" style={{ background: "transparent", ["--bs-table-bg" as any]: "transparent" }}>
                  <thead>
                    <tr className="border-bottom border-secondary" style={{ fontSize: "0.95rem" }}>
                      <th className="text-info fw-bold" style={{ textShadow: "0 1px 2px rgba(0,0,0,0.5)" }}>Date</th>
                      <th className="text-info text-center fw-bold" style={{ textShadow: "0 1px 2px rgba(0,0,0,0.5)" }}>Soutenances</th>
                    </tr>
                  </thead>
                  <tbody>
                    {Object.entries(soutenancesPerDate).map(([date, count]) => (
                      <tr key={date} className="border-bottom border-secondary-subtle" style={{ verticalAlign: "middle" }}>
                        <td className="text-white fw-bold" style={{ textShadow: "0 1px 2px rgba(0,0,0,0.8)", fontSize: "0.95rem" }}>{date}</td>
                        <td className="text-center text-white fw-bold" style={{ textShadow: "0 1px 2px rgba(0,0,0,0.8)", fontSize: "0.95rem" }}>
                          {count}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card custom-card">
              <div className="card-header bg-warning bg-opacity-10 border-bottom border-warning">
                <h5 className="card-title text-warning mb-0">Etudiants par Filiere</h5>
              </div>
              <div className="card-body">
                <PieChart data={fieldStats} label="Étudiants" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default Dashboard;
