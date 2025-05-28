package com.resistorm.resonator.manager;

import android.app.Activity;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.resistorm.resonator.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResistorDisplayManager {
    private static final Map<String, Integer> COLOR_MAP = new HashMap<String, Integer>() {{
        put("Black", Color.BLACK);
        put("Brown", Color.parseColor("#A52A2A"));
        put("Red", Color.RED);
        put("Orange", Color.parseColor("#FFA500"));
        put("Yellow", Color.YELLOW);
        put("Green", Color.GREEN);
        put("Blue", Color.BLUE);
        put("Violet", Color.parseColor("#8A2BE2"));
        put("Gray", Color.GRAY);
        put("White", Color.WHITE);
        put("Gold", Color.parseColor("#FFD700"));
        put("Silver", Color.parseColor("#C0C0C0"));
    }};

    private final Activity activity;

    public ResistorDisplayManager(Activity activity) {
        this.activity = activity;
    }

    public void displayResistorData(JSONObject response) throws JSONException {
        TextView tvResistanceValue = activity.findViewById(R.id.tvResistanceValue);
        LinearLayout bandContainer = activity.findViewById(R.id.resistorBandContainer);

        bandContainer.removeViews(1, bandContainer.getChildCount() - 1);

        String resistance = response.getString("value");
        tvResistanceValue.setText("Resistance: " + resistance + " Ω");
        tvResistanceValue.setVisibility(View.VISIBLE);

        JSONArray colors = response.getJSONArray("colors");
        LayoutInflater inflater = LayoutInflater.from(activity);

        for (int i = 0; i < colors.length(); i++) {
            String colorName = colors.getString(i);
            View band = inflater.inflate(R.layout.resistor_band, bandContainer, false);
            View bandView = band.findViewById(R.id.resistorBand);

            Integer color = COLOR_MAP.get(colorName);
            if (color != null) {
                bandView.setBackgroundColor(color);
            } else {
                bandView.setBackgroundColor(Color.BLACK);
            }

            bandContainer.addView(band);
        }

        bandContainer.setVisibility(View.VISIBLE);
    }

    public void hideResistorViews() {
        activity.findViewById(R.id.tvResistanceValue).setVisibility(View.GONE);
        activity.findViewById(R.id.resistorBandContainer).setVisibility(View.GONE);
    }
}