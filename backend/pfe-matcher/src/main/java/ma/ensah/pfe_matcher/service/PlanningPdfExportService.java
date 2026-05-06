package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Soutenance;

import java.util.List;

public interface PlanningPdfExportService {
    void generatePlanningPdf(List<Soutenance> soutenances, String fileName) throws Exception;
}
