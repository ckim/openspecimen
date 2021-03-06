
package edu.wustl.catissuecore.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.ProtocolEventDetailsForm;
import edu.wustl.catissuecore.bean.CollectionProtocolEventBean;
import edu.wustl.catissuecore.bean.SpecimenRequirementBean;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;

/**
 * This class is for Support delete feature from CP.
 *
 * @author virender_mehta
 */
public class DeleteNodeFromCPAction extends BaseAction
{

	/**
	 * Overrides the executeSecureAction method of SecureAction class.
	 *
	 * @param mapping
	 *            object of ActionMapping
	 * @param form
	 *            object of ActionForm
	 * @param request
	 *            object of HttpServletRequest
	 * @param response
	 *            object of HttpServletResponse
	 * @throws Exception
	 *             generic exception
	 * @return ActionForward : ActionForward
	 */
	@Override
	protected ActionForward executeAction(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		final ProtocolEventDetailsForm protocolEventDetailsForm = (ProtocolEventDetailsForm) form;
		final HttpSession session = request.getSession();
		final String nodeId = request.getParameter(Constants.TREE_NODE_ID);
		Map collectionProtocolEventMap = null;
		if (session
				.getAttribute(Constants.COLLECTION_PROTOCOL_EVENT_SESSION_MAP) != null)
		{
			collectionProtocolEventMap = (LinkedHashMap) session
					.getAttribute(Constants.COLLECTION_PROTOCOL_EVENT_SESSION_MAP);
		}
		String eventMapKey = null;
		String parentReqMapKey = null;
		String forwardTo = null;
		String parentNodeId = (String) request.getParameter("parentNodeId");
		if ("specimenRequirement".equals(request.getParameter("pageOf")))
		{
			final String collectionProtocolEventKey = request
					.getParameter(Constants.EVENT_KEY);

			final StringTokenizer st = new StringTokenizer(
					collectionProtocolEventKey, "_");
			if (st.hasMoreTokens())
			{
				eventMapKey = st.nextToken();
			}
			final CollectionProtocolEventBean collectionProtocolEventBean = (CollectionProtocolEventBean) collectionProtocolEventMap
					.get(eventMapKey);
			Map specimenReqMap = collectionProtocolEventBean
					.getSpecimenRequirementbeanMap();
			if (specimenReqMap.containsKey(collectionProtocolEventKey))
			{
				specimenReqMap.remove(collectionProtocolEventKey);
				//parentReqMapKey = parentNodeId.substring((parentNodeId.indexOf("_")+1));
				parentReqMapKey = eventMapKey;
				forwardTo = Constants.PAGE_OF_DEFINE_EVENTS;
			}
			else
			{
				parentReqMapKey = parentNodeId.substring((parentNodeId
						.indexOf("_") + 1));
				char specimenTypeInitial = this
						.getSpecimenType(collectionProtocolEventKey);
				String mapKey = null;
				final StringTokenizer strtkn = new StringTokenizer(
						collectionProtocolEventKey, "_");
				final int tokenCount = strtkn.countTokens();
				if (tokenCount > 3)
				{
					mapKey = strtkn.nextToken() + "_" + strtkn.nextToken();
					int keyCount = 2;
					String specimenType = null;
					while (strtkn.hasMoreTokens())
					{
						specimenType = strtkn.nextToken();
						specimenTypeInitial = specimenType.charAt(0);
						if (keyCount < tokenCount - 1)
						{
							SpecimenRequirementBean spReqBean = (SpecimenRequirementBean) specimenReqMap
									.get(mapKey);
							if ('A' == specimenTypeInitial)
							{
								specimenReqMap = spReqBean
										.getAliquotSpecimenCollection();
							}
							else
							{
								specimenReqMap = spReqBean
										.getDeriveSpecimenCollection();
							}
							mapKey = mapKey + "_" + specimenType;
							keyCount++;
						}
					}
				}
				//final char specimenType = this.getSpecimenType(collectionProtocolEventKey);
				this.removeChildSpecimen(specimenReqMap, parentReqMapKey,
						collectionProtocolEventKey, st, specimenTypeInitial);
				forwardTo = Constants.PAGE_OF_SPECIMEN_REQUIREMENT;
			}
			request.getSession().setAttribute(Constants.PARENT_NODE_ID,
					parentReqMapKey);
			request.getSession().setAttribute(Constants.TREE_NODE_ID,
					parentNodeId);
			request.getSession().setAttribute("listKey", eventMapKey);
		}
		else
		{

			eventMapKey = protocolEventDetailsForm
					.getCollectionProtocolEventkey();
			collectionProtocolEventMap.remove(eventMapKey);
			forwardTo = Constants.PAGE_OF_COLLECTION_PROTOCOL;
			request.getSession().setAttribute(Constants.TREE_NODE_ID,
					parentNodeId);
		}
		request.setAttribute("deleteNode", nodeId);
		return mapping.findForward(forwardTo);
	}

