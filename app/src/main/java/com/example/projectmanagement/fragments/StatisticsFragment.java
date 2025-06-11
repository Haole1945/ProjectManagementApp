package com.example.projectmanagement.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectmanagement.R;
import com.example.projectmanagement.viewmodels.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;
    private PieChart pieChartProjects;
    private BarChart barChartTasks;
    private LineChart lineChartPerformance;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressIndicator;

    // Text views for statistics
    private TextView tvTotalProjects;
    private TextView tvCompletedProjects;
    private TextView tvInProgressProjects;
    private TextView tvNotStartedProjects;
    private TextView tvTotalTasks;
    private TextView tvCompletedTasks;
    private TextView tvInProgressTasks;
    private TextView tvNotStartedTasks;
    private TextView tvAvgProjectTime;
    private TextView tvAvgTaskTime;
    private TextView tvOnTimeRate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar(view);
        setupViews(view);
        setupCharts();
        setupSwipeRefresh(view);
        observeViewModel();
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }

    private void setupViews(View view) {
        // Initialize charts
        pieChartProjects = view.findViewById(R.id.pieChartProjects);
        barChartTasks = view.findViewById(R.id.barChartTasks);
        lineChartPerformance = view.findViewById(R.id.lineChartPerformance);

        // Initialize text views
        tvTotalProjects = view.findViewById(R.id.tvTotalProjects);
        tvCompletedProjects = view.findViewById(R.id.tvCompletedProjects);
        tvInProgressProjects = view.findViewById(R.id.tvInProgressProjects);
        tvNotStartedProjects = view.findViewById(R.id.tvNotStartedProjects);
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvInProgressTasks = view.findViewById(R.id.tvInProgressTasks);
        tvNotStartedTasks = view.findViewById(R.id.tvNotStartedTasks);
        tvAvgProjectTime = view.findViewById(R.id.tvAvgProjectTime);
        tvAvgTaskTime = view.findViewById(R.id.tvAvgTaskTime);
        tvOnTimeRate = view.findViewById(R.id.tvOnTimeRate);

        // Initialize progress indicator
        progressIndicator = view.findViewById(R.id.progressIndicator);
    }

    private void setupCharts() {
        // Setup pie chart for projects
        pieChartProjects.getDescription().setEnabled(false);
        pieChartProjects.setDrawHoleEnabled(true);
        pieChartProjects.setHoleColor(Color.WHITE);
        pieChartProjects.setTransparentCircleRadius(61f);
        pieChartProjects.setDrawCenterText(true);
        pieChartProjects.setRotationAngle(0);
        pieChartProjects.setRotationEnabled(true);
        pieChartProjects.setHighlightPerTapEnabled(true);
        pieChartProjects.animateY(1400);
        pieChartProjects.getLegend().setEnabled(true);
        pieChartProjects.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        pieChartProjects.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        pieChartProjects.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        pieChartProjects.getLegend().setDrawInside(false);
        pieChartProjects.getLegend().setXEntrySpace(7f);
        pieChartProjects.getLegend().setYEntrySpace(0f);
        pieChartProjects.getLegend().setYOffset(0f);

        // Setup bar chart for tasks
        barChartTasks.getDescription().setEnabled(false);
        barChartTasks.setDrawGridBackground(false);
        barChartTasks.setDrawBarShadow(false);
        barChartTasks.setHighlightFullBarEnabled(false);
        barChartTasks.getLegend().setEnabled(true);
        barChartTasks.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        barChartTasks.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        barChartTasks.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        barChartTasks.getLegend().setDrawInside(false);
        barChartTasks.getLegend().setXEntrySpace(7f);
        barChartTasks.getLegend().setYEntrySpace(0f);
        barChartTasks.getLegend().setYOffset(0f);
        barChartTasks.animateY(1400);

        XAxis xAxis = barChartTasks.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChartTasks.getAxisLeft().setDrawGridLines(true);
        barChartTasks.getAxisRight().setEnabled(false);

        // Setup line chart for performance
        lineChartPerformance.getDescription().setEnabled(false);
        lineChartPerformance.setDrawGridBackground(false);
        lineChartPerformance.getLegend().setEnabled(true);
        lineChartPerformance.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        lineChartPerformance.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        lineChartPerformance.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        lineChartPerformance.getLegend().setDrawInside(false);
        lineChartPerformance.getLegend().setXEntrySpace(7f);
        lineChartPerformance.getLegend().setYEntrySpace(0f);
        lineChartPerformance.getLegend().setYOffset(0f);
        lineChartPerformance.animateX(1400);

        XAxis xAxisPerformance = lineChartPerformance.getXAxis();
        xAxisPerformance.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisPerformance.setDrawGridLines(false);
        xAxisPerformance.setGranularity(1f);

        lineChartPerformance.getAxisLeft().setDrawGridLines(true);
        lineChartPerformance.getAxisRight().setEnabled(false);
    }

    private void setupSwipeRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        });

        // Observe error
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Observe project statistics
        viewModel.getTotalProjects().observe(getViewLifecycleOwner(), total -> 
            tvTotalProjects.setText(String.valueOf(total)));
        viewModel.getCompletedProjects().observe(getViewLifecycleOwner(), completed -> 
            tvCompletedProjects.setText(String.valueOf(completed)));
        viewModel.getInProgressProjects().observe(getViewLifecycleOwner(), inProgress -> 
            tvInProgressProjects.setText(String.valueOf(inProgress)));
        viewModel.getNotStartedProjects().observe(getViewLifecycleOwner(), notStarted -> 
            tvNotStartedProjects.setText(String.valueOf(notStarted)));

        // Observe task statistics
        viewModel.getTotalTasks().observe(getViewLifecycleOwner(), total -> 
            tvTotalTasks.setText(String.valueOf(total)));
        viewModel.getCompletedTasks().observe(getViewLifecycleOwner(), completed -> 
            tvCompletedTasks.setText(String.valueOf(completed)));
        viewModel.getInProgressTasks().observe(getViewLifecycleOwner(), inProgress -> 
            tvInProgressTasks.setText(String.valueOf(inProgress)));
        viewModel.getNotStartedTasks().observe(getViewLifecycleOwner(), notStarted -> 
            tvNotStartedTasks.setText(String.valueOf(notStarted)));

        // Observe performance statistics
        viewModel.getAvgProjectTime().observe(getViewLifecycleOwner(), avgTime -> 
            tvAvgProjectTime.setText(String.format(Locale.getDefault(), "%.1f days", avgTime)));
        viewModel.getAvgTaskTime().observe(getViewLifecycleOwner(), avgTime -> 
            tvAvgTaskTime.setText(String.format(Locale.getDefault(), "%.1f days", avgTime)));
        viewModel.getOnTimeRate().observe(getViewLifecycleOwner(), rate -> 
            tvOnTimeRate.setText(String.format(Locale.getDefault(), "%.1f%%", rate)));

        // Observe chart data
        viewModel.getProjectStatusData().observe(getViewLifecycleOwner(), this::updateProjectPieChart);
        viewModel.getTaskStatusData().observe(getViewLifecycleOwner(), this::updateTaskBarChart);
        viewModel.getPerformanceData().observe(getViewLifecycleOwner(), this::updatePerformanceLineChart);
    }

    private void updateProjectPieChart(Map<String, Integer> statusData) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusData.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Project Status");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChartProjects));

        PieData data = new PieData(dataSet);
        pieChartProjects.setData(data);
        pieChartProjects.invalidate();
    }

    private void updateTaskBarChart(Map<String, Integer> statusData) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : statusData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Task Status");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        barChartTasks.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartTasks.setData(data);
        barChartTasks.invalidate();
    }

    private void updatePerformanceLineChart(Map<String, Float> performanceData) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : performanceData.entrySet()) {
            entries.add(new Entry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Performance");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        LineData data = new LineData(dataSet);
        lineChartPerformance.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartPerformance.setData(data);
        lineChartPerformance.invalidate();
    }
} 