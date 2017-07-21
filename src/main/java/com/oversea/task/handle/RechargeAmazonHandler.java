package com.oversea.task.handle;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.service.order.AmazonAutoBuy;
import com.oversea.task.service.order.AutoBuy;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

@Component
public class RechargeAmazonHandler implements RechargeHandler {
	
	final Logger logger = Logger.getLogger(getClass());
	
	public void handle(TaskResult taskResult,Task task,List<GiftCard> giftCard){
		OrderAccount account = (OrderAccount) task.getParam("account");
		if (account == null){
			logger.error("account is null");
			return;
		}
		logger.error("doService 开始新的一次调用");
		
		taskResult.addParam("rechargeStatus", Boolean.valueOf(false));
		taskResult.addParam("accountId", account.getAccountId());
		for(int i =0;i<RETRY_COUNT;i++){
			AutoBuy autoBuy = new AmazonAutoBuy();
			logger.error("i = " + i);
			try{
				AutoBuyStatus status = autoBuy.login(account.getPayAccount(), account.getLoginPwd());
				status = Utils.switchStatus(status);
				if (AutoBuyStatus.AUTO_SCRIBE_LOGIN_SUCCESS.equals(status)){
					String balance = autoBuy.redeemGiftCard(giftCard);
					if(StringUtil.isNotEmpty(balance)){
						taskResult.addParam("balance", balance);
					}
					break;
				}else{
					logger.error("账号:"+account.getPayAccount()+"登录失败");
					if (AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_NEED_AUTH.equals(status)){//这些错误不需要重试
						break;
					}
				}
			}catch(Exception e){
				logger.debug("RechargeAmazonHandler.doService 碰到异常 = ", e);
			}finally{
				autoBuy.logout(false);
				logger.error("=================>RechargeAmazonHandler成功完结<======================");
			}
		}
	}
}
