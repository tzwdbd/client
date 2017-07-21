package com.oversea.task.service.express;

import java.util.ArrayList;

import com.oversea.task.service.express.entity.ResultItem;


public interface ExpressSpider {

	public ArrayList<ResultItem> spiderByExpressNo(String expressNo) ;
	
}
