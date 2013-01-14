// Generated by eBay Common SDK Platform
package com.ebay.services.merchandising.client;

import java.util.HashMap;
import java.util.Map;

import org.ebay.msif.core.ServiceAccessPolicy;

public class MerchandisingServiceAccessPolicy implements ServiceAccessPolicy {
	/**
	 * Tell whether the service requires authentication
	 */
	public boolean requireAuthentication() {
		return false;
	}
	
	/**
	 * Return the supported data bindings for the service
	 */
	public String[] getSupportedDataBindings() {
		return new String[]{"JSON", "XML"};
	}
	
	/**
	 * Reteurn the list of service endpoint based on different environment
	 */
	public Map<String, String> getServiceLocation() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("production", "http://svcs.ebay.com/MerchandisingService");
		map.put("sandbox", "http://svcs.sandbox.ebay.com/MerchandisingService");
		return map;
	}

}