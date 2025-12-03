package com.example.nirs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.R;
import com.example.nirs.entity.Order;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public OrdersAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderNumber.setText("Заказ #" + order.getId());
        holder.orderDate.setText(order.getFormattedDate());
        holder.orderTotal.setText(order.getFormattedTotal());
        holder.orderStatus.setText(order.getStatus());

        // Устанавливаем цвет статуса
        switch (order.getStatus()) {
            case "Новый":
                holder.orderStatus.setTextColor(0xFF2196F3); // Синий
                break;
            case "В процессе":
                holder.orderStatus.setTextColor(0xFFFF9800); // Оранжевый
                break;
            case "Завершен":
                holder.orderStatus.setTextColor(0xFF4CAF50); // Зеленый
                break;
            case "Отменен":
                holder.orderStatus.setTextColor(0xFFF44336); // Красный
                break;
            default:
                holder.orderStatus.setTextColor(0xFF757575); // Серый
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber, orderDate, orderTotal, orderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.order_number);
            orderDate = itemView.findViewById(R.id.order_date);
            orderTotal = itemView.findViewById(R.id.order_total);
            orderStatus = itemView.findViewById(R.id.order_status);
        }
    }
}