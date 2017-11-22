package com.oversea.task.handle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.ExternalOrderDetail;
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
public class ExternalOrderNormalHandler implements ExternalOrderHandler {
	final Logger logger = Logger.getLogger(getClass());

	@Override
	public void handle(Task task, TaskResult taskResult,Object obj) {
		// TODO Auto-generated method stub
		OrderAccount account = (OrderAccount) task.getParam("account");
		String count = (String) task.getParam("count");
		UserTradeAddress address = (UserTradeAddress) task.getParam("address");
		String mallName = (String) task.getParam("mallName");
		OrderPayAccount payAccount = (OrderPayAccount)task.getParam("orderPayAccount");
		String expressAddress = (String)task.getParam("expressAddress");
		List<GiftCard> giftCard = (List<GiftCard>) task.getParam("giftCardList");
		Float rate = (Float)task.getParam("rate");
		String type = (String) task.getParam("type");
		
		List<ExternalOrderDetail> externalOrderDetailList  = null;
		if(obj instanceof List){
			try{
				externalOrderDetailList = (List)obj;
			}catch(Exception e){
				externalOrderDetailList = null;
				logger.error("orderDetailList typecast exception");
			}
		}
		if(externalOrderDetailList == null || externalOrderDetailList.size() == 0){
			logger.error("orderDetailList is null");
			return;
		}
		
		
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
		
		for(int i = 0;i<3;i++){
			boolean isScreenShot = true;
			AutoBuy autoBuy = null;
			if("zcn".equalsIgnoreCase(mallName)){
				autoBuy = new ZcnAutoBuy(true);
			}else{
				autoBuy = ClassUtil.getAutoBuy(mallName);
			}
			autoBuy.setTask(task);
			autoBuy.setTaskResult(taskResult);
			autoBuy.setOrderNo(externalOrderDetailList.get(0).getSaleOrderCode());
			if(autoBuy == null){
				logger.error("mallName:"+mallName+"找不到对应的Autobuy");
				for(ExternalOrderDetail externalOrderDetail : externalOrderDetailList){
					if(externalOrderDetail == null){
						continue;
					}
					externalOrderDetail.setStatus(AutoBuyStatus.AUTO_LOGIN_SETUP_FAIL.getValue());
				}
				return;
			}else{
				logger.error("获取到autobuy:"+autoBuy.getClass().getSimpleName());
			}
			if(!autoBuy.isInitSuccess()){
				return;
			}
			autoBuy.setExternalOrderDetailList(externalOrderDetailList);
			try{
				setOrderDetailListStatus(externalOrderDetailList,AutoBuyStatus.AUTO_ORDER_ING.getValue());
				AutoBuyStatus status = autoBuy.login(account.getPayAccount(), account.getLoginPwd());
				setOrderDetailListStatus(externalOrderDetailList,status.getValue());
				if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
					status = autoBuy.cleanCart();
					setOrderDetailListStatus(externalOrderDetailList,status.getValue());
					if (AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
						boolean isSelectSkuSuccess = true;
						boolean isFirst = true;
						String promotion = "";
						float myPrice = 0f ;
						
						for(ExternalOrderDetail externalOrderDetail : externalOrderDetailList){
							int num = 1;
							if(externalOrderDetail.getItemCount() != null){
								num = externalOrderDetail.getItemCount().intValue();
							}
							if(num < 1){
								num = 1;
							}
							try{
								myPrice += Float.parseFloat(externalOrderDetail.getRealPriceOrg()) * num *1.03;
							}catch(Exception ee){}
						}
						for(ExternalOrderDetail externalOrderDetail : externalOrderDetailList){
							Map<String, String> parmas = new HashMap<String, String>();
							String url = Utils.isEmpty(externalOrderDetail.getProductRebateUrl()) ? externalOrderDetail.getProductUrl() : externalOrderDetail.getProductRebateUrl();
							if (Utils.isEmpty(url)){
								logger.error("商品链接url为空");
								return;
							}
							
							//添加优惠码
							String promotionStr = externalOrderDetail.getDiscountCode();
							if(StringUtil.isNotEmpty(promotionStr)){
								promotion += promotionStr;
								promotion += ";";
							}
							if(mallName.equalsIgnoreCase("amazon")){
								if(myPrice>=25.75){
									parmas.put("addon", "1");
								}
							}else if (mallName.equalsIgnoreCase("amazon.jp")){
								if(myPrice>=2060){
									parmas.put("addon", "1");
								}
							}
							parmas.put("orginalUrl", externalOrderDetail.getProductUrl());
							parmas.put("isFirst", String.valueOf(isFirst));
							parmas.put("url", url);
							parmas.put("sku", externalOrderDetail.getItemAttr());
							parmas.put("num", String.valueOf(externalOrderDetail.getItemCount()));
							parmas.put("productEntityId", String.valueOf(externalOrderDetail.getId()));
							parmas.put("sign", "1");
							
							isFirst = false;
							status = autoBuy.selectProduct(parmas);
							externalOrderDetail.setStatus(status.getValue());
							if (AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
								externalOrderDetail.setSinglePrice(autoBuy.getPriceMap().get(String.valueOf(externalOrderDetail.getId())));
							}else{
								isSelectSkuSuccess = false;
								break;
							}
						}
						
						boolean isStock = false;
						if(isSelectSkuSuccess){
							
							
							ExternalOrderDetail externalOrderDetail = externalOrderDetailList.get(0);
							if(!StringUtil.isBlank(externalOrderDetail.getOrderAmount())){
								autoBuy.setTotalPrice(String.valueOf(Float.parseFloat(externalOrderDetail.getOrderAmount())/rate));
							}
							String isPrime = "yes".equalsIgnoreCase(account.getIsPrime()) ? String.valueOf(true) : String.valueOf(false);
							Map<String, String> params = new HashMap<String, String>();
							
							params.put("my_price", String.valueOf(myPrice));
							params.put("count", count);
							params.put("isPay", String.valueOf(true));
							params.put("size", String.valueOf(externalOrderDetailList.size()));
							params.put("isPrime", isPrime);
							params.put("expressAddress",expressAddress);
							params.put("userName", account.getPayAccount());
							params.put("password", account.getLoginPwd());
							params.put("promotion", promotion);
							params.put("suffixNo", account.getSuffixNo());
							params.put("isStock", String.valueOf(isStock));
							params.put("cardNo", account.getCardNo());
							params.put("type", type);
							
							
							status = autoBuy.pay(params,address,payAccount,giftCard);
							setOrderDetailListStatus(externalOrderDetailList,status.getValue());
							setOrderDetailListTotalPrice(externalOrderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE));
							if (AutoBuyStatus.AUTO_PAY_SUCCESS.equals(status)){
								//下单成功亚马逊不需要截图
								if("amazon".equalsIgnoreCase(mallName) || "amazon.jp".equalsIgnoreCase(mallName)){
									isScreenShot = false;
								}
								setOrderDetailListMallOrderNo(externalOrderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO));
								setOrderDetailListBalancewb(externalOrderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BALANCE_WB));
								break;
							}
							if(AutoBuyStatus.AUTO_PAY_GIFTCARD_IS_TAKEOFF.equals(status) || AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL.equals(status)){
								break;
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
	
	private static void setOrderDetailListTotalPrice(List<ExternalOrderDetail> externalOrderDetailList,String totalPrice){
		if(externalOrderDetailList != null && externalOrderDetailList.size() > 0 && StringUtil.isNotEmpty(totalPrice)){
			for(ExternalOrderDetail externalOrderDetail : externalOrderDetailList){
				if(externalOrderDetail != null){
					externalOrderDetail.setTotalPrice(totalPrice);
				}
			}
		}
	}
	
	private static void setOrderDetailListStatus(List<ExternalOrderDetail> externalOrderDetailList,int status){
		if(externalOrderDetailList != null && externalOrderDetailList.size() > 0){
			for(ExternalOrderDetail externalOrderDetail : externalOrderDetailList){
				if(externalOrderDetail != null){
					externalOrderDetail.setStatus(status);
				}
			}
		}
	}
	
	private static void setOrderDetailListBalancewb(List<ExternalOrderDetail> externalOrderDetailList,String balanceWb){
		if(externalOrderDetailList != null && externalOrderDetailList.size() > 0 && StringUtil.isNotEmpty(balanceWb)){
			for(ExternalOrderDetail externalOrderDetail : externalOrderDetailList){
				if(externalOrderDetail != null){
					externalOrderDetail.setBalanceWb(balanceWb);
				}
			}
		}
	}
	
	private static void setOrderDetailListMallOrderNo(List<ExternalOrderDetail> externalOrderDetailList,String mallOrderNo){
		if(externalOrderDetailList != null && externalOrderDetailList.size() > 0 && StringUtil.isNotEmpty(mallOrderNo)){
			for(ExternalOrderDetail externalOrderDetail : externalOrderDetailList){
				if(externalOrderDetail != null){
					externalOrderDetail.setMallOrderNo(mallOrderNo);
				}
			}
		}
	}

}
