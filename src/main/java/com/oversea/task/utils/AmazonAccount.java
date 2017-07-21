package com.oversea.task.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fengjian
 * @version V1.0
 * @title: oversea
 * @Package com.oversea.task.utils
 * @Description:
 * @date 16/6/5
 */
public class AmazonAccount {

    private static AtomicLong COUNT = new AtomicLong(0);

    public static final String[] ENDPOINTS = {"webservices.amazon.com", "ecs.amazonaws.jp", "webservices.amazonaws.co.uk"};

    public static final String[] ENDPOINT_DEF = {"us", "jp", "uk"};

    public static final String[] AWS_ACCESS_KEY_IDS =
    /* 5个账号*/        {"AKIAI7OK5G3AHUUB3CVQ", "AKIAILU4CB7UDOFDP4FQ",
            "AKIAIZ2W6WTHFJZ3P3AQ", "AKIAISCKAIVK7H43OWZQ",
            "AKIAIFCEOK4LYKRYMONA", "AKIAJGM6XEMX64E7POUQ",
            "AKIAIJ7KTYQX4LYY5QMA", "AKIAJCQ6BTLUKNV4TDYQ",
            "AKIAJOT4KLBFDN5ZLT7A", "AKIAICX3QB7JX6WJPNZQ"};

    public static final String[] AWS_SECRET_KEYS =
            {"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", "coLRLSiuPeUXMEbpB213Sc9uPrMcA3s5Enylsu4c",
                    "qm/VTK779z3up11DH/pNQzdD17jfyGaeYr8OoZ3/", "w4hnMtrscpfDGEcTuICcLGwNbHW6VZk8lYVNjxxi",
                    "cRz2dBsiOBp6GUNNHryTmmrNG8PlrOYiaFbGJyB/", "fnbSHNxcSKS1NMI/iXOhMzSGWQ5VUgE3y2wMSGPW",
                    "dSU923ny7GFxJDEr4uRPn6D2enXsP9OtA5t6duyy", "TlSg5dFKuFvC296v38HG6sLvzfgOLpT/HrA7rJ8W",
                    "L+fn7JTxXVr2TlRlKWKGVB5wUricB0PzZ2+IQPUs", "MYq0aK0icyUs+rEkqJAVKh86AWcFz8zUCHikY8S4"};

    private static Map<String, SignedRequestsHelper[]> signedRequestsHelperMap = new HashMap<String, SignedRequestsHelper[]>();

    private static AmazonAccount amazonAccount = new AmazonAccount();

    public static AmazonAccount getInstance() {
        return amazonAccount;
    }

    public AmazonAccount() {
        try {
            for (int j = 0; j < AmazonAccount.ENDPOINT_DEF.length; j++) {
                SignedRequestsHelper[] HEALPERS = new SignedRequestsHelper[AmazonAccount.AWS_ACCESS_KEY_IDS.length];
                for (int i = 0; i < AmazonAccount.AWS_ACCESS_KEY_IDS.length; i++) {
                    SignedRequestsHelper HEALPER = SignedRequestsHelper.getInstance(AmazonAccount.ENDPOINTS[j], AmazonAccount.AWS_ACCESS_KEY_IDS[i], AmazonAccount.AWS_SECRET_KEYS[i]);
                    HEALPERS[i] = HEALPER;
                }
                signedRequestsHelperMap.put(AmazonAccount.ENDPOINT_DEF[j], HEALPERS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SignedRequestsHelper getSignedRequestsHelper(String co) {
        return signedRequestsHelperMap.get(co)[(int) (COUNT.getAndAdd(1) % AmazonAccount.AWS_ACCESS_KEY_IDS.length)];
    }
}
