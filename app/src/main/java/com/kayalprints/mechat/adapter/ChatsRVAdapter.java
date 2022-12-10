package com.kayalprints.mechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsRVAdapter extends RecyclerView.Adapter<ChatsRVAdapter.ChatsViewHolder> {

    private final Context c;
    private final List<User> chats;

    public ChatsRVAdapter(List<User> users, Context c) {
        this.chats = users;
        this.c = c;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatcardview, parent, false);
        return new ChatsViewHolder(v);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ChatsViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        holder.card.setTranslationX(300);
        holder.card.setTranslationY(-50);
        holder.card.setScaleX(0);
        holder.card.setScaleY(0);
        holder.card.setAlpha(0);
        holder.card.animate().translationX(0).translationY(0)
                .alpha(1)
                .scaleX(1).scaleY(1)
                .setDuration(500).setStartDelay(200).start();

    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position) {
        String dpLink = chats.get(position).getDpLink();
        String userName = chats.get(position).getUserName();
        String phNo = chats.get(position).getPhNumber();

        if(!dpLink.equals("null"))
            Picasso.get().load(dpLink).into(holder.image);
        else holder.image.setImageResource(R.drawable.ic_baseline_profile_black);

        if (userName.equals("null")) holder.name.setText(phNo);
        else holder.name.setText(userName);

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView image;
        private final TextView name;
        CardView card;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.circleImageViewCard);
            name = itemView.findViewById(R.id.textViewNameCard);
            card = itemView.findViewById(R.id.card);

            card.setOnClickListener(v -> {

            });

        }
    }

}
