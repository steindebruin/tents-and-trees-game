package org.example;
public enum Difficulty {
    Easy(6, 8, false),
    Medium(10, 20, false),
    Hard(15, 0, true);

    public final int size;
    public final int treeCount;
    public final boolean isFixed;

    Difficulty(int size, int treeCount, boolean isFixed) {
        this.size = size;
        this.treeCount = treeCount;
        this.isFixed = isFixed;
    }
}
