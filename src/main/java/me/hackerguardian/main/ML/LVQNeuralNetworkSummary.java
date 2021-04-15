package me.hackerguardian.main.ML;

public class LVQNeuralNetworkSummary {

    private int epoch;

    private double step_size;

    private int input_count;

    private int output_count;

    LVQNeuralNetworkSummary(int epoch, double step_size, int input_count, int output_count) {
        this.epoch = epoch;
        this.step_size = step_size;
        this.input_count = input_count;
        this.output_count = output_count;
    }

    public int getEpoch() {
        return epoch;
    }

    public double getCurrentStepSize() {
        return step_size;
    }

    public int getInputCount() {
        return input_count;
    }

    public int getOutputCount() {
        return output_count;
    }
}
