
package com.krishagni.catissueplus.core.administrative.services;

import com.krishagni.catissueplus.core.administrative.events.AllDistributionProtocolsEvent;
import com.krishagni.catissueplus.core.administrative.events.CreateDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.DeleteDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolCreatedEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolDeletedEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolPatchedEvent;
import com.krishagni.catissueplus.core.administrative.events.DistributionProtocolUpdatedEvent;
import com.krishagni.catissueplus.core.administrative.events.GetDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.GotDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.PatchDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.ReqAllDistributionProtocolEvent;
import com.krishagni.catissueplus.core.administrative.events.UpdateDistributionProtocolEvent;

public interface DistributionProtocolService {

	public DistributionProtocolCreatedEvent createDistributionProtocol(CreateDistributionProtocolEvent event);

	public DistributionProtocolUpdatedEvent updateDistributionProtocol(UpdateDistributionProtocolEvent reqEvent);

	public DistributionProtocolPatchedEvent patchDistributionProtocol(PatchDistributionProtocolEvent reqEvent);

	public DistributionProtocolDeletedEvent deleteDistributionProtocol(DeleteDistributionProtocolEvent event);

	public GotDistributionProtocolEvent getDistributionProtocol(GetDistributionProtocolEvent event);

	public AllDistributionProtocolsEvent getAllDistributionProtocols(ReqAllDistributionProtocolEvent reqEvent);

}
