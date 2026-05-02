package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.Professor;
import java.util.List;

public interface PdfExportService {
    void generateAssignmentPdf(List<Assignment> allAssignments, List<Professor> allProfessors, String fileName) throws Exception;
}