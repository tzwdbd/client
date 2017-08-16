package com.oversea.task.handle;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.domain.OrderAccount;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.service.order.AmazonAutoBuy;
import com.oversea.task.service.order.AmazonJpAutoBuy;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

@Component
public class CheckGiftCardAmazonJPHandler implements GiftCardCheckHandler {
	
	final Logger logger = Logger.getLogger(getClass());
	

	@Override
	public void handle(Task task, TaskResult taskResult) {
		OrderAccount account = (OrderAccount) task.getParam("account");
		if (account == null){
			logger.error("account is null");
			return;
		}
		
		logger.error("doService 开始新的一次调用任务:爬取物流");

		AmazonJpAutoBuy autoBuy = new AmazonJpAutoBuy();
		try {
			AutoBuyStatus status = autoBuy.login(account.getPayAccount(), account.getLoginPwd());
			status = Utils.switchStatus(status);
			if (AutoBuyStatus.AUTO_SCRIBE_LOGIN_SUCCESS.equals(status)){
				String balance = autoBuy.checkGiftCard();
				if(!StringUtil.isBlank(balance)){
					account.setBalanceWb(Double.parseDouble(balance));
					taskResult.setValue(account);
				}
			}
		}catch (Exception e){
			logger.debug("CheckGiftCardAmazonHandler.doService 碰到异常 = ", e);
		}finally{
			autoBuy.logout(true);
			logger.error("=================>CheckGiftCardAmazonHandler成功完结<======================");
		}
	}
}
