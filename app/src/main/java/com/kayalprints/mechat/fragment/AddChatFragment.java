package com.kayalprints.mechat.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.kayalprints.mechat.classes.Operations;
import com.kayalprints.mechat.databinding.FragmentAddChatBinding;

import java.util.Objects;

public class AddChatFragment extends Fragment {

    private FragmentAddChatBinding binding;

    private final DatabaseReference databaseReference;

    private Boolean haveData;

    private AddChatFragment(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference.child("UsersData");
    }

    public static AddChatFragment getInstance(DatabaseReference reference) {
        return new AddChatFragment(reference);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddChatBinding.inflate(inflater);

        binding.buttonAddChatFrag.setOnClickListener(a -> {
            String phNum = Operations.extractPhoneNumber(binding.editTextAddChatFragPhone.getText().toString().trim());
            Toast.makeText(getContext(), "Entered ph number is : "+phNum, Toast.LENGTH_LONG).show();

            if(phNum.isEmpty())
                Toast.makeText(requireContext(), "Please enter your friend's phone number.", Toast.LENGTH_SHORT).show();
            else
                findUser("+91"+phNum);
        });
        return binding.getRoot();
    }

    private void findUser(String phNumber) {

        databaseReference.child(phNumber).get().addOnSuccessListener(dataSnapshot -> {
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
                binding.editTextAddChatFragPhone.setText("");
            }
            else {
                Log.i("ashis","account not found");
                binding.editTextAddChatFragPhone.setText("");
                binding.editTextAddChatFragPhone.setHint("Account no found.");
                haveData = false;
            }

        }).addOnFailureListener(e -> Log.i("ashis","addChat on fail"));
    }

}