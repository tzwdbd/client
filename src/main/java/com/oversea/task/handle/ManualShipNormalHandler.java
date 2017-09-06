package com.oversea.task.handle;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.domain.AutoOrderExpressDetail;
import com.oversea.task.domain.AutoOrderLogin;
import com.oversea.task.domain.AutoOrderScribeExpress;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.service.order.ManualBuy;

@Component
public class ManualShipNormalHandler implements ManualShipHandler {
	final Logger logger = Logger.getLogger(getClass());

	@Override
	public void handle(Task task, TaskResult taskResult,List<RobotOrderDetail> orderDetails){
		OrderAccount account = (OrderAccount) task.getParam("account");
		AutoOrderLogin autoOrderLogin = (AutoOrderLogin) task.getParam("autoOrderLogin");
		AutoOrderScribeExpress autoOrderScribeExpress = (AutoOrderScribeExpress) task.getParam("autoOrderScribeExpress");
		AutoOrderExpressDetail autoOrderExpressDetail = (AutoOrderExpressDetail) task.getParam("autoOrderExpressDetail");
		
		if (account == null){
			logger.error("account is null");
			return;
		}
		RobotOrderDetail orderDetail = orderDetails.get(0);
		
		logger.error("ManualShipNormalHandler 开始新的一次调用任务:爬取物流");
		boolean mark = false;
		for (int i = 0; i < RETRY_COUNT; i++){
			ManualBuy manualBuy = new ManualBuy(false);
			manualBuy.setTask(task);
			manualBuy.setTaskResult(taskResult);
			manualBuy.setOrderDetailList(orderDetails);
			try {
				AutoBuyStatus status = manualBuy.login(account.getPayAccount(), account.getLoginPwd(), autoOrderLogin);
				if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
					if(autoOrderScribeExpress!=null){
						status = manualBuy.scribeExpress(orderDetail,autoOrderScribeExpress);
						if(AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED.equals(status)){
							orderDetail.setStatus(status.getValue());
						}else if(AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY.equals(status)){
							orderDetail.setStatus(status.getValue());
						}else if(AutoBuyStatus.AUTO_SCRIBE_SUCCESS.equals(status)){
							if(autoOrderExpressDetail!=null){
								status = manualBuy.expressDetail(orderDetail, autoOrderExpressDetail);
								orderDetail.setStatus(status.getValue());
								mark = true;
							}
						}
					}else{
						logger.debug("autoOrderScribeExpress 为空");
					}
					break;
				}
			} catch (Exception e) {
				logger.debug("ManualShipNormalHandler.doService 碰到异常 = ", e);
			}
			finally{
				if(mark){
					manualBuy.killFirefox();
				}
				logger.error("=================>ManualShipNormalHandler成功完结<======================");
			}
		}
	}
	private static void setOrderDetailListStatus(List<RobotOrderDetail> orderDetailList,int status){
		if(orderDetailList != null && orderDetailList.size() > 0){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setStatus(status);
				}
			}
		}
	}
	
}
