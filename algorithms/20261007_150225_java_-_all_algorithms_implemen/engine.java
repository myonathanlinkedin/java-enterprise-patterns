package algos;

import java.util.*;

public class Engine {

    // QuickSort (in-place)
    public static void quickSort(int[] arr) {
        quickSortRec(arr, 0, arr.length - 1);
    }

    private static void quickSortRec(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSortRec(arr, low, pi - 1);
            quickSortRec(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    // MergeSort (returns new sorted array)
    public static int[] mergeSort(int[] arr) {
        if (arr.length <= 1) {
            return arr.clone();
        }
        int mid = arr.length / 2;
        int[] left = Arrays.copyOfRange(arr, 0, mid);
        int[] right = Arrays.copyOfRange(arr, mid, arr.length);
        return merge(mergeSort(left), mergeSort(right));
    }

    private static int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0, li = 0, ri = 0;
        while (li < left.length && ri < right.length) {
            if (left[li] <= right[ri]) {
                result[i++] = left[li++];
            } else {
                result[i++] = right[ri++];
            }
        }
        while (li < left.length) {
            result[i++] = left[li++];
        }
        while (ri < right.length) {
            result[i++] = right[ri++];
        }
        return result;
    }

    // Binary Search (iterative)
    public static int binarySearch(int[] sorted, int target) {
        int lo = 0, hi = sorted.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (sorted[mid] == target) {
                return mid;
            } else if (sorted[mid] < target) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return -1;
    }

    // Reverse singly linked list
    public static Types.ListNode reverseLinkedList(Types.ListNode head) {
        Types.ListNode prev = null;
        Types.ListNode curr = head;
        while (curr != null) {
            Types.ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    // Breadth-First Search (returns visitation order)
    public static List<Integer> bfs(Types.Graph g, int start) {
        boolean[] visited = new boolean[g.size()];
        List<Integer> order = new ArrayList<>();
        Queue<Integer> q = new ArrayDeque<>();
        visited[start] = true;
        q.add(start);
        while (!q.isEmpty()) {
            int v = q.poll();
            order.add(v);
            for (Types.Edge e : g.neighbors(v)) {
                if (!visited[e.to]) {
                    visited[e.to] = true;
                    q.add(e.to);
                }
            }
        }
        return order;
    }

    // Depth-First Search (iterative, returns visitation order)
    public static List<Integer> dfs(Types.Graph g, int start) {
        boolean[] visited = new boolean[g.size()];
        List<Integer> order = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (visited[v]) continue;
            visited[v] = true;
            order.add(v);
            // push neighbors in reverse order to mimic recursive order
            List<Types.Edge> neigh = g.neighbors(v);
            for (int i = neigh.size() - 1; i >= 0; i--) {
                int to = neigh.get(i).to;
                if (!visited[to]) {
                    stack.push(to);
                }
            }
        }
        return order;
    }

    // Dijkstra's algorithm (returns distance array, Integer.MAX_VALUE for unreachable)
    public static int[] dijkstra(Types.Graph g, int source) {
        int n = g.size();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{source, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int v = cur[0];
            int d = cur[1];
            if (d != dist[v]) continue; // stale entry
            for (Types.Edge e : g.neighbors(v)) {
                int nd = d + e.weight;
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                    pq.offer(new int[]{e.to, nd});
                }
            }
        }
        return dist;
    }
}
