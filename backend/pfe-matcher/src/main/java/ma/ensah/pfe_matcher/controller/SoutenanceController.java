package ma.ensah.pfe_matcher.controller;

import jakarta.servlet.ServletContext;
import ma.ensah.pfe_matcher.model.PlanningRequest;
import ma.ensah.pfe_matcher.model.PlanningResult;
import ma.ensah.pfe_matcher.model.Soutenance;
import ma.ensah.pfe_matcher.service.PVMetier; // Import PVMetier
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
    private PVMetier pvService; // New Dependency

    @Autowired
    private ServletContext servletContext;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlanning(@RequestBody PlanningRequest request) {
        try {
            pvService.prepareForNewGeneration(servletContext.getRealPath("/"));

            List<Soutenance> soutenances = planningGenerationService.generatePlanning(request);
            List<String> violations = planningValidationService.runAll(soutenances);

            String fileName = "planning_soutenances.pdf";
            planningPdfExportService.generatePlanningPdf(soutenances, fileName);

            // --- AUTOMATIC PV GENERATION ---
            pvService.generatePVsFromSoutenances(soutenances, servletContext.getRealPath("/"));

            Map<String, Object> stats = planningGenerationService.buildStats(soutenances, violations);

            logger.info("Planning generated and PVs created. Soutenances: {}", soutenances.size());

            return ResponseEntity.ok(new PlanningResult(soutenances, violations, stats, fileName));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid planning request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Failed to generate planning", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Internal error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<FileSystemResource> viewPlanningPdf(@PathVariable("fileName") String fileName) {
        String folder = servletContext.getRealPath("/pdfs/");
        File file = new File(folder + File.separator + fileName);
        if (!file.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/list-pvs")
    public ResponseEntity<Map<String, List<String>>> listPvs() {
        File pvsFolder = new File(servletContext.getRealPath("/upload/PVs/"));
        Map<String, List<String>> result = new TreeMap<>();

        File[] professorFolders = pvsFolder.listFiles(File::isDirectory);
        if (professorFolders == null) {
            return ResponseEntity.ok(result);
        }

        for (File professorFolder : professorFolders) {
            File[] files = professorFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".docx"));
            List<String> fileNames = files == null
                    ? Collections.emptyList()
                    : Arrays.stream(files).map(File::getName).sorted().toList();
            result.put(professorFolder.getName(), fileNames);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/download-pv/{profName}/{fileName}")
    public ResponseEntity<FileSystemResource> downloadPv(
            @PathVariable("profName") String profName,
            @PathVariable("fileName") String fileName) {
        String decodedProfName = URLDecoder.decode(profName, StandardCharsets.UTF_8);
        String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        Path root = Paths.get(servletContext.getRealPath("/upload/PVs/")).normalize();
        Path filePath = root.resolve(decodedProfName).resolve(decodedFileName).normalize();
        if (!filePath.startsWith(root)) {
            return ResponseEntity.badRequest().build();
        }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            logger.error("PV file not found at: {}", file.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
