package ma.ensah.pfe_matcher.util;

import java.util.Random;

public class ColorUtil {
    public static String getCouleur(String inputField) {
        if (inputField == null) return "#FFFFFF";
        String normalized = inputField.trim().toUpperCase();

        // Random color generator for fields
        Random random = new Random(normalized.hashCode());
        // Let's generate a slightly lighter/pastel color so text is readable
        int r = random.nextInt(128) + 127; 
        int g = random.nextInt(128) + 127;
        int b = random.nextInt(128) + 127;
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
