package me.hackerguardian.main.ML;

import me.hackerguardian.main.HackerGuardian;

public class LabeledData implements Cloneable {
    private int category;
    private double[] data;

    public LabeledData(int category, double[] values) {
        this.category = category;
        this.data = values;
    }

    public int getCategory() {
        return this.category;
    }

    public double[] getData() {
        return this.data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    public void setData(int row, double data) {
        this.data[row] = data;
    }
    public LabeledData clone() {
        try {
            return (LabeledData) super.clone();
        } catch (CloneNotSupportedException e) {
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
            return null;
        }
    }
}