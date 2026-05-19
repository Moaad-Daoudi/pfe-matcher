import { Link } from "react-router-dom";
import LogoUAE from "../assets/logo-uae-petit-format.png";

const navbarStyles = `
  .custom-navbar-gradient {
    background: linear-gradient(90deg, #00b4d8, #00e676) !important;
  }
  .custom-navbar-gradient .navbar-brand {
    font-weight: 700;
    color: #fff !important;
    letter-spacing: 0.3px;
  }
  .custom-navbar-gradient .nav-link {
    color: #fff !important;
    opacity: 0.9;
    transition: opacity 0.3s ease;
    font-size: 14px;
  }
  .custom-navbar-gradient .nav-link:hover {
    opacity: 1;
  }
  .custom-navbar-gradient .nav-link.active {
    font-weight: 600;
    opacity: 1;
  }
`;

interface NavbarProps {
  activePage?: "home" | "planing" | "dashboard" | "contact" | "about";
  dashboardState?: unknown;
}

function Navbar({ activePage, dashboardState }: NavbarProps) {
  return (
    <>
      <style>{navbarStyles}</style>
      <nav className="navbar navbar-expand-lg navbar-dark custom-navbar-gradient">
        <div className="container-fluid">
          <Link className="navbar-brand d-flex align-items-center gap-2" to="/">
            <img src={LogoUAE} alt="UAE Logo" style={{ height: "35px", width: "auto" }} />
            <span>PFE ENSAH</span>
          </Link>
          <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="navbarNav">
            <ul className="navbar-nav ms-auto">
              <li className="nav-item">
                <Link className={`nav-link ${activePage === "home" ? "active" : ""}`} to="/">Home</Link>
              </li>
              <li className="nav-item">
                <Link className={`nav-link ${activePage === "planing" ? "active" : ""}`} to="/planing">Planing</Link>
              </li>
              <li className="nav-item">
                <Link className={`nav-link ${activePage === "dashboard" ? "active" : ""}`} to="/dashbord" state={dashboardState}>Dashboard</Link>
              </li>
              <li className="nav-item">
                <Link className={`nav-link ${activePage === "contact" ? "active" : ""}`} to="/contact">Contact</Link>
              </li>
              <li className="nav-item">
                <Link className={`nav-link ${activePage === "about" ? "active" : ""}`} to="/about">À propos</Link>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    </>
  );
}

export default Navbar;
