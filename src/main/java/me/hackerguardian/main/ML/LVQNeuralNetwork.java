package me.hackerguardian.main.ML;

import me.hackerguardian.main.Core;
import me.hackerguardian.main.Utils.SLMaths;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LVQNeuralNetwork {


    private double step_size;
    private double step_dec_rate;
    private double min_step_size;

    private int epoch = 0;

    private int dimension;

    private List<LabeledData> vectors = new ArrayList<>();

    private List<LabeledData> classCenters = new ArrayList<>();

    private double[][] minMaxOfRow;

    public LVQNeuralNetwork(int dimension, double step_size, double step_dec_rate, double min_step_size) {
        this.dimension = dimension;
        this.step_size = step_size;
        this.step_dec_rate = step_dec_rate;
        this.min_step_size = min_step_size;
    }
    @SuppressWarnings({})
    public void addData(LabeledData vector) {
        if (vector.getData().length != dimension)
            throw new IllegalArgumentException(String.format("Input has illegal dim (%d, excepted &d)" , vector.getData().length, dimension));

        vectors.add(vector);
    }

    private TreeMap<Double, Integer> getDistanceToClassCenters(double[] vector) {

        if (classCenters.size() == 0)
            throw new IllegalArgumentException("Output layer is not init yet");
        TreeMap<Double, Integer> distanceToInput = new TreeMap<>();
        for (int i = 0; i <= classCenters.size() - 1; i++)
            distanceToInput.put(SLMaths.euclideanDistance(vector, classCenters.get(i).getData()), i);
        return distanceToInput;
    }

    public void initializeOutputLayer() {
        epoch = 0;

        vectors.stream()
                .map(LabeledData::getCategory)
                .collect(Collectors.toSet())
                .forEach(category -> vectors.stream()
                        .filter(vector -> vector.getCategory() == category)
                        .findAny()
                        .ifPresent(randomVector -> classCenters.add(randomVector.clone()))
                );
    }

    public void normalize() {
        minMaxOfRow = SLMaths.normalize(vectors);
    }

    public void train() {
        for (LabeledData vector : vectors) {
            LabeledData nearestOutput = classCenters.get(getDistanceToClassCenters(vector.getData()).firstEntry().getValue());
            double[] distToNearestOutput = SLMaths.multiply(SLMaths.subtract(vector.getData(), nearestOutput.getData()), step_size);

            if (vector.getCategory() == nearestOutput.getCategory())
                nearestOutput.setData(SLMaths.add(nearestOutput.getData(), distToNearestOutput));
            else
                nearestOutput.setData(SLMaths.subtract(nearestOutput.getData(), distToNearestOutput));
        }

        if (step_size > min_step_size)
            step_size *= step_dec_rate;
        else
            step_size = min_step_size;

        epoch++;
    }

    public LVQNeuralNetworkPredictResult predict(double[] vector) {
        if (classCenters.size() == 0)
            throw new IllegalArgumentException("Output layer is not init yet");

        double[] vectorNormalized = vector.clone();
        for (int i = 0; i <= vector.length - 1; i++)
            vectorNormalized[i] = SLMaths.normalize(vector[i], minMaxOfRow[i][0], minMaxOfRow[i][1]);
        return new LVQNeuralNetworkPredictResult(getDistanceToClassCenters(vectorNormalized));
    }

    void printVectors() {
        if (dimension != 2)
            throw new IllegalArgumentException("No more then 2 dims");
        System.out.println("Input vectors: ");
        int[][] outputImage = new int[vectors.size()][vectors.size()];
        for (LabeledData vector : vectors)
            outputImage[(int) (vector.getData()[0] * 10)][(int) (vector.getData()[1] * 10)] = vector.getCategory() + 1;

        System.out.println("+" + StringUtils.repeat("--", vectors.size()) + "+");
        for (int i = 0; i <= outputImage.length - 1; i++) {
            System.out.print("|");
            for (int j = 0; j <= outputImage.length - 1; j++)
                System.out.print("|\n");
        }
        System.out.println("+" + StringUtils.repeat("--", vectors.size()) + "+");

        for (int i = 0; i<= outputImage.length -1; i++)
            for (int j = 0; j <= outputImage.length -1; j++)
                outputImage[i][j] = 0;
        System.out.println("Output layer: ");
        for (LabeledData vector : classCenters)
            outputImage[(int) Math.round(vector.getData()[0] * 10)][(int) Math.round(vector.getData()[1] * 10)] = vector.getCategory() + 1;
        System.out.println("+" + StringUtils.repeat("--", vectors.size()) + "+");
        for (int i = 0; i <= outputImage.length -1; i++) {
            System.out.print("|");
            for (int j = 0; j <= outputImage.length - 1; j++)
                System.out.print(outputImage[i][j] == 0 ? " " : outputImage[i][j] + " ");
            System.out.print("|\n");
        }
        System.out.println("+" + StringUtils.repeat("--", vectors.size()) + "+");
    }

    public void printStats(Logger logger) {
        logger.info("Current Epoch: " + epoch + ", Current step size: " + step_size);
        logger.info("Output layer:");
        classCenters.forEach(vector -> logger.info( " - " + vector.getCategory() + " " + Arrays.toString(vector.getData())));
        logger.info("Dataset (normalized):");
        vectors.forEach(vector -> logger.info(" - " + vector.getCategory() + " " + Arrays.toString(vector.getData())));
        //Figure out a way to implement printVectors
        //printVectors();


    }

    public LVQNeuralNetworkSummary getSummaryStatistics() {
        return new LVQNeuralNetworkSummary(epoch, step_size, vectors.size(), classCenters.size());
    }
}
