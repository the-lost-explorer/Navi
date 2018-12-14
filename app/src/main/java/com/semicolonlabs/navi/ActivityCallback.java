package com.semicolonlabs.navi;

import org.json.JSONObject;

public interface ActivityCallback {
    /**
     * callback for getting predictions
     *
     * @return: jsonObject
     * {
     * "data": [
     *  [
     *      "place1", "place2", ...
     *  ],
     *  [
     *      "id1", "id2", ...
     *  ],
     * ]}
     */
    void onPredictions(JSONObject jsonObject);

    /**
     * callback for getting routes
     */
    void onRoutes(JSONObject jsonObject);
}