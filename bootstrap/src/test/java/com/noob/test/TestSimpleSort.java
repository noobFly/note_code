package com.noob.test;

import com.noob.sort.impl.BubbleSort;
import com.noob.sort.impl.HeapSort;
import com.noob.sort.impl.InsertionSort;
import com.noob.sort.impl.MergeSort;
import com.noob.sort.impl.QuickSort;
import com.noob.sort.impl.RadixSort;
import com.noob.sort.impl.SelectionSort;
import com.noob.sort.impl.ShellSort;

/**
 * Created by bear on 2016/2/29.
 */
public class TestSimpleSort {


    public static void main(String[] args) {

        new BubbleSort().print();
        new InsertionSort().print();
        new ShellSort().print();
        new SelectionSort().print();
        new MergeSort().print();
        new QuickSort().print();
        new HeapSort().print();
        new RadixSort().print();


    }
}
