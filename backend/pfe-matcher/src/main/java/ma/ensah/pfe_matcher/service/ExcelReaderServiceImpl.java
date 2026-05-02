package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Student;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelReaderServiceImpl implements ExcelReaderService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelReaderServiceImpl.class);

    @Override
    public List<Student> readStudent(String field, MultipartFile file) throws Exception {
        List<Student> students = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Read CNE
                String cne = formatter.formatCellValue(row.getCell(0)).trim();
                String nom = formatter.formatCellValue(row.getCell(1)).trim();

                // HEADER DETECTION: If row starts with "CNE" or "Nom", it's a header, SKIP IT
                if (cne.equalsIgnoreCase("CNE") || cne.equalsIgnoreCase("id") || nom.equalsIgnoreCase("NOM")) {
                    continue;
                }

                // If empty row, skip
                if (cne.isEmpty()) continue;

                String lastname = nom;
                String firstname = formatter.formatCellValue(row.getCell(2)).trim();

                students.add(new Student(cne, cne, lastname, firstname, field));
            }
        }
        logger.info("Total students parsed from {} for field {}: {}", file.getOriginalFilename(), field, students.size());
        return students;
    }

    @Override
    public List<Professor> readProfessors(MultipartFile file) throws Exception {
        List<Professor> professors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        int count = 1;

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                String lastname = formatter.formatCellValue(row.getCell(0)).trim();
                String firstname = formatter.formatCellValue(row.getCell(1)).trim();
                String dept = formatter.formatCellValue(row.getCell(2)).trim();

                // HEADER DETECTION: If row contains labels, SKIP IT
                if (lastname.equalsIgnoreCase("Nom") || lastname.equalsIgnoreCase("Encadrant")) {
                    continue;
                }

                // If the first cell is empty, it might be the merged "Encadrant" header row, skip it
                if (lastname.isEmpty()) continue;

                // Department might be merged, if null or empty, put default
                if (dept.isEmpty()) dept = "Informatique";

                String id = "PROF" + count++;
                professors.add(new Professor(id, lastname, firstname, dept, 5, 0));
            }
        }
        return professors;
    }
}