package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.dao.PVDAOImpl;
import ma.ensah.pfe_matcher.model.PV;
import ma.ensah.pfe_matcher.model.Soutenance;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Service
public class PVMetierImpl implements PVMetier {

    @Autowired
    private PVDAOImpl pvDao;

    @Override
    public void generatePVsFromSoutenances(List<Soutenance> soutenances, String realPath) {
        for (Soutenance s : soutenances) {
            PV pv = new PV();
            pv.setNom(s.getAssignment().getStudent().getLastname());
            pv.setPrenom(s.getAssignment().getStudent().getFirstname());
            pv.setFiliere(s.getAssignment().getStudent().getField());
            pv.setEncadrant(s.getEncadrant().getLastname() + " " + s.getEncadrant().getFirstname());
            pv.setJury1(s.getJury1().getLastname() + " " + s.getJury1().getFirstname());
            pv.setJury2(s.getJury2().getLastname() + " " + s.getJury2().getFirstname());
            pv.setDate(s.getDate().toString());

            try {
                processWordDocument(pv, realPath);
            } catch (IOException e) {
                System.err.println("Error creating PV for: " + pv.getNom());
                e.printStackTrace();
            }
        }
    }

    private void processWordDocument(PV ligne, String realPath) throws IOException {
        Path templatePath = resolveTemplatePath(realPath);
        Path outputDir = Paths.get(realPath, "upload", "PVs", safePathSegment(ligne.getEncadrant()));
        Files.createDirectories(outputDir);

        Path outputPath = outputDir.resolve("Fiche_Evaluation_PFE_"
                + safePathSegment(ligne.getNom()) + "_"
                + safePathSegment(ligne.getPrenom()) + ".docx");

        try (XWPFDocument document = new XWPFDocument(Files.newInputStream(templatePath))) {
            remplacerTexteDansParagraphes(document.getParagraphs(), "${NOM}", ligne.getNom() + " " + ligne.getPrenom(), false);
            remplacerTexteDansParagraphes(document.getParagraphs(), "${DATE}", ligne.getDate(), false);

            String coucheTdia = "\u2610", coucheId = "\u2610", coucheGi = "\u2610";
            String f = ligne.getFiliere() == null ? "" : ligne.getFiliere().toLowerCase();
            if (f.contains("tdia")) {
                coucheTdia = "\u2611";
            } else if (f.contains("gi")) {
                coucheGi = "\u2611";
            } else {
                coucheId = "\u2611";
            }

            remplacerTexteDansParagraphes(document.getParagraphs(), "${FILIERE_ID}", coucheId, true);
            remplacerTexteDansParagraphes(document.getParagraphs(), "${FILIERE_GI}", coucheGi, true);
            remplacerTexteDansParagraphes(document.getParagraphs(), "${FILIERE_TDIA}", coucheTdia, true);

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        remplacerTexteDansParagraphes(cell.getParagraphs(), "${JURY_PRESIDENT}", ligne.getEncadrant(), false);
                        remplacerTexteDansParagraphes(cell.getParagraphs(), "${JURY_RAPP1}", ligne.getJury1(), false);
                        remplacerTexteDansParagraphes(cell.getParagraphs(), "${JURY_RAPP2}", ligne.getJury2(), false);
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                document.write(fos);
            }
        }
    }

    private Path resolveTemplatePath(String realPath) throws IOException {
        Path templateDir = Paths.get(realPath, "WEB-INF", "template");
        try (var stream = Files.list(templateDir)) {
            return stream
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".docx"))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException("No DOCX template found in " + templateDir));
        }
    }

    private String safePathSegment(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private void remplacerTexteDansParagraphes(List<XWPFParagraph> paragraphes, String cible, String remplacement, boolean bold) {
        for (XWPFParagraph paragraph : paragraphes) {
            String texteParagraphe = paragraph.getText();
            if (texteParagraphe != null && texteParagraphe.contains(cible)) {
                String nouveauTexte = texteParagraphe.replace(cible, remplacement == null ? " " : remplacement);
                for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(nouveauTexte);
                newRun.setFontFamily("Times New Roman");
                newRun.setFontSize(12);
                newRun.setBold(bold);
            }
        }
    }

    @Override
    public void prepareForNewGeneration(String realPath) {
        pvDao.clear();

        Path pvPath = Paths.get(realPath, "upload", "PVs");
        if (Files.exists(pvPath)) {
            try {
                Files.walk(pvPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (IOException e) {
                System.err.println("Could not clear old PVs: " + e.getMessage());
            }
        }
    }
}
