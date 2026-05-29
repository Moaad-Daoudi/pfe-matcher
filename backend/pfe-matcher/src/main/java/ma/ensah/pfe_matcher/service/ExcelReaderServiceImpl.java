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

// Removed ConfigService dependency

    @Override
    public List<Student> readStudent(MultipartFile file) throws Exception {
        List<Student> students = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = null;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (workbook.getSheetName(i).toLowerCase().contains("etudiant") || workbook.getSheetName(i).toLowerCase().contains("étudiant")) {
                    sheet = workbook.getSheetAt(i);
                    break;
                }
            }
            if (sheet == null) sheet = workbook.getSheetAt(1); // fallback

            Row header = sheet.getRow(0);

            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : header) {
                String headerName = cell.getStringCellValue().trim().toUpperCase();
                // Simple hardcoded fallback to find the proper column dynamically without configuration file
                if (headerName.contains("CNE") || headerName.contains("MASSAR")) colMap.put("CNE", cell.getColumnIndex());
                else if (headerName.equals("NOM")) colMap.put("NOM", cell.getColumnIndex());
                else if (headerName.equals("PRENOM") || headerName.equals("PRÉNOM")) colMap.put("PRENOM", cell.getColumnIndex());
                else if (headerName.contains("FILIERE") || headerName.contains("FILIÈRE")) colMap.put("FILIERE", cell.getColumnIndex());
            }

            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String cne = colMap.containsKey("CNE") ? formatter.formatCellValue(row.getCell(colMap.get("CNE"))).trim() : "";
                String nom = colMap.containsKey("NOM") ? formatter.formatCellValue(row.getCell(colMap.get("NOM"))).trim() : "";
                String prenom = colMap.containsKey("PRENOM") ? formatter.formatCellValue(row.getCell(colMap.get("PRENOM"))).trim() : "";
                String field = colMap.containsKey("FILIERE") ? formatter.formatCellValue(row.getCell(colMap.get("FILIERE"))).trim().toUpperCase() : "UNKNOWN";

                if (!nom.isEmpty()) students.add(new Student(cne.isEmpty() ? UUID.randomUUID().toString() : cne, cne, nom, prenom, field));
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
            Sheet sheet = null;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (workbook.getSheetName(i).toLowerCase().contains("prof")) {
                    sheet = workbook.getSheetAt(i);
                    break;
                }
            }
            if (sheet == null) sheet = workbook.getSheetAt(0); // fallback

            for (Row row : sheet) {
                if (row.getRowNum() < 2) continue; // Skip headers (rows 0 and 1)
                String lastname = formatter.formatCellValue(row.getCell(0)).trim();
                if (lastname.isEmpty()) continue;
                String firstname = formatter.formatCellValue(row.getCell(1)).trim();
                String dept = formatter.formatCellValue(row.getCell(2)).trim();
                
                professors.add(new Professor("PROF" + count++, lastname, firstname, dept.isEmpty() ? "INFORMATIQUE" : dept, 0, 0));
            }
        }
        return professors;
    }
}