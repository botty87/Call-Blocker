package com.botty.callblocker.fragments.allowedBlockedFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.botty.callblocker.R
import com.botty.callblocker.data.PhoneNumber
import com.botty.callblocker.databinding.PhoneNumberItemBinding
import com.botty.callblocker.tools.log
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException

class PhoneNumberAdapter(options: FirestoreRecyclerOptions<PhoneNumber>,
                         private val onDeleteAction: ((PhoneNumber) -> Unit)):
    FirestoreRecyclerAdapter<PhoneNumber, PhoneNumberAdapter.Holder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return DataBindingUtil.inflate<PhoneNumberItemBinding>(LayoutInflater
            .from(parent.context), R.layout.phone_number_item, parent, false)
            .run { Holder(this) }
    }

    override fun onBindViewHolder(holder: Holder, position: Int, phoneNumber: PhoneNumber) {
        holder.binding.phoneNumber = phoneNumber
        holder.binding.buttonRemove.setOnClickListener { onDeleteAction.invoke(phoneNumber) }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        e.log()
    }

    class Holder(val binding: PhoneNumberItemBinding): RecyclerView.ViewHolder(binding.root)
}