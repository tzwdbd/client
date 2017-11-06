package com.oversea.task.handle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.BrushOrderDetail;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.service.order.AmazonAutoBuy;
import com.oversea.task.service.order.AmazonJpAutoBuy;
import com.oversea.task.service.order.ZcnAutoBuy;
import com.oversea.task.service.order.AutoBuy;
import com.oversea.task.utils.ClassUtil;
import com.oversea.task.utils.Utils;

@Component
public class BrushShipNormalHandler implements BrushShipHandler {
	final Logger logger = Logger.getLogger(getClass());

	@Override
	public void handle(Task task, TaskResult taskResult,BrushOrderDetail brushOrderDetail){
		OrderAccount account = (OrderAccount) task.getParam("account");
		if (account == null){
			logger.error("account is null");
			return;
		}
		String mallName = brushOrderDetail.getSiteName();
		
		Map<Long,String> asinMap = null;
		Object objMap = task.getParam("asinMap");
		if(objMap instanceof Map){
			asinMap = (Map<Long,String>)objMap;
		}
		if(asinMap == null){
			logger.error("asinMap is null");
			return;
		}
		logger.error("doService 开始新的一次调用任务:爬取物流");

		for (int i = 0; i < RETRY_COUNT; i++){
			AutoBuy autoBuy = null;
			if(brushOrderDetail.getExpressStatus()!=null && brushOrderDetail.getExpressStatus()>=5){
				if(brushOrderDetail.getSiteName().equalsIgnoreCase("amazon")){
					autoBuy = new AmazonAutoBuy(false);
				}else if(brushOrderDetail.getSiteName().equalsIgnoreCase("amazon.jp")){
					autoBuy = new AmazonJpAutoBuy(false);
				}
			}else{
				autoBuy = ClassUtil.getAutoBuy(mallName);
			}
			autoBuy.setAsinMap(asinMap);
			autoBuy.setTask(task);
			autoBuy.setTaskResult(taskResult);
			autoBuy.setOrderNo(brushOrderDetail.getOrderNo());
			if(autoBuy == null){
				logger.error("mallName:"+mallName+"找不到对应的Autobuy");
				brushOrderDetail.setStatus(AutoBuyStatus.AUTO_SCRIBE_SETUP_FAIL.getValue());
				return;
			}else{
				logger.error("获取到autobuy:"+autoBuy.getClass().getSimpleName());
			}
			autoBuy.setBrushOrderDetail(brushOrderDetail);
			try{
				AutoBuyStatus status = autoBuy.login(account.getPayAccount(), account.getLoginPwd());
				status = Utils.switchStatus(status);
				brushOrderDetail.setStatus(status.getValue());
				if (AutoBuyStatus.AUTO_SCRIBE_LOGIN_SUCCESS.equals(status)){
					boolean isFirst = true;
					for (int k = 0; k < RETRY_COUNT; k++){
						try{
							if (isFirst){
								isFirst = false;
							}
							else{
								autoBuy.gotoMainPage();
							}
							status = autoBuy.scribeExpress(brushOrderDetail);
							brushOrderDetail.setStatus(status.getValue());
							if (AutoBuyStatus.AUTO_FEED_BACK_SUCCESS.equals(status) || AutoBuyStatus.AUTO_REVIEW_SUCCESS.equals(status)){
								break;
							}
							if (AutoBuyStatus.AUTO_SCRIBE_SUCCESS.equals(status)){
								brushOrderDetail.setExpressCompany(autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY));
								brushOrderDetail.setExpressNo(autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO));
								break;
							}
							else{
								if (AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY.equals(status) || AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED.equals(status) || AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND.equals(status))
								{//这些错误不需要重试
									break;
								}
							}
						}
						catch (Exception e)
						{
							logger.debug("爬取订单物流碰到异常", e);
						}
					}
					break;
				}
				else
				{
					if (AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_NEED_AUTH.equals(status)){//这些错误不需要重试
						break;
					}
				}
			}
			catch (Exception e){
				logger.debug("ShipAmazonJPHandler.doService 碰到异常 = ", e);
			}
			finally{
				autoBuy.logout(true);
				logger.error("=================>ShipAmazonJPHandler成功完结<======================");
			}
		}
	}
}
