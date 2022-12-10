package com.kayalprints.mechat.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.User;
import com.kayalprints.mechat.activity.MainActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowAddedUserFragment extends DialogFragment {

    private Bundle usersData;

    public ShowAddedUserFragment(Bundle data) {
        this.usersData = data;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_show_added_user, container, false);

        CircleImageView dp = v.findViewById(R.id.profileCircleImageNewChat);
        TextView name = v.findViewById(R.id.nameNewChat);
        ImageView chat = v.findViewById(R.id.imageViewNewChat);

        String userName = usersData.getString("username");
        String phNo = usersData.getString("phNo");
        String dpLink = usersData.getString("dpLink");

        if(!userName.equals("null")) name.setText(phNo);
        if (dpLink.equals("null")) {
            dp.setImageResource(R.drawable.ic_baseline_profile_black);
        } else
            Picasso.get().load(dpLink).into(dp);

        chat.setOnClickListener(onClick -> {
            User newChat = new User(userName,dpLink,phNo);
            MainActivity.updateChatList(newChat);
            dismiss();
        });

        return v;
    }
}