package com.botty.callblocker.data

import java.util.*

data class BlockedCall(val number: String,
                       val date: Date = Date()) {

    constructor(phoneNumber: PhoneNumber) : this(phoneNumber.number)
}