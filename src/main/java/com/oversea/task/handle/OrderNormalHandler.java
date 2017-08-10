package com.oversea.task.handle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.AutoBuyConst;
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
public class OrderNormalHandler implements OrderHandler {
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
		Float rate = (Float)task.getParam("rate");
		String type = (String) task.getParam("type");
		
		List<RobotOrderDetail> orderDetailList = null;
		if(obj instanceof List){
			try{
				orderDetailList = (List)obj;
			}catch(Exception e){
				orderDetailList = null;
				logger.error("orderDetailList typecast exception");
			}
		}
		if(orderDetailList == null || orderDetailList.size() == 0){
			logger.error("orderDetailList is null");
			return;
		}
		
		setOrderDetailListStatus(orderDetailList, AutoBuyStatus.AUTO_LOGIN_SETUP_FAIL.getValue());
		
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
			autoBuy.setOrderNo(orderDetailList.get(0).getOrderNo());
			if(autoBuy == null){
				logger.error("mallName:"+mallName+"找不到对应的Autobuy");
				for(RobotOrderDetail orderDetail : orderDetailList){
					if(orderDetail == null){
						continue;
					}
					orderDetail.setStatus(AutoBuyStatus.AUTO_LOGIN_SETUP_FAIL.getValue());
				}
				return;
			}else{
				logger.error("获取到autobuy:"+autoBuy.getClass().getSimpleName());
			}
			if(!autoBuy.isInitSuccess()){
				return;
			}
			autoBuy.setOrderDetailList(orderDetailList);
			try{
				setOrderDetailListStatus(orderDetailList,AutoBuyStatus.AUTO_ORDER_ING.getValue());
				AutoBuyStatus status = autoBuy.login(account.getPayAccount(), account.getLoginPwd());
				setOrderDetailListStatus(orderDetailList,status.getValue());
				if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
					status = autoBuy.cleanCart();
					setOrderDetailListStatus(orderDetailList,status.getValue());
					if (AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
						boolean isSelectSkuSuccess = true;
						boolean isFirst = true;
						String promotion = "";
						for(RobotOrderDetail orderDetail : orderDetailList){
							if(orderDetail == null){
								continue;
							}
							Map<String, String> parmas = new HashMap<String, String>();
							String url = Utils.isEmpty(orderDetail.getProductRebateUrl()) ? orderDetail.getProductUrl() : orderDetail.getProductRebateUrl();
							if (Utils.isEmpty(url)){
								logger.error("商品链接url为空");
								return;
							}
							//添加优惠码
							String promotionStr = orderDetail.getPromotionCodeList();
							if(StringUtil.isNotEmpty(promotionStr)){
								promotion += promotionStr;
								promotion += ";";
							}
							parmas.put("orginalUrl", orderDetail.getProductUrl());
							parmas.put("isFirst", String.valueOf(isFirst));
							parmas.put("url", url);
							parmas.put("sku", orderDetail.getProductSku());
							parmas.put("num", String.valueOf(orderDetail.getNum()));
							parmas.put("productEntityId", String.valueOf(orderDetail.getProductEntityId()));
							
							isFirst = false;
							status = autoBuy.selectProduct(parmas);
							orderDetail.setStatus(status.getValue());
							if (AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
								orderDetail.setSinglePrice(autoBuy.getPriceMap().get(String.valueOf(orderDetail.getProductEntityId())));
							}else{
								isSelectSkuSuccess = false;
								break;
							}
						}
						
						boolean isStock = false;
						if(isSelectSkuSuccess){
							float myPrice = 0f ;
							
							for(RobotOrderDetail orderDetail : orderDetailList){
								if(orderDetail == null){
									continue;
								}
								int num = 1;
								if(orderDetail.getNum() != null){
									num = orderDetail.getNum().intValue();
								}
								if(num < 1){
									num = 1;
								}
								try{
									myPrice += Float.parseFloat(orderDetail.getMyPrice()) * num;
								}catch(Exception ee){}
							}
							if(!StringUtil.isBlank(orderDetailList.get(0).getTotalPromotion())){
								myPrice = myPrice - (Float.parseFloat(orderDetailList.get(0).getTotalPromotion())/rate);
							}
							RobotOrderDetail robotOrderDetail = orderDetailList.get(0);
							if(robotOrderDetail != null && "1".equals(robotOrderDetail.getIsStockpile())){
								isStock = true;
							}
							
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
							params.put("type", type);
							if(orderCreditCard != null){
								params.put("owner", orderCreditCard.getOwner());
							}
							
							
							status = autoBuy.pay(params,address,payAccount,giftCard);
							setOrderDetailListStatus(orderDetailList,status.getValue());
							if(!autoBuy.getStatusMap().isEmpty()){
								Map<String, Integer> map = autoBuy.getStatusMap();
								Iterator<Map.Entry<String, Integer>> entries = map.entrySet().iterator();
								while (entries.hasNext()) {  
								    Map.Entry<String, Integer> entry = entries.next();
								    String productEntityId = entry.getKey();
								    if(StringUtil.isNotEmpty(productEntityId)){
								    	logger.error("pay.productEntityId = "+productEntityId);
								    	for(RobotOrderDetail orderDetail : orderDetailList){
								    		logger.error("orderDetail.getProductEntityId() = "+orderDetail.getProductEntityId());
											if(orderDetail != null && productEntityId.equals(String.valueOf(orderDetail.getProductEntityId()))){
												orderDetail.setStatus(entry.getValue());
												break;
											}
										}
								    }
								}  
							}
							setOrderDetailListTotalPrice(orderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE));
							setOrderDetailListMallFee(orderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_MALL_EXPRESS_FEE));
							setOrderDetailListPromotionFee(orderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PROMOTION_FEE));
							if (AutoBuyStatus.AUTO_PAY_SUCCESS.equals(status)){
								//下单成功亚马逊不需要截图
								if("amazon".equalsIgnoreCase(mallName) || "amazon.jp".equalsIgnoreCase(mallName)){
									isScreenShot = false;
								}
								setOrderDetailListMallOrderNo(orderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO));
								setOrderDetailListBalancewb(orderDetailList, autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BALANCE_WB));
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
	
	private static void setOrderDetailListStatus(List<RobotOrderDetail> orderDetailList,int status){
		if(orderDetailList != null && orderDetailList.size() > 0){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setStatus(status);
				}
			}
		}
	}
	private static void setOrderDetailListTotalPrice(List<RobotOrderDetail> orderDetailList,String totalPrice){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(totalPrice)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setTotalPrice(totalPrice);
				}
			}
		}
	}
	private static void setOrderDetailListMallOrderNo(List<RobotOrderDetail> orderDetailList,String mallOrderNo){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(mallOrderNo)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setMallOrderNo(mallOrderNo);
				}
			}
		}
	}
	private static void setOrderDetailListMallFee(List<RobotOrderDetail> orderDetailList,String mallFee){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(mallFee)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setMallExpressFee(mallFee);
				}
			}
		}
	}
	private static void setOrderDetailListPromotionFee(List<RobotOrderDetail> orderDetailList,String promotionFee){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(promotionFee)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setPromotionFee(promotionFee);
				}
			}
		}
	}
	
	private static void setOrderDetailListBalancewb(List<RobotOrderDetail> orderDetailList,String balanceWb){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(balanceWb)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setBalanceWb(balanceWb);
				}
			}
		}
	}

}
