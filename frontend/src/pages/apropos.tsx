import Navbar from "../components/Navbar";

const styles = `
  .pg { font-family: 'Segoe UI', sans-serif; background: #0f1923; color: #fff; min-height: 100vh; }
  .hero-dark { background: #132030; border-bottom: 1px solid #1e3245; padding: 2.5rem 2rem 2rem; text-align: center; }
  .hero-dark h1 { font-size: 1.8rem; font-weight: 700; color: #fff; margin-bottom: 0.5rem; letter-spacing: -0.3px; }
  .hero-dark p { font-size: 14px; color: #8fa8be; }
  .content { padding: 2rem 1.5rem; max-width: 720px; margin: 0 auto; display: flex; flex-direction: column; gap: 1rem; }
  .card-d { background: #132030; border: 1px solid #1e3245; border-radius: 10px; padding: 1.25rem 1.5rem; }
  .card-head { display: flex; align-items: center; gap: 10px; margin-bottom: 0.85rem; padding-bottom: 0.75rem; border-bottom: 1px solid #1e3245; }
  .card-head-icon {
    width: 32px; height: 32px; border-radius: 8px;
    background: rgba(0,230,118,0.12); border: 1px solid rgba(0,230,118,0.25);
    display: flex; align-items: center; justify-content: center;
  }
  .card-head-icon svg { width: 16px; height: 16px; stroke: #00e676; fill: none; stroke-width: 2; }
  .card-head-title { font-size: 14px; font-weight: 600; color: #00e676; letter-spacing: 0.2px; }
  .card-text { font-size: 13px; color: #8fa8be; line-height: 1.7; }
  .items-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 8px; }
  .item-pill { display: flex; align-items: center; gap: 8px; background: #0f1923; border: 1px solid #1e3245; border-radius: 8px; padding: 9px 12px; font-size: 13px; color: #c8dae8; }
  .pill-dot { width: 6px; height: 6px; border-radius: 50%; background: #00b4d8; flex-shrink: 0; }
  .tech-row { display: flex; flex-wrap: wrap; gap: 6px; }
  .tech-tag { font-size: 12px; font-weight: 500; color: #00b4d8; background: rgba(0,180,216,0.1); border: 1px solid rgba(0,180,216,0.25); border-radius: 99px; padding: 4px 12px; }
  .supervisor-row { display: flex; align-items: center; gap: 12px; background: #0f1923; border: 1px solid #1e3245; border-radius: 8px; padding: 12px 14px; margin-top: 0.75rem; }
  .sup-avatar { width: 40px; height: 40px; border-radius: 50%; background: rgba(0,230,118,0.15); border: 1px solid rgba(0,230,118,0.3); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; color: #00e676; flex-shrink: 0; }
  .sup-name { font-size: 14px; font-weight: 600; color: #fff; }
  .sup-role { font-size: 12px; color: #8fa8be; margin-top: 2px; }
  .accent { color: #00e676; }
`;

function About() {
  return (
    <>
      <style>{styles}</style>
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
              Projet réalisé à l'ENSAH par des étudiants en ingénierie TDAI, dans le cadre d'un module
              de développement web appliqué à la transformation digitale.
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