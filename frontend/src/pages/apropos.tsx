import Navbar from "../components/Navbar";

function About() {
  return (
    <>
      <div className="pg">

        {/* NAVBAR */}
        <Navbar activePage="about" />

        {/* HERO */}
        <div className="hero-dark">
          <h1>À propos <span className="accent">du projet</span></h1>
          <p>Application de planification des soutenances PFE — ENSAH</p>
        </div>

        {/* CONTENT */}
        <div className="content">

          {/* Présentation */}
          <div className="card-d">
            <div className="card-head">
              <div className="card-head-icon">
                <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 8v4l3 3"/></svg>
              </div>
              <span className="card-head-title">Présentation</span>
            </div>
            <p className="card-text">
              Cette application permet de gérer et planifier automatiquement les soutenances de Projets
              de Fin d'Études à l'ENSAH. Elle simplifie l'import des données et la génération du
              calendrier des soutenances.
            </p>
          </div>

          {/* Fonctionnalités */}
          <div className="card-d">
            <div className="card-head">
              <div className="card-head-icon">
                <svg viewBox="0 0 24 24"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/></svg>
              </div>
              <span className="card-head-title">Ce que fait l'application</span>
            </div>
            <div className="items-grid">
              {[
                "Import fichiers Excel",
                "Listes étudiants & encadrants",
                "Configuration des dates PFE",
                "Planification automatique",
                "Calendrier des soutenances",
                "Réduction des erreurs",
              ].map((item) => (
                <div key={item} className="item-pill">
                  <div className="pill-dot"></div>
                  {item}
                </div>
              ))}
            </div>
          </div>

          {/* Technologies */}
          <div className="card-d">
            <div className="card-head">
              <div className="card-head-icon">
                <svg viewBox="0 0 24 24"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
              </div>
              <span className="card-head-title">Technologies</span>
            </div>
            <div className="tech-row">
              {["React.js", "TypeScript", "Bootstrap","Spring MVC"].map((t) => (
                <span key={t} className="tech-tag">{t}</span>
              ))}
            </div>
          </div>

          {/* Contexte */}
          <div className="card-d">
            <div className="card-head">
              <div className="card-head-icon">
                <svg viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
              </div>
              <span className="card-head-title">Contexte académique</span>
            </div>
            <p className="card-text">
              Projet réalisé à l'ENSAH par des étudiants en ingénierie TDAI, dans le cadre de notre module
              de développement web appliqué.
            </p>
            <div className="supervisor-row">
              <div className="sup-avatar">MC</div>
              <div>
                <div className="sup-name">M. Cherradi</div>
                <div className="sup-role">Encadrant — ENSAH</div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </>
  );
}

export default About;