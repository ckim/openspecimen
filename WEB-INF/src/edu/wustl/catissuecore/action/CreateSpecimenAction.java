/**
 * <p>Title: CreateSpecimenAction Class>
 * <p>Description:	CreateSpecimenAction initializes the fields in the Create Specimen page.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Aniruddha Phadnis
 * @version 1.00
 */

package edu.wustl.catissuecore.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.actionForm.CreateSpecimenForm;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.StorageContainerBizLogic;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.StorageContainer;
import edu.wustl.catissuecore.util.StorageContainerUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.common.action.SecureAction;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.bizlogic.DefaultBizLogic;
import edu.wustl.common.cde.CDE;
import edu.wustl.common.cde.CDEManager;
import edu.wustl.common.cde.PermissibleValue;
import edu.wustl.common.security.exceptions.SMException;
import edu.wustl.common.util.MapDataParser;
import edu.wustl.common.util.dbManager.DAOException;
import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.logger.Logger;

/**
 * CreateSpecimenAction initializes the fields in the Create Specimen page.
 * @author aniruddha_phadnis
 */
public class CreateSpecimenAction extends SecureAction
{

	/**
	 * Overrides the execute method of Action class.
	 */
	public ActionForward executeSecureAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		CreateSpecimenForm createForm = (CreateSpecimenForm) form;

		//List of keys used in map of ActionForm
		List key = new ArrayList();
		key.add("ExternalIdentifier:i_name");
		key.add("ExternalIdentifier:i_value");

		//boolean to indicate whether the suitable containers to be shown in dropdown 
		//is exceeding the max limit.
		String exceedingMaxLimit = "false";
		
		//Gets the map from ActionForm
		Map map = createForm.getExternalIdentifier();

		//Calling DeleteRow of BaseAction class
		MapDataParser.deleteRow(key, map, request.getParameter("status"));

		//Gets the value of the operation parameter.
		String operation = request.getParameter(Constants.OPERATION);

		//Sets the operation attribute to be used in the Add/Edit User Page. 
		request.setAttribute(Constants.OPERATION, operation);
		String virtuallyLocated = request.getParameter("virtualLocated");
		if(virtuallyLocated!=null && virtuallyLocated.equals("true"))
		{
			createForm.setVirtuallyLocated(true);
		}
        
         /**
          * Patch ID: 3835_1_16
          * See also: 1_1 to 1_5
          * Description : CreatedOn date by default should be current date.
          */ 
    
         createForm.setCreatedDate(Utility.parseDateToString(Calendar.getInstance().getTime(), Constants.DATE_PATTERN_MM_DD_YYYY));
    
        
        
