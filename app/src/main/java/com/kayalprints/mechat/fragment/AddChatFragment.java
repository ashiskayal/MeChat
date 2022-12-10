package com.kayalprints.mechat.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.kayalprints.mechat.R;

import java.util.Objects;

public class AddChatFragment extends DialogFragment {

    private EditText phNo;
    private Button find;

    private DatabaseReference databaseReference;

    private Boolean haveData;

    public AddChatFragment(DatabaseReference reference) {
        this.databaseReference = reference;
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
                if(phNum.startsWith("+91")) findUser(phNum);
                else findUser("+91"+phNum);
            }
        });
        return v;
    }

    private void findUser(String phNumber) {

        databaseReference.child(phNumber).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                haveData = (Boolean) dataSnapshot.child("haveData").getValue();

                if(haveData != null) {
                    Log.i("ashis","account found");
                    Bundle addUserData = new Bundle();
                    addUserData.putBoolean("haveData",true);
                    addUserData.putString("username",
                            Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
                    addUserData.putString("dpLink",
                            Objects.requireNonNull(dataSnapshot.child("dp").getValue()).toString());
                    addUserData.putString("phNo",phNumber);

                    ShowAddedUserFragment fragment = new ShowAddedUserFragment(addUserData);
                    fragment.show(requireActivity().getSupportFragmentManager(),"ShowAddedUserFragment");
                    dismiss();

                }
                else {
                    Log.i("ashis","account not found");
                    phNo.setText("");
                    phNo.setHint("Account no found.");
                    haveData = false;
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("ashis","addChat on fail");
            }
        });
    }

}