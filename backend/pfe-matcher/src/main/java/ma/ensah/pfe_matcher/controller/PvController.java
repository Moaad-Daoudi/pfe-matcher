package ma.ensah.pfe_matcher.controller;

import jakarta.servlet.ServletContext;
import ma.ensah.pfe_matcher.service.PVMetier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pv")
public class PvController {

    @Autowired
    private PVMetier pvService;

    @Autowired
    private ServletContext servletContext;

    @PostMapping("/generate")
    public ResponseEntity<String> handlePvGeneration(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Define Paths
            String uploadBase = servletContext.getRealPath("/upload/Planning");
            Path path = Paths.get(uploadBase);
            Files.createDirectories(path);

            // 2. Save the file
            String fileName = file.getOriginalFilename();
            String uploadPath = uploadBase + File.separator + fileName;
            file.transferTo(new File(uploadPath));

            // 3. Run the Service logic
            pvService.PDFTableExtractor(uploadBase, fileName);
            pvService.generateDoc(servletContext.getRealPath("/"));

            // 4. Return Success
            return ResponseEntity.ok("Generation de PVs fait avec success !");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error during processing: " + e.getMessage());
        }
    }
}