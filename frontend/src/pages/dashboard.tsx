import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import Navbar from "../components/Navbar";
import { useLocation, Link } from "react-router-dom";

function Dashboard() {
  const { state } = useLocation();
  const data = state || { affectation: null, planning: null };
  const { affectation, planning } = data;

  // Handle case where user refreshes the page and state is lost
  if (!affectation || !planning) {
    return (
      <div className="pg text-center text-white py-5">
        <Navbar activePage="dashboard" />
        <h3 className="mt-5">Aucune donnée disponible.</h3>
        <p>Veuillez revenir à la page d'accueil pour importer les fichiers.</p>
        <Link to="/" className="btn btn-primary mt-3">Retour à l'import</Link>
      </div>
    );
  }

  // --- LOGIC TO TRANSFORM DATA ---
  
  // 1. Get list of all unique professors from the assignment list
  const uniqueProfNames = Array.from(new Set(affectation.assignments.map((a: any) => a.professor.lastname)));

  // 2. Map backend data to the Professor interface expected by the UI
  const professorsData = uniqueProfNames.map((name, index) => ({
    id: index,
    name: name,
    // Count how many students are assigned to this professor
    students: affectation.assignments.filter((a: any) => a.professor.lastname === name).length,
    // Count how many defenses this prof is 'encadrant' for
    defenses: planning.soutenances.filter((s: any) => s.encadrant.lastname === name).length
  }));

  // 3. Map backend data to the Specialty interface expected by the UI
  const uniqueFields = Array.from(new Set(affectation.assignments.map((a: any) => a.student.field)));
  const specialtiesData = uniqueFields.map((field, index) => ({
    id: index,
    name: field as string,
    defenses: planning.soutenances.filter((s: any) => s.assignment.student.field === field).length
  }));

  // Calculations for UI totals
  const totalStudents = affectation.assignments.length;
  const totalDefenses = planning.soutenances.length;
  const totalSpecialties = specialtiesData.length;

  return (
    <>
      <Navbar activePage="dashboard" />
      
      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">📊 Tableau de Bord</h2>
          <p className="text-white-50 mb-0">Statistiques générées automatiquement</p>
        </div>
      </div>

      <div className="container my-5">
        {/* STATISTIQUES PRINCIPALES */}
        <div className="row g-4 mb-5">
          <div className="col-md-3">
            <div className="card custom-card h-100 border-success">
              <div className="card-body text-center">
                <h5 className="card-title text-success mb-2">👥 Total Étudiants</h5>
                <h2 className="text-white fw-bold">{totalStudents}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card custom-card h-100 border-info">
              <div className="card-body text-center">
                <h5 className="card-title text-info mb-2">🎓 Total Soutenances</h5>
                <h2 className="text-white fw-bold">{totalDefenses}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card custom-card h-100 border-warning">
              <div className="card-body text-center">
                <h5 className="card-title text-warning mb-2">🏫 Filières</h5>
                <h2 className="text-white fw-bold">{totalSpecialties}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card custom-card h-100 border-danger">
              <div className="card-body text-center">
                <h5 className="card-title text-danger mb-2">👨‍🏫 Professeurs</h5>
                <h2 className="text-white fw-bold">{professorsData.length}</h2>
              </div>
            </div>
          </div>
        </div>

        {/* ÉTUDIANTS / SOUTENANCES PAR PROF */}
        <div className="row g-4 mb-5">
          <div className="col-lg-6">
            <div className="card custom-card">
              <div className="card-header bg-primary bg-opacity-10 border-bottom border-primary">
                <h5 className="card-title text-primary mb-0">📚 Étudiants par Professeur</h5>
              </div>
              <div className="card-body">
                {professorsData.map((prof) => (
                  <div key={prof.id} className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <span className="text-white fw-500">{prof.name}</span>
                      <span className="badge bg-primary">{prof.students}</span>
                    </div>
                    <div className="progress" style={{ height: "8px" }}>
                      <div className="progress-bar bg-primary" style={{ width: `${(prof.students / (totalStudents/professorsData.length + 5)) * 100}%` }}></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="col-lg-6">
            <div className="card custom-card">
              <div className="card-header bg-success bg-opacity-10 border-bottom border-success">
                <h5 className="card-title text-success mb-0">🎓 Soutenances par Professeur</h5>
              </div>
              <div className="card-body">
                {professorsData.map((prof) => (
                  <div key={prof.id} className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <span className="text-white fw-500">{prof.name}</span>
                      <span className="badge bg-success">{prof.defenses}</span>
                    </div>
                    <div className="progress" style={{ height: "8px" }}>
                      <div className="progress-bar bg-success" style={{ width: `${(prof.defenses / (totalDefenses/professorsData.length + 2)) * 100}%` }}></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* SOUTENANCES PAR FILIÈRE */}
        <div className="row mb-5">
          <div className="col-lg-12">
            <div className="card custom-card">
              <div className="card-header bg-warning bg-opacity-10 border-bottom border-warning">
                <h5 className="card-title text-warning mb-0">🏫 Soutenances par Filière</h5>
              </div>
              <div className="card-body">
                <table className="table table-hover text-white">
                  <thead>
                    <tr className="border-bottom border-secondary">
                      <th className="text-warning">Filière</th>
                      <th className="text-warning text-center">Soutenances</th>
                      <th className="text-warning text-center">Pourcentage</th>
                    </tr>
                  </thead>
                  <tbody>
                    {specialtiesData.map((s) => (
                      <tr key={s.id} className="border-bottom border-secondary-subtle">
                        <td>{s.name}</td>
                        <td className="text-center">{s.defenses}</td>
                        <td className="text-center">{((s.defenses / totalDefenses) * 100).toFixed(1)}%</td>
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