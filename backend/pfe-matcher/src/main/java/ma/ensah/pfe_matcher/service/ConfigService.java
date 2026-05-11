package ma.ensah.pfe_matcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.*;

@Service
public class ConfigService {
    private JsonNode root;

    @PostConstruct
    public void init() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource("config.json").getInputStream()) {
            root = mapper.readTree(is);
        }
    }

    public List<String> getAllowedDepartments() {
        List<String> depts = new ArrayList<>();
        if (root != null && root.has("allowedDepartments")) {
            for (JsonNode node : root.get("allowedDepartments")) {
                depts.add(node.asText());
            }
        }
        return depts;
    }

    public List<String> getAllFiliereCodes() {
        List<String> codes = new ArrayList<>();
        if (root != null && root.has("departements")) {
            for (JsonNode dept : root.get("departements")) {
                for (JsonNode fil : dept.get("filieres")) {
                    if (fil.has("code")) {
                        String code = fil.get("code").asText();
                        if (!codes.contains(code)) {
                            codes.add(code);
                        }
                    }
                }
            }
        }
        return codes;
    }

    public String getCouleur(String inputField) {
        if (inputField == null) return "#FFFFFF";
        String normalized = inputField.trim().toUpperCase();

        for (JsonNode dept : root.get("departements")) {
            for (JsonNode fil : dept.get("filieres")) {
                for (JsonNode alias : fil.get("aliases")) {
                    if (normalized.contains(alias.asText().toUpperCase())) {
                        return fil.get("couleur").asText();
                    }
                }
            }
        }
        return "#FFFFFF";
    }

    public String getMappedField(String inputField) {
        if (inputField == null) return "UNKNOWN";
        String normalized = inputField.trim().toUpperCase();

        for (JsonNode dept : root.get("departements")) {
            for (JsonNode fil : dept.get("filieres")) {
                for (JsonNode alias : fil.get("aliases")) {
                    if (normalized.contains(alias.asText().toUpperCase())) {
                        return fil.get("code").asText();
                    }
                }
            }
        }
        return normalized;
    }

    public String getCanonicalHeader(String input) {
        if (input == null) return null;
        String cleanInput = input.trim().toUpperCase();
        JsonNode mappings = root.get("headerMappings");
        
        var fields = mappings.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            for (JsonNode alias : entry.getValue()) {
                if (alias.asText().equalsIgnoreCase(cleanInput)) return entry.getKey();
            }
        }
        return null;
    }
}