import { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [profFile, setProfFile] = useState(null);
  const [studentFiles, setStudentFiles] = useState({
    GI: null,
    ID: null,
    TDIA: null
  });
  const [results, setResults] = useState([]);

  // Handles updating the state (this handles the "update what I enter" requirement)
  const handleFileChange = (e, field) => {
    setStudentFiles({ ...studentFiles, [field]: e.target.files[0] });
  };

  const handleFinish = async () => {
    // 1. Validation
    if (!profFile) { alert("Please upload Professor Excel first!"); return; }
    
    // Check if at least one field file exists
    const filesToProcess = Object.entries(studentFiles).filter(([_, file]) => file !== null);
    if (filesToProcess.length === 0) { alert("Please upload at least one student file!"); return; }

    const allAssignments = [];

    // 2. Process each file one by one
    for (const [field, file] of filesToProcess) {
      const formData = new FormData();
      formData.append("studentFile", file);
      formData.append("profFile", profFile);
      formData.append("field", field);

      try {
        const response = await axios.post("http://localhost:8080/api/affectations/process", formData);
        allAssignments.push(...response.data); // Aggregate results
      } catch (err) {
        alert(`Error processing ${field} file`);
      }
    }

    setResults(allAssignments);
    alert("Processing complete!");
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>PFE Affectation Tool</h1>

      <div>
        <h3>1. Upload Professors (Required)</h3>
        <input type="file" onChange={(e) => setProfFile(e.target.files[0])} />
      </div>

      <hr />

      <h3>2. Upload Students (Select at least one)</h3>
      {['GI', 'ID', 'TDIA'].map(field => (
        <div key={field} style={{ margin: '10px 0' }}>
          <label>{field}: </label>
          <input type="file" onChange={(e) => handleFileChange(e, field)} />
        </div>
      ))}

      <button onClick={handleFinish} style={{ marginTop: '20px', padding: '10px 20px' }}>
        Finish & Process
      </button>

      {results.length > 0 && (
        <table border="1" style={{ marginTop: '30px', width: '100%' }}>
          <thead><tr><th>Student</th><th>Field</th><th>Assigned Professor</th></tr></thead>
          <tbody>
            {results.map((res, index) => (
              <tr key={index}>
                <td>{res.student.name} {res.student.firstName}</td>
                <td>{res.student.field}</td>
                <td>{res.professor.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default App;

