package me.hackerguardian.main.Utils;

import me.hackerguardian.main.ML.LabeledData;
import org.apache.commons.lang.ArrayUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SLMaths {

    private SLMaths() {}

    public static double[] extractFeatures(List<Float> angleSequence) {
        List<Double> anglesDouble = toDoubleList(angleSequence);
        List<Double> anglesDoubleDelta = calculateDelta(anglesDouble);

        double featureA = stddev(anglesDouble);
        double featureB = mean(anglesDouble);
        double featureC = stddev(anglesDoubleDelta);
        double featureD = mean(anglesDoubleDelta);

        return new double[] {featureA, featureB, featureC, featureD};
    }

    public static List<Double> calculateDelta(List<Double> doubleList) {
        if (doubleList.size() <= 1) {
            throw new IllegalArgumentException(
                    "There is supposed to be 2 instead of 1 to calculate Delta"
            );
        }
        List<Double> out = new ArrayList<>();
        for (int i = 1; i <= doubleList.size() - 1; i++)
            out.add(doubleList.get(i) - doubleList.get(i - 1));
        return out;
    }

    public static List<Double> toDoubleList(List<Float> floatList) {
        return floatList.stream().map(e -> (double) e).collect(Collectors.toList());
    }

    public static double mean(List<Double> angles) {
        return angles.stream().mapToDouble(e -> e).sum() / angles.size();
    }

    public static double stddev(List<Double> angles) {
        double mean = mean(angles);
        double output = 0;
        for (double angle : angles)
            output += Math.pow(angle - mean, 2);
        return output / angles.size();
    }

    public static double euclideanDistance(double[] vectorA, double[] vectorB) {
        validateDimension("2 vectors are needed to be in the same dim!", vectorA, vectorB);

        double dist = 0;
        for (int i = 0; i <= vectorA.length -1; i++)
            dist += Math.pow(vectorA[i] - vectorB[i], 2);
        return Math.sqrt(dist);
    }
    public static List<Double> toList(double[] doubleArray) {
        return Arrays.asList(ArrayUtils.toObject(doubleArray));
    }

    public static double[] toArray(List<Double> doubleList) {
        return doubleList.stream().mapToDouble(e -> e).toArray();
    }

    public static double[] randomArray(int length) {
        double[] randomArray = new double[length];
        applyFunc(randomArray, e -> e = ThreadLocalRandom.current().nextDouble());
        return randomArray;
    }

    public static void applyFunc(double[] doubleArray, Function<Double, Double> func) {
        for (int i = 0; i <= doubleArray.length - 1; i++)
            doubleArray[i] = func.apply(doubleArray[i]);
    }

    public static double[] add(double[] vectorA, double[] vectorB) {
        validateDimension("Two vectors need to have exact the same dimension", vectorA, vectorB);

        double[] output = new double[vectorA.length];
        for (int i = 0; i <= vectorA.length - 1; i++)
            output[i] = vectorA[i] + vectorB[i];
        return output;
    }

    public static double[] subtract(double[] vectorA, double[] vectorB) {
        validateDimension("Two vectors need to have exact the same dimension", vectorA, vectorB);
        return add(vectorA, opposite(vectorB));
    }

    public static double[] opposite(double[] vector) {
        return multiply(vector, -1);
    }

    public static double[] multiply(double[] vector, double factor) {
        double[] output = vector.clone();
        applyFunc(output, e -> e * factor);
        return output;
    }

    public static double[][] normalize(List<LabeledData> dataset) {
        validateDimension("Data in dataset have inconsistent features",
                dataset.stream().map(LabeledData::getData).toArray(double[][]::new));

        int dimension = dataset.get(0).getData().length;
        double[][] minMax = new double[dimension][2];

        for (int row = 0; row <= dimension - 1; row++) {
            int rowCurrent = row;
            double min = Collections.min(dataset.stream()
                    .map(data -> data.getData()[rowCurrent]).collect(Collectors.toList()));
            double max = Collections.max(dataset.stream()
                    .map(data -> data.getData()[rowCurrent]).collect(Collectors.toList()));
            minMax[row] = new double[]{min, max};
            for (int i = 0; i <= dataset.size() - 1; i++) {
                double originalValue = dataset.get(i).getData()[row];
                dataset.get(i).setData(row, (originalValue - min) / (max - min));
            }
        }
        return minMax;
    }

    public static double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    public static double round(double value, int precision, RoundingMode mode) {
        return BigDecimal.valueOf(value).round(new MathContext(precision, mode)).doubleValue();
    }

    @SuppressWarnings("SameParameterValue")
    private static void validateDimension(String message, double[]... vectors) {
        for (int i = 0; i <= vectors.length - 1; i++) {
            if (vectors[0].length != vectors[i].length)
                throw new IllegalArgumentException(message);
        }
    }
}
