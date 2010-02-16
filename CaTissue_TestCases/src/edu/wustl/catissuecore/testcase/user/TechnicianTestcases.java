package edu.wustl.catissuecore.testcase.user;

import java.util.Collection;
import java.util.HashSet;

import edu.wustl.catissuecore.domain.Site;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.testcase.CaTissueSuiteBaseTest;
import edu.wustl.catissuecore.testcase.bizlogic.BaseTestCaseUtility;
import edu.wustl.catissuecore.testcase.util.CaTissueSuiteTestUtil;
import edu.wustl.catissuecore.testcase.util.TestCaseUtility;
import edu.wustl.catissuecore.testcase.util.UniqueKeyGeneratorUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.util.logger.Logger;


public class TechnicianTestcases extends CaTissueSuiteBaseTest
{
	/**
	 * Negative Test Case.
	 * Test logout.
	 */
	
	public void testLogoutSupervisorAndLoginAsSuperAdmin()
	{
		setRequestPathInfo("/Logout") ;
		actionPerform();		
		verifyForward("success");
		CaTissueSuiteTestUtil.USER_SESSION_DATA_BEAN = null;
		
		setRequestPathInfo("/Login") ;
		addRequestParameter("loginName", "admin@admin.com");
		addRequestParameter("password", "Test123");
		logger.info("start in login");
		actionPerform();
		logger.info("Login: "+getActualForward());
		verifyForward("pageOfNonWashU");

		SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
		assertEquals("user name should be equal to logged in username","admin@admin.com",bean.getUserName());
		CaTissueSuiteTestUtil.USER_SESSION_DATA_BEAN=bean;
		verifyNoActionErrors();
		logger.info("end in login");
	}
	
	public void testTechnicianAdd()
	{
		try
		 {
			User user = BaseTestCaseUtility.initUser();
			user.setRoleId( "3" );
			user.setEmailAddress("Technician"+ UniqueKeyGeneratorUtil.getUniqueKey()+"@Technician.com");
			user.setLoginName(user.getEmailAddress());
			user.setPageOf(Constants.PAGE_OF_USER_ADMIN);
			Site site = (Site)TestCaseUtility.getNameObjectMap("Site");
			Collection<Site> siteCollection = new HashSet<Site>();
			siteCollection.add(site);
			user.setSiteCollection(siteCollection);
			user = (User)appService.createObject(user);			
			Logger.out.info("Technician User added successfully");
			assertTrue("Technician User added successfully", true);
		    user.setNewPassword("Login123");
		   	user = (User)appService.updateObject(user);	
		   	TestCaseUtility.setNameObjectMap( "Technician", user );
			
		 }
		 catch(Exception e)
		 {
			 System.out.println("Technician.testTechnicianAdd()"+ e.getMessage());
			 e.printStackTrace();
			 assertFalse("Could not add a Technician into System", true);
		 }
		
	}
	/**
	 * change Supervisor password
	 */
	public void testTechnicianUserLoginAndChangePassword()
	{
		setRequestPathInfo("/Login");
		User user = (User)TestCaseUtility.getNameObjectMap( "Technician" );
		addRequestParameter("loginName", user.getEmailAddress());
		addRequestParameter("password", "Login123");
		actionPerform();
		String pageOf = (String) request.getAttribute(Constants.PAGE_OF);
		SessionDataBean sessionData = null;
		if(getSession().getAttribute(Constants.TEMP_SESSION_DATA) != null) 
		{
			sessionData = (SessionDataBean)getSession().getAttribute(Constants.TEMP_SESSION_DATA);
		}
		else 
		{
			sessionData = (SessionDataBean)getSession().getAttribute(Constants.SESSION_DATA);
		}
		String userId = sessionData.getUserId().toString();
		setRequestPathInfo("/UpdatePassword");		
		addRequestParameter("id",userId);
		addRequestParameter("operation", "");
		addRequestParameter("pageOf",pageOf);
		addRequestParameter("oldPassword", "Login123");
		addRequestParameter("newPassword", "Test123");
		addRequestParameter("confirmNewPassword", "Test123");
		actionPerform();
		verifyForward("success");		
		System.out.println("----"+getActualForward());
	}
	/**
	 * Test Login with Valid Login name and Password.
	 */	

	public void testTechnicianLogin()
	{
		setRequestPathInfo("/Login") ;
		User user = (User)TestCaseUtility.getNameObjectMap( "Technician" );
		addRequestParameter("loginName", user.getEmailAddress());
		addRequestParameter("password", "Test123");
		logger.info("start in login");
		actionPerform();
		logger.info("Login: "+getActualForward());
		SessionDataBean bean = (SessionDataBean)getSession().getAttribute("sessionData");
		assertEquals("user name should be equal to logged in username",user.getEmailAddress(),bean.getUserName());
		CaTissueSuiteTestUtil.USER_SESSION_DATA_BEAN=bean;
		verifyNoActionErrors();
		logger.info("end in login");
	} 
	
}