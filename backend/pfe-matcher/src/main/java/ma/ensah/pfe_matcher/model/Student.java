package ma.ensah.pfe_matcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private String id;
    private String cne;
    private String lastname;
    private String firstname;
    private String field;
}