	/**
	 * This method is for removing child specimen.
	 *
	 * @param specimenReqMap
	 *            Requirement Specimen map
	 * @param parentReqMapKey
	 *            Map key
	 * @param collectionProtocolEventKey
	 *            Event Key
	 * @param st
	 *            String toknizer
	 * @param specimenType
	 *            type of specimen
	 */
	private void removeChildSpecimen(Map specimenReqMap,
			String parentReqMapKey, String collectionProtocolEventKey,
			StringTokenizer st, char specimenType)
	{
		final SpecimenRequirementBean spReqBean = (SpecimenRequirementBean) specimenReqMap
				.get(parentReqMapKey);
		if (spReqBean != null)
		{
			Map childMap = null;
			if (specimenType == 'A')
			{
				childMap = this.removeAliquot(collectionProtocolEventKey,
						spReqBean);
				if (childMap != null)
				{
					spReqBean.setNoOfAliquots(String.valueOf(childMap.size()));
				}
			}
			else
			{
				childMap = this.removeDerive(collectionProtocolEventKey,
						spReqBean);
				if (childMap != null)
				{
					spReqBean.setNoOfDeriveSpecimen(childMap.size());
				}
			}
			if (childMap != null && !childMap.isEmpty())
			{
				parentReqMapKey = parentReqMapKey + "_" + st.nextToken();
				this.removeChildSpecimen(childMap, parentReqMapKey,
						collectionProtocolEventKey, st, specimenType);
			}
		}
	}

	/**
	 * @param specimenKey
	 *            Specimen key.
	 * @return child specimen's first character
	 */
	private char getSpecimenType(String specimenKey)
	{
		String specimenType = null;
		final StringTokenizer st = new StringTokenizer(specimenKey, "_");
		while (st.hasMoreTokens())
		{
			specimenType = st.nextToken();
		}
		return specimenType.charAt(0);
	}

	/**
	 * Remove aliquot specimen.
	 *
	 * @param collectionProtocolEventKey
	 *            Event key
	 * @param spReqBean
	 *            Requirement bean
	 * @return aliquotMap map of aliquot specimen
	 */
	private Map removeAliquot(String collectionProtocolEventKey,
			SpecimenRequirementBean spReqBean)
	{

		Map aliquotMap = spReqBean.getAliquotSpecimenCollection();
		if (aliquotMap.containsKey(collectionProtocolEventKey))
		{
			aliquotMap.remove(collectionProtocolEventKey);
			aliquotMap = null;
		}
		return aliquotMap;
	}

	/**
	 * Remove derive specimen.
	 *
	 * @param collectionProtocolEventKey
	 *            : collectionProtocolEventKey
	 * @param spReqBean
	 *            : spReqBean
	 * @return Map : Map
	 */
	private Map removeDerive(String collectionProtocolEventKey,
			SpecimenRequirementBean spReqBean)
	{

		Map driveMap = spReqBean.getDeriveSpecimenCollection();
		if (driveMap.containsKey(collectionProtocolEventKey))
		{
			driveMap.remove(collectionProtocolEventKey);
		}
		return driveMap;
	}
}
