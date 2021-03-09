package com.austinpurtell.wf.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.austinpurtell.wf.R;
import com.austinpurtell.wf.adapters.PacksAdapter;

public class UpgradeFragment extends Fragment {

    RecyclerView recyclerView;
    PacksAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    View view;
    public static int numCols = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_upgrade, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_view);

        layoutManager = new GridLayoutManager(view.getContext(), numCols);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PacksAdapter(view, recyclerView);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
