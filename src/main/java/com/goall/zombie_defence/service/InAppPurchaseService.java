package com.goall.zombie_defence.service;

import com.goall.zombie_defence.googleCredentials.GoogleCredentialsConfig;
import com.goall.zombie_defence.mapper.GoodsMapper;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Service
public class InAppPurchaseService {

    private final GoogleCredentialsConfig googleCredentialsConfig;

    @Autowired
    public InAppPurchaseService(GoogleCredentialsConfig googleCredentialsConfig) {
        Objects.requireNonNull(googleCredentialsConfig, "googleCredentialsConfig must not be null");
        this.googleCredentialsConfig = googleCredentialsConfig;
    }

    public boolean googleInAppPurchaseVerify(Map<String, Object> receiptRequest) throws GeneralSecurityException, IOException {
        AndroidPublisher publisher = googleCredentialsConfig.androidPublisher();
        AndroidPublisher.Purchases.Products.Get get = publisher.purchases().products()
                .get(String.valueOf(receiptRequest.get("packageName")), String.valueOf(receiptRequest.get("productId")),
                        String.valueOf(receiptRequest.get("purchaseToken")));
        ProductPurchase purchase = get.execute();

        if (purchase.getPurchaseState() == 1) {
            throw new IllegalArgumentException("purchase_canceled");
        }
        return true;
    }

}
