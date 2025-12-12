package com.example.nirs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.R;
import com.example.nirs.entity.Order;

import java.util.List;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onStatusChange(long orderId, String newStatus);
        void onViewDetails(Order order);
    }

    public AdminOrdersAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderNumber.setText("Заказ #" + order.getId());
        holder.orderDate.setText(order.getFormattedDate());
        holder.orderTotal.setText(order.getFormattedTotal());
        holder.orderStatus.setText(order.getStatus());

        // Цвет статуса
        holder.orderStatus.setTextColor(order.getStatusColor());

        // Кнопки действий в зависимости от статуса
        setupActionButtons(holder, order);

        // Просмотр деталей
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(order);
            }
        });
    }

    private void setupActionButtons(OrderViewHolder holder, Order order) {
        // Скрываем все кнопки сначала
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReady.setVisibility(View.GONE);
        holder.btnComplete.setVisibility(View.GONE);

        switch (order.getStatus()) {
            case "В обработке":
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusChange(order.getId(), "Принятый");
                    }
                });
                break;

            case "Принятый":
                holder.btnReady.setVisibility(View.VISIBLE);
                holder.btnReady.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusChange(order.getId(), "Готов");
                    }
                });
                break;

            case "Готов":
                holder.btnComplete.setVisibility(View.VISIBLE);
                holder.btnComplete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusChange(order.getId(), "Завершен");
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber, orderDate, orderTotal, orderStatus;
        Button btnAccept, btnReady, btnComplete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.order_number);
            orderDate = itemView.findViewById(R.id.order_date);
            orderTotal = itemView.findViewById(R.id.order_total);
            orderStatus = itemView.findViewById(R.id.order_status);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReady = itemView.findViewById(R.id.btn_ready);
            btnComplete = itemView.findViewById(R.id.btn_complete);
        }
    }
}