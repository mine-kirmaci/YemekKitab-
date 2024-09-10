package com.minekirmaci.yemekkitabi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.room.util.recursiveFetchArrayMap
import com.minekirmaci.yemekkitabi.databinding.RecyclerRowBinding
import com.minekirmaci.yemekkitabi.model.Tarif
import com.minekirmaci.yemekkitabi.view.ListeFragmentDirections

class TarifAdapter(val tarifListesi:List<Tarif>):RecyclerView.Adapter<TarifAdapter.TarifHolder>() {
    class TarifHolder(val binding: RecyclerRowBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder {
        val RecyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TarifHolder(RecyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return tarifListesi.size
    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) {
    holder.binding.recyclerViewTextView.text=tarifListesi[position].isim
     holder.itemView.setOnClickListener{
         val action=ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "eski",id=tarifListesi[position].id)
         Navigation.findNavController(it).navigate(action)
     }
    }
}