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
import com.austinpurtell.wf.adapters.HomeAdapter;

public class HomeFragment extends Fragment {

    View view;
    RecyclerView recyclerView;
    HomeAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    public static int numCols = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_view);

        layoutManager = new GridLayoutManager(view.getContext(), numCols);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new HomeAdapter(view, recyclerView);
        recyclerView.setAdapter(adapter);

        return view;

    }

}
