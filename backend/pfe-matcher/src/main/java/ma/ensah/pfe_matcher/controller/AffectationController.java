package ma.ensah.pfe_matcher.controller;

import jakarta.servlet.ServletContext;
import ma.ensah.pfe_matcher.dao.AssignmentDAO;
import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.AssignmentResult;
import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Student;
import ma.ensah.pfe_matcher.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/affectations")
public class AffectationController {

    private static final Logger logger = LoggerFactory.getLogger(AffectationController.class);

    @Autowired
    private ExcelReaderService readerService;

    @Autowired
    private MatchingEngineService matchingEngine;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private AssignmentDAO assignmentDAO;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private DashboardService dashboardService;

    @PostMapping("/clear")
    public ResponseEntity<String> clearAssignments() {
        assignmentDAO.clear();
        return ResponseEntity.ok("Assignments cleared successfully.");
    }

    @PostMapping("/process")
    public ResponseEntity<AssignmentResult> processAffectation(
            @RequestParam("studentFile") MultipartFile studentFile,
            @RequestParam("profFile") MultipartFile profFile,
            @RequestParam("field") String field) {

        try {
            // Read Files
            List<Student> students = readerService.readStudent(field, studentFile);
            List<Professor> professors = readerService.readProfessors(profFile);
            assignmentDAO.saveJuryProfessors(professors);

            // Perform Matching
            Collections.shuffle(professors);
            List<Assignment> newAssignments = matchingEngine.assignStudentsToProfs(students, professors);

            // Get all assignments for PDF & Validation
            List<Assignment> allAssignments = assignmentDAO.getAll();

            // Generate PDF
            String fileName = "affectation_final.pdf";
            pdfExportService.generateAssignmentPdf(allAssignments, professors, fileName);

            // Run Validations (Anomaly detection)
            List<String> violations = validationService.runAll(allAssignments);

            // Get Dashboard Stats
            Map<String, Object> stats = dashboardService.getStats(allAssignments);

            logger.info("Processing complete. Assignments: {}. Violations detected: {}",
                    newAssignments.size(), violations.size());

            // Return the combined result
            return ResponseEntity.ok(new AssignmentResult(allAssignments, violations, stats));

        } catch (Exception e) {
            logger.error("Error processing files", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<FileSystemResource> viewPdf(@PathVariable("fileName") String fileName) {
        // Get the same folder as your Service
        String folder = servletContext.getRealPath("/pdfs/");
        File file = new File(folder + File.separator + fileName);

        // Check if file exists
        if (!file.exists()) {
            logger.error("File not found at: {}", file.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        // Return with "inline" instead of "attachment"
        // "inline" tells the browser to display it in the window
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }
}