		String pageOf = request.getParameter(Constants.PAGEOF);
		SessionDataBean sessionData = (SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA);
		/*
		 // ---- chetan 15-06-06 ----
		 StorageContainerBizLogic bizLogic = (StorageContainerBizLogic)BizLogicFactory.getInstance().getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
		 Map containerMap = bizLogic.getAllocatedContainerMap();
		 request.setAttribute(Constants.AVAILABLE_CONTAINER_MAP,containerMap);
		 // -------------------------
		 request.setAttribute(Constants.PAGEOF,pageOf);
		 */
		request.setAttribute(Constants.PAGEOF,pageOf);
		DefaultBizLogic dao = new DefaultBizLogic(); 
		TreeMap containerMap = new TreeMap();
		List initialValues = null;
		if (operation.equals(Constants.ADD))
		{
			//if this action bcos of delete external identifier then validation should not happen.
			if (request.getParameter("button") == null)
			{
				String parentSpecimenLabel = null;
				Long parentSpecimenID = null;
				//Bug-2784: If coming from NewSpecimen page, then only set parent specimen label.
				Map forwardToHashMap = (Map) request.getAttribute("forwardToHashMap");
				if(forwardToHashMap != null && forwardToHashMap.get("parentSpecimenId") != null && forwardToHashMap.get(Constants.SPECIMEN_LABEL) != null)
				{
					parentSpecimenID = (Long)forwardToHashMap.get("parentSpecimenId");
					parentSpecimenLabel = Utility.toString(forwardToHashMap.get(Constants.SPECIMEN_LABEL));
					request.setAttribute(Constants.PARENT_SPECIMEN_ID, parentSpecimenID);
					createForm.setParentSpecimenLabel(parentSpecimenLabel);
					createForm.setLabel("");
				}

				if(createForm.getLabel()==null || createForm.getLabel().equals(""))
				{
					/**
		        	 * Name : Virender Mehta
		             * Reviewer: Sachin Lale
		             * Description: By getting instance of AbstractSpecimenGenerator abstract class current label retrived and set.
		        	 */
					//int totalNoOfSpecimen = bizLogic.totalNoOfSpecimen(sessionData)+1;
					
					HashMap inputMap = new HashMap();
					inputMap.put(Constants.PARENT_SPECIMEN_LABEL_KEY, parentSpecimenLabel);
					inputMap.put(Constants.PARENT_SPECIMEN_ID_KEY, String.valueOf(parentSpecimenID));

					//SpecimenLabelGenerator abstractSpecimenGenerator  = SpecimenLabelGeneratorFactory.getInstance();
					//createForm.setLabel(abstractSpecimenGenerator.getNextAvailableDeriveSpecimenlabel(inputMap));
				}
				
				if (forwardToHashMap == null && ((createForm.getCheckedButton().equals("1") && createForm
						.getParentSpecimenLabel() != null && !createForm.getParentSpecimenLabel().equals(""))
						|| (createForm.getCheckedButton().equals("2") && createForm
								.getParentSpecimenBarcode() != null && !createForm.getParentSpecimenBarcode().equals(""))))
				{
					String errorString = null;
					String []columnName = new String[1];
					Object []columnValue = new String[1];

					// checks whether label or barcode is selected
					if (createForm.getCheckedButton().equals("1"))
					{
						columnName[0] = Constants.SYSTEM_LABEL; 
						columnValue[0] = createForm.getParentSpecimenLabel().trim();
						errorString = ApplicationProperties.getValue("quickEvents.specimenLabel");
					}
					else
					{
						columnName[0] = Constants.SYSTEM_BARCODE;
						columnValue[0] = createForm.getParentSpecimenBarcode().trim();
						errorString = ApplicationProperties.getValue("quickEvents.barcode");
					}
					
					String []selectColumnName={Constants.COLUMN_NAME_SCG_ID};
					String []whereColumnCondition={"="};
					List scgList = dao.retrieve(Specimen.class.getName(),selectColumnName,columnName
							,whereColumnCondition,columnValue,null ); 
					
					boolean isSpecimenExist = true;
					if (scgList != null && !scgList.isEmpty())
					{
//						Specimen sp = (Specimen) spList.get(0);
						Long scgId = (Long)scgList.get(0);
						long cpId = getCpId(dao,scgId);
						if(cpId == -1)
						{
							isSpecimenExist = false;
						}
						String spClass = createForm.getClassName();
						
						request.setAttribute(Constants.COLLECTION_PROTOCOL_ID, cpId + "");
						request.setAttribute(Constants.SPECIMEN_CLASS_NAME, spClass);
						if(virtuallyLocated!=null && virtuallyLocated.equals("false")) 
						{
							createForm.setVirtuallyLocated(false);
						}
						if(spClass!=null && createForm.getStContSelection() != Constants.RADIO_BUTTON_VIRTUALLY_LOCATED)
						{
						
							StorageContainerBizLogic scbizLogic = (StorageContainerBizLogic) BizLogicFactory
								.getInstance().getBizLogic(Constants.STORAGE_CONTAINER_FORM_ID);
							containerMap = scbizLogic.getAllocatedContaienrMapForSpecimen(cpId,
									spClass, 0,exceedingMaxLimit,sessionData,true);
							ActionErrors errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
							if (containerMap.isEmpty())
							{
								if (errors == null || errors.size() == 0)
								{
									errors = new ActionErrors();
								}
								errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
										"storageposition.not.available"));
								saveErrors(request, errors);
							}
							if (errors == null || errors.size() == 0)
							{
								initialValues = StorageContainerUtil.checkForInitialValues(containerMap);
							}
							else
							{
								String[] startingPoints = new String[3];
								startingPoints[0] = createForm.getStorageContainer();
								startingPoints[1] = createForm.getPositionDimensionOne();
								startingPoints[2] = createForm.getPositionDimensionTwo() ;
								initialValues = new ArrayList();
								initialValues.add(startingPoints);
							}
					
						}
						/**
						 * Name : Vijay_Pande
						 * Patch ID: 4283_2 
						 * See also: 1-3
						 * Description: If radio button is clicked for map then clear values in the drop down list for storage position
						 */
						if(spClass!=null && createForm.getStContSelection() == Constants.RADIO_BUTTON_FOR_MAP)
						{
							String[] startingPoints = new String[]{"-1", "-1", "-1"};
							initialValues = new ArrayList();
							initialValues.add(startingPoints);
							request.setAttribute("initValues", initialValues);
						}
						/** -- patch ends here  --*/
					}
					else
					{
						isSpecimenExist = false;
					}
					
					if(!isSpecimenExist)
					{
						ActionErrors errors = (ActionErrors) request
						.getAttribute(Globals.ERROR_KEY);
						if (errors == null)
						{
							errors = new ActionErrors();
						}
						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
								"quickEvents.specimen.notExists", errorString));
						saveErrors(request, errors);
						request.setAttribute("disabled", "true");
						createForm.setVirtuallyLocated(true);
					}

				}
			}
		}
		else
		{
			containerMap = new TreeMap();
			Integer id = new Integer(createForm.getStorageContainer());
			String parentContainerName = "";
			
			Object object = dao.retrieve(StorageContainer.class.getName(), new Long(
					createForm.getStorageContainer()));
			if (object != null)
			{
				StorageContainer container = (StorageContainer) object;
				parentContainerName = container.getName();

			}
			Integer pos1 = new Integer(createForm.getPositionDimensionOne());
			Integer pos2 = new Integer(createForm.getPositionDimensionTwo());

			List pos2List = new ArrayList();
			pos2List.add(new NameValueBean(pos2, pos2));

			Map pos1Map = new TreeMap();
			pos1Map.put(new NameValueBean(pos1, pos1), pos2List);
			containerMap.put(new NameValueBean(parentContainerName, id), pos1Map);

			String[] startingPoints = new String[]{"-1", "-1", "-1"};
			if (createForm.getStorageContainer() != null
					&& !createForm.getStorageContainer().equals("-1"))
			{
				startingPoints[0] = createForm.getStorageContainer();

			}
			if (createForm.getPositionDimensionOne() != null
					&& !createForm.getPositionDimensionOne().equals("-1"))
			{
				startingPoints[1] = createForm.getPositionDimensionOne();
			}
			if (createForm.getPositionDimensionTwo() != null
					&& !createForm.getPositionDimensionTwo().equals("-1"))
			{
				startingPoints[2] = createForm.getPositionDimensionTwo();
			}
			initialValues = new ArrayList();
			initialValues.add(startingPoints);

		}
		request.setAttribute("initValues", initialValues);
		request.setAttribute(Constants.AVAILABLE_CONTAINER_MAP, containerMap);
		// -------------------------
		//Setting the specimen type list
		List specimenTypeList = CDEManager.getCDEManager().getPermissibleValueList(
				Constants.CDE_NAME_SPECIMEN_TYPE, null);
		request.setAttribute(Constants.SPECIMEN_TYPE_LIST, specimenTypeList);
				//Setting biohazard list
		List biohazardList = CDEManager.getCDEManager().getPermissibleValueList(
				Constants.CDE_NAME_BIOHAZARD, null);
		request.setAttribute(Constants.BIOHAZARD_TYPE_LIST, biohazardList);
		
		Map subTypeMap = Utility.getSpecimenTypeMap();	
		List specimenClassList = Utility.getSpecimenClassList();
		request.setAttribute(Constants.SPECIMEN_CLASS_LIST, specimenClassList);
		// set the map to subtype
		request.setAttribute(Constants.SPECIMEN_TYPE_MAP, subTypeMap);

		//*************  ForwardTo implementation *************
		HashMap forwardToHashMap = (HashMap) request.getAttribute("forwardToHashMap");

		if (forwardToHashMap != null)
		{
			Long parentSpecimenId = (Long) forwardToHashMap.get("parentSpecimenId");

			if (parentSpecimenId != null)
			{
				createForm.setParentSpecimenId(parentSpecimenId.toString());
				createForm.setPositionInStorageContainer("");
				createForm.setSelectedContainerName("");
				createForm.setQuantity("");
				createForm.setPositionDimensionOne("");
				createForm.setPositionDimensionTwo("");
				createForm.setStorageContainer("");
				createForm.setBarcode(null);
				map.clear();
				createForm.setExternalIdentifier(map);
				createForm.setExIdCounter(1);
				createForm.setVirtuallyLocated(false);
				createForm.setStContSelection(1);
//				containerMap = getContainerMap(createForm.getParentSpecimenId(), createForm
//						.getClassName(), dao, scbizLogic,exceedingMaxLimit,request);
//				initialValues = checkForInitialValues(containerMap);
				/**
				 * Name : Vijay_Pande
				 * Reviewer Name : Sachin_Lale
				 * Bug ID: 4283
				 * Patch ID: 4283_1 
				 * See also: 1-3
				 * Description: Proper Storage location of derived specimen was not populated while coming from newly created parent specimen page.
				 * Initial value were generated but not set to form variables.
				 */
//				if(initialValues!=null)
//				{
//					initialValues = checkForInitialValues(containerMap);
//					String[] startingPoints=(String[])initialValues.get(0);
//					createForm.setStorageContainer(startingPoints[0]);
//					createForm.setPos1(startingPoints[1]);
//					createForm.setPos2(startingPoints[2]);		
//				}
				/**  --patch ends here -- */
			}
		}
		//*************  ForwardTo implementation *************
		request.setAttribute(Constants.EXCEEDS_MAX_LIMIT,exceedingMaxLimit);
		request.setAttribute(Constants.AVAILABLE_CONTAINER_MAP, containerMap);
		if (createForm.isVirtuallyLocated())
		{
			request.setAttribute("disabled", "true");
		}
		
		return mapping.findForward(Constants.SUCCESS);
	}

	TreeMap getContainerMap(String specimenId, String className, DefaultBizLogic dao,
			StorageContainerBizLogic scbizLogic,String exceedingMaxLimit, HttpServletRequest request) throws DAOException,SMException
	{
		TreeMap containerMap = new TreeMap();

		Object object = dao.retrieve(Specimen.class.getName(), new Long(
				specimenId));
		SessionDataBean sessionData = (SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA);
		if (object != null)
		{
			Specimen sp = (Specimen) object;
			long cpId = sp.getSpecimenCollectionGroup().getCollectionProtocolRegistration()
					.getCollectionProtocol().getId().longValue();
			String spClass = className;
			Logger.out.info("cpId :" + cpId + "spClass:" + spClass);
			containerMap = scbizLogic.getAllocatedContaienrMapForSpecimen(cpId, spClass, 0,exceedingMaxLimit,sessionData,true);
		}

		return containerMap;
	}
	
	private long getCpId(DefaultBizLogic dao, Long scgId) throws DAOException
	{
		long cpId = -1;
		String []columnName = new String[1];
		Object []columnValue = new Long[1];

		columnName[0] = Constants.SYSTEM_IDENTIFIER; 
		columnValue[0] = scgId;
		String []selectColumnName={"collectionProtocolRegistration.collectionProtocol.id"};
		String []whereColumnCondition={"="};
		List cpList = dao.retrieve(SpecimenCollectionGroup.class.getName(),selectColumnName,columnName
				,whereColumnCondition,columnValue,null );
		if (cpList != null && !cpList.isEmpty())
		{
			cpId = (Long)cpList.get(0);
		}
		return cpId;
	}
}