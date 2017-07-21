package com.oversea.task.service.express;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.oversea.task.service.express.entity.ResultItem;
import com.oversea.task.utils.StringUtils;

public class UPSExpressSpider implements ExpressSpider{
	
	private final Logger logger = Logger.getLogger(getClass());

	@Override
	public ArrayList<ResultItem> spiderByExpressNo(String expressNo) {
		ArrayList<ResultItem> list = new ArrayList<ResultItem>() ;
		Document document = null ;
		try {
			document = Jsoup.connect("https://wwwapps.ups.com/WebTracking/track?trackNums=" + expressNo+ "&track.x=%E8%BF%BD%E8%B8%AA").timeout(5000).get();
		} catch (IOException e) {
			logger.error("连接ups官网出现异常：",e);
		}
		Elements elements = document.select("table.dataTable tbody tr") ;
		for (Element element : elements) {
			ResultItem resultItem = new ResultItem() ;
			Elements tds = element.select("td") ;
			if(tds!=null&&tds.size()>0){
				if(StringUtils.isBlank(tds.get(0).text())){
					resultItem.setContext(tds.get(3).text());
				}else{
					resultItem.setContext("["+tds.get(0).text()+"]"+tds.get(3).text());
				}
				//时间为2016/11/30	12:55
				String time = tds.get(1).text()+" "+tds.get(2).text() ;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm") ;
				Date date = null ;
				try {
					date = sdf.parse(time) ;
				} catch (ParseException e) {
					logger.error("日期转换异常:",e);
				}
				resultItem.setOccurTime(date);
				resultItem.setTime(new Date());
				list.add(resultItem) ;
			}
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		UPSExpressSpider upsExpressSpider = new UPSExpressSpider() ;
		ArrayList<ResultItem> list = upsExpressSpider.spiderByExpressNo("1Z98A64AP218840223") ;
		for(ResultItem resultItem:list){
			System.out.println(resultItem.getContext()+"\t"+resultItem.getOccurTime()) ;
		}
	}

}
