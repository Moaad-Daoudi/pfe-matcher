package web.actions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import services.PVMetier;
import services.PVMetierImpl;

class PvAction implements Action {

	private static final PVMetier service = PVMetierImpl.getInstance();
	
	@Override
	public String execute(HttpServletRequest req, HttpServletResponse rep) throws Exception {
		Path path = Paths.get(req.getServletContext().getRealPath("") + "upload" + File.separator + "Planning");
		Files.createDirectories(path);
		
		Part filePart = req.getPart("file");
		String fileName = filePart.getSubmittedFileName();
		String uploadPath = path.toString() + File.separator + fileName;
		
		filePart.write(uploadPath);
		
		service.PDFTableExtractor(req.getServletContext().getRealPath("/upload/Planning"), fileName);
		service.generateDoc(req.getServletContext().getRealPath("/"));
		
		req.setAttribute("success", "Generation de PVs fait avec success !");
		return "index";
	}

}
