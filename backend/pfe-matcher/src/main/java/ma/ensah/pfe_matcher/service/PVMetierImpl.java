package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.PV;
import ma.ensah.pfe_matcher.model.Soutenance;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PVMetierImpl implements PVMetier {

    @Override
    public void generatePVsFromSoutenances(List<Soutenance> soutenances, String realPath) {
        for (Soutenance s : soutenances) {
            // Convert Soutenance to PV object
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
        String templatePath = realPath + File.separator + "WEB-INF" + File.separator + "template" + File.separator + "Fiche_Evaluation_PFE_NomEtudiant_Prénom.docx";
        Path outputDir = Paths.get(realPath, "upload", "PVs", ligne.getEncadrant());
        Files.createDirectories(outputDir);

        String outputPath = outputDir.toString() + File.separator + "Fiche_Evaluation_PFE_" + ligne.getNom() + "_" + ligne.getPrenom() + ".docx";

        try (FileInputStream fis = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            remplacerTexteDansParagraphes(document.getParagraphs(), "${NOM}", ligne.getNom() + " " + ligne.getPrenom(), false);
            remplacerTexteDansParagraphes(document.getParagraphs(), "${DATE}", ligne.getDate(), false);

            String coucheTdia = "☐", coucheId = "☐", coucheGi = "☐";
            String f = ligne.getFiliere().toLowerCase();
            if(f.contains("tdia")) coucheTdia = "☑";
            else if(f.contains("gi")) coucheGi = "☑";
            else coucheId = "☑";

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

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }
        }
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
}