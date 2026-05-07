package ma.ensah.pfe_matcher.service;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface PVMetier {
    void PDFTableExtractor(String chemin, String fileName) throws IOException;
    void generateDoc(String chemin) throws IOException;
}