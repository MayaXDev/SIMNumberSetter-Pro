package com.mayaxdev.simnumbersetter;

import android.os.Message;

interface IPhoneNumberSetter {

    boolean setLine1Number(String number, in Message onComplete);
    String getLine1Number();
    boolean setLine1NumberForSlot(int slotIndex, String number, in Message onComplete);
    String getLine1NumberForSlot(int slotIndex);
    int getPhoneCount();

}