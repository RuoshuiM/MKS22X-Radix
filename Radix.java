import java.util.Arrays;

/**
 * @author ruosh
 *
 */
public class Radix {

    /**
     * Radix sort should get faster when the arraySize : maxValue ratio increases.
     * (More elements and smaller max value)
     *
     * This sort modifies the array
     *
     * Negative values can be done as per the discussion in class
     *
     * @param data array to be sorted
     */
    public static void radixsort(int[] data) {
        // two buckets for each value of a bit: 0, 1
        MyLinkedList<Integer> b0 = new MyLinkedList<>(), b1 = new MyLinkedList<>(), sorted = new MyLinkedList<>();
        int absMax = 0;

        // on the first iteration, find the max length
        // and dump array data into list
        for (int j = 0; j < data.length; j++) {
            absMax = Math.max(absMax, Math.abs(data[j]));
            if ((data[j] & 1) == 1) {
                b1.push(data[j]);
            } else {
                b0.push(data[j]);
            }
        }

        ///////////////////// DEBUG
//        System.out.println("B0: " + b0.toString());
//        System.out.println("B1: " + b1.toString());
        ////////////////////// END DEBUG

        // number of iterations, excluding first and last
        int maxNumBits = Integer.SIZE - Integer.numberOfLeadingZeros(absMax);

        // join buckets and reset them for next round
        b0.extend(b1);
        sorted.from(b0);
        b0.minimalClear();
        b1.minimalClear();

        //////////////////// DEBUG
//        System.out.format("first pass%n");
//        System.out.println(sorted.toString());
        //////////////////// END DEBUG

        for (int i = 1; i < maxNumBits; i++) {
            for (int e : sorted) {
                if (((e >>> i) & 1) == 1) {
                    b1.push(e);
                } else {
                    b0.push(e);
                }
            }

///////////// DEBUG
//            System.out.println("B0: " + b0.toString());
//            System.out.println("B1: " + b1.toString());
//            System.out.println(i + " pass");
//            System.out.println(sorted.toString());
            /////////////// END DEBUG

            // join buckets and reset them for next round
            b0.extend(b1);
            sorted.from(b0);
            b0.minimalClear();
            b1.minimalClear();
        }

        // compare sign bit
        for (int e : sorted) {
            if (((e >>> 31) & 1) == 1) {
                b1.push(e);
            } else {
                b0.push(e);
            }
        }

        // negative are in b1 while positive in b0, all sorted from least to greatest
        b1.extend(b0);
        sorted.from(b1);

        // copy data back to array
        int i = 0;
        Object[] array = sorted.toArray();
        for (int j = 0; j < array.length; j++) {
            Object e = array[j];
            data[i++] = (int) e;
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
//        int[] data = new int[] { 4, 5, -1, 54, 5, 134, 154, 135, 456, -1002 };
//        System.out.println(Arrays.toString(data));
//
//        radixsort(data);
//        System.out.println(Arrays.toString(data));
        
        compareSorts();
    }

    

    public static void compareSorts() {
        System.out.println("Size\t\tMax Value\tradix/built-in ratio ");
        int[] MAX_LIST = { 1000000000, 500, 10 };
        for (int MAX : MAX_LIST) {
            for (int size = 31250; size < 2000001; size *= 2) {
                long qtime = 0;
                long btime = 0;
                // average of 5 sorts.
                for (int trial = 0; trial <= 5; trial++) {
                    int[] data1 = new int[size];
                    int[] data2 = new int[size];
                    for (int i = 0; i < data1.length; i++) {
                        data1[i] = (int) (Math.random() * MAX);
                        data2[i] = data1[i];
                    }
                    long t1, t2;
                    t1 = System.currentTimeMillis();
                    radixsort(data2);
                    t2 = System.currentTimeMillis();
                    qtime += t2 - t1;
                    t1 = System.currentTimeMillis();
                    Arrays.sort(data1);
                    t2 = System.currentTimeMillis();
                    btime += t2 - t1;
                    if (!Arrays.equals(data1, data2)) {
                        System.out.println("FAIL TO SORT!");
                        System.exit(0);
                    }
                }
//                System.out.format("qtime: %d%nbtime: %d%n", qtime, btime);
                System.out.println(size + "\t\t" + MAX + "\t" + 1.0 * qtime / btime);
            }
            System.out.println();
        }
    }
}
