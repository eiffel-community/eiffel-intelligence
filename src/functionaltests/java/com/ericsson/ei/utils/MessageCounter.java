package com.ericsson.ei.utils;

public class MessageCounter {

    private static MessageCounter messageCounter;

    private int array[];

    public static MessageCounter getInstance() {
        if (messageCounter == null) {
            messageCounter = new MessageCounter();
        }
        return messageCounter;
    }

    public void setSize(int size) {
        array = new int[size];
    }
    
    public void addOne(int index) {
        array[index]++;
    }
    
    public int getCount(int index) {
        return array[index];
    }
}
