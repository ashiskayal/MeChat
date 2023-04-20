package com.kayalprints.mechat.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kayalprints.mechat.activity.MassagingActivity;
import com.kayalprints.mechat.adapter.ChatsRVAdapter;
import com.kayalprints.mechat.classes.ChatDataHolder;
import com.kayalprints.mechat.classes.User;
import com.kayalprints.mechat.databinding.FragmentChatsBinding;

import java.util.Collections;

public class ChatsFragment extends Fragment {

    private Context context;
    private ChatsRVAdapter adapter;

    public ChatsFragment() { // Need an empty constructor

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        com.kayalprints.mechat.databinding.FragmentChatsBinding binding = FragmentChatsBinding.inflate(inflater);

        this.context = getContext();

        binding.recyclerViewChats.setLayoutManager(new LinearLayoutManager(this.context));
        adapter = new ChatsRVAdapter(ChatDataHolder.getChats(),this.context);

        initiateItemTouchHelper(binding.recyclerViewChats);

        adapter.setOnItemClickListener(user -> {
            Intent i = new Intent(context, MassagingActivity.class);
            i.putExtra("chatWithPh",user.getPhNumber());
            i.putExtra("chatWithName",user.getUserName());
            startActivity(i);
        });
/*
        adapter.setOnItemLongClickListener(new ChatsRVAdapter.onItemLongClickListener() {
            @Override
            public void onItemLongClick(int pos, View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.setGravity(Gravity.CENTER);
                popupMenu.getMenu().add("Up");
                popupMenu.getMenu().add("Down");
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Up" : return changeChatPositionTo(pos, pos-1, adapter);
                            case "Down" : return changeChatPositionTo(pos, pos+1, adapter);
                            default: return false;
                        }
                    }
                });
            }
        });
**/
        binding.recyclerViewChats.setAdapter(adapter);
        return binding.getRoot();
    }

/*
    @SuppressLint("NotifyDataSetChanged")
    private boolean changeChatPositionTo(int initial, int finalPos, @NonNull ChatsRVAdapter rvAdapter) {
        User user = ChatDataHolder.removeChat(initial);
        boolean value = ChatDataHolder.addChat(user, finalPos);

        if (value) rvAdapter.notifyItemMoved(initial,finalPos);

        return value;
    }
**/

    private void initiateItemTouchHelper(RecyclerView recyclerView) {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Collections.swap(ChatDataHolder.getChats(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.END) {
                    User user = ChatDataHolder.removeChat(viewHolder.getAdapterPosition());
                    assert user != null;
                    String name = user.getUserName();

                    if(name.equals("null")) name = user.getPhNumber();
                    Toast.makeText(context, name+" removed", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                } else
                    Toast.makeText(context, "Default Called", Toast.LENGTH_SHORT).show();
            }
        });

        helper.attachToRecyclerView(recyclerView);
    }

}