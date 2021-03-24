package com.austinpurtell.wf.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.austinpurtell.wf.R;

import java.util.Arrays;
import java.util.List;

public class SupportFragment extends Fragment {

    View view;
    private BillingClient billingClient;
    private ConsumeResponseListener consumeResponseListener;
    private List<SkuDetails> skuDetailsList;

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

        }
    };

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                Toast.makeText(view.getContext(), "Successful", Toast.LENGTH_SHORT).show();
                //handleItemAlreadyPurchased(list);
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(view.getContext(), "User cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), "Error: " + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void setupBillingClient(){

        billingClient = BillingClient.newBuilder(view.getContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        consumeResponseListener =  (ConsumeResponseListener) new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    Toast.makeText(SupportFragment.this.getActivity(), "Success 1", Toast.LENGTH_SHORT).show();
                }
            }
        };

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    Toast.makeText(SupportFragment.this.getActivity(), "Successful Connection", Toast.LENGTH_SHORT).show();
                    //Query
                    List<Purchase> purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList();
                    Log.d("purchases size", purchases.size() + "");
                    handleItemPurchased(purchases);
                }
                else{
                    Toast.makeText(SupportFragment.this.getActivity(), "Failed Connection:" + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(SupportFragment.this.getActivity(), "Billing service disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_support, container, false);
        setHasOptionsMenu(true);

        setupBillingClient();

        Log.d("isready", billingClient.isReady() + "");
        if(billingClient.isReady()){
            SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(Arrays.asList("dono_1","dono_5","dono_10"))
                    .setType(BillingClient.SkuType.INAPP).build();
            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        skuDetailsList = list;
                        Log.d("list size", list.size() + "");
                        Toast.makeText(SupportFragment.this.getActivity(), "list size: " + list.size(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SupportFragment.this.getActivity(), "Error:" + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        View.OnClickListener supportListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            //Toast.makeText(SupportFragment.this.getActivity(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                try {
                    Toast.makeText(SupportFragment.this.getActivity(), "ready: " + billingClient.isReady(), Toast.LENGTH_SHORT).show();
                    if (billingClient.isReady()) {
                        Log.d("list size", skuDetailsList.size()+"");
                        if (v == view.findViewById(R.id.support1)) {
                            handleBilling(0);
                        } else if (v == view.findViewById(R.id.support5)) {
                            handleBilling(1);
                        } else if (v == view.findViewById(R.id.support10)) {
                            handleBilling(2);
                        }
                    }
                }
                catch (Exception e){
                    Toast.makeText(SupportFragment.this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                //startActivity(new Intent(SupportFragment.this.getActivity(), PurchaseItemActivity.class));
            }
        };

        Button button1 = view.findViewById(R.id.support1);
        button1.setOnClickListener(supportListener);

        Button button5 = view.findViewById(R.id.support5);
        button5.setOnClickListener(supportListener);

        Button button10 = view.findViewById(R.id.support10);
        button10.setOnClickListener(supportListener);

        return view;

    }


    private void handleBilling(int index) {

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(this.skuDetailsList.get(index)).build();
        int response = billingClient.launchBillingFlow(this.getActivity(), billingFlowParams).getResponseCode();
        switch (response){

            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                Toast.makeText(this.getActivity(), "Error: Billing unavailable", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                Toast.makeText(this.getActivity(), "Error: Dev error... whoops!", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                Toast.makeText(this.getActivity(), "Error: Feature not supported", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                Toast.makeText(this.getActivity(), "Error: Already owned", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                Toast.makeText(this.getActivity(), "Error: Service disconnected", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                Toast.makeText(this.getActivity(), "Error: Service timed out", Toast.LENGTH_SHORT).show();
                break;
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                Toast.makeText(this.getActivity(), "Error: Item unavailable", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

    }

    private void handleItemPurchased(List<Purchase> purchases) {
        for(Purchase purchase : purchases){
            handlePurchase(purchase);
           /* if(purchase.getSku().equals("dono_1")){
                ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.consumeAsync(consumeParams, consumeResponseListener);
            }*/
        }
    }

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    /*@Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> list) {

    }*/
}
