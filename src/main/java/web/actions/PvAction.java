package web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import services.PVMetier;
import services.PVMetierImpl;

class PvAction implements Action {

	private static final PVMetier service = PVMetierImpl.getInstance();
	
	@Override
	public String execute(HttpServletRequest req, HttpServletResponse rep) throws Exception {
		service.PDFTableExtractor(req.getServletContext().getRealPath("/upload/Planning"));
		service.generateDoc(req.getServletContext().getRealPath("/"));
		
		req.setAttribute("success", "Generation de PVs fait avec success !");
		return "index";
	}

}
