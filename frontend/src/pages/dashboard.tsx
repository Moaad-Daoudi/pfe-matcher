import "bootstrap/dist/css/bootstrap.min.css";
import "../App.css";
import Navbar from "../components/Navbar";
import { useState } from "react";

interface Professor {
  id: number;
  name: string;
  students: number;
  defenses: number;
}

interface Specialty {
  id: number;
  name: string;
  defenses: number;
}

function Dashboard() {
  // Données d'exemple
  const [professorsData] = useState<Professor[]>([
    { id: 1, name: "Dr. Cherradi", students: 12, defenses: 8 },
    { id: 2, name: "Pr. Hassan", students: 15, defenses: 10 },
    { id: 3, name: "Dr. Fatima", students: 10, defenses: 7 },
    { id: 4, name: "Pr. Ahmed", students: 18, defenses: 12 },
    { id: 5, name: "Dr. Samir", students: 14, defenses: 9 },
  ]);

  const [specialtiesData] = useState<Specialty[]>([
    { id: 1, name: "Ingénierie Informatique", defenses: 15 },
    { id: 2, name: "Génie Civil", defenses: 12 },
    { id: 3, name: "Électronique", defenses: 10 },
    { id: 4, name: "Télécommunications", defenses: 8 },
  ]);

  const totalStudents = professorsData.reduce((sum, prof) => sum + prof.students, 0);
  const totalDefenses = professorsData.reduce((sum, prof) => sum + prof.defenses, 0);
  const totalSpecialties = specialtiesData.length;

  return (
    <>
      {/* NAVBAR */}
      <Navbar activePage="dashboard" />
      
      <div className="custom-hero">
        <div className="container py-4">
          <h2 className="text-white fw-bold mb-1">📊 Tableau de Bord</h2>
          <p className="text-white-50 mb-0">Visualisez les statistiques clés de l'application</p>
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
                <p className="text-white-50 mb-0 small">Tous les étudiants</p>
              </div>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card custom-card h-100 border-info">
              <div className="card-body text-center">
                <h5 className="card-title text-info mb-2">🎓 Total Soutenances</h5>
                <h2 className="text-white fw-bold">{totalDefenses}</h2>
                <p className="text-white-50 mb-0 small">Défenses planifiées</p>
              </div>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card custom-card h-100 border-warning">
              <div className="card-body text-center">
                <h5 className="card-title text-warning mb-2">🏫 Filières</h5>
                <h2 className="text-white fw-bold">{totalSpecialties}</h2>
                <p className="text-white-50 mb-0 small">Nombre de spécialités</p>
              </div>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card custom-card h-100 border-danger">
              <div className="card-body text-center">
                <h5 className="card-title text-danger mb-2">👨‍🏫 Professeurs</h5>
                <h2 className="text-white fw-bold">{professorsData.length}</h2>
                <p className="text-white-50 mb-0 small">Total encadrants</p>
              </div>
            </div>
          </div>
        </div>

        {/* ÉTUDIANTS PAR PROFESSEUR */}
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
                      <div
                        className="progress-bar bg-primary"
                        style={{ width: `${(prof.students / 20) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* SOUTENANCES PAR PROFESSEUR */}
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
                      <div
                        className="progress-bar bg-success"
                        style={{ width: `${(prof.defenses / 15) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* SOUTENANCES PAR FILIÈRE */}
        <div className="row mb-5">
          <div className="col-lg-8">
            <div className="card custom-card">
              <div className="card-header bg-warning bg-opacity-10 border-bottom border-warning">
                <h5 className="card-title text-warning mb-0">🏫 Soutenances par Filière</h5>
              </div>
              <div className="card-body">
                <div className="table-responsive">
                  <table className="table table-hover">
                    <thead>
                      <tr className="border-bottom border-secondary">
                        <th className="text-warning">Filière</th>
                        <th className="text-warning text-center">Soutenances</th>
                        <th className="text-warning text-center">Pourcentage</th>
                      </tr>
                    </thead>
                    <tbody>
                      {specialtiesData.map((specialty) => (
                        <tr key={specialty.id} className="border-bottom border-secondary-subtle">
                          <td className="text-white">{specialty.name}</td>
                          <td className="text-center">
                            <span className="badge bg-warning text-dark">{specialty.defenses}</span>
                          </td>
                          <td className="text-center">
                            <span className="text-info">
                              {((specialty.defenses / totalDefenses) * 100).toFixed(1)}%
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          {/* STATISTIQUES RAPIDES */}
          <div className="col-lg-4">
            <div className="card custom-card">
              <div className="card-header bg-info bg-opacity-10 border-bottom border-info">
                <h5 className="card-title text-info mb-0">ℹ️ Statistiques Rapides</h5>
              </div>
              <div className="card-body">
                <div className="mb-3">
                  <p className="text-white-50 mb-2">Moyenne étudiants/professeur</p>
                  <h4 className="text-info fw-bold">{(totalStudents / professorsData.length).toFixed(1)}</h4>
                </div>
                <div className="mb-3">
                  <p className="text-white-50 mb-2">Moyenne soutenances/professeur</p>
                  <h4 className="text-success fw-bold">{(totalDefenses / professorsData.length).toFixed(1)}</h4>
                </div>
                <div className="mb-3">
                  <p className="text-white-50 mb-2">Moyenne soutenances/filière</p>
                  <h4 className="text-warning fw-bold">{(totalDefenses / totalSpecialties).toFixed(1)}</h4>
                </div>
                <hr className="border-secondary" />
                <p className="text-white-50 mb-2">Taux de complétude</p>
                <div className="progress" style={{ height: "20px" }}>
                  <div
                    className="progress-bar bg-success"
                    style={{ width: `${(totalDefenses / totalStudents) * 100}%` }}
                  >
                    <span className="fw-bold">{((totalDefenses / totalStudents) * 100).toFixed(0)}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </>
  );
}

export default Dashboard;
