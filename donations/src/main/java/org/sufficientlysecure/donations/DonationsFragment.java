/*
 * Copyright (C) 2011-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sufficientlysecure.donations;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.sufficientlysecure.donations.google.util.IabHelper;
import org.sufficientlysecure.donations.google.util.IabResult;
import org.sufficientlysecure.donations.google.util.Purchase;

public class DonationsFragment extends Fragment {

    public static final String ARG_DEBUG = "debug";

    public static final String ARG_GOOGLE_ENABLED = "googleEnabled";
    public static final String ARG_GOOGLE_PUBKEY = "googlePubkey";
    public static final String ARG_GOOGLE_CATALOG = "googleCatalog";
    public static final String ARG_GOOGLE_CATALOG_VALUES = "googleCatalogValues";

    public static final String ARG_PAYPAL_ENABLED = "paypalEnabled";
    public static final String ARG_PAYPAL_USER = "paypalUser";
    public static final String ARG_PAYPAL_CURRENCY_CODE = "paypalCurrencyCode";
    public static final String ARG_PAYPAL_ITEM_NAME = "mPaypalItemName";

    public static final String ARG_FLATTR_ENABLED = "flattrEnabled";
    public static final String ARG_FLATTR_PROJECT_URL = "flattrProjectUrl";
    public static final String ARG_FLATTR_URL = "flattrUrl";

    public static final String ARG_BITCOIN_ENABLED = "bitcoinEnabled";
    public static final String ARG_BITCOIN_ADDRESS = "bitcoinAddress";

    private static final String TAG = "Donations Library";

    // http://developer.android.com/google/play/billing/billing_testing.html
    private static final String[] CATALOG_DEBUG = new String[]{"android.test.purchased",
            "android.test.canceled", "android.test.refunded", "android.test.item_unavailable"};

    private Spinner mGoogleSpinner;
    private TextView mFlattrUrlTextView;

    // Google Play helper object
    private IabHelper mHelper;

    protected boolean mDebug = false;

    protected boolean mGoogleEnabled = false;
    protected String mGooglePubkey = "";
    protected String[] mGgoogleCatalog = new String[]{};
    protected String[] mGoogleCatalogValues = new String[]{};

    protected boolean mPaypalEnabled = false;
    protected String mPaypalUser = "";
    protected String mPaypalCurrencyCode = "";
    protected String mPaypalItemName = "";

    protected boolean mFlattrEnabled = false;
    protected String mFlattrProjectUrl = "";
    protected String mFlattrUrl = "";

    protected boolean mBitcoinEnabled = false;
    protected String mBitcoinAddress = "";

    /**
     * Instantiate DonationsFragment.
     *
     * @param debug               You can use BuildConfig.DEBUG to propagate the debug flag from your app to the Donations library
     * @param googleEnabled       Enabled Google Play donations
     * @param googlePubkey        Your Google Play public key
     * @param googleCatalog       Possible item names that can be purchased from Google Play
     * @param googleCatalogValues Values for the names
     * @param paypalEnabled       Enable PayPal donations
     * @param paypalUser          Your PayPal email address
     * @param paypalCurrencyCode  Currency code like EUR. See here for other codes:
     *                            https://developer.paypal.com/webapps/developer/docs/classic/api/currency_codes/#id09A6G0U0GYK
     * @param paypalItemName      Display item name on PayPal, like "Donation for NTPSync"
     * @param flattrEnabled       Enable Flattr donations
     * @param flattrProjectUrl    The project URL used on Flattr
     * @param flattrUrl           The Flattr URL to your thing. NOTE: Enter without http://
     * @param bitcoinEnabled      Enable bitcoin donations
     * @param bitcoinAddress      The address to receive bitcoin
     * @return DonationsFragment
     */
    public static DonationsFragment newInstance(boolean debug, boolean googleEnabled, String googlePubkey, String[] googleCatalog,
                                                String[] googleCatalogValues, boolean paypalEnabled, String paypalUser,
                                                String paypalCurrencyCode, String paypalItemName, boolean flattrEnabled,
                                                String flattrProjectUrl, String flattrUrl, boolean bitcoinEnabled, String bitcoinAddress) {
        DonationsFragment donationsFragment = new DonationsFragment();
        Bundle args = new Bundle();

        args.putBoolean(ARG_DEBUG, debug);

        args.putBoolean(ARG_GOOGLE_ENABLED, googleEnabled);
        args.putString(ARG_GOOGLE_PUBKEY, googlePubkey);
        args.putStringArray(ARG_GOOGLE_CATALOG, googleCatalog);
        args.putStringArray(ARG_GOOGLE_CATALOG_VALUES, googleCatalogValues);

        args.putBoolean(ARG_PAYPAL_ENABLED, paypalEnabled);
        args.putString(ARG_PAYPAL_USER, paypalUser);
        args.putString(ARG_PAYPAL_CURRENCY_CODE, paypalCurrencyCode);
        args.putString(ARG_PAYPAL_ITEM_NAME, paypalItemName);

        args.putBoolean(ARG_FLATTR_ENABLED, flattrEnabled);
        args.putString(ARG_FLATTR_PROJECT_URL, flattrProjectUrl);
        args.putString(ARG_FLATTR_URL, flattrUrl);

        args.putBoolean(ARG_BITCOIN_ENABLED, bitcoinEnabled);
        args.putString(ARG_BITCOIN_ADDRESS, bitcoinAddress);

        donationsFragment.setArguments(args);
        return donationsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDebug = getArguments().getBoolean(ARG_DEBUG);

        mGoogleEnabled = getArguments().getBoolean(ARG_GOOGLE_ENABLED);
        mGooglePubkey = getArguments().getString(ARG_GOOGLE_PUBKEY);
        mGgoogleCatalog = getArguments().getStringArray(ARG_GOOGLE_CATALOG);
        mGoogleCatalogValues = getArguments().getStringArray(ARG_GOOGLE_CATALOG_VALUES);

        mPaypalEnabled = getArguments().getBoolean(ARG_PAYPAL_ENABLED);
        mPaypalUser = getArguments().getString(ARG_PAYPAL_USER);
        mPaypalCurrencyCode = getArguments().getString(ARG_PAYPAL_CURRENCY_CODE);
        mPaypalItemName = getArguments().getString(ARG_PAYPAL_ITEM_NAME);

        mFlattrEnabled = getArguments().getBoolean(ARG_FLATTR_ENABLED);
        mFlattrProjectUrl = getArguments().getString(ARG_FLATTR_PROJECT_URL);
        mFlattrUrl = getArguments().getString(ARG_FLATTR_URL);

        mBitcoinEnabled = getArguments().getBoolean(ARG_BITCOIN_ENABLED);
        mBitcoinAddress = getArguments().getString(ARG_BITCOIN_ADDRESS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.donations__fragment, container, false);
    }

    @TargetApi(11)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* Google */
        /*if (mGoogleEnabled) {
            ViewStub googleViewStub = getActivity().findViewById(R.id.donations__google_stub);
            googleViewStub.inflate();

            // choose donation amount
            mGoogleSpinner = getActivity().findViewById(
                    R.id.donations__google_android_market_spinner);
            ArrayAdapter<CharSequence> adapter;
            if (mDebug) {
                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        android.R.layout.simple_spinner_item, CATALOG_DEBUG);
            } else {
                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        android.R.layout.simple_spinner_item, mGoogleCatalogValues);
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mGoogleSpinner.setAdapter(adapter);

            Button btGoogle = getActivity().findViewById(
                    R.id.donations__google_android_market_donate_button);
            btGoogle.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        donateGoogleOnClick(0);
                    } catch (IllegalStateException e) {     // In some devices, it is impossible to setup IAB Helper
                        if (mDebug)                         // and this exception is thrown, being almost "impossible"
                            Log.e(TAG, e.getMessage());     // to the user to control it and forcing app close.
                        openDialog(android.R.drawable.ic_dialog_alert, 
                                   R.string.donations__google_android_market_not_supported_title,
                                   getString(R.string.donations__google_android_market_not_supported));
                    }
                }
            });

            // Create the helper, passing it our context and the public key to verify signatures with
            if (mDebug)
                Log.d(TAG, "Creating IAB helper.");
            mHelper = new IabHelper(getActivity(), mGooglePubkey);

            // enable debug logging (for a production application, you should set this to false).
            mHelper.enableDebugLogging(mDebug);

            // Start setup. This is asynchronous and the specified listener
            // will be called once setup completes.
            if (mDebug)
                Log.d(TAG, "Starting setup.");
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (mDebug)
                        Log.d(TAG, "Setup finished.");

                    if (!result.isSuccess()) {
                        openDialog(android.R.drawable.ic_dialog_alert, R.string.donations__google_android_market_not_supported_title,
                                getString(R.string.donations__google_android_market_not_supported));
                        return;
                    }

                    // Have we been disposed of in the meantime? If so, quit.
                    if (mHelper == null) return;
                }
            });
        }*/

        if (mGoogleEnabled) {
            View.OnClickListener supportListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(v.getId() == R.id.support1){
                            donateGoogleOnClick(0);
                        }
                        else if(v.getId() == R.id.support5){
                            donateGoogleOnClick(1);
                        }
                        else if(v.getId() == R.id.support10){
                            donateGoogleOnClick(2);
                        }
                    } catch (IllegalStateException e) {     // In some devices, it is impossible to setup IAB Helper
                        if (mDebug)                         // and this exception is thrown, being almost "impossible"
                            Log.e(TAG, e.getMessage());     // to the user to control it and forcing app close.
                        Toast.makeText(v.getContext(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            Button button1 = getActivity().findViewById(R.id.support1);
            button1.setOnClickListener(supportListener);

            Button button5 = getActivity().findViewById(R.id.support5);
            button5.setOnClickListener(supportListener);

            Button button10 = getActivity().findViewById(R.id.support10);
            button10.setOnClickListener(supportListener);

            // Create the helper, passing it our context and the public key to verify signatures with
            if (mDebug)
                Log.d(TAG, "Creating IAB helper.");
            mHelper = new IabHelper(getActivity(), mGooglePubkey);

            // enable debug logging (for a production application, you should set this to false).
            mHelper.enableDebugLogging(mDebug);

            // Start setup. This is asynchronous and the specified listener
            // will be called once setup completes.
            if (mDebug)
                Log.d(TAG, "Starting setup.");
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (mDebug)
                        Log.d(TAG, "Setup finished.");

                    if (!result.isSuccess()) {
                        Toast.makeText(getContext(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Have we been disposed of in the meantime? If so, quit.
                    if (mHelper == null) return;
                }
            });
        }
    }

    /**
     * Open dialog
     */
    void openDialog(int icon, int title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(icon);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setNeutralButton(R.string.donations__button_close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        dialog.show();
    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    public void donateGoogleOnClick(int index) {
        if (mDebug)
            Log.d(TAG, "selected item in spinner: " + index);

        if (mDebug) {
            // when debugging, choose android.test.x item
            mHelper.launchPurchaseFlow(getActivity(),
                    CATALOG_DEBUG[index], IabHelper.ITEM_TYPE_INAPP,
                    0, mPurchaseFinishedListener, null);
        } else {
            mHelper.launchPurchaseFlow(getActivity(),
                    mGgoogleCatalog[index], IabHelper.ITEM_TYPE_INAPP,
                    0, mPurchaseFinishedListener, null);
        }
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mDebug) {
                Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            }
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            Log.d("---result---", result.isSuccess() + "");
            if (result.isSuccess()) {
                if (mDebug)
                    Log.d(TAG, "Purchase successful.");

                // directly consume in-app purchase, so that people can donate multiple times
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);

                // show thanks openDialog
                openDialog(android.R.drawable.ic_dialog_info, R.string.donations__thanks_dialog_title,
                        getString(R.string.donations__thanks_dialog));
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (mDebug)
                Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isSuccess()) {
                if (mDebug)
                    Log.d(TAG, "Consumption successful. Provisioning.");
            }
            if (mDebug)
                Log.d(TAG, "End consumption flow.");
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mDebug)
            Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the fragment result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            if (mDebug)
                Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

}
