package ma.ensah.pfe_matcher.controller;

import jakarta.servlet.ServletContext;
import ma.ensah.pfe_matcher.model.PlanningRequest;
import ma.ensah.pfe_matcher.model.PlanningResult;
import ma.ensah.pfe_matcher.model.Soutenance;
import ma.ensah.pfe_matcher.service.PlanningGenerationService;
import ma.ensah.pfe_matcher.service.PlanningPdfExportService;
import ma.ensah.pfe_matcher.service.PlanningValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soutenances")
public class SoutenanceController {

    private static final Logger logger = LoggerFactory.getLogger(SoutenanceController.class);

    @Autowired
    private PlanningGenerationService planningGenerationService;

    @Autowired
    private PlanningValidationService planningValidationService;

    @Autowired
    private PlanningPdfExportService planningPdfExportService;

    @Autowired
    private ServletContext servletContext;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlanning(@RequestBody PlanningRequest request) {
        try {
            List<Soutenance> soutenances = planningGenerationService.generatePlanning(request);
            List<String> violations = planningValidationService.runAll(soutenances);

            String fileName = "planning_soutenances.pdf";
            planningPdfExportService.generatePlanningPdf(soutenances, fileName);

            Map<String, Object> stats = planningGenerationService.buildStats(soutenances, violations);

            logger.info("Planning generated. Soutenances: {}. Violations: {}", soutenances.size(), violations.size());

            return ResponseEntity.ok(new PlanningResult(soutenances, violations, stats, fileName));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid planning request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Failed to generate planning", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Internal error while generating planning.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<FileSystemResource> viewPlanningPdf(@PathVariable("fileName") String fileName) {
        String folder = servletContext.getRealPath("/pdfs/");
        File file = new File(folder + File.separator + fileName);

        if (!file.exists()) {
            logger.error("Planning PDF not found: {}", file.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }
}
