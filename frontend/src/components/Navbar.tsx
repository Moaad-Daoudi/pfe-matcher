import { Link } from "react-router-dom";
import LogoUAE from "../assets/logo-uae-petit-format.png";

const navbarStyles = `
  .custom-navbar-gradient {
    background: linear-gradient(90deg, #00b4d8, #00e676) !important;
    box-shadow: 0 8px 32px rgba(30, 157, 60, 0.2) !important;
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(30, 157, 60, 0.2);
  }
  .custom-navbar-gradient .navbar-brand {
    font-weight: 800;
    color: #fff !important;
    letter-spacing: 0.5px;
    font-size: 1.3rem;
    transition: all 0.3s ease;
  }
  .custom-navbar-gradient .navbar-brand:hover {
    transform: scale(1.03);
    color: #fff !important;
    text-shadow: 0 0 12px rgba(255, 255, 255, 0.8);
  }
  .custom-navbar-gradient .nav-link {
    color: rgba(255, 255, 255, 0.75) !important;
    transition: all 0.25s ease;
    font-size: 15px;
    font-weight: 500;
    position: relative;
    padding: 8px 16px !important;
    margin: 0 4px;
  }
  .custom-navbar-gradient .nav-link::after {
    content: '';
    position: absolute;
    bottom: 2px;
    left: 16px;
    right: 16px;
    height: 3px;
    background: #ffffff;
    transition: all 0.25s ease;
    transform: scaleX(0);
    border-radius: 2px;
  }
  .custom-navbar-gradient .nav-link:hover {
    color: #ffffff !important;
  }
  .custom-navbar-gradient .nav-link:hover::after {
    transform: scaleX(1);
    background: rgba(255, 255, 255, 0.6);
  }
  .custom-navbar-gradient .nav-link.active {
    font-weight: 700;
    color: #ffffff !important;
  }
  .custom-navbar-gradient .nav-link.active::after {
    transform: scaleX(1);
    background: #ffffff;
  }
  .custom-navbar-gradient .navbar-toggler {
    border-color: rgba(255, 255, 255, 0.5) !important;
    transition: all 0.3s ease;
  }
  .custom-navbar-gradient .navbar-toggler:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  .custom-navbar-gradient .navbar-toggler-icon {
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 30 30'%3e%3cpath stroke='rgba%28255, 255, 255, 0.9%29' stroke-linecap='round' stroke-miterlimit='10' stroke-width='2' d='M4 7h22M4 15h22M4 23h22'/%3e%3c/svg%3e");
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
