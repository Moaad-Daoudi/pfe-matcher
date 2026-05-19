import Navbar from "../components/Navbar";

const styles = `
  .pg { font-family: 'Segoe UI', sans-serif; background: #0f1923; color: #fff; min-height: 100vh; }
  .hero-dark { background: #132030; border-bottom: 1px solid #1e3245; padding: 2.5rem 2rem 2rem; text-align: center; }
  .hero-dark h1 { font-size: 1.8rem; font-weight: 700; color: #fff; margin-bottom: 0.5rem; letter-spacing: -0.3px; }
  .hero-dark p { font-size: 14px; color: #8fa8be; }
  .content { padding: 2rem 1.5rem; max-width: 720px; margin: 0 auto; }
  .card-d { background: #132030; border: 1px solid #1e3245; border-radius: 10px; padding: 1.25rem 1.5rem; margin-bottom: 1rem; }
  .card-text { font-size: 13px; color: #8fa8be; line-height: 1.7; }
  .accent { color: #00e676; }
`;

function Contact() {
  return (
    <>
      <style>{styles}</style>
      <div className="pg">

        {/* NAVBAR */}
        <Navbar activePage="contact" />

        {/* HERO */}
        <div className="hero-dark">
          <h1>Nous <span className="accent">Contacter</span></h1>
          <p>Pour toute question ou demande d'information</p>
        </div>

        {/* CONTENT */}
        <div className="content">

          {/* Contact Info */}
          <div className="card-d">
            <h3 className="text-white mb-3">Informations de Contact</h3>
            <p className="card-text mb-2">
              <strong className="accent">Email:</strong> contact@ensah.ac.ma
            </p>
            <p className="card-text mb-2">
              <strong className="accent">Téléphone:</strong> +212 6XX XX XX XX
            </p>
            <p className="card-text mb-0">
              <strong className="accent">Adresse:</strong> École Nationale des Sciences Appliquées, Al Hoceima
            </p>
          </div>

          {/* Message */}
          <div className="card-d">
            <h3 className="text-white mb-3">Envoyez-nous un Message</h3>
            <p className="card-text">Un formulaire de contact sera disponible prochainement.</p>
          </div>

        </div>
      </div>
    </>
  );
}

export default Contact;
