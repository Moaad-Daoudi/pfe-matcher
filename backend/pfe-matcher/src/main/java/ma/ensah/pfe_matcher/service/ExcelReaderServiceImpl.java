package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Student;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.*;

@Service
public class ExcelReaderServiceImpl implements ExcelReaderService {

    @Autowired
    private ConfigService configService;

    @Override
    public List<Student> readStudent(MultipartFile file) throws Exception {
        List<Student> students = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);

            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : header) {
                String canonical = configService.getCanonicalHeader(cell.getStringCellValue());
                if (canonical != null) colMap.put(canonical, cell.getColumnIndex());
            }

            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String cne = formatter.formatCellValue(row.getCell(colMap.getOrDefault("CNE", 0))).trim();
                String nom = formatter.formatCellValue(row.getCell(colMap.getOrDefault("NOM", 1))).trim();
                String prenom = formatter.formatCellValue(row.getCell(colMap.getOrDefault("PRENOM", 2))).trim();
                String field = formatter.formatCellValue(row.getCell(colMap.getOrDefault("FILIERE", 5))).trim().toUpperCase();

                if (!cne.isEmpty()) students.add(new Student(cne, cne, nom, prenom, field));
            }
        }
        return students;
    }

    @Override
    public List<Professor> readProfessors(MultipartFile file) throws Exception {
        List<Professor> professors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        int count = 1;
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                String lastname = formatter.formatCellValue(row.getCell(0)).trim();
                if (lastname.equalsIgnoreCase("Nom") || lastname.isEmpty()) continue;
                String firstname = formatter.formatCellValue(row.getCell(1)).trim();
                String dept = formatter.formatCellValue(row.getCell(2)).trim();
                
                professors.add(new Professor("PROF" + count++, lastname, firstname, dept.isEmpty() ? "INFORMATIQUE" : dept, 0, 0));
            }
        }
        return professors;
    }
}