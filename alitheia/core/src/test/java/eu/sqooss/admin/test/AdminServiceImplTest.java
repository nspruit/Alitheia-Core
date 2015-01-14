package eu.sqooss.admin.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.impl.service.admin.AdminServiceImpl.ActionContainer;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.AdminAction.AdminActionStatus;
import eu.sqooss.service.admin.actions.RunTimeInfo;

public class AdminServiceImplTest {

    private AdminServiceImpl impl;
    private long failid;
    private long successid;

    @Before
    public void setUp() {
        impl = new AdminServiceImpl();
    }
    
    private void setUpRunTimeInfo() {
    	RunTimeInfo rti = new RunTimeInfo();
        impl.registerAdminAction(rti.mnemonic(), RunTimeInfo.class);
    }
    
    private void setUpFailingAction() {
    	FailingAction fa = new FailingAction();
        impl.registerAdminAction(fa.mnemonic(), FailingAction.class);
    }
    
    private void setUpSucceedingAction() {
    	SucceedingAction su = new SucceedingAction();
        impl.registerAdminAction(su.mnemonic(), SucceedingAction.class);
    }

    @Test
    public void testAdminServiceImpl() {
        assertNotNull(impl);
    }

    @Test
    public void testRegisterAdminAction() {
        setUpRunTimeInfo();
        setUpFailingAction();
        setUpSucceedingAction();
        assertEquals(3, impl.getAdminActions().size());
    }

    @Test
    public void testGetAdminActions() {
    	setUpRunTimeInfo();
    	setUpFailingAction();
    	setUpSucceedingAction();
    	
        Set<AdminAction> actions = impl.getAdminActions();
        for (AdminAction aa : actions)
            assertNotNull (aa);
    }
    
    @Test
    public void testCreate() {
    	setUpFailingAction();
    	
        AdminAction fail = impl.create("blah");
        assertNull(fail);

        fail = impl.create("fail");
        assertNotNull(fail);
        ActionContainer ac = impl.liveactions().get(1L);
        assertNotNull(ac);
        assertEquals(-1, ac.end);

        assertEquals(AdminActionStatus.CREATED, fail.status());
        assertNull(fail.errors());
        assertNull(fail.results());
        failid = fail.id();
    }
    
    @Test
    public void testExecute() {
    	setUpSucceedingAction();
    	setUpFailingAction();
    	
        AdminAction success = impl.create("win");
        assertNotNull(success);
        impl.execute(success);
        
        assertNull(success.errors());
        assertEquals("#win", success.results().get("1"));
        assertEquals(AdminActionStatus.FINISHED, success.status());
        successid = success.id();
        
        AdminAction fail = impl.create("fail");
        assertNotNull(fail);
        impl.execute(fail);
        
        assertNull(fail.results());
        assertEquals("#fail", fail.errors().get("1"));
        assertEquals(AdminActionStatus.ERROR, fail.status());
        failid = fail.id();
    }
    
    @Test
    public void testShow() {
    	testExecute();
        AdminAction aa = impl.show(failid);
        assertNotNull(aa);
        
        aa = impl.show(successid);
        assertNotNull(aa);
    }
    
    @Test
    public void testGC() {
    	testExecute();
        try {
            Thread.sleep (300);
        } catch (InterruptedException e) {}
        int collected = impl.gc(1);
        
        assertEquals(collected, 2);
        
        AdminAction aa = impl.show(failid);
        assertNull(aa);
    }
}
