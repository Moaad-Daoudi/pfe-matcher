package dao;

import java.util.List;

public interface PVDAO {
	boolean save(PV pv);

	boolean delete(long id);

	boolean update(PV pv);
	
	PV findById(long id);

	List<PV> findAll();
}
