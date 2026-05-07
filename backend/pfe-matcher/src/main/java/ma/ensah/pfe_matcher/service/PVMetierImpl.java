package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.dao.PVDAO;
import ma.ensah.pfe_matcher.dao.PVDAOImpl;
import ma.ensah.pfe_matcher.model.PV;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PVMetierImpl implements PVMetier {

    // Indices des colonnes
    private static final int COL_ID = 0;
    private static final int COL_ENCADRANT = 1;
    private static final int COL_JURY1 = 2;
    private static final int COL_JURY2 = 3;
    private static final int COL_DATE = 4;
    private static final int COL_NOM = 7;
    private static final int COL_PRENOM = 8;
    private static final int COL_FILIERE = 9;

    private static final String ASPOSE_WATERMARK = "Evaluation Only";
    private static final String HEADER_ID = "ID";

    @Autowired
    private PVDAOImpl pvDao; // Injected by Spring

    public PVMetierImpl() {
        // Empty constructor for Spring
    }

    @Override
    public void PDFTableExtractor(String chemin, String fileNamePDF) throws IOException {
        String cheminPdf = chemin + File.separator + fileNamePDF;
        String cheminExcel = chemin + File.separator + fileNamePDF + ".xlsx";

        pdfToExcelWithTabula(cheminPdf, cheminExcel);
        lireFichier(cheminExcel);

        Path path = Paths.get(cheminExcel);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public void generateDoc(String chemin) throws IOException {
        String inputPath = chemin + File.separator + "WEB-INF" + File.separator + "template" + File.separator + "Fiche_Evaluation_PFE_NomEtudiant_Prénom.docx";
        List<PV> lignes = pvDao.findAll();

        for(PV ligne : lignes) {
            String coucheTdia = "☐", coucheId = "☐", coucheGi = "☐";
            Path path = Paths.get(chemin + File.separator + "upload" + File.separator + "PVs" + File.separator + ligne.getEncadrant());
            String outputPath = path + File.separator + "Fiche_Evaluation_PFE_" + ligne.getNom() + "_" + ligne.getPrenom() + ".docx";

            try (FileInputStream fis = new FileInputStream(inputPath);
                 XWPFDocument document = new XWPFDocument(fis)) {

                Files.createDirectories(path);

                remplacerTexteDansParagraphes(document.getParagraphs(), "${NOM}", ligne.getNom() + " " + ligne.getPrenom(), false);
                remplacerTexteDansParagraphes(document.getParagraphs(), "${DATE}", ligne.getDate(), false);

                if(ligne.getFiliere().toLowerCase().equals("tdia")) coucheTdia = "☑";
                else if(ligne.getFiliere().toLowerCase().equals("gi")) coucheGi = "☑";
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
    }

    private void remplacerTexteDansParagraphes(List<XWPFParagraph> paragraphes, String cible, String remplacement, boolean bold) {
        for (XWPFParagraph paragraph : paragraphes) {
            String texteParagraphe = paragraph.getText();
            if (texteParagraphe != null && texteParagraphe.contains(cible)) {
                if(remplacement == null) remplacement = " ";
                String nouveauTexte = texteParagraphe.replace(cible, remplacement);
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

    private void pdfToExcelWithTabula(String cheminPdf, String cheminExcel) {
        try (Workbook workbook = new XSSFWorkbook();
             PDDocument pdfDocument = PDDocument.load(new File(cheminPdf))) {
            Sheet sheet = workbook.createSheet("Planning PFE");
            int numeroLigneExcel = 0;
            ObjectExtractor extracteur = new ObjectExtractor(pdfDocument);
            SpreadsheetExtractionAlgorithm algorithmeExtraction = new SpreadsheetExtractionAlgorithm();

            for (int p = 1; p <= pdfDocument.getNumberOfPages(); p++) {
                Page page = extracteur.extract(p);
                List<Table> tableaux = algorithmeExtraction.extract(page);
                for (Table tableau : tableaux) {
                    for (List<RectangularTextContainer> lignePdf : tableau.getRows()) {
                        Row ligneExcel = sheet.createRow(numeroLigneExcel++);
                        int numeroColonneExcel = 0;
                        for (RectangularTextContainer cellulePdf : lignePdf) {
                            Cell celluleExcel = ligneExcel.createCell(numeroColonneExcel++);
                            String texte = cellulePdf.getText().replace("\r", " ").replace("\n", " ");
                            celluleExcel.setCellValue(texte);
                        }
                    }
                }
            }
            try (FileOutputStream fileOut = new FileOutputStream(cheminExcel)) {
                workbook.write(fileOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void lireFichier(String cheminFichierExcel) throws IOException {
        try (FileInputStream fis = new FileInputStream(cheminFichierExcel); Workbook workbook = new XSSFWorkbook(fis)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                lireFeuille(workbook.getSheetAt(i));
            }
        }
    }

    private void lireFeuille(Sheet sheet) {
        PV courant = null;
        for (Row row : sheet) {
            String cellId = getCellValue(row, COL_ID);
            String nom = getCellValue(row, COL_NOM);
            String prenom = getCellValue(row, COL_PRENOM);
            String filiere = getCellValue(row, COL_FILIERE);
            String encad = getCellValue(row, COL_ENCADRANT);
            String jury1 = getCellValue(row, COL_JURY1);
            String jury2 = getCellValue(row, COL_JURY2);
            String date = getCellValue(row, COL_DATE);

            if (isBlank(cellId) && isBlank(nom)) continue;
            if (cellId != null && (cellId.contains(ASPOSE_WATERMARK) || cellId.equals(HEADER_ID))) continue;

            boolean hasId = !isBlank(cellId) && isNumeric(cellId);
            boolean hasNom = !isBlank(nom);

            if (hasId) {
                if (courant != null && isRecordValide(courant)) pvDao.save(courant);
                courant = new PV(nom, prenom, filiere, encad, jury1, jury2, date);
            } else if (hasNom && courant != null) {
                if (isBlank(courant.getNom())) courant.setNom(nom);
                if (isBlank(courant.getPrenom())) courant.setPrenom(prenom);
                if (isBlank(courant.getFiliere())) courant.setFiliere(filiere);
                if (isBlank(courant.getEncadrant())) courant.setEncadrant(encad);
                if (isBlank(courant.getJury1())) courant.setJury1(jury1);
                if (isBlank(courant.getJury2())) courant.setJury2(jury2);
                if (isBlank(courant.getDate())) courant.setDate(date);

                if (isRecordValide(courant)) {
                    pvDao.save(courant);
                    courant = null;
                }
            }
        }
        if (courant != null && isRecordValide(courant)) pvDao.save(courant);
    }

    private String getCellValue(Row row, int colIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private boolean isNumeric(String s) {
        try { Double.parseDouble(s); return true; } catch (NumberFormatException e) { return false; }
    }

    private boolean isRecordValide(PV s) { return !isBlank(s.getNom()) && !isBlank(s.getEncadrant()); }
}