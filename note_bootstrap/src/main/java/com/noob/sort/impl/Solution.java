package com.noob.sort.impl;

class ListNode {
    int val;
    ListNode next;

    ListNode() {
    }

    ListNode(int val) {
        this.val = val;
    }

    ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}

public class Solution {
    public static ListNode mergeKLists(ListNode[] lists) {
        if (lists == null) {
            return null;
        }

        if (lists.length == 1) {
            return lists[0];
        }

        ListNode node = null;
        ListNode pointer = null;
        ListNode min = null;
        do {
            min = getMin(lists);
            if (min != null) {
                if (node == null) {
                    node = min;
                    pointer = node;
                } else {
                    pointer.next = min;
                    pointer =  pointer.next;
                }
            }
        } while (min != null);
        return node;

    }

    public static ListNode getMin(ListNode[] lists) {
        ListNode min = null;
        Integer index = null;
        for (int i = 0; i < lists.length; i++) {
            if (lists[i] != null) {
                if (min == null || lists[i].val < min.val) {
                    min = lists[i];
                    index = i;
                }
            }

        }
        if (index != null) {
            lists[index] = lists[index].next;
        }

        return min;
    }

    public static void main(String[] args) {
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(4);
        ListNode node5 = new ListNode(5);
        ListNode node6 = new ListNode(6);
        node1.next = node4;
        node2.next = node6;
        node3.next = node5;

        mergeKLists(new ListNode[]{node1, node2, node3});
    }
}