package ma.ensah.pfe_matcher.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.ServletContext;
import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Soutenance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanningPdfExportServiceImpl implements PlanningPdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final float SMALL_FONT = 7f;
    private static final float NORMAL_FONT = 8f;
    private static final float LARGE_FONT = 9f;
    private static final DeviceRgb[] PROFESSOR_COLORS = new DeviceRgb[]{
            new DeviceRgb(255, 179, 186),
            new DeviceRgb(186, 225, 255),
            new DeviceRgb(186, 255, 201),
            new DeviceRgb(255, 223, 186),
            new DeviceRgb(218, 186, 255),
            new DeviceRgb(255, 255, 186),
            new DeviceRgb(186, 255, 246),
            new DeviceRgb(255, 204, 229)
    };

    @Autowired
    private ServletContext servletContext;

    @Override
    public void generatePlanningPdf(List<Soutenance> soutenances, String fileName) throws Exception {
        String folder = servletContext.getRealPath("/pdfs/");
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String path = folder + File.separator + fileName;

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(path));
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4.rotate())) {

            soutenances.sort(Comparator.comparing(Soutenance::getDate)
                    .thenComparing(Soutenance::getStartTime)
                    .thenComparing(Soutenance::getSalle)
                    .thenComparing(s -> s.getAssignment().getStudent().getLastname(), String.CASE_INSENSITIVE_ORDER));

            Map<String, DeviceRgb> colorByDayAndProfessor = new HashMap<>();
            Map<String, Integer> nextPaletteByDay = new HashMap<>();

            document.add(new Paragraph("Planning des soutenances PFE")
                    .setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Annee universitaire 2024/2025")
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));

            float[] columnWidths = {0.7f, 2.05f, 2.05f, 2.05f, 1.0f, 0.42f, 0.52f, 1.15f, 1.95f, 0.46f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setFixedLayout();

            String[] headers = {
                    "ID", "Encadrant", "Membre jury 1", "Membre jury 2",
                    "Date", "Heure", "Salle", "Nom", "Prenom", "Filiere"
            };

            for (String header : headers) {
                boolean compact = "Heure".equals(header) || "Salle".equals(header) || "Filiere".equals(header);
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setBold().setFontSize(compact ? 7f : 9f))
                        .setBackgroundColor(new DeviceRgb(220, 220, 220))
                        .setTextAlignment(TextAlignment.CENTER);
                if (compact) {
                    headerCell.setPadding(1f);
                }
                table.addHeaderCell(headerCell);
            }

            int displayId = 1;
            for (Soutenance s : soutenances) {
                table.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(displayId++)).setFontSize(NORMAL_FONT))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(coloredProfessorCell(s.getDate(), s.getEncadrant(), colorByDayAndProfessor, nextPaletteByDay));
                table.addCell(coloredProfessorCell(s.getDate(), s.getJury1(), colorByDayAndProfessor, nextPaletteByDay));
                table.addCell(coloredProfessorCell(s.getDate(), s.getJury2(), colorByDayAndProfessor, nextPaletteByDay));
                table.addCell(new Cell().add(new Paragraph(s.getDate().format(DATE_FORMATTER)).setFontSize(NORMAL_FONT)));
                table.addCell(compactCell(s.getStartTime().format(TIME_FORMATTER), SMALL_FONT));
                table.addCell(compactCell(s.getSalle(), SMALL_FONT));
                table.addCell(new Cell().add(new Paragraph(s.getAssignment().getStudent().getLastname()).setFontSize(NORMAL_FONT)));
                table.addCell(new Cell().add(new Paragraph(s.getAssignment().getStudent().getFirstname()).setFontSize(LARGE_FONT)));
                table.addCell(compactCell(s.getAssignment().getStudent().getField(), SMALL_FONT));
            }

            document.add(table);
        }
    }

    private String fullName(Professor professor) {
        if (professor == null) {
            return "";
        }
        String last = professor.getLastname() == null ? "" : professor.getLastname();
        String first = professor.getFirstname() == null ? "" : professor.getFirstname();
        return (last + " " + first).trim();
    }

    private Cell coloredProfessorCell(LocalDate date,
                                      Professor professor,
                                      Map<String, DeviceRgb> colorByDayAndProfessor,
                                      Map<String, Integer> nextPaletteByDay) {
        String name = fullName(professor);
        Cell cell = new Cell().add(new Paragraph(name).setFontSize(LARGE_FONT).setBold());

        if (professor == null || date == null) {
            return cell;
        }

        String dayKey = date.toString();
        String professorKey = professorColorKey(professor);
        String mapKey = dayKey + "|" + professorKey;

        DeviceRgb color = colorByDayAndProfessor.get(mapKey);
        if (color == null) {
            int next = nextPaletteByDay.getOrDefault(dayKey, 0);
            color = PROFESSOR_COLORS[next % PROFESSOR_COLORS.length];
            colorByDayAndProfessor.put(mapKey, color);
            nextPaletteByDay.put(dayKey, next + 1);
        }

        cell.setBackgroundColor(color);
        return cell;
    }

    private String professorColorKey(Professor professor) {
        if (professor.getId() != null && !professor.getId().isBlank()) {
            return professor.getId().trim().toLowerCase();
        }
        return (safe(professor.getLastname()) + "|" + safe(professor.getFirstname())).toLowerCase();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private Cell compactCell(String text, float fontSize) {
        return new Cell()
                .setPadding(1f)
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(text == null ? "" : text).setFontSize(fontSize).setMargin(0f));
    }
}
