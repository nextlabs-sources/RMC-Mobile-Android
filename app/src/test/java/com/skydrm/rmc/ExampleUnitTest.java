package com.skydrm.rmc;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by jrzhou on 8/1/2017.
 */
public class ExampleUnitTest {

    @Test
    public void newCachedThreadPool_test() throws InterruptedException {

    }

    private void throwExceptionMethod(int test) {
        if (test > 90) {
            throw new NullPointerException("" + test);
        }
    }

    @Test
    public void subList_test() {
        List<Integer> list = new ArrayList<>();

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);

        List<Integer> subList = list.subList(0, 2);

        System.out.print("list :" + list.toString());
        System.out.print("subList :" + subList.toString());
    }

    @Test
    public void fuzzyMatch() {
        String aaa = "/aaa/bbb_1/ccc_1/";
//        String bbb="aaa/bbb_1/ccc_1/";
        String bbb = "/aaa/bbb_1/ccc_2/";
        boolean b = aaa.startsWith(bbb);
        System.out.print(b);
    }

    @Test
    public void test_subList() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        List<Integer> subList = list.subList(0, 5);
    }


    @Test
    public void test_copyOnWristArrayList() {
        final CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

        CopyOnWriteArrayList<String> remove = new CopyOnWriteArrayList<>();

        for (int i = 0; i < 100; i++) {
            list.add(i + "");
        }

        for (int i = 0; i < 50; i++) {
            remove.add(i + "");
        }

        for (String s : list) {
            System.out.println(s);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 100; i < 200; i++) {
                    list.add(i + "");
                }
            }
        }).start();

        list.removeAll(remove);

        for (String s : list) {
            System.out.println(s);
        }
    }


    @Test
    public void hashBasedTable() {
        Table<String, String, Integer> table = HashBasedTable.create();

        table.put("0010", "i", 1);
        table.put("0010", "e", 2);
        table.put("0020", "c", 2);
        table.put("0010", "i", 3);

        Set<String> columnKeySet = table.columnKeySet();
        Set<Table.Cell<String, String, Integer>> cellSet = table.cellSet();

        Map<String, Integer> column = table.column("e");

        Map<String, Integer> row = table.row("0010");
        Map<String, Integer> row1 = table.row("0011");

        boolean b = table.containsRow("0011");

        Map<String, Map<String, Integer>> rowMap = table.rowMap();

        Set<String> rowKeySet = table.rowKeySet();//获取行的值到一个set集合中

        List<Map<String, Integer>> list = new ArrayList();
        int i = 0;

        for (String r : rowKeySet) {

            Map<String, Integer> map = new HashMap();

            map = table.row(r); //map的key-value是column和参数的一个对应（key-value不为一个）这就算是遍历出同一个节点下的内容了
            list.add(i, map);
            i++;
        }

        for (int j = 0; j < list.size(); j++) {
            for (String m : list.get(j).keySet()) {
                System.out.print(m + " " + list.get(j).get(m));
            }
            System.out.println();
        }
    }
}
