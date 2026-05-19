import { Routes, Route } from "react-router-dom";
import Home from "./pages/home";
import Pages2 from "./pages/pages2";
import Apropos from "./pages/apropos";
import Dashboard from "./pages/dashboard";
import Contact from "./pages/contact";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/planing" element={<Pages2 />} />
      <Route path="/about" element={<Apropos />} />
      <Route path="/apropos" element={<Apropos />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/dashbord" element={<Dashboard />} />
      <Route path="/contact" element={<Contact />} />
    </Routes>
  );
}

export default App;