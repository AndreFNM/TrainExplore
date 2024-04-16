import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.R
import com.example.trainexplore.database.PoiDB.PlaceData

class PlacesAdapter : ListAdapter<PlaceData, PlacesAdapter.PlaceViewHolder>(DIFF_CALLBACK) {
    var onItemClick: ((PlaceData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item_view, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place, onItemClick)
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(place: PlaceData, clickListener: ((PlaceData) -> Unit)?) {
            itemView.findViewById<TextView>(R.id.placeName).text = place.nome
            itemView.setOnClickListener { clickListener?.invoke(place) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PlaceData>() {
            override fun areItemsTheSame(oldItem: PlaceData, newItem: PlaceData) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PlaceData, newItem: PlaceData) = oldItem == newItem
        }
    }
}
