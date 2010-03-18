package com.replica.replicaisland;

import java.util.Arrays;
import java.util.Comparator;

public class StandardSorter<T> extends Sorter {

    @Override
    public void sort(Object[] array, int count, Comparator comparator) {
        Arrays.sort(array, 0, count, comparator);
    }

}
