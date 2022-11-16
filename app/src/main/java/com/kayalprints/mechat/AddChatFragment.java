package com.kayalprints.mechat;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddChatFragment extends Fragment {

    private EditText phNo;
    private Button find;

    public AddChatFragment() {
        // Required empty public constructor
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_chat, container, false);

        phNo = v.findViewById(R.id.editTextAddChatFragPhone);
        find = v.findViewById(R.id.buttonAddChatFrag);

        find.setOnClickListener(a -> {
            String phNum = phNo.getText().toString().trim();
            if(phNum.isEmpty())
                Toast.makeText(requireContext(), "Please enter your friend's phone number.", Toast.LENGTH_SHORT).show();
            else {
                findUser(phNum);
            }
        });

        return v;
    }

    private boolean findUser(String phNumber) {



        return false;
    }

}