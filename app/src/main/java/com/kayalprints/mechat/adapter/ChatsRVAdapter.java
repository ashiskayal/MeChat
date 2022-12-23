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
    private onItemClickListener listener;
    private onItemLongClickListener longListener;

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
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position) {
        String dpLink = chats.get(position).getDpLink();
        String userName = chats.get(position).getUserName();
        String phNo = chats.get(position).getPhNumber();

        if(!dpLink.equals("null"))
            Picasso.get().load(dpLink).into(holder.image);
//        else holder.image.setImageResource(R.drawable.ic_baseline_profile_white); // This line is not needed because this
                                                                                        // default image resource is already set in design.

        if (userName.equals("null")) holder.name.setText(phNo);
        else holder.name.setText(userName);

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
    public int getItemCount() {
        return chats.size();
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView image;
        private final TextView name;
        CardView card;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.circleImageViewCard);
            name = itemView.findViewById(R.id.textViewNameCard);
            card = itemView.findViewById(R.id.card);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(listener != null && position != RecyclerView.NO_POSITION)
                        listener.onItemClick(chats.get(position));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    int position = getAdapterPosition();
                    if(longListener != null && position != RecyclerView.NO_POSITION)
                        longListener.onItemLongClick(position, v);

                    return false;
                }
            });

        }
    }



    public interface onItemClickListener {
        void onItemClick(User user);
    }

    public interface onItemLongClickListener {
        void onItemLongClick(int pos, View v);
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(onItemLongClickListener listener) {
        this.longListener = listener;
    }

}
