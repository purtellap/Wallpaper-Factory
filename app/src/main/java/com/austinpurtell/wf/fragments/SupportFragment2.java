package com.austinpurtell.wf.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.austinpurtell.wf.R;

import org.sufficientlysecure.donations.DonationsFragment;

public class SupportFragment2 extends Fragment {

    View view;

    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1jmiUe8+bw6kadI995DP5uWNBOnyMX2vhvSinh7lHVL1FVzDPCItbcbzHlebID4GRIBDidDWQWfOnuU7T/4aYqvWtW/u7NA2ow/zpXkg3EdspQxowocM0PJRfQPDB0YflEMgj5GtbAZWscwWCBdN5RUv8gchRL3Uly6Em9mqqfRAVRSTkQnnpGfddx6xsp7hs3I4AvS9++BgU6jsJOFDZdigut1DqNmoxouKLM0Z44pmFbfc4NRKVufpzt1XWHO13dj7ynQiKNlPAO56Ym2p0gse1TLEiQIbjzyVEDt2viSooPcTZ+h7VNuuMFpyypbJ2HHpT1yHYpXD7Jvs/Br8kQIDAQAB";
    private static final String[] GOOGLE_CATALOG = new String[]{"dono_1","dono_5","dono_10"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_support, container, false);
        setHasOptionsMenu(true);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        DonationsFragment donationsFragment;
        donationsFragment = DonationsFragment.newInstance(false, true, GOOGLE_PUBKEY, GOOGLE_CATALOG,
                getResources().getStringArray(R.array.donation_google_catalog_values), false, null, null,
                null, false, null, null, false, null);

        ft.replace(R.id.donations_activity_container, donationsFragment, "donationsFragment");
        ft.commit();

        return view;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
