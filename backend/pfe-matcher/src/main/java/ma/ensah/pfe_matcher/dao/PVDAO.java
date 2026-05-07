package ma.ensah.pfe_matcher.dao;

import ma.ensah.pfe_matcher.model.PV;

import java.util.List;

public interface PVDAO {
    boolean save(PV pv);

    boolean delete(long id);

    boolean update(PV pv);

    PV findById(long id);

    List<PV> findAll();
}
