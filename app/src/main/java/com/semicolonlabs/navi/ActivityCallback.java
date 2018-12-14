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
     *      "name": "place1",
     *      "id": "id1"
     *  ],
     *  [
     *      "name": "place2",
     *      "id": "id2"
     *  ],
     * ]}
     */
    void onPredictions(JSONObject jsonObject);

    /**
     * callback for getting routes
     */
    void onRoutes(JSONObject jsonObject);
}