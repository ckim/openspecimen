
package edu.wustl.catissuecore.printservicemodule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import edu.wustl.catissuecore.domain.ExternalIdentifier;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.util.global.CommonUtilities;
import edu.wustl.common.util.global.Validator;

/**
 * This class is used to define method for Specimen label printing.
 * @author Renuka_bajpai
 */
public class WashuSpecimenCollectionGroupLabelPrinterImpl
		extends
			SpecimenCollectionGroupLabelPrinterImpl
{

	/**
	 * @param abstractDomainObject Specimen Collection group
	 * @param printerType printer Type
	 * @param printerLocation printer Location
	 * @param ipAddress IP Address
	 * @return List List of all Specimen including child Specimen
	 */
	@Override
	List createObjectMap(AbstractDomainObject abstractDomainObject, String printerType,
			String printerLocation, String ipAddress,String loginName)
	{
		final List listMap = new ArrayList();
		if (abstractDomainObject instanceof SpecimenCollectionGroup)
		{
			final SpecimenCollectionGroup objSCG = (SpecimenCollectionGroup) abstractDomainObject;
			final Collection specimenCollection = objSCG.getSpecimenCollection();
			final Iterator itr = specimenCollection.iterator();
			final ArrayList specimenList = new ArrayList();
			while (itr.hasNext())
			{
				final Specimen objSpecimen = (Specimen) itr.next();
				specimenList.add(objSpecimen);
			}
			for (int cnt = 0; cnt < specimenList.size(); cnt++)
			{
				final Specimen obj = (Specimen) specimenList.get(cnt);
				final LinkedHashMap<String, String> dataMap = new LinkedHashMap<String, String>();
				if (obj.getCollectionStatus().equals(Constants.COLLECTION_STATUS_COLLECTED))
				{
					String label = obj.getLabel();
					final String barcode = obj.getBarcode();
					dataMap.put("class", obj.getClassName());
					dataMap.put("id", obj.getId().toString());
					dataMap.put(PrintWebServiceConstants.USER_IPADDRESS, ipAddress);
					if(obj.getExternalIdentifierCollection() != null && !obj.getExternalIdentifierCollection().isEmpty()){
						
						 StringBuilder exIds = new StringBuilder(100);
						 for (ExternalIdentifier exId : obj.getExternalIdentifierCollection()) {
						 exIds.append(exId.getName());
						 exIds.append(":");
						 exIds.append(exId.getValue());
						 exIds.append(",");
						 }
						 dataMap.put(PrintWebServiceConstants.EX_ID, exIds.toString());
						 }
						 if (!Validator.isEmpty(loginName))
						 {
						 dataMap.put(PrintWebServiceConstants.LOGIN_NAME, loginName);
						}
					if (obj.getClassName() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_CLASS, obj.getClassName());
					}
					if (obj.getSpecimenType() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_TYPE, obj.getSpecimenType());
					}
					if (obj.getId() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_IDENTIFIER, obj.getId()
								.toString());
					}
					if (label == null)
					{
						label = obj.getSpecimenType();
					}
					dataMap.put(PrintWebServiceConstants.SPECIMEN_LABEL, label);
					if (obj.getBarcode() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_BARCODE, barcode);
					}
					if (obj.getTissueSite() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_TISSUE_SITE, CommonUtilities
								.toString(obj.getTissueSite()));
					}
					if (obj.getCollectionStatus() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_COLLECTION_STATUS,
								CommonUtilities.toString(obj.getCollectionStatus()));
					}
					if (obj.getComment() != null && !(obj.getComment().equals("")))
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_COMMENT, CommonUtilities
								.toString(obj.getComment()));
					}
					if (obj.getCreatedOn() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_CREATED_ON, CommonUtilities
								.toString(obj.getCreatedOn()));
					}
					if (obj.getLineage() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_LINEAGE, CommonUtilities
								.toString(obj.getLineage()));
					}
					if (obj.getMessageLabel() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_MESSAGE_LABEL,
								CommonUtilities.toString(obj.getMessageLabel()));
					}
					if (obj.getSpecimenPosition() != null
							&& obj.getSpecimenPosition().getStorageContainer() != null)
					{
						dataMap.put(PrintWebServiceConstants.SPECIMEN_STORAGE_CONTAINER_NAME,
								CommonUtilities.toString(obj.getSpecimenPosition()
										.getStorageContainer().getName()));
						dataMap.put(PrintWebServiceConstants.SPECIMEN_POSITION_DIMENSION_ONE,
								CommonUtilities.toString(obj.getSpecimenPosition()
										.getPositionDimensionOne()));
						dataMap.put(PrintWebServiceConstants.SPECIMEN_POSITION_DIMENSION_TWO,
								CommonUtilities.toString(obj.getSpecimenPosition()
										.getPositionDimensionTwo()));
					}

					if (Constants.MOLECULAR.equals(obj.getClassName()))
					{
						final String concentration = CommonUtilities.toString(String
								.valueOf(( obj)
										.getConcentrationInMicrogramPerMicroliter()));
						dataMap.put(PrintWebServiceConstants.CONCENTRATION, concentration);
					}
					if (obj.getAvailableQuantity() != null)
					{
						dataMap.put(PrintWebServiceConstants.QUANTITY, CommonUtilities
								.toString(String.valueOf(obj.getAvailableQuantity())));
					}
					if (obj.getPathologicalStatus() != null)
					{
						dataMap.put(PrintWebServiceConstants.PATHOLOGICAL_STATUS, CommonUtilities
								.toString(obj.getPathologicalStatus()));
					}

					try
					 {
					 String participant = obj.getSpecimenCollectionGroup().getCollectionProtocolRegistration().getParticipant().getId().toString();
					 dataMap.put("participant", participant);
					 String participantF = obj.getSpecimenCollectionGroup().getCollectionProtocolRegistration().getParticipant().getFirstName().toString();
					 String participantL = obj.getSpecimenCollectionGroup().getCollectionProtocolRegistration().getParticipant().getLastName().toString();
					 if(!Validator.isEmpty(participantF)){
					 dataMap.put("participantF", participantF);
					 }
					 if(!Validator.isEmpty(participantL)){
					 dataMap.put("participantL", participantL);
					 }
					 System.out.println((new StringBuilder("ParticipantT: ")).append(participant).append(" ").append(participantF).append(" ").append(participantL).toString());
					 }
					 catch(Exception e)
					 {
					 e.printStackTrace();
					// dataMap.put("participant", "null");
					// dataMap.put("participantF", "null");
					// dataMap.put("participantL", "null");
					 }
					
					final String cpTitle = CommonUtilities.toString(obj
							.getSpecimenCollectionGroup().getCollectionProtocolRegistration()
							.getCollectionProtocol().getShortTitle());//getTitle());
					final String ppi = obj.getSpecimenCollectionGroup()
							.getCollectionProtocolRegistration().getProtocolParticipantIdentifier();
					dataMap.put(PrintWebServiceConstants.CP_TITLE, cpTitle);
					if (ppi != null && !ppi.equals(""))
					{
						dataMap.put(PrintWebServiceConstants.PARTICIPANT_PROTOCOL_IDENTIFIER,
								CommonUtilities.toString(ppi));
					}
					listMap.add(dataMap);

				}
			}
		}
		return listMap;
	}
}
