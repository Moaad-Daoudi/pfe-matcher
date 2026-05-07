package services;

import java.io.FileNotFoundException;

import com.aspose.pdf.internal.imaging.internal.Exceptions.IO.IOException;

public interface PVMetier {
	void PDFTableExtractor(String chemin, String fileName) throws IOException, FileNotFoundException, java.io.IOException;

	void generateDoc(String chemin) throws FileNotFoundException, java.io.IOException;
}
