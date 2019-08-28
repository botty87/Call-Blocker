package com.call.blocker.fragments.allowedBlockedFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.call.blocker.R
import com.call.blocker.data.PhoneNumber
import com.call.blocker.databinding.PhoneNumberItemBinding
import com.call.blocker.tools.logException
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException

class PhoneNumberAdapter(options: FirestoreRecyclerOptions<PhoneNumber>,
                         private val onDeleteAction: ((PhoneNumber) -> Unit)):
    FirestoreRecyclerAdapter<PhoneNumber, PhoneNumberAdapter.Holder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = DataBindingUtil.inflate<PhoneNumberItemBinding>(LayoutInflater
            .from(parent.context), R.layout.phone_number_item, parent, false)

        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int, phoneNumber: PhoneNumber) {
        holder.binding.phoneNumber = phoneNumber
        holder.binding.buttonRemove.setOnClickListener { onDeleteAction.invoke(phoneNumber) }
    }

    override fun onError(e: FirebaseFirestoreException) {
        super.onError(e)
        logException(e)
    }

    class Holder(val binding: PhoneNumberItemBinding): RecyclerView.ViewHolder(binding.root)
}