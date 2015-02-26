
package com.krishagni.catissueplus.core.biospecimen.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.Participant;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CollectionProtocolRegistrationFactory;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CpeErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.CprErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.VisitFactory;
import com.krishagni.catissueplus.core.biospecimen.events.CollectionProtocolRegistrationDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ParticipantDetail;
import com.krishagni.catissueplus.core.biospecimen.events.ParticipantRegistrationsList;
import com.krishagni.catissueplus.core.biospecimen.events.RegistrationQueryCriteria;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenDetail;
import com.krishagni.catissueplus.core.biospecimen.events.VisitDetail;
import com.krishagni.catissueplus.core.biospecimen.events.VisitSpecimensQueryCriteria;
import com.krishagni.catissueplus.core.biospecimen.events.VisitSummary;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.biospecimen.repository.VisitsListCriteria;
import com.krishagni.catissueplus.core.biospecimen.services.CollectionProtocolRegistrationService;
import com.krishagni.catissueplus.core.biospecimen.services.ParticipantService;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class CollectionProtocolRegistrationServiceImpl implements CollectionProtocolRegistrationService {
	private DaoFactory daoFactory;

	private CollectionProtocolRegistrationFactory cprFactory;
	
	private VisitFactory visitFactory;
	
	private ParticipantService participantService;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setCprFactory(CollectionProtocolRegistrationFactory cprFactory) {
		this.cprFactory = cprFactory;
	}
	
	public void setVisitFactory(VisitFactory visitFactory) {
		this.visitFactory = visitFactory;
	}

	public void setParticipantService(ParticipantService participantService) {
		this.participantService = participantService;
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolRegistrationDetail> getRegistration(RequestEvent<RegistrationQueryCriteria> req) {				
		try {			
			RegistrationQueryCriteria crit = req.getPayload();
			CollectionProtocolRegistrationDetail cpr = null;
			
			if (crit.getCprId() != null) {
				cpr = getByCprId(crit.getCprId());
			} else if (crit.getCpId() != null && crit.getPpid() != null) {
				cpr = getByCpIdAndPpid(crit.getCpId(), crit.getPpid());
			} 
			
			return ResponseEvent.response(cpr);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolRegistrationDetail> createRegistration(RequestEvent<CollectionProtocolRegistrationDetail> req) {
		try {
			CollectionProtocolRegistrationDetail cprDetail = req.getPayload();
			CollectionProtocolRegistration cpr = cprFactory.createCpr(cprDetail);
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			
			ensureUniqueParticipantReg(cpr, ose);
			ensureUniquePpid(cpr, ose);
			ensureUniqueBarcode(cpr.getBarcode(), ose);
			ose.checkAndThrow();
			
			saveParticipant(cpr, cprDetail.getParticipant());
			daoFactory.getCprDao().saveOrUpdate(cpr);
			return ResponseEvent.response(CollectionProtocolRegistrationDetail.from(cpr));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<VisitSummary>> getVisits(RequestEvent<VisitsListCriteria> req) {
		try {
			return ResponseEvent.response(daoFactory.getVisitsDao().getVisits(req.getPayload()));
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<SpecimenDetail>> getSpecimens(RequestEvent<VisitSpecimensQueryCriteria> req) {
		VisitSpecimensQueryCriteria crit = req.getPayload();
		
		try {
			List<SpecimenDetail> specimens = Collections.emptyList();			
			if (crit.getVisitId() != null) {
				specimens = getSpecimensByVisit(crit.getCprId(), crit.getVisitId());
			} else if (crit.getEventId() != null) {
				specimens = getAnticipatedSpecimens(crit.getCprId(), crit.getEventId());
			}
			
			return ResponseEvent.response(specimens);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<VisitDetail> addVisit(RequestEvent<VisitDetail> req) {
		try { // TODO: visit name
			Visit visit = visitFactory.createVisit(req.getPayload()); 
			visit.setName(UUID.randomUUID().toString()); 

			daoFactory.getVisitsDao().saveOrUpdate(visit); 
			return ResponseEvent.response(VisitDetail.from(visit));			
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);			
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
		
	
	@Override
	@PlusTransactional
	public ResponseEvent<ParticipantRegistrationsList> createBulkRegistration(RequestEvent<ParticipantRegistrationsList> req) {
		try {
			ParticipantRegistrationsList participantRegDetails = req.getPayload();
			for (int i =0; i < participantRegDetails.getRegistrations().size(); i++) {
				CollectionProtocolRegistrationDetail cprDetail = buildCprForBulkParticipantDetails(participantRegDetails, i);
				
				ResponseEvent<CollectionProtocolRegistrationDetail> response = 
						createRegistration(new RequestEvent<CollectionProtocolRegistrationDetail>(req.getSessionDataBean(), cprDetail));
				
				if (response.isSuccessful()) {
					CollectionProtocolRegistrationDetail savedCpr = response.getPayload(); 
					participantRegDetails.setId(savedCpr.getParticipant().getId());
					participantRegDetails.getRegistrations().get(i).setId(savedCpr.getId());
				} else {
					return ResponseEvent.error(response.getError());
				}
			}
			
			return ResponseEvent.response(participantRegDetails);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<CollectionProtocolRegistrationDetail> updateRegistration(RequestEvent<CollectionProtocolRegistrationDetail> req) {
		try {
			CollectionProtocolRegistrationDetail cprDetail = req.getPayload();
			
			CollectionProtocolRegistration oldCpr = null;
			if (cprDetail.getId() != null) {
				oldCpr = daoFactory.getCprDao().getById(cprDetail.getId());
			} else if (cprDetail.getPpid() != null && cprDetail.getCpId() != null) {
				oldCpr = daoFactory.getCprDao().getCprByPpId(cprDetail.getCpId(), cprDetail.getPpid());
			}

			if (oldCpr == null) {
				return ResponseEvent.userError(CprErrorCode.NOT_FOUND);
			}
			
			CollectionProtocolRegistration cpr = cprFactory.createCpr(cprDetail);

			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			validatePpid(oldCpr, cpr, ose);
			validateBarcode(oldCpr.getBarcode(), cpr.getBarcode(), ose);
			ose.checkAndThrow();
			
			oldCpr.update(cpr);
			daoFactory.getCprDao().saveOrUpdate(cpr);
			return ResponseEvent.response(CollectionProtocolRegistrationDetail.from(cpr));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	private void saveParticipant(CollectionProtocolRegistration cpr, ParticipantDetail detail) {
		if (detail.getId() != null) {
			return; 
		}
		
		Participant participant = cpr.getParticipant();
		participantService.createParticipant(participant);
		cpr.setParticipant(participant);
	}
	
	//
	// Checks whether same participant is registered for same protocol already
	//
	private void ensureUniqueParticipantReg(CollectionProtocolRegistration cpr, OpenSpecimenException ose) {
		if (cpr.getParticipant() == null || cpr.getParticipant().getId() == null) {
			return ;
		}
		
		Long participantId = cpr.getParticipant().getId();
		Long cpId = cpr.getCollectionProtocol().getId();
		
		if (daoFactory.getCprDao().getRegistrationId(cpId, participantId) != null) {
			ose.addError(CprErrorCode.DUP_REGISTRATION);
		}
	}

	private void ensureUniquePpid(CollectionProtocolRegistration cpr, OpenSpecimenException ose) {
		Long cpId = cpr.getCollectionProtocol().getId();
		String ppid = cpr.getPpid();
		
		if (daoFactory.getCprDao().getCprByPpId(cpId, ppid) != null) {
			ose.addError(CprErrorCode.DUP_PPID);
		}
	}

	private void ensureUniqueBarcode(String barcode, OpenSpecimenException ose) {
		if (StringUtils.isNotBlank(barcode) && daoFactory.getCprDao().getCprByBarcode(barcode) != null) {
			ose.addError(CprErrorCode.DUP_BARCODE);
		}
	}

	private void validatePpid(CollectionProtocolRegistration oldCpr, CollectionProtocolRegistration cpr,
			OpenSpecimenException ose) {
		String oldPpid = oldCpr.getPpid();
		String newPpid = cpr.getPpid();
		if (!oldPpid.equals(newPpid)) {
			ensureUniquePpid(cpr, ose);
		}
	}

	private void validateBarcode(String oldBarcode, String newBarcode, OpenSpecimenException ose) {
		if (!StringUtils.isBlank(newBarcode) && !newBarcode.equals(oldBarcode)) {
			ensureUniqueBarcode(newBarcode, ose);
		}
	}

	private CollectionProtocolRegistrationDetail buildCprForBulkParticipantDetails(ParticipantRegistrationsList participantRegList, int idx) {
		CollectionProtocolRegistrationDetail result = new CollectionProtocolRegistrationDetail();
		
		ParticipantDetail participant = (ParticipantDetail)participantRegList;		
		result.setParticipant(participant);
		
		CollectionProtocolRegistrationDetail cpr = participantRegList.getRegistrations().get(idx);
		result.setCpId(cpr.getCpId());
		result.setCpTitle(cpr.getCpTitle());
		result.setId(cpr.getId());
		result.setPpid(cpr.getPpid());
		result.setRegistrationDate(cpr.getRegistrationDate());
		result.setConsentDetails(cpr.getConsentDetails());
		
		return result;
	}
	
	private CollectionProtocolRegistrationDetail getByCprId(Long cprId) {
		CollectionProtocolRegistration cpr = daoFactory.getCprDao().getById(cprId);
		if (cpr == null) {
			throw OpenSpecimenException.userError(CprErrorCode.NOT_FOUND);
		}
		
		return CollectionProtocolRegistrationDetail.from(cpr);
	}
	
	private CollectionProtocolRegistrationDetail getByCpIdAndPpid(Long cpId, String ppid) {
		CollectionProtocolRegistration cpr = daoFactory.getCprDao().getCprByPpId(cpId, ppid);
		if (cpr == null) {
			throw OpenSpecimenException.userError(CprErrorCode.INVALID_CP_AND_PPID);
		}
		
		return CollectionProtocolRegistrationDetail.from(cpr);
	}
	
	private List<SpecimenDetail> getSpecimensByVisit(Long cprId, Long visitId) {
		Visit visit = daoFactory.getVisitsDao().getById(visitId);
		if (visit == null) {
			throw OpenSpecimenException.userError(VisitErrorCode.NOT_FOUND);
		}
		
		Set<SpecimenRequirement> anticipatedSpecimens = visit.getCpEvent().getTopLevelAnticipatedSpecimens();
		Set<Specimen> specimens = visit.getTopLevelSpecimens();

		return getSpecimens(anticipatedSpecimens, specimens);
	}
	
	private List<SpecimenDetail> getAnticipatedSpecimens(Long cprId, Long eventId) {
		CollectionProtocolEvent cpe = daoFactory.getCollectionProtocolDao().getCpe(eventId);
		if (cpe == null) {
			throw OpenSpecimenException.userError(CpeErrorCode.NOT_FOUND);
		}
		
		Set<SpecimenRequirement> anticipatedSpecimens = cpe.getTopLevelAnticipatedSpecimens();
		return getSpecimens(anticipatedSpecimens, Collections.<Specimen>emptySet());		
	}
	
	private List<SpecimenDetail> getSpecimens(Collection<SpecimenRequirement> anticipated, Collection<Specimen> specimens) {		
		List<SpecimenDetail> result = SpecimenDetail.from(specimens);
		merge(anticipated, result, null, getReqSpecimenMap(result));

		SpecimenDetail.sort(result);
		return result;
	}
	
	private Map<Long, SpecimenDetail> getReqSpecimenMap(List<SpecimenDetail> specimens) {
		Map<Long, SpecimenDetail> reqSpecimenMap = new HashMap<Long, SpecimenDetail>();
						
		List<SpecimenDetail> remaining = new ArrayList<SpecimenDetail>();
		remaining.addAll(specimens);
		
		while (!remaining.isEmpty()) {
			SpecimenDetail specimen = remaining.remove(0);
			Long srId = (specimen.getReqId() == null) ? -1 : specimen.getReqId();
			reqSpecimenMap.put(srId, specimen);
			
			remaining.addAll(specimen.getChildren());
		}
		
		return reqSpecimenMap;
	}
	
	private void merge(
			Collection<SpecimenRequirement> anticipatedSpecimens, 
			List<SpecimenDetail> result, 
			SpecimenDetail currentParent,
			Map<Long, SpecimenDetail> reqSpecimenMap) {
		
		for (SpecimenRequirement anticipated : anticipatedSpecimens) {
			SpecimenDetail specimen = reqSpecimenMap.get(anticipated.getId());
			if (specimen != null) {
				merge(anticipated.getChildSpecimenRequirements(), result, specimen, reqSpecimenMap);
			} else {
				specimen = SpecimenDetail.from(anticipated);
				
				if (currentParent == null) {
					result.add(specimen);
				} else if (specimen == null) {
					currentParent.getChildren().add(specimen);
				}				
			}						
		}
	}
}
