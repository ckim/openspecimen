package edu.wustl.catissuecore.bizlogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.wustl.catissuecore.util.StorageContainerUtil;
import edu.wustl.catissuecore.util.global.AppUtility;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.exception.ApplicationException;
import edu.wustl.common.exception.BizLogicException;
import edu.wustl.common.util.NameValueBeanRelevanceComparator;
import edu.wustl.common.util.XMLPropertyHandler;
import edu.wustl.common.util.global.Validator;
import edu.wustl.common.util.logger.Logger;
import edu.wustl.dao.DAO;
import edu.wustl.dao.JDBCDAO;
import edu.wustl.dao.exception.DAOException;

public abstract class AbstractSCSelectionBizLogic
{
	/**
	 * Logger object.
	 */
	private static final transient Logger logger = Logger.getCommonLogger(AbstractSCSelectionBizLogic.class);
	private static final int CONTAINERS_MAX_LIMIT = Integer.parseInt(XMLPropertyHandler
			.getValue(Constants.CONTAINERS_MAX_LIMIT));
	/**
	 * Returns a map of allocated containers versus their respective
	 * position maps, for the given containers
	 * 
	 * @return Returns a map of allocated containers versus their respective free
	 *         locations.
	 * @throws DAOException
	 */
	protected Map<NameValueBean, Map<NameValueBean, List<NameValueBean>>>
	getAllocDetailsForContainers(final List<?> containerList, final DAO dao) throws BizLogicException
	{
		logger.info("No of containers:" + containerList.size());
		final Map<NameValueBean, Map<NameValueBean, List<NameValueBean>>> containerMap = 
			new TreeMap<NameValueBean, Map<NameValueBean, List<NameValueBean>>>(new NameValueBeanRelevanceComparator());
		long relevance = 1;
		for (final Iterator<?> itr = containerList.listIterator(); itr.hasNext(); relevance++)
		{
			final ArrayList<?> container = (ArrayList<?>) itr.next();
			final Map<NameValueBean, List<NameValueBean>> positionMap = 
				StorageContainerUtil.getAvailablePositionMapForContainer(String
					.valueOf(container.get(0)), 0, Integer.parseInt(String
					.valueOf(container.get(2))),
					Integer.parseInt(String.valueOf(container.get(3))), dao);
			if (!positionMap.isEmpty())
			{
				final NameValueBean nvb = new NameValueBean(container.get(1), container.get(0),
						relevance);
				containerMap.put(nvb, positionMap);
			}
		}
		return containerMap;
	}
	
	public Map<NameValueBean, Map<NameValueBean, List<NameValueBean>>>
	getAllocDetailsForGivenContainer(String containerId,String containerName,Integer positionDimensionOne,Integer positionDimensionTwo, final DAO dao) throws BizLogicException
	{
		
		final Map<NameValueBean, Map<NameValueBean, List<NameValueBean>>> containerMap = 
			new TreeMap<NameValueBean, Map<NameValueBean, List<NameValueBean>>>(new NameValueBeanRelevanceComparator());
		long relevance = 1;
		final Map<NameValueBean, List<NameValueBean>> positionMap = 
				StorageContainerUtil.getAvailablePositionMapForContainer(containerId, 0, positionDimensionOne,
						positionDimensionTwo, dao);
			if (!positionMap.isEmpty())
			{
				final NameValueBean nvb = new NameValueBean(containerName, containerId,
						relevance);
				containerMap.put(nvb, positionMap);
			}
	return containerMap;
	}
	
