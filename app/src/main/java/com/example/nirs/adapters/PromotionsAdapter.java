package com.example.nirs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.R;
import com.example.nirs.entity.Promotion;

import java.util.List;

public class PromotionsAdapter extends RecyclerView.Adapter<PromotionsAdapter.PromotionViewHolder> {

    private List<Promotion> promotionList;

    public PromotionsAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);
        holder.promotionTitle.setText(promotion.getTitle());
        holder.promotionDescription.setText(promotion.getDescription());

        // Здесь можно добавить загрузку изображения с помощью Glide или Picasso
        // Glide.with(holder.itemView.getContext()).load(promotion.getImageUrl()).into(holder.promotionImage);
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView promotionTitle, promotionDescription;
        // ImageView promotionImage; // Раскомментируйте когда добавите ImageView в item_promotion.xml

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            promotionTitle = itemView.findViewById(R.id.promotion_title);
            promotionDescription = itemView.findViewById(R.id.promotion_description);
            // promotionImage = itemView.findViewById(R.id.promotion_image);
        }
    }
}