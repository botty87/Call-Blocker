package com.call.blocker.data

import com.tomash.androidcontacts.contactgetter.entity.ContactData

class ContactForAdapter(val data: ContactData) {
    override fun toString(): String {
        return data.compositeName
    }
}