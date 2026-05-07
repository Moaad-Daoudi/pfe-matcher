package dao;

import java.util.ArrayList;
import java.util.List;

public class PVImpl implements PVDAO {
	private List<PV> liste = new ArrayList<PV>();
	
	public void init() {
		System.out.println("PV dao est bien fonctionnée !");
	}

	@Override
	public boolean save(PV pv) {
		pv.setId(new Long(liste.size() + 1));
		return liste.add(pv);
	}

	@Override
	public boolean delete(long id) {
		return liste.remove(findById(id));
	}

	@Override
	public boolean update(PV pv) {
		for(PV elt : liste) {
			if(elt.getId() == pv.getId()) {
				elt.setNom(pv.getNom());
				elt.setPrenom(pv.getPrenom());
				elt.setFiliere(pv.getFiliere());
				elt.setJury1(pv.getJury1());
				elt.setJury2(pv.getJury2());
				elt.setDate(pv.getDate());
				return true;
			}
		}
		return false;
	}

	@Override
	public PV findById(long id) {
		return liste.get((int) id);
	}

	@Override
	public List<PV> findAll() {
		return liste;
	}

}
