package me.hackerguardian.main.ML;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LVQNeuralNetworkPredictResult {

    private List<Map.Entry<Double, Integer>> distances;

    LVQNeuralNetworkPredictResult(TreeMap<Double, Integer> distances) {
        this.distances = new ArrayList<>(distances.entrySet());
    }

    public int getCategory() {
        return distances.get(0).getValue();
    }

    public double getDifference() {
        return distances.get(0).getKey();
    }

    public double getLikelihood() {
        return distances.get(0).getKey() / distances.get(1).getKey();
    }
}
