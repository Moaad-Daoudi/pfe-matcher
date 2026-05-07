package web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {

	/**
     * Exécute l'action métier et retourne le nom logique de la vue.
     * @param request  La requête HTTP (paramètres du formulaire/URL).
     * @param response La réponse HTTP (pour les redirections).
     * @return Nom de la vue ou "redirect:URL" pour une redirection.
     * @throws Exception En cas d'erreur non gérée (capturée par FrontController).
     */
	String execute(HttpServletRequest req, HttpServletResponse rep) throws Exception;
}
