package ma.ensah.pfe_matcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Professor {
    private String id;
    private String lastname;
    private String firstname;
    private String department;
    private int maxCapacity;
    private int currentLoad = 0;
}