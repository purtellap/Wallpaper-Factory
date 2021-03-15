package com.austinpurtell.wf.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.austinpurtell.wf.BillingClientSetup;
import com.austinpurtell.wf.IProductClickListener;
import com.austinpurtell.wf.R;

import java.util.Arrays;
import java.util.List;

public class SupportFragment extends Fragment implements PurchasesUpdatedListener {

    View view;
    BillingClient billingClient;
    ConsumeResponseListener consumeResponseListener;
    List<SkuDetails> skuDetailsList;
    IProductClickListener iProductClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_support, container, false);
        setHasOptionsMenu(true);

        setupBillingClient();

 /*       final Activity a = this.getActivity();

        View.OnClickListener supportListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                *//*BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetailsList.get(i)).build();
                billingClient.launchBillingFlow(a, billingFlowParams);*//*

            }};*/

        if(billingClient.isReady()){
            SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(Arrays.asList("dono_1","dono_5","dono_10"))
                    .setType(BillingClient.SkuType.INAPP).build();
            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        skuDetailsList = list;
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
                    if (billingClient.isReady()) {
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

        //Log.d("list size", skuDetailsList.size()+"");
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

/*    public void setiProductClickListener(IProductClickListener iProductClickListener){
        this.iProductClickListener = iProductClickListener;
    }*/

    private void setupBillingClient(){

        consumeResponseListener =  (ConsumeResponseListener) new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    Toast.makeText(SupportFragment.this.getActivity(), "Success 1", Toast.LENGTH_SHORT).show();
                }
            }
        };

        billingClient = BillingClientSetup.getInstance(getContext(), this);
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    Toast.makeText(SupportFragment.this.getActivity(), "Success Connection", Toast.LENGTH_SHORT).show();
                    //Query
                    List<Purchase> purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                            .getPurchasesList();
                    //handleItemAlreadyPurchased(purchases);
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

/*    private void handleItemAlreadyPurchased(List<Purchase> purchases) {
        StringBuilder purchasedItem = new StringBuilder(txtPremium.getText());
        for(Purchase purchase : purchases){
            if(purchase.getSku().equals("purchase name")){
                ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.consumeAsync(consumeParams, consumeResponseListener);
            }
            purchasedItem.append("\n"+purchase.getSku()+"\n");
        }
        txtPremium.setText(purchasedItem.toString());
        txtPremium.setVisibility(View.VISIBLE);
    }*/

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            Toast.makeText(this.getActivity(), "Successful", Toast.LENGTH_SHORT).show();
            //handleItemAlreadyPurchased(list);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this.getActivity(), "User cancelled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getActivity(), "Error: " + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
        }
    }
}
