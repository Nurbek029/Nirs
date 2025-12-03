package com.example.nirs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.R;

import java.util.List;

public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.OrderItemViewHolder> {

    private List<OrderItem> orderItems;

    public static class OrderItem {
        private String name;
        private int quantity;
        private double price;
        private double total;

        public OrderItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.total = quantity * price;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }

    public OrderDetailsAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.itemName.setText(item.getName());
        holder.itemQuantity.setText(String.format("%d x %.0f сом", item.getQuantity(), item.getPrice()));
        holder.itemTotal.setText(String.format("%.0f сом", item.getTotal()));
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity, itemTotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemTotal = itemView.findViewById(R.id.item_total);
        }
    }
}