package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import com.aspose.pdf.*;
import com.aspose.pdf.internal.imaging.internal.Exceptions.IO.IOException;

import dao.*;

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

	// Textes à ignorer 
	private static final String ASPOSE_WATERMARK = "Evaluation Only";
	private static final String HEADER_ID = "ID";

	public static PVMetierImpl instance;
	private PVDAO pvDao;

	private PVMetierImpl() {
		pvDao = new PVImpl();
		((PVImpl) pvDao).init();
	}

	public static PVMetierImpl getInstance() {
		if (instance == null) {
			instance = new PVMetierImpl();
		}
		return instance;
	}

	@Override
	public void PDFTableExtractor(String chemin, String fileNamePDF) throws IOException, FileNotFoundException, java.io.IOException {

		String cheminPdf = chemin + File.separator + fileNamePDF;
		String cheminExcel = chemin + File.separator + fileNamePDF + ".xlsx";

		pdfToExcelWithTabula(cheminPdf, cheminExcel);

		lireFichier(cheminExcel);
		
		// Convertir la String en objet Path
        Path path = Paths.get(cheminExcel);
        
		try {
            // Supprimer le fichier excel s'il existe
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Gérer les erreurs (fichier utilisé, pas de permissions, etc.)
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
	}

	@Override
	public void generateDoc(String chemin) throws FileNotFoundException, java.io.IOException {
		
		// Chemins vers le template d'origine et le fichier de sortie
        String inputPath = chemin + File.separator + "WEB-INF" + File.separator + "template" + File.separator + "Fiche_Evaluation_PFE_NomEtudiant_Prénom.docx"; 
        
        List<PV> lignes = pvDao.findAll();
        
        for(PV ligne : lignes) {
        	String coucheTdia = "☐", coucheId = "☐", coucheGi = "☐";
        	
        	Path path = Paths.get(chemin + File.separator + "upload" + File.separator + "PVs" + File.separator + ligne.getEncadrant());
            String outputPath = path + File.separator + "Fiche_Evaluation_PFE_" + ligne.getNom() + "_" + ligne.getPrenom() + ".docx";
            
            try (FileInputStream fis = new FileInputStream(inputPath);
                 XWPFDocument document = new XWPFDocument(fis)) {

            	// Crée le dossier s'il n'existe pas, ignore silencieusement s'il est déjà là
                Files.createDirectories(path);
                
                // 1. Remplir le nom et la date (dans les paragraphes normaux)
                remplacerTexteDansParagraphes(document.getParagraphs(), "${NOM}", ligne.getNom() + " " + ligne.getPrenom(), false);
                remplacerTexteDansParagraphes(document.getParagraphs(), "${DATE}", ligne.getDate(), false);

                // 2. Cocher la filière (On met ☑ pour la filière choisie et ☐ pour les autres)
                if(ligne.getFiliere().toLowerCase().equals("tdia")) coucheTdia = "☑";
                else if(ligne.getFiliere().toLowerCase().equals("gi")) coucheGi = "☑";
                else coucheId = "☑";
                
                remplacerTexteDansParagraphes(document.getParagraphs(), "${FILIERE_ID}", coucheId, true); // Coche Ingénierie des Données
                remplacerTexteDansParagraphes(document.getParagraphs(), "${FILIERE_GI}", coucheGi, true); // Laisse vide
                remplacerTexteDansParagraphes(document.getParagraphs(), "${FILIERE_TDIA}", coucheTdia, true); // Laisse vide

                // 3. Remplir les membres du jury (Généralement situés dans des tableaux)
                for (XWPFTable table : document.getTables()) {
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            remplacerTexteDansParagraphes(cell.getParagraphs(), "${JURY_PRESIDENT}", ligne.getEncadrant(), false);
                            remplacerTexteDansParagraphes(cell.getParagraphs(), "${JURY_RAPP1}", ligne.getJury1(), false);
                            remplacerTexteDansParagraphes(cell.getParagraphs(), "${JURY_RAPP2}", ligne.getJury2(), false);
                        }
                    }
                }

                // Sauvegarder le nouveau document
                try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                    document.write(fos);
                }

            } catch (IOException e) {
                System.err.println("Erreur lors de la manipulation du fichier : " + e.getMessage());
                e.printStackTrace();
            }
        }
	}
	
	/**
     * Méthode utilitaire pour chercher et remplacer du texte dans une liste de paragraphes.
     * Cette méthode reconstruit le paragraphe pour éviter les problèmes de découpage (XWPFRun).
     */
    private static void remplacerTexteDansParagraphes(List<XWPFParagraph> paragraphes, String cible, String remplacement, boolean bool) {
        for (XWPFParagraph paragraph : paragraphes) {
            String texteParagraphe = paragraph.getText();
            if (texteParagraphe != null && texteParagraphe.contains(cible)) {
                // Remplacer le texte
            	if(remplacement == null) remplacement = " ";
                String nouveauTexte = texteParagraphe.replace(cible, remplacement);
                
                // Effacer les anciens 'runs' (morceaux de texte formatés)
                for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                
                // Créer un nouveau run avec le texte mis à jour
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(nouveauTexte);
                // Définir la police ici si elle se perd
                newRun.setFontFamily("Times New Roman");
                newRun.setFontSize(12);
                newRun.setBold(bool);
            }
        }
    }

    private void pdfToExcelWithTabula(String cheminPdf, String cheminExcel) {
    	// Créer le document Excel
        try (Workbook workbook = new XSSFWorkbook(); 
             PDDocument pdfDocument = PDDocument.load(new File(cheminPdf))) {

            Sheet sheet = workbook.createSheet("Planning PFE");
            int numeroLigneExcel = 0;

            // Instancier l'extracteur Tabula
            ObjectExtractor extracteur = new ObjectExtractor(pdfDocument);
            
            // L'algorithme "Spreadsheet" est parfait pour les tableaux avec des lignes bien tracées
            SpreadsheetExtractionAlgorithm algorithmeExtraction = new SpreadsheetExtractionAlgorithm();

            // Parcourir toutes les pages du PDF (de la page 1 jusqu'à la fin)
            for (int p = 1; p <= pdfDocument.getNumberOfPages(); p++) {
                Page page = extracteur.extract(p);
                
                // Extraire les tableaux de la page
                List<Table> tableaux = algorithmeExtraction.extract(page);

                for (Table tableau : tableaux) {
                    for (List<RectangularTextContainer> lignePdf : tableau.getRows()) {
                        Row ligneExcel = sheet.createRow(numeroLigneExcel++);
                        
                        int numeroColonneExcel = 0;
                        for (RectangularTextContainer cellulePdf : lignePdf) {
                            Cell celluleExcel = ligneExcel.createCell(numeroColonneExcel++);
                            // Nettoyer le texte (les retours à la ligne dans le PDF)
                            String texte = cellulePdf.getText().replace("\r", " ").replace("\n", " ");
                            celluleExcel.setCellValue(texte);
                        }
                    }
                }
            }

            // Sauvegarder le fichier Excel
            try (FileOutputStream fileOut = new FileOutputStream(cheminExcel)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
//	private void pdfToExcel(String cheminPdf, String cheminExcel) {
//
//		try {
//			// 1. Charger le document PDF
//			Document pdfDocument = new Document(cheminPdf);
//
//			// 2. Instancier les options de sauvegarde Excel
//			ExcelSaveOptions excelOptions = new ExcelSaveOptions();
//
//			// 3. Minimiser le nombre de feuilles de calcul dans l'Excel
//			excelOptions.setMinimizeTheNumberOfWorksheets(true);
//
//			// (Optionnel) Spécifier explicitement le format XLSX
//		    excelOptions.setFormat(ExcelSaveOptions.ExcelFormat.XLSX);
//
//		    // 4. Enregistrer le résultat au format Excel
//		    pdfDocument.save(cheminExcel, excelOptions);
//
//			pdfDocument.close();
//
//		} catch (Exception e) {
//			System.err.println("Erreur lors de la conversion : " + e.getMessage());
//			e.printStackTrace();
//		}
//	}

	/**
	 * Lit toutes les feuilles du classeur et retourne la liste des soutenances.
	 * 
	 * @throws java.io.IOException
	 * @throws FileNotFoundException
	 */
	private void lireFichier(String cheminFichierExcel) throws IOException, FileNotFoundException, java.io.IOException {

		try (FileInputStream fis = new FileInputStream(cheminFichierExcel); Workbook workbook = new XSSFWorkbook(fis)) {

			int nbFeuilles = workbook.getNumberOfSheets();

			for (int i = 0; i < nbFeuilles; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				lireFeuille(sheet);
			}
		}
	}

	/**
	 * Lit une feuille Excel et extrait les enregistrements valides.
	 *
	 * Gestion des cellules fusionnées : Certaines lignes sont divisées sur deux
	 * rangées (cellules fusionnées dans le PDF d'origine). On utilise un buffer «
	 * courant » : si une ligne contient un ID, on démarre un nouveau record ; si
	 * elle ne contient pas d'ID mais contient un nom d'étudiant, on complète le
	 * record précédent.
	 */
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

			// Ignorer les lignes d'en-tête, filigrane, ou vides
			if (isBlank(cellId) && isBlank(nom))
				continue;
			if (cellId != null && (cellId.contains(ASPOSE_WATERMARK) || cellId.equals(HEADER_ID)))
				continue;
			if (nom != null && nom.contains(ASPOSE_WATERMARK))
				continue;

			boolean hasId = !isBlank(cellId) && isNumeric(cellId);
			boolean hasNom = !isBlank(nom);

			if (hasId) {
				// Nouvelle ligne principale → sauvegarder le record précédent si complet
				if (courant != null && isRecordValide(courant)) {
					pvDao.save(courant);
				}
				courant = new PV(nom, prenom, filiere, encad, jury1, jury2, date);
			} else if (hasNom && courant != null) {
				// Ligne de continuation (cellules fusionnées) : compléter l'étudiant manquant
				if (isBlank(courant.getNom()))
					courant.setNom(nom);
				if (isBlank(courant.getPrenom()))
					courant.setPrenom(prenom);
				if (isBlank(courant.getFiliere()))
					courant.setFiliere(filiere);
				if (isBlank(courant.getEncadrant()) && !isBlank(encad))
					courant.setEncadrant(encad);
				if (isBlank(courant.getJury1()) && !isBlank(jury1))
					courant.setJury1(jury1);
				if (isBlank(courant.getJury2()) && !isBlank(jury2))
					courant.setJury2(jury2);
				if (isBlank(courant.getDate()) && !isBlank(date))
					courant.setDate(date);

				// Une fois l'étudiant renseigné, sauvegarder et réinitialiser
				if (isRecordValide(courant)) {
					pvDao.save(courant);
					courant = null;
				}
			} else if (hasId && !hasNom && courant == null) {
				// Ligne avec ID mais sans étudiant (données étalées sur la ligne suivante)
				courant = new PV(null, null, null, encad, jury1, jury2, date);
			}
		}

		// Sauvegarder le dernier record 
		if (courant != null && isRecordValide(courant)) {
			pvDao.save(courant);
		}
	}

	/**
	 * Lit la valeur d'une cellule sous forme de String (gère texte, numérique,
	 * date).
	 */
	private String getCellValue(Row row, int colIndex) {
		if (row == null)
			return null;
		Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		if (cell == null)
			return null;

		switch (cell.getCellType()) {
		case STRING:
			String s = cell.getStringCellValue().trim();
			return s.isEmpty() ? null : s;
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				// Formater la date en dd/MM/yyyy
				java.util.Date d = cell.getDateCellValue();
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
				return sdf.format(d);
			}
			// Nombre entier ou décimal
			double val = cell.getNumericCellValue();
			if (val == Math.floor(val)) {
				return String.valueOf((long) val);
			}
			return String.valueOf(val);
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			try {
				return cell.getStringCellValue().trim();
			} catch (Exception e) {
				return String.valueOf(cell.getNumericCellValue());
			}
		default:
			return null;
		}
	}

	private boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

	private boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/** Un record est valide s'il a au minimum un nom d'étudiant et un encadrant. */
	private boolean isRecordValide(PV s) {
		return !isBlank(s.getNom()) && !isBlank(s.getEncadrant());
	}

}
