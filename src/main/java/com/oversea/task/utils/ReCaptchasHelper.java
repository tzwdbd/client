package com.oversea.task.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class ReCaptchasHelper {
	
	public static final String IP1 = "47.88.24.38";
	public static final String IP2 = "47.88.0.160";
	public static final String PORT = "8080";
	public static final String FAILCAMP_STRING = "FAIL";
	private static Logger log = Logger.getLogger(ReCaptchasHelper.class);
	
	public static String getCaptchas(String imageUrl, String platform) {
		try {
			Map<String, String> params = new HashMap<>();
			params.put("imgSrc", imageUrl);
			params.put("from", IP2);
			params.put("platform", platform);
			RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(90000).setSocketTimeout(90000);
			RequestConfig requestConfig = builder.build();
			HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
			HttpPost post = null;
//			if (((int) (1 + Math.random() * 10)) % 2 == 1) {
//				post = new HttpPost("http://" + IP1 + ":" + PORT + "/ocr2");
//			} else {
//				post = new HttpPost("http://" + IP2 + ":" + PORT + "/ocr2");
//			}
			post = new HttpPost("http://" + IP2 + ":" + PORT + "/ocr2");
			List<NameValuePair> pairParams = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> enty : params.entrySet()) {
				pairParams.add(new BasicNameValuePair(enty.getKey(), enty.getValue()));
			}
			post.setEntity(new UrlEncodedFormEntity(pairParams));
			HttpResponse response = client.execute(post);
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			log.error("ReCaptchasHelper_error", e);
			return FAILCAMP_STRING;
		}
	}
	
	public static boolean isFail(String cap) {
		return FAILCAMP_STRING.equals(cap);
	}
	
	public static void main(String[] args) {
		try {
			String imageUrl = "https://www.google.com/recaptcha/api/image?c=03AOP2lf4sfdw_fQ-o0ZtQUaJvHhcC9VXBCUFWLaulh4yt-MSnT-wyY3ipXKaJTKT-VZGfEBx2aNsaXuW3y64BgI52a1ssxEoPszP3MgjiLQzGytPGbb1AxcqBld7bzCJNh8fHiNqwrMHVfycbjY5FEEjwKuLEt4QLJK1hP_0rgTVuZ5qh_kWqk2xFYN1eyMle11DR43rYo2Pd&amp;th=,eIlXiZ6wkSv61p-Ltnq2YNDGyiFfKg79AAQVBTwGawIIMzrNnMwhlZ2f8Qlwgro2Op_n0mbo_HFMLQ9J2wR9U9dj5meyZhSrxxUQDjJ6zfaFO8lQB2tnN-MoA-ac4troSlIGBAZ22EtWHq0RXLKiSUbTD49gdC32C3thBC9oKbdXUbZMXA96ZwYvm2p_65qM1Mof6yRNuyKOc1CfWRgz6RA2aKsi8LSWRHo5CXM846dM1n_a4KnqZZxAFXTT_vRecepkNGRXZ2n5cwvYU3YvSUI3R6Xm-UnfQjOXdv9UDHK4OP1sEdgexw7ost5sbsT9tjIlj5Nt5wcj_pu4saC46PJSxeXmlbZsDLBCcMXOV-uN-noo3zdmiKMtGVbH1XA8pIWOLIF4VR_iGGPneUI-ThuNQfnJ_XivGzFFlou6VRCAKb_dotyFofIxXgWJBy96Cwqe-aCeUAG90tob-A6R6u3pPeduq8YVliVGahstS8MchnefzOBcr-7vETgRwAE1v227qVM1GbKUDrSpvlo8_lcn8PErKChk05pRHD_NTYnLFtIkZheQf8x6IhM8PSZaonNxMT8nuzQ39Z_hk3ikCeXNx7I_4H5f1QLUdLERa5_FOrNhX7zIlc9CCag40GQ1DluWQNvM6xlsyqmvNYCaBZPnQitEXkdLBuc_rGoke39R3DrTHts9srLN3GKcTC9dtM5ixWAWad7rqnidE0j4OYq-ThErXP5A7A";
			System.out.println(getCaptchas(imageUrl,"iherb"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
