package com.kayalprints.mechat.classes;

import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

public final class ChatDataHolder {

    private static List<User> chats;

    private static void createInstance() {
        if(chats == null) chats = new LinkedList<>();
    }
    public static List<User> getChats() {
        if(chats == null) createInstance();
        return chats;
    }

    public static boolean addChat(User user) {
        if(chats == null) createInstance();
        return addChat(user, 0);
    }

    public static boolean addChat(User user, int pos) {
        if(chats == null) createInstance();
        if(pos < 0) return false;
        chats.add(pos,user);
        return true;
    }

    public static User removeChat(int pos) {
        if(chats == null) createInstance();
        if(!(pos >= chats.size())) return chats.remove(pos);
        return null;
    }

    public static User removeChat(User user) {
        if(chats == null) createInstance();
        int i = posOfUser(user);
        if(i!=-1) return removeChat(i);
        return null;
    }

    public static int posOfUser(User user) {
        if(chats == null) createInstance();
        if (chats.size() != 0)
            for (int i=0; i< chats.size(); i++)
                if(chats.get(i).getPhNumber().equals(user.getPhNumber())) return i;
        return -1;
    }

    @Nullable
    public static User getChatUserAt(int pos) {
        if(chats == null) createInstance();
        if(!(pos >= chats.size())) return chats.get(pos);
        else return null;
    }

}
