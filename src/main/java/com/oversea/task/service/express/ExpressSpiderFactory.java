package com.oversea.task.service.express;

public class ExpressSpiderFactory {
	
	private static UPSExpressSpider upsExpressSpider = new UPSExpressSpider() ;
	
	public static UPSExpressSpider getUPSExpressSpider(){
		return upsExpressSpider ;
	}
}
