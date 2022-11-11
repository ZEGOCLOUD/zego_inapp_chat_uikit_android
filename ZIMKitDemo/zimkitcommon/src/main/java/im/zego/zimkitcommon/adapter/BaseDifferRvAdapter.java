package im.zego.zimkitcommon.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public abstract class BaseDifferRvAdapter<holder extends RecyclerView.ViewHolder, model> extends RecyclerView.Adapter<holder> {

    private final DiffUtil.ItemCallback<model> mDiffCallback = new DiffUtil.ItemCallback<model>() {

        @Override
        public boolean areItemsTheSame(@NonNull model oldItem, @NonNull model newItem) {
            return itemsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull model oldItem, @NonNull model newItem) {
            return contentsTheSame(oldItem, newItem);
        }
    };

    protected final AsyncListDiffer<model> mDiffer = new AsyncListDiffer<>(this, mDiffCallback);

    public void submitList(ArrayList<model> models) {
        mDiffer.submitList(models); //Make sure it's new array every time
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        onBind(holder, mDiffer.getCurrentList().get(holder.getLayoutPosition()), position);
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    abstract protected boolean itemsTheSame(@NonNull model oldItem, @NonNull model newItem);

    abstract protected boolean contentsTheSame(@NonNull model oldItem, @NonNull model newItem);

    abstract protected void onBind(holder holder, model model, int position);

    abstract protected holder getHolder(@NonNull ViewGroup parent, int viewType);
}
