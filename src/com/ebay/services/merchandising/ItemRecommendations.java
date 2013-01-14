// Generated by eBay Common SDK Platform
package com.ebay.services.merchandising;

import com.ebay.jxb.annotation.*;
import java.util.List;
/**
 * 
 * This is a container for list of item recommendations.
 * 
 */
@Root(name = "ItemRecommendations")
@Namespace(namespaceURI="http://www.ebay.com/marketplace/services", prefix="")
public class ItemRecommendations   {


    @ElementList(entry="item")
	private List<Item> item;
	
    
	/**
     * 
     * Contains details for a single recommended item. Returned when there is
     * at least one item that matches the search criteria.
     * 
	 */
	public List<Item> getItem() {
	    return this.item;
	}
	
	public void setItem(List<Item> item) {
	    this.item = item;
	}
	
}