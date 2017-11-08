package com.oversea.task.handle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.BrushInfo;
import com.oversea.task.domain.BrushOrderDetail;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.OrderCreditCard;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.service.order.ZcnAutoBuy;
import com.oversea.task.service.order.AutoBuy;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.ClassUtil;
import com.oversea.task.utils.Utils;

@Component
public class BrushOrderNormalHandler implements BrushOrderHandler {
	final Logger logger = Logger.getLogger(getClass());

	@Override
	public void handle(Task task, TaskResult taskResult,Object obj) {
		// TODO Auto-generated method stub
		OrderAccount account = (OrderAccount) task.getParam("account");
		String count = (String) task.getParam("count");
		Boolean isPay = (Boolean) task.getParam("isPay");
		UserTradeAddress address = (UserTradeAddress) task.getParam("address");
		String mallName = (String) task.getParam("mallName");
		OrderPayAccount payAccount = (OrderPayAccount)task.getParam("orderPayAccount");
		String expiryDate = (String)task.getParam("expiryDate");
		String expressAddress = (String)task.getParam("expressAddress");
		OrderCreditCard orderCreditCard = (OrderCreditCard)task.getParam("orderCreditCard");
		List<GiftCard> giftCard = (List<GiftCard>) task.getParam("giftCardList");
		BrushInfo brushInfo = (BrushInfo)task.getParam("brushInfo");
		
		BrushOrderDetail  brushOrderDetail = (BrushOrderDetail) obj;
		
		brushOrderDetail.setStatus(AutoBuyStatus.AUTO_LOGIN_SETUP_FAIL.getValue());
		if (account == null)
		{
			logger.error("account is null");
			return;
		}
		if (Utils.isEmpty(count))
		{
			count = "0";
		}
		

		logger.error("doService 开始新的一次调用");
		
		boolean isScreenShot = true;
		AutoBuy autoBuy = null;
		if("zcn".equalsIgnoreCase(mallName)){
			autoBuy = new ZcnAutoBuy(true);
		}else{
			autoBuy = ClassUtil.getAutoBuy(mallName);
		}
		Map<Long,String> asinMap = null;
		Object objMap = task.getParam("asinMap");
		if(objMap instanceof Map){
			asinMap = (Map<Long,String>)objMap;
		}
		autoBuy.setAsinMap(asinMap);
		autoBuy.setTask(task);
		autoBuy.setTaskResult(taskResult);
		autoBuy.setOrderNo(brushOrderDetail.getOrderNo());
		if(autoBuy == null){
			logger.error("mallName:"+mallName+"找不到对应的Autobuy");
			brushOrderDetail.setStatus(AutoBuyStatus.AUTO_LOGIN_SETUP_FAIL.getValue());
			return;
		}else{
			logger.error("获取到autobuy:"+autoBuy.getClass().getSimpleName());
		}
		if(!autoBuy.isInitSuccess()){
			return;
		}
		autoBuy.setBrushOrderDetail(brushOrderDetail);
		try{
			brushOrderDetail.setStatus(AutoBuyStatus.AUTO_ORDER_ING.getValue());
			AutoBuyStatus status = autoBuy.login(account.getPayAccount(), account.getLoginPwd());
			brushOrderDetail.setStatus(status.getValue());
			if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
				status = autoBuy.cleanCart();
				brushOrderDetail.setStatus(status.getValue());
				if (AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
					boolean isSelectSkuSuccess = true;
					boolean isFirst = true;
					String promotion = "";
					Map<String, String> parmas = new HashMap<String, String>();
					String url = Utils.isEmpty(brushOrderDetail.getProductRebateUrl()) ? brushOrderDetail.getProductUrl() : brushOrderDetail.getProductRebateUrl();
					if (Utils.isEmpty(url)){
						logger.error("商品链接url为空");
						return;
					}
					//添加优惠码
					parmas.put("orginalUrl", brushOrderDetail.getProductUrl());
					parmas.put("isFirst", String.valueOf(isFirst));
					parmas.put("url", url);
					parmas.put("sku", brushOrderDetail.getProductSku());
					parmas.put("num", String.valueOf(brushOrderDetail.getNum()));
					parmas.put("productEntityId", String.valueOf(brushOrderDetail.getProductEntityId()));
					parmas.put("sign", "0");
					parmas.put("productName",brushInfo.getKeyWord());
					parmas.put("title",brushInfo.getProductName());
					parmas.put("position",brushInfo.getProductLocate());
					
					isFirst = false;
					status = autoBuy.selectProduct(parmas);
					logger.error("hander选择完成");
					brushOrderDetail.setStatus(status.getValue());
					if (AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
						//brushOrderDetail.setSinglePrice(autoBuy.getPriceMap().get(String.valueOf(brushOrderDetail.getProductEntityId())));
					}else{
						isSelectSkuSuccess = false;
					}
					
					boolean isStock = false;
					if(isSelectSkuSuccess){
						float myPrice = 0f ;
						
						try{
							myPrice += Float.parseFloat(brushOrderDetail.getSinglePrice()) * brushOrderDetail.getNum();
						}catch(Exception ee){}
						
						String isPrime = "yes".equalsIgnoreCase(account.getIsPrime()) ? String.valueOf(true) : String.valueOf(false);
						Map<String, String> params = new HashMap<String, String>();
						params.put("my_price", String.valueOf(myPrice));
						params.put("count", count);
						params.put("isPay", String.valueOf(isPay));
						params.put("cardNo", account.getCardNo());
						params.put("isPrime", isPrime);
						params.put("expiryDate",expiryDate);
						params.put("expressAddress",expressAddress);
						params.put("userName", account.getPayAccount());
						params.put("password", account.getLoginPwd());
						params.put("promotion", promotion);
						params.put("suffixNo", account.getSuffixNo());
						params.put("isStock", String.valueOf(isStock));
						params.put("cardNo", account.getCardNo());
						params.put("payType",brushOrderDetail.getPayType() );
						params.put("review",brushOrderDetail.getIsDirect());
						if(orderCreditCard != null){
							params.put("owner", orderCreditCard.getOwner());
						}
						
						
						status = autoBuy.pay(params,address,payAccount,giftCard);
						brushOrderDetail.setStatus(status.getValue());
						brushOrderDetail.setTotalPrice(autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE));
						if (AutoBuyStatus.AUTO_PAY_SUCCESS.equals(status)){
							//下单成功亚马逊不需要截图
							if("amazon".equalsIgnoreCase(mallName) || "amazon.jp".equalsIgnoreCase(mallName)){
								isScreenShot = false;
							}
							brushOrderDetail.setMallOrderNo(autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO));
							brushOrderDetail.setBalanceWb(autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BALANCE_WB));
						}
					}
				}
			}
		}catch(Exception e){
			logger.debug("AutomanOrderExecutor.doService 碰到异常 = ", e);
		}finally{
			autoBuy.logout(isScreenShot);
			logger.debug("=================>AutomanOrderExecutor成功完结<======================");
			logger.debug(obj.toString());
		}
	}
}
