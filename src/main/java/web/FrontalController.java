package web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import web.actions.*;

@WebServlet("/controller")
public class FrontalController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String VUE_SUFFIX = ".jsp";
	
	/** Délègue GET au traitement commun. */
	protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
		traiter(req, rep);
	}

	/** Délègue POST au traitement commun. */
	protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
		traiter(req, rep);
	}

	private void traiter(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {

		String actionName = req.getParameter("action");

		try {
			// Pattern Command : obtient l'action sans connaître sa classe concrète
			Action action = ActionImpl.getAction(actionName);

			// Exécute l'action — retourne le nom de la vue ou "redirect:..."
			String vue = action.execute(req, rep);

			// Résolution de la vue
			if (vue != null && vue.startsWith("redirect:")) {
				// POST-REDIRECT-GET : empêche la double soumission
				String url = vue.substring("redirect:".length());
				rep.sendRedirect(req.getContextPath() + "/" + url);
			} else {
				// Forward vers la JSP (le chemin complet est construit ici)
				req.getRequestDispatcher(vue + VUE_SUFFIX).forward(req, rep);
			}

		} catch (IllegalArgumentException e) {
			req.getRequestDispatcher("index" + VUE_SUFFIX).forward(req, rep);

		} catch (Exception e) {
			System.err.println("[FrontController] Erreur action='" + actionName + "' : " + e.getMessage());
			e.printStackTrace();
			req.setAttribute("erreurMessage", e.getMessage());
		}
	}
}