	/**
	 * Returns a list of storage containers. Each index corresponds to the entry:<br>
	 * 		[id, name, one_dimension_capacity, two_dimension_capacity ...]
	 * @param holdsType - The type that the containers can 
	 * hold (Specimen/SpecimenArray/Container)
	 * @param cpId - Collection Protocol Id
	 * @param spClass - Specimen Class
	 * @param aliquotCount - Number of aliquotes that the fetched containers 
	 * should minimally support. A value of <b>0</b> specifies that there's
	 * no such restriction
	 * @param containerTypeId  containerTypeId
	 * @param specimenArrayTypeId specimenArrayTypeId
	 * @param exceedingLimit exceedingLimit value
	 * @param storageType Auto, Manual, Virtual
	 * @param selContName 
	 * @param type_id 
	 * @return a list of storage containers
	 * @throws ApplicationException 
	 */
	protected List<?> getStorageContainerList(final String storageType, final String[] queries, String selContName, long type_id) throws ApplicationException
	{
		final JDBCDAO dao = AppUtility.openJDBCSession();
		final List containers = new ArrayList();
		try
		{
			int remainingContainersNeeded = CONTAINERS_MAX_LIMIT;
			for (int i = 0; i < queries.length; i++)
			{
				logger.debug(String.format("Firing query: query%d", i));
				logger.debug(queries[i]);
				final List resultList = getContainerListByQuery(storageType, dao, queries[i],
				remainingContainersNeeded); 
				//dao.executeQuery(queries[i]);
				if (resultList == null || resultList.size() == 0)
				{
					continue;
				}
				if(!Constants.STORAGE_TYPE_POSITION_MANUAL.equals(storageType) 
				&& resultList.size() >= remainingContainersNeeded)
				{
					List subListOfCont = resultList.subList(0, remainingContainersNeeded);
					if(!containers.containsAll( subListOfCont ))
					{
					  containers.addAll(subListOfCont);
					  break;
					}
				}
				if(!containers.containsAll( resultList ))
				{
				   containers.addAll(resultList);
				}
				remainingContainersNeeded = remainingContainersNeeded - resultList.size();
			}
			boolean selContFound = false;
			if(!Validator.isEmpty(selContName)){
			for (Object object : containers) {
				List list = (List)object;
//				String[] obj = (String[])object;
				if(selContName.equals(list.get(1))){
					selContFound = true;
					break;
				}
			}
			if(!selContFound){
				String sql = "SELECT VIEW1.IDENTIFIER, VIEW1.NAME, VIEW1.ONE_DIMENSION_CAPACITY, VIEW1.TWO_DIMENSION_CAPACITY FROM  "
						+ "(SELECT cont.IDENTIFIER, cont.NAME, cap.ONE_DIMENSION_CAPACITY, cap.TWO_DIMENSION_CAPACITY, "
						+ "(cap.ONE_DIMENSION_CAPACITY * cap.TWO_DIMENSION_CAPACITY)  CAPACITY 	FROM CATISSUE_CAPACITY cap JOIN CATISSUE_CONTAINER "
						+ "cont   on cap.IDENTIFIER = cont.CAPACITY_ID   LEFT OUTER JOIN CATISSUE_SPECIMEN_POSITION K ON "
						+ "cont.IDENTIFIER = K.CONTAINER_ID   LEFT OUTER JOIN CATISSUE_CONTAINER_POSITION L ON cont.IDENTIFIER = L.PARENT_CONTAINER_ID "
						+ " WHERE cont.IDENTIFIER IN  (SELECT t4.STORAGE_CONTAINER_ID   FROM CATISSUE_ST_CONT_ST_TYPE_REL t4  WHERE "
						+ "(t4.STORAGE_TYPE_ID = '"+type_id+"' OR t4.STORAGE_TYPE_ID='1') and t4.STORAGE_CONTAINER_ID in  "
						+ "(select SC.IDENTIFIER from CATISSUE_STORAGE_CONTAINER SC  join CATISSUE_SITE S on sc.site_id=S.IDENTIFIER and "
						+ "S.ACTIVITY_STATUS!='Closed' ))  AND cont.NAME like '"+selContName+"' AND cont.ACTIVITY_STATUS='Active' and "
						+ "cont.CONT_FULL=0 ) VIEW1  GROUP BY VIEW1.IDENTIFIER, VIEW1.NAME,VIEW1.ONE_DIMENSION_CAPACITY, VIEW1.TWO_DIMENSION_CAPACITY ,"
						+ "VIEW1.CAPACITY  HAVING (VIEW1.CAPACITY - COUNT(*)) >  0  ORDER BY VIEW1.IDENTIFIER";
				List subListOfCont = dao.executeQuery(sql);
				if(subListOfCont!= null && !subListOfCont.isEmpty()){
					List res = (List)subListOfCont.get(0);
//					Object[] obj = {res.get(0),res.get(1),res.get(2),res.get(3)};
					containers.add(res);
				}
			}
			}
		}
		finally
		{
			AppUtility.closeJDBCSession(dao);
		}
		logger.debug(String.format("%s:%s:%d", this.getClass().getSimpleName(),
				"getStorageContainers() number of containers fetched", containers.size()));
		return containers;
	}
	/**
	 * @param storageType 
	 * @param dao A dao object to be used to execute query.
	 * @param query to be executed to get containers.
	 * @param maxRecords maximum records to be retrieved by query.
	 * @return containers as query result in the form of List
	 * @throws DAOException DAO Exception 
	 */
	private List getContainerListByQuery(String storageType, final JDBCDAO dao,
			final String query, int maxRecords) throws DAOException
	{
		final List resultList;
		if(!"Manual".equals(storageType))
		{
			resultList= dao.executeQuery(query,0,maxRecords,null );
		}
		else
		{
			resultList= dao.executeQuery(query);
		}
		return resultList;
	}
}
