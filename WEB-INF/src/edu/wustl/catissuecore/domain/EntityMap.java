package edu.wustl.catissuecore.domain;


import java.util.Date;

import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.exception.AssignDataException;


/**
 * @author sandeep_chinta
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @hibernate.class table="CATISSUE_ENTITY_MAP"
 */

public class EntityMap extends AbstractDomainObject implements java.io.Serializable
{
    
    /**Unique id of the object.*/
	private Long id;
	
	private Long containerId;
	
	private Long typeId;
	
	private Long staticRecordId;
	
	private Long staticEntityId;
	
	/**
	 * Status of the link as attached detached.
	 */
	protected String linkStatus;
	
	
	/**
	 * Created by user name
	 */
	protected String createdBy;
	
    private Date createdDate;
    /**
     * @hibernate.property name="containerId" column="CONTAINER_ID" type="long" length="30"
     * @return Returns the containerId.
     */
    public Long getContainerId()
    {
        return containerId;
    }
    /**
     * @param containerId The containerId to set.
     */
    public void setContainerId(Long containerId)
    {
        this.containerId = containerId;
    }
	
	   
    public void setAllValues(AbstractActionForm abstractForm) throws AssignDataException
    {
        // TODO Auto-generated method stub
        
    }
   
    public Long getSystemIdentifier()
    {
        // TODO Auto-generated method stub
        return id;
    }
    
    public void setSystemIdentifier(Long systemIdentifier)
    {
        this.id = systemIdentifier;
    }

    /**
     * Returns the systemIdentifier assigned to user.
     * @hibernate.id name="id" column="IDENTIFIER" type="long" length="30"
     * unsaved-value="null" generator-class="native"
     * @hibernate.generator-param name="sequence" value="CATISSUE_ENTITY_MAP_SEQ"
     * @return Returns the systemIdentifier.
     */
    public Long getId()
    {
        return id;
    }
    /**
     * @param id The id to set.
     */
    public void setId(Long id)
    {
        this.id = id;
    }
    
    /**
	 * @hibernate.property name="linkStatus" type="string" column="STATUS" length="10"
	 * @return Status of the link as attached/detached etc
	 */
	public String getLinkStatus()
	{
		return this.linkStatus;
	}

	/**
	 * 	@param linkStatus : Status of the link as attached/detached
	 */
	public void setLinkStatus(String linkStatus)
	{
		this.linkStatus = linkStatus;
	}
  
    /**
     * @return
     * @hibernate.property name="staticRecordId" column="STATIC_RECORD_ID" type="long" length="30"
     * 
     */
    public Long getStaticRecordId()
    {
        return staticRecordId;
    }
    /**
     * @param collectionProtocol The collectionProtocol to set.
     */
    public void setStaticRecordId(
            Long collectionProtocol)
    {
        this.staticRecordId = collectionProtocol;
    }
    /**
     * @return Returns the typeId.
     *  @hibernate.property name="typeId" column="TYPE_ID" type="long" length="30"
     */
    public Long getTypeId()
    {
        return typeId;
    }
    /**
     * @param typeId The typeId to set.
     */
    public void setTypeId(Long typeId)
    {
        this.typeId = typeId;
    }
    /**
     * @return Returns the staticEntityId.
     *  @hibernate.property name="staticEntityId" column="STATIC_ENTITY_ID" type="long" length="30"
     */
    public Long getStaticEntityId()
    {
        return staticEntityId;
    }
    /**
     * @param staticEntityId The staticEntityId to set.
     */
    public void setStaticEntityId(Long staticEntityId)
    {
        this.staticEntityId = staticEntityId;
    }
    /**
     * @return Returns the createdDate.
     * @hibernate.property name="createdDate" type="date" column="CREATED_DATE"
     */
    public Date getCreatedDate()
    {
        return createdDate;
    }
    /**
     * @param createdDate The createdDate to set.
     */
    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }
    /**
     * @return Returns the createdBy.
     * @hibernate.property name="createdBy" type="string" column="CREATED_BY" length="255"
     */
    public String getCreatedBy()
    {
        return createdBy;
    }
    /**
     * @param createdBy The createdBy to set.
     */
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }
}
