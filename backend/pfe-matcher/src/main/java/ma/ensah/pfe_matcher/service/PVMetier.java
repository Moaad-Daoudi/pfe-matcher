package ma.ensah.pfe_matcher.service;
import ma.ensah.pfe_matcher.model.Soutenance;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface PVMetier {
    void generatePVsFromSoutenances(List<Soutenance> soutenances, String chemin) throws IOException;
    void prepareForNewGeneration(String realPath);
}