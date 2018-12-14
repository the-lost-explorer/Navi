package com.semicolonlabs.navi;

import org.json.JSONObject;

import java.util.List;

public interface ActivityCallback {
    /**
     * callback for getting predictions
     */
    void onPredictions(List<String> places, List<String> place_ids);

    /**
     * callback for getting routes
     */
    void onRoutes(JSONObject jsonObject);
}