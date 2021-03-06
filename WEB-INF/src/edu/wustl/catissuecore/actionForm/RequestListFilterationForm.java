/**
 * <p>Title: RequestListFilterationForm Class>
 * <p>Description:	This class contains attributes to display on RequestListAdministratorView.jsp Page</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Ashish Gupta
 * @version 1.00
 * Created on Oct 04,2006
 */

package edu.wustl.catissuecore.actionForm;

import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.domain.AbstractDomainObject;

/**
 * @author renuka_bajpai
 *
 */
public class RequestListFilterationForm extends AbstractActionForm
{

	/**
	 * The autogenerated form serial version Id.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The number of order requests with status 'New'.
	 */

	private int newRequests = 0;
	/**
	 * The number of order requests with status 'Pending'.
	 */
	private int pendingRequests = 0;
	/**
	 * The parameter to filter the requests.
	 */
	private String requestStatusSelected = "New";

	/**
	 * @return requestStatusSelected The parameter to filter the requests.
	 */
	public String getRequestStatusSelected()
	{
		return this.requestStatusSelected;
	}

	/**
	 * @param requestStatusSelected The parameter to filter the requests.
	 */
	public void setRequestStatusSelected(String requestStatusSelected)
	{
		this.requestStatusSelected = requestStatusSelected;
	}

	/**
	 * @return newRequests The number of order requests with status 'New'.
	 */
	public int getNewRequests()
	{
		return this.newRequests;
	}

	/**
	 * @param newRequests The number of order requests with status 'New'.
	 */
	public void setNewRequests(int newRequests)
	{
		this.newRequests = newRequests;
	}

	/**
	 * @return pendingRequests The number of order requests with status 'Pending'.
	 */
	public int getPendingRequests()
	{
		return this.pendingRequests;
	}

	/**
	 * @param pendingRequests The number of order requests with status 'Pending'.
	 */
	public void setPendingRequests(int pendingRequests)
	{
		this.pendingRequests = pendingRequests;
	}

	/**
	 * @return int Form Id
	 */
	@Override
	public int getFormId()
	{
		return Constants.REQUEST_LIST_FILTERATION_FORM_ID;
	}

	/**
	 * @param 
	 */
	@Override
	protected void reset()
	{

	}

	/**
	 * @param abstractDomain object
	 */
	public void setAllValues(AbstractDomainObject abstractDomain)
	{

	}

	@Override
	public void setAddNewObjectIdentifier(String arg0, Long arg1)
	{
		// TODO Auto-generated method stub

	}

}
