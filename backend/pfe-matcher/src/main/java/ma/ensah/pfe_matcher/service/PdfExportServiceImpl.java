package ma.ensah.pfe_matcher.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.ServletContext;
import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfExportServiceImpl implements PdfExportService {

    @Autowired
    private ServletContext servletContext;

    // Define allowed departments here
    private static final Set<String> ALLOWED_DEPARTMENTS = new HashSet<>(
            Arrays.asList("Informatique", "Mathématique")
    );

    @Override
    public void generateAssignmentPdf(List<Assignment> allAssignments, List<Professor> allProfessors, String fileName) throws Exception {
        String folder = servletContext.getRealPath("/pdfs/");
        java.io.File dir = new java.io.File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String path = folder + java.io.File.separator + fileName;

        PdfWriter writer = new PdfWriter(new FileOutputStream(path));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        // --- SORTING LOGIC ---
        // Create a copy and sort it alphabetically by lastname
        List<Professor> sortedProfessors = allProfessors.stream()
                .filter(p -> ALLOWED_DEPARTMENTS.stream()
                        .anyMatch(dept -> dept.equalsIgnoreCase(p.getDepartment())))
                .sorted(Comparator.comparing(Professor::getLastname, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // Header
        document.add(new Paragraph("Ecole Nationale des Sciences Appliquées - Al Hoceima").setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Département Mathématiques et Informatique").setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Affectation des encadrants de Projet de Fin d'Etude").setBold());
        document.add(new Paragraph("Année Universitaire 2024/2025").setFontSize(10));

        addLegend(document);
        document.add(new Paragraph(" "));

        // Define Table Layout (Fixed to 6 columns)
        float[] columnWidths = {1.5f, 1.5f, 2.25f, 2.25f, 2.25f, 2.25f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setFixedLayout();

        // Header Row
        String[] headers = {"Nom", "Prénom", "Etudiant 1", "Etudiant 2", "Etudiant 3", "Etudiant 4"};
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontSize(9)).setBackgroundColor(new DeviceRgb(220, 220, 220)));
        }

        // Group assignments for easy lookup
        Map<String, List<Assignment>> assignmentsByProf = allAssignments.stream()
                .collect(Collectors.groupingBy(a -> a.getProfessor().getLastname()));

        // --- USE sortedProfessors HERE ---
        for (Professor prof : sortedProfessors) {
            table.addCell(new Cell().add(new Paragraph(prof.getLastname()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(prof.getFirstname()).setFontSize(8)));

            List<Assignment> profAssignments = assignmentsByProf.getOrDefault(prof.getLastname(), Collections.emptyList());

            for (int i = 0; i < 4; i++) {
                if (i < profAssignments.size()) {
                    Student s = profAssignments.get(i).getStudent();
                    Cell cell = new Cell().add(new Paragraph(s.getLastname() + " " + s.getFirstname()).setFontSize(8));

                    if ("ID".equalsIgnoreCase(s.getField())) {
                        cell.setBackgroundColor(new DeviceRgb(245, 204, 178));
                    } else if ("GI".equalsIgnoreCase(s.getField())) {
                        cell.setBackgroundColor(new DeviceRgb(173, 216, 230));
                    } else if ("TDIA".equalsIgnoreCase(s.getField())) {
                        cell.setBackgroundColor(new DeviceRgb(200, 230, 201));
                    }
                    table.addCell(cell);
                } else {
                    table.addCell(new Cell().add(new Paragraph(" ")));
                }
            }
        }

        document.add(table);
        document.close();
    }

    private void addLegend(Document document) {
        Table legendTable = new Table(3); // 3 Columns
        legendTable.setWidth(UnitValue.createPercentValue(45));

        legendTable.addCell(new Cell().add(new Paragraph("Filière ID")).setBackgroundColor(new DeviceRgb(245, 204, 178)).setFontSize(9));
        legendTable.addCell(new Cell().add(new Paragraph("Filière GI")).setBackgroundColor(new DeviceRgb(173, 216, 230)).setFontSize(9));
        legendTable.addCell(new Cell().add(new Paragraph("Filière TDIA")).setBackgroundColor(new DeviceRgb(200, 230, 201)).setFontSize(9));

        document.add(legendTable);
    }
}