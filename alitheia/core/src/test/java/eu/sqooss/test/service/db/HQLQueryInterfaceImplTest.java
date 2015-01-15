package eu.sqooss.test.service.db;

import eu.sqooss.service.db.HQLQueryInterface;

public class HQLQueryInterfaceImplTest extends HQLQueryInterfaceTest {

	@Override
	protected HQLQueryInterface getHQLQueryInterface() {
		return getDB().getHQLInterface();
	}

}
