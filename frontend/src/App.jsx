import { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [profFile, setProfFile] = useState(null);
  const [studentFiles, setStudentFiles] = useState({ GI: null, ID: null, TDIA: null });
  
  // State for the new requirements
  const [results, setResults] = useState([]);
  const [violations, setViolations] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleFileChange = (e, field) => {
    setStudentFiles({ ...studentFiles, [field]: e.target.files[0] });
  };

  const handleFinish = async () => {
    setLoading(true);
    try {
      await axios.post("http://localhost:8080/api/affectations/clear");
      
      const filesToProcess = Object.entries(studentFiles).filter(([_, file]) => file !== null);
      let allAssignments = [];
      let finalViolations = [];
      let finalStats = null;

      for (const [field, file] of filesToProcess) {
        const formData = new FormData();
        formData.append("studentFile", file);
        formData.append("profFile", profFile);
        formData.append("field", field);

        // This matches your new AssignmentResultDTO structure
        const response = await axios.post("http://localhost:8080/api/affectations/process", formData);
        
        allAssignments = response.data.assignments; // From DTO
        finalViolations = response.data.violations; // From DTO
        finalStats = response.data.stats;           // From DTO
      }

      setResults(allAssignments);
      setViolations(finalViolations);
      setStats(finalStats);
      
    } catch (err) {
      console.error(err);
      alert("Error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial' }}>
      <h1>PFE Management Application</h1>

      {/* 1. Uploads */}
      <div style={{ border: '1px solid #ccc', padding: '10px' }}>
        <h3>Inputs</h3>
        <input type="file" onChange={(e) => setProfFile(e.target.files[0])} />
        {['GI', 'ID', 'TDIA'].map(field => (
          <div key={field}><label>{field}: </label><input type="file" onChange={(e) => handleFileChange(e, field)} /></div>
        ))}
        <button onClick={handleFinish} disabled={loading} style={{ marginTop: '10px' }}>
          {loading ? "Processing..." : "Finish & Process"}
        </button>
      </div>

      {/* 2. Dashboard Section*/}
      {stats && (
        <div style={{ marginTop: '20px', background: '#e3f2fd', padding: '15px' }}>
          <h3>Dashboard - Key Statistics</h3>
          <div style={{ display: 'flex', gap: '20px' }}>
            {Object.entries(stats).map(([key, value]) => (
              <div key={key}><strong>{key}:</strong> {JSON.stringify(value)}</div>
            ))}
          </div>
        </div>
      )}

      {/* 3. Anomalies Section*/}
      {violations.length > 0 && (
        <div style={{ marginTop: '20px', background: '#ffebee', border: '1px solid red', padding: '15px', color: 'red' }}>
          <h3>⚠️ Anomalies Detected (Compliance Check)</h3>
          <ul>
            {violations.map((v, i) => <li key={i}>{v}</li>)}
          </ul>
        </div>
      )}

      {/* 4. Results & PDF View */}
      {results.length > 0 && (
        <div style={{ marginTop: '30px' }}>
          <h3>Results</h3>
          <button onClick={() => window.open("http://localhost:8080/api/affectations/view/affectation_final.pdf", "_blank")}>
            View PDF
          </button>
          
          <table border="1" style={{ width: '100%', marginTop: '10px', borderCollapse: 'collapse' }}>
            <thead><tr><th>Student</th><th>Field</th><th>Professor</th></tr></thead>
            <tbody>
              {results.map((res, index) => (
                <tr key={index}>
                  <td>{res.student.lastname} {res.student.firstname}</td>
                  <td>{res.student.field}</td>
                  <td>{res.professor.lastname}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default App;