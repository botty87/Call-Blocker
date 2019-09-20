package com.botty.callblocker.fragments.allowedBlockedFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.botty.callblocker.R
import com.wickerlabs.logmanager.LogObject
import com.wickerlabs.logmanager.LogsManager
import kotlinx.android.synthetic.main.call_log_item.view.*
import java.text.DateFormat
import java.util.*

class CallLogAdapter(private val calls: List<LogObject>, private val contactsPermission: Boolean): RecyclerView.Adapter<CallLogAdapter.Holder>() {

    private val dateFormat = DateFormat.getDateTimeInstance()
    var onCallLogClick: ((LogObject) -> Unit)? = null

    override fun getItemCount(): Int = calls.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        LayoutInflater.from(parent.context).inflate( R.layout.call_log_item, parent, false)
            .run { Holder(this) }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.view.run {
            val callLog = calls[holder.adapterPosition]
            textViewCaller.text = if(contactsPermission) callLog.contactName else callLog.number
            textViewWhen.text = Date(callLog.date).run { dateFormat.format(this) }
            if(callLog.type == LogsManager.INCOMING_CALLS) {
                imageViewLogo.setImageResource(R.drawable.ic_call_received_35dp)
            } else {
                imageViewLogo.setImageResource(R.drawable.ic_call_missed_35dp)
            }
            callLogItemLayout.setOnClickListener { onCallLogClick?.invoke(callLog) }
        }
    }

    class Holder(val view: View): RecyclerView.ViewHolder(view)
}