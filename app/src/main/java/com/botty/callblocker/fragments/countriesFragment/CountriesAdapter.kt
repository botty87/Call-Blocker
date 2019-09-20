package com.botty.callblocker.fragments.countriesFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.botty.callblocker.R
import com.botty.callblocker.data.country.Country
import com.botty.callblocker.databinding.CountryItemBinding

class CountriesAdapter : RecyclerView.Adapter<CountriesAdapter.Holder>() {

    private val countries: AsyncListDiffer<Country> = AsyncListDiffer(this, DIFF_UTIL_CALLBACK)
    var countryListener: CountryListener? = null

    fun setNewCountries(newCountries: List<Country>, reset: Boolean = false) {
        if(reset) {
            countries.submitList(null)
        }
        countries.submitList(newCountries)
    }

    override fun getItemCount(): Int = countries.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = DataBindingUtil.inflate<CountryItemBinding>(LayoutInflater
            .from(parent.context), R.layout.country_item, parent, false)
        return Holder(binding).apply { binding.lifecycleOwner = this }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.country = countries.currentList[holder.adapterPosition]
        holder.binding.checkboxEnabled.setOnClickListener {
            countryListener?.onCountrySelected(countries.currentList[holder.adapterPosition])
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        super.onViewAttachedToWindow(holder)
        holder.markAttach()
    }

    override fun onViewDetachedFromWindow(holder: Holder) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetach()
    }

    companion object {
        private val DIFF_UTIL_CALLBACK = object: DiffUtil.ItemCallback<Country>() {
            override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem == newItem
            }

        }
    }

    class Holder(val binding: CountryItemBinding): RecyclerView.ViewHolder(binding.root), LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)

        init {
            lifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
        }

        fun markAttach() {
            lifecycleRegistry.markState(Lifecycle.State.STARTED)
        }

        fun markDetach() {
            lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }
    }
}