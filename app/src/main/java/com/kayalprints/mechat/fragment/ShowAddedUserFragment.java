package com.kayalprints.mechat.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.ChatDataHolder;
import com.kayalprints.mechat.classes.User;
import com.kayalprints.mechat.databinding.FragmentShowAddedUserBinding;

public class ShowAddedUserFragment extends DialogFragment {

    private final Bundle usersData;

    public ShowAddedUserFragment(Bundle data) {
        this.usersData = data;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentShowAddedUserBinding binding = FragmentShowAddedUserBinding.inflate(inflater);

        String userName = usersData.getString("username");
        String phNo = usersData.getString("phNo");
        String dpLink = usersData.getString("dpLink");

        if(!userName.equals("null")) binding.nameNewChat.setText(phNo);
        if (dpLink.equals("null"))
            Glide.with(requireContext()).load(R.drawable.ic_baseline_profile_black).into(binding.profileCircleImageNewChat);
        else
            Glide.with(requireContext()).load(dpLink).into(binding.profileCircleImageNewChat);

        binding.imageViewNewChat.setOnClickListener(onClick -> {
            User newChat = new User(userName,dpLink,phNo);
            ChatDataHolder.addChat(newChat);
            dismiss();
        });

        return binding.getRoot();
    }
}