package algos;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        // QuickSort test
        int[] unsorted1 = {5, 2, 9, 1, 5, 6};
        int[] expected1 = {1, 2, 5, 5, 6, 9};
        Engine.quickSort(unsorted1);
        assert Arrays.equals(unsorted1, expected1) : "QuickSort failed";

        // MergeSort test
        int[] unsorted2 = {3, 7, 4, 9, 5, 2, 6, 1};
        int[] expected2 = {1, 2, 3, 4, 5, 6, 7, 9};
        int[] sorted2 = Engine.mergeSort(unsorted2);
        assert Arrays.equals(sorted2, expected2) : "MergeSort failed";

        // Binary Search test
        int idx = Engine.binarySearch(expected2, 5);
        assert idx == 4 : "BinarySearch existing element failed";
        int notFound = Engine.binarySearch(expected2, 8);
        assert notFound == -1 : "BinarySearch missing element failed";

        // Linked List reversal test
        Types.ListNode n1 = new Types.ListNode(1);
        Types.ListNode n2 = new Types.ListNode(2);
        Types.ListNode n3 = new Types.ListNode(3);
        n1.next = n2; n2.next = n3;
        Types.ListNode rev = Engine.reverseLinkedList(n1);
        assert rev.val == 3 && rev.next.val == 2 && rev.next.next.val == 1 && rev.next.next.next == null
                : "LinkedList reversal failed";

        // Graph construction
        Types.Graph g = new Types.Graph(5);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        // Weighted edges for Dijkstra
        g.addEdge(0, 4, 10);
        g.addEdge(1, 4, 5);
        g.addEdge(2, 4, 2);

        // BFS test (starting from 0)
        List<Integer> bfsOrder = Engine.bfs(g, 0);
        List<Integer> expectedBfs = Arrays.asList(0, 1, 2, 3, 4);
        assert bfsOrder.equals(expectedBfs) : "BFS order mismatch";

        // DFS test (starting from 0)
        List<Integer> dfsOrder = Engine.dfs(g, 0);
        List<Integer> expectedDfs = Arrays.asList(0, 1, 3, 4, 2);
        assert dfsOrder.equals(expectedDfs) : "DFS order mismatch";

        // Dijkstra test from source 0
        int[] distances = Engine.dijkstra(g, 0);
        int[] expectedDist = {0, 1, 2, 3, 4}; // based on added edges weights (all weight 1 except explicit)
        // Compute expected manually:
        // 0->1 weight1, 0->2 weight1, 0->4 weight10, 0->1->4 weight1+5=6, 0->2->4 weight1+2=3 (shortest)
        // 0->1->3 weight1+1=2, 0->2->3 weight1+1=2, 0->1->3->4 weight2+1=3 (shorter than 6)
        // So final distances: 0:0, 1:1, 2:1, 3:2, 4:3
        int[] correctDist = {0, 1, 1, 2, 3};
        assert Arrays.equals(distances, correctDist) : "Dijkstra distances mismatch";

        // All assertions passed
        System.out.println("All algorithm tests passed.");
    }
}
