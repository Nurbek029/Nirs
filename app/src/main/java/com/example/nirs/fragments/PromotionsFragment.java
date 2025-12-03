package com.example.nirs.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirs.R;
import com.example.nirs.adapters.PromotionsAdapter;
import com.example.nirs.entity.Promotion;

import java.util.ArrayList;
import java.util.List;

public class PromotionsFragment extends Fragment {

    private RecyclerView promotionsRecyclerView;
    private List<Promotion> promotionList;
    private PromotionsAdapter promotionsAdapter;

    public PromotionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(requireContext(), "Возврат в меню", Toast.LENGTH_SHORT).show();
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotions, container, false);

        promotionList = new ArrayList<>();

        // Инициализация RecyclerView
        promotionsRecyclerView = view.findViewById(R.id.promotions_recycler_view);
        promotionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Создание адаптера
        promotionsAdapter = new PromotionsAdapter(promotionList);
        promotionsRecyclerView.setAdapter(promotionsAdapter);

        // Загрузка акций
        loadPromotions();

        return view;
    }

    private void loadPromotions() {
        // Добавляем тестовые акции
        promotionList.add(new Promotion(
                "Скидка 20% на все салаты",
                "Акция действует с 01.12.2024 по 31.12.2024. Скидка применяется автоматически при заказе любого салата.",
                "https://example.com/salad_promo.jpg"
        ));

        promotionList.add(new Promotion(
                "Комбо обед всего за 350 сом",
                "Суп + второе + салат + напиток. Идеальный обед по специальной цене!",
                "https://example.com/combo_lunch.jpg"
        ));

        promotionList.add(new Promotion(
                "Бесплатная доставка",
                "При заказе от 500 сом доставка бесплатно в радиусе 3 км от столовой.",
                "https://example.com/free_delivery.jpg"
        ));

        promotionList.add(new Promotion(
                "Счастливые часы",
                "С 14:00 до 16:00 скидка 15% на все горячие блюда.",
                "https://example.com/happy_hours.jpg"
        ));

        promotionsAdapter.notifyDataSetChanged();
    }
}