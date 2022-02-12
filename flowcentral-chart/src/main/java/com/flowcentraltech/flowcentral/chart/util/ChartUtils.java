/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.chart.util;

import com.flowcentraltech.flowcentral.chart.data.ChartCategories;
import com.flowcentraltech.flowcentral.chart.data.ChartData;
import com.flowcentraltech.flowcentral.chart.data.ChartDef;
import com.flowcentraltech.flowcentral.chart.data.ChartSeries;
import com.flowcentraltech.flowcentral.chart.data.DateChartCategories;
import com.flowcentraltech.flowcentral.chart.data.DoubleChartSeries;
import com.flowcentraltech.flowcentral.chart.data.IntegerChartCategories;
import com.flowcentraltech.flowcentral.chart.data.IntegerChartSeries;
import com.flowcentraltech.flowcentral.chart.data.LongChartCategories;
import com.flowcentraltech.flowcentral.chart.data.LongChartSeries;
import com.flowcentraltech.flowcentral.chart.data.StringChartCategories;
import com.flowcentraltech.flowcentral.configuration.constants.ChartCategoryDataType;
import com.flowcentraltech.flowcentral.configuration.constants.ChartType;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.util.DataUtils;
import com.tcdng.unify.core.util.StringUtils;
import com.tcdng.unify.core.util.json.JsonWriter;

/**
 * Chart utilities.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public final class ChartUtils {

    private ChartUtils() {

    }

    public static JsonWriter getOptionsJsonWriter(ChartDef chartDef, ChartData chartData, boolean sparkLine)
            throws UnifyException {
        JsonWriter jw = new JsonWriter();
        jw.beginObject();
        final ChartType chartType = chartDef.getType();
        final ChartCategories<?> categories = chartData.getCategories();
        final ChartCategoryDataType categoryType = categories.getCategoryType();
        // Title
        if (!StringUtils.isBlank(chartDef.getTitle())) {
            jw.beginObject("title");
            jw.write("text", chartDef.getTitle());
            jw.endObject();
        }

        // Sub-title
        if (!StringUtils.isBlank(chartDef.getSubTitle())) {
            jw.beginObject("subtitle");
            jw.write("text", chartDef.getSubTitle());
            jw.endObject();
        }

        // Chart
        jw.beginObject("chart");
        if (chartDef.getWidth() > 0) {
            jw.write("width", chartDef.getWidth());
        }

        if (chartDef.getHeight() > 0) {
            jw.write("height", chartDef.getHeight());
        }

        jw.write("type", chartType.optionsType());
        jw.write("stacked", chartDef.isStacked());
        jw.beginObject("toolbar");
        jw.write("show", false);
        jw.endObject();

        jw.beginObject("sparkline");
        jw.write("enabled", sparkLine);
        jw.endObject();

        jw.endObject();

        // Grid
        jw.beginObject("grid");
        jw.write("show", chartDef.isShowGrid());
        jw.endObject();

        // Stroke
        jw.beginObject("stroke");
        if (chartDef.isSmooth()) {
            jw.write("curve", "smooth");
        } else {
            jw.write("curve", "straight");
        }
        jw.endObject();

        // Data labels
        jw.beginObject("dataLabels");
        jw.write("enabled", chartDef.isShowDataLabels());
        jw.endObject();

        // Theme
        jw.beginObject("theme");
        jw.write("mode", "light");
        jw.write("palette", chartDef.getPaletteType().optionsType());
        jw.endObject();

        // Options
        if (chartType.plotOptions()) {
            jw.beginObject("plotOptions");
            if (chartType.axisChart()) {
                jw.beginObject("bar");
                jw.write("horizontal", ChartType.BAR.equals(chartType));
                jw.endObject();
            } else {
                jw.beginObject("pie");
                jw.write("customScale", 0.8);
                // jw.write("offsetY", -20);
                if (ChartType.DONUT.equals(chartType)) {
                    jw.beginObject("donut");
                    jw.write("size", "75%");
                    jw.endObject();
                }
                jw.endObject();
            }

            jw.endObject();
        }

        if (chartType.axisChart()) {
            // Series
            jw.beginArray("series");
            for (ChartSeries<?> series : chartData.getSeries()) {
                jw.beginObject();
                jw.write("name", series.getName());
                switch (series.getSeriesType()) {
                    case DOUBLE:
                        jw.write("data", ((DoubleChartSeries) series).dataToArray());
                        break;
                    case INTEGER:
                        jw.write("data", ((IntegerChartSeries) series).dataToArray());
                        break;
                    case LONG:
                        jw.write("data", ((LongChartSeries) series).dataToArray());
                        break;
                    default:
                        break;
                }
                jw.endObject();
            }
            jw.endArray();

            // X-axis
            jw.beginObject("xaxis");
            jw.write("type", categoryType.optionsType());
            switch (categoryType) {
                case DATE:
                    jw.write("categories",
                            DataUtils.convert(Long[].class, ((DateChartCategories) categories).categoriesToArray()));
                    break;
                case INTEGER:
                    jw.write("categories", ((IntegerChartCategories) categories).categoriesToArray());
                    break;
                case LONG:
                    jw.write("categories", ((LongChartCategories) categories).categoriesToArray());
                    break;
                case STRING:
                    jw.write("categories", ((StringChartCategories) categories).categoriesToArray());
                    break;
                default:
                    break;
            }
            jw.endObject();

            // Legend
        } else {
            // Series
            ChartSeries<?> series = chartData.getSeries().get(0);
            switch (series.getSeriesType()) {
                case DOUBLE:
                    jw.write("series", ((DoubleChartSeries) series).dataToArray());
                    break;
                case INTEGER:
                    jw.write("series", ((IntegerChartSeries) series).dataToArray());
                    break;
                case LONG:
                    jw.write("series", ((LongChartSeries) series).dataToArray());
                    break;
                default:
                    break;
            }

            // Labels
            jw.write("labels", DataUtils.convert(String[].class, categories.categoriesToArray()));

            // Legend
            jw.beginObject("legend");
            jw.write("position", "left");
            jw.write("offsetY", 60);
            jw.endObject();
        }

        jw.endObject();
        return jw;
    }

    public static String getOptionsJson(ChartDef chartDef, ChartData chartData, boolean sparkLine)
            throws UnifyException {
        return ChartUtils.getOptionsJsonWriter(chartDef, chartData, sparkLine).toString();
    }
}
