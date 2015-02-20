package com.krishagni.catissueplus.core.administrative.domain.factory;

import com.krishagni.catissueplus.core.common.errors.ErrorCode;

public enum DistributionOrderErrorCode implements ErrorCode {
	NOT_FOUND,
	
	DUP_NAME,
	
	NAME_REQUIRED, 
	
	INVALID_STATUS, 
	
	INVALID_CREATION_DATE,
	
	INVALID_EXECUTION_DATE, 
	
	INVALID_DISTRIB_QUANTITY;

	@Override
	public String code() {
		return "DIST_ORDER_" + this.name();
	}

}
