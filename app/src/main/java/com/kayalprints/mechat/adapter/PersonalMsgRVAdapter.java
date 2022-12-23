package com.kayalprints.mechat.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.PersonalMassage;

import java.util.List;

public class PersonalMsgRVAdapter extends RecyclerView.Adapter<PersonalMsgRVAdapter.PersonalMassageHolder> {

    private final List<PersonalMassage> massages;
    private final String SENDER;

    private final int SENDING = 1;
    private static boolean isSending;


    public PersonalMsgRVAdapter(List<PersonalMassage> massages, String sentUser) {
        this.massages = massages;
        this.SENDER = sentUser;
        isSending = false;
    }

    @NonNull
    @Override
    public PersonalMassageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;

        Log.i("ashisdb","at onCreateView :"+viewType);

        if(viewType == SENDING) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sendmsgdesign, parent, false);
            isSending = true;
        }
        else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.receivemsgdesign, parent, false);
            isSending = false;
        }

        return new PersonalMassageHolder(v);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull PersonalMassageHolder holder, int position) {
        holder.msg.setText(massages.get(position).getMassage());

//        if (isSending)
//            holder.card.setTranslationX(300);
//        else
//            holder.card.setTranslationX(-300);
//
//        holder.card.setAlpha(0);
//        holder.card.animate().translationX(0)
//                .alpha(1)
//                .setDuration(500).setStartDelay(200).start();
    }

    @Override
    public int getItemViewType(int position) {
        if(massages.get(position).getSender().equals(SENDER)) return SENDING;
        else return 2;
    }

    @Override
    public int getItemCount() {
        return massages.size();
    }


    public static class PersonalMassageHolder extends RecyclerView.ViewHolder {
        TextView msg;
        CardView card;

        public PersonalMassageHolder(@NonNull View itemView) {
            super(itemView);

            msg = itemView.findViewById(R.id.textViewReceiveMsg);
            card = itemView.findViewById(R.id.cardReceiveMsg);

            if(isSending) {
                msg = itemView.findViewById(R.id.textViewSendMsg);
                card = itemView.findViewById(R.id.cardSendMsg);
            }

        }
    }

}
