package com.mayaxdev.simnumbersetter.service

import android.os.Message
import com.android.internal.telephony.Phone
import com.android.internal.telephony.PhoneFactory
import com.mayaxdev.simnumbersetter.IPhoneNumberSetter

/**
 *  Proxy Service for calls to [Phone] methods to be called via the [IPhoneNumberSetter] service.
 *  Preserves 100% of original core methods (getDefaultPhone) while adding multi-slot support.
 */
class PhoneNumberSetterService: IPhoneNumberSetter.Stub() {

    private val defaultPhone: Phone by lazy {
        PhoneFactory.getDefaultPhone()
    }

    private fun getPhoneForSlot(slotIndex: Int): Phone {
        if (slotIndex == 0) return defaultPhone
        return try {
            PhoneFactory.getPhone(slotIndex) ?: defaultPhone
        } catch (t: Throwable) {
            defaultPhone
        }
    }

    // ORIGINAL CORE METHOD 1: 100% identical to v1.0
    override fun setLine1Number(number: String, onComplete: Message?): Boolean {
        var tag = defaultPhone.line1AlphaTag
        if(tag.isNullOrEmpty()){
            tag = "Voice Line 1"
        }
        return defaultPhone.setLine1Number(tag, number, onComplete)
    }

    // ORIGINAL CORE METHOD 2: 100% identical to v1.0
    override fun getLine1Number(): String {
        return defaultPhone.line1Number ?: ""
    }

    // UPGRADED METHOD 3: Multi-slot setter (falls back to defaultPhone if slot 0)
    override fun setLine1NumberForSlot(slotIndex: Int, number: String, onComplete: Message?): Boolean {
        if (slotIndex == 0) return setLine1Number(number, onComplete)
        val phone = getPhoneForSlot(slotIndex)
        var tag = try {
            phone.line1AlphaTag
        } catch (e: Exception) {
            null
        }
        if(tag.isNullOrEmpty()){
            tag = "Voice Line ${slotIndex + 1}"
        }
        return phone.setLine1Number(tag, number, onComplete)
    }

    // UPGRADED METHOD 4: Multi-slot getter
    override fun getLine1NumberForSlot(slotIndex: Int): String {
        if (slotIndex == 0) return getLine1Number()
        val phone = getPhoneForSlot(slotIndex)
        return try {
            phone.line1Number ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // UPGRADED METHOD 5: Phone count getter
    override fun getPhoneCount(): Int {
        return try {
            val phones = PhoneFactory.getPhones()
            phones?.size ?: 1
        } catch (e: Exception) {
            1
        }
    }

}
