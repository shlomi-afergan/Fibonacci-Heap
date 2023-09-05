/**
 * FibonacciHeap
 *
 * ______________________________________
 * Students:
 * Shlomi Afergan, shlomia
 * Guy Bilitski, guybilitski
 * _______________________________________
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap {

    private HeapNode min;
    private HeapNode leftRoot;
    private int size;
    private int numOfTrees;
    private int numOfMarks;
    private static int numOfLinks = 0;
    private static int numOfCuts = 0;  // Use it by static function.

    public FibonacciHeap(HeapNode node) {
        this.min = node;
        this.size = node.size;
        this.leftRoot = node;
        this.numOfMarks = 0;
        this.numOfTrees = 0;
    }

    public FibonacciHeap() {
        this.min = null;
        this.leftRoot = null;
        this.size = 0;
    }

    /**
     * public boolean isEmpty()
     * Complexity: O(1)
     * <p>
     * Returns true if and only if the heap is empty.
     */
    public boolean isEmpty() {
        return this.min == null;
    }

    /**
     * public HeapNode insert(int key)
     * <p>
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * The added key is assumed not to already belong to the heap.
     * <p>
     * Complexity: O(1)
     * Returns the newly created node.
     */
    public HeapNode insert(int key) {
        HeapNode newNode = new HeapNode(key);
        addHeapNode(newNode);
        return newNode;
    }

    /**
     * public void deleteMin()
     * <p>
     * W.C Complexity: O(n)
     * Amortized Complexity: O(logn)
     * Deletes the node containing the minimum key.
     */
    public void deleteMin() {
        if (this.size == 1) {
            this.min.prev = this.min.next = this.min.child = null;
            this.min = null;
            this.leftRoot = null;
        } else {
            if (this.min == this.leftRoot) {
                if (this.min.child != null) {
                    this.leftRoot = this.min.child;
                } else {
                    this.leftRoot = this.min.next;
                }
            }

            if (this.min.child != null) {  // remove the min node and update the pointers (prev, next, child)
                HeapNode childNode = this.min.child;
                setParentsAsNull(childNode);
                childNode.prev.next = this.min.next; // update the right child node, then update the left child node
                this.min.next.prev = childNode.prev;
                childNode.prev = this.min.prev;
                this.min.prev.next = childNode;
                this.numOfTrees += this.min.rank; //The number of trees increases by the number of children of min.
            } else {
                this.min.prev.next = this.min.next;   // if dont have a child - just remove the min node.
                this.min.next.prev = this.min.prev;
            }
        }
        this.size--;
        this.numOfTrees --;   // The number of trees is reduced by 1 (line 97: increased by the number of children)
        if (this.size > 1) {
            this.consolidate();
        }
        else {
            this.min = this.leftRoot;
        }

    }

    /**
     * public HeapNode findMin()
     * <p>
     * Complexity: O(1)
     * Returns the node of the heap whose key is minimal, or null if the heap is empty.
     */
    public HeapNode findMin() {
        if (this.isEmpty()) {
            return null;
        } else {
            return this.min;
        }
    }

    /**
     * Performs consolidation to the Fibonacci heap.
     * @pre: heap is not empty
     * W.C Complexity: O(logn)
     * Amortized Complexity: O(1)
     */
    public void consolidate() {
        int reqSize = (int) (Math.log(this.size) / Math.log(2)) + 2; // upper bound for biggest tree rank (real one using the golden ratio)
        HeapNode moveNode = this.leftRoot;
        HeapNode[] basket = new HeapNode[reqSize];
        HeapNode node, node1, node2, nextToConcatenate;

        do {
            node = moveNode;
            moveNode = moveNode.next;
            if (basket[node.rank] == null) {
                basket[node.rank] = node; //insert to basket
                disconnectFromList(node); // disconnect from list
                node.next = node.prev = node;
            }
            else {
                node2 = minOfNodes(node, basket[node.rank])[0]; // the minimal
                node1 = minOfNodes(node, basket[node.rank])[1]; // the bigger (gets disconnected)
                basket[node.rank] = null;
                disconnectFromList(node);
                concatenateRoots(node1, node2); //node1 is taken as the child of node2
                node2.next = node2.prev = node2;
                while (basket[node2.rank] != null) {
                    nextToConcatenate = basket[node2.rank];
                    basket[node2.rank] = null;
                    node1 = minOfNodes(node2, nextToConcatenate)[1]; // the bigger (gets disconnected)
                    node2 = minOfNodes(node2, nextToConcatenate)[0]; // the minimal

                    concatenateRoots(node1, node2);
                }

                basket[node2.rank] = node2;
            }
        }
        while (!this.isEmpty());
        for (int i=basket.length - 1; i >= 0; i--) {
            if (basket[i] != null) {
                this.addHeapNode(basket[i]);
            }
        }
    }

    /**
     * public void meld (FibonacciHeap heap2)
     * <p>
     * Complexity: O(1)
     * Melds heap2 with the current heap.
     */
    public void meld(FibonacciHeap heap2) {
        if (this.min.key > heap2.min.key) {
            this.min = heap2.min;
        }

        HeapNode rightNodeHeap1 = this.leftRoot.prev;
        HeapNode leftNodeHeap1 = this.leftRoot;
        HeapNode rightNodeHeap2 = heap2.leftRoot.prev;
        HeapNode leftNodeHeap2 = heap2.leftRoot;

        rightNodeHeap1.next = leftNodeHeap2;
        leftNodeHeap2.prev = rightNodeHeap1;

        leftNodeHeap1.prev = rightNodeHeap2;
        rightNodeHeap2.next = leftNodeHeap1;

        this.size += heap2.size;
        this.numOfTrees += heap2.numOfTrees;
    }

    /**
     * public int size()
     * <p>
     * Complexity: O(1)
     * Returns the number of elements in the heap.
     */
    public int size() {
        return this.size;
    }

    /**
     * public int[] countersRep()
     * <p>
     * Complexity: O(n) TODO: validate that
     * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
     * Note: The size of of the array depends on the maximum order of a tree, and an empty heap returns an empty array.
     */
    public int[] countersRep() {
        if (this.isEmpty()) {
            return new int[] {};
        }
        int reqSize = (int) (Math.log(this.size) / Math.log(2)) + 2; // upper bound for biggest tree rank (real one using the golden ratio)
        int[] arr = new int[reqSize];
        HeapNode node = this.leftRoot;
        do {
            arr[node.rank]++;
            node = node.next;
        }
        while (node != this.leftRoot);
        return arr;
    }

    /**
     * public void delete(HeapNode x)
     * W.C Complexity: O(n)
     * Amortized Complexity: O(logn)
     * <p>
     * Deletes the node x from the heap.
     * It is assumed that x indeed belongs to the heap.
     */
    public void delete(HeapNode x) {
        if (x != this.min) {
            actualDecreaseKey(x, (Math.abs(this.min.key) + Math.abs(x.key) + 1), true);
        }
        deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     * W.C Complexity: O(logn)
     * Amortized Complexity: O(1)
     * <p>
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta) {
        actualDecreaseKey(x, delta, false);  //There is no intention to delete the node at the end of the decrease
    }


    /**
     * public int potential()
     * Complexity: O(1)
     * <p>
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     * <p>
     * In words: The potential equals to the number of trees in the heap
     * plus twice the number of marked nodes in the heap.
     */
    public int potential() {
        return (this.numOfTrees + 2 * this.numOfMarks);
    }

    /**
     * public static int totalLinks()
     * Complexity: O(1)
     * <p>
     * This static function returns the total number of link operations made during the
     * run-time of the program. A link operation is the operation which gets as input two
     * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
     * tree which has larger value in its root under the other tree.
     */
    public static int totalLinks() {
        return numOfLinks;
    }

    /**
     * public static int totalCuts()
     * Complexity: O(1)
     * <p>
     * This static function returns the total number of cut operations made during the
     * run-time of the program. A cut operation is the operation which disconnects a subtree
     * from its parent (during decreaseKey/delete methods).
     */
    public static int totalCuts() {
        return numOfCuts;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     * Complexity: O(K*deg(H))
     * <p>
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
     * <p>
     * ###CRITICAL### : you are NOT allowed to change H.
     */
    public static int[] kMin(FibonacciHeap H, int k) {
        int[] arr = new int[k];
        int index = 0;
        FibonacciHeap minFibHeap = new FibonacciHeap();
        HeapNode pointer = H.min; // should be the root.
        if (H.isEmpty() || k == 0) {
            return arr;
        }
        else{
            arr[index++] = H.min.key;
            k--;
        }
        while (k > 0){
            if (pointer.child != null){
                pointer = pointer.child;
                HeapNode stable = pointer;
                do{
                    minFibHeap.insert(pointer.key);
                    minFibHeap.leftRoot.pointer = pointer;
                    pointer = pointer.next;
                }
                while (pointer != stable);
            }
            pointer = minFibHeap.min.pointer;
            arr[index++] = pointer.key;
            minFibHeap.deleteMin();
            k --;
        }
        return arr;
    }

    // *****----------------------------------------                   ----------------------------------------***** //
    // *****---------------------------------------- Private functions ----------------------------------------***** //
    // *****----------------------------------------                   ----------------------------------------***** //

    /**
     * Disconnects node from current heap.
     * Complexity: O(1)
     * @pre: node must be a root of current heap
     * @pre: heap must have at least one tree
     */
    private void disconnectFromList(HeapNode node) {
        if (this.numOfTrees == 1) {
            this.min = this.leftRoot = null;
        }
        if (node == this.leftRoot) {
            leftRoot = leftRoot.next;
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
        this.numOfTrees --;
        this.size -= node.size;
    }

    /**
     * Gets two nodes and returns the minimal one at index 0 , the maximal one at index 1
     * Complexity: O(1)
     */
    private HeapNode[] minOfNodes(HeapNode node1, HeapNode node2) {
        if (node1.key < node2.key) {
            return new HeapNode[] {node1, node2};
        }
        else {
            return new HeapNode[] {node2, node1};
        }
    }

    /**
     * Gets a node and adds it to the most left place of roots inside the heap
     * Complexity: O(1)
     */
    private void addHeapNode(HeapNode newNode) {
        if (this.min == null) {
            this.min = this.leftRoot = newNode;
        } else {
            newNode.next = this.leftRoot;
            newNode.prev = this.leftRoot.prev;
            this.leftRoot.prev = newNode;
            newNode.prev.next = newNode;
            this.leftRoot = newNode;
            if (this.min.key > newNode.key) { // check if min should change
                this.min = newNode;
            }
        }

        this.size += newNode.size;
        this.numOfTrees ++;
    }


    /**
     * Gets two HeapNodes and concatenates them to each other
     * the newly concatenated tree is node 2
     * Complexity: O(1)
     * @pre: root2 key < root 1 key
     * @pre: rank of both HeapNodes is equal.
     */
    private void concatenateRoots(HeapNode root1, HeapNode root2) {

        if (root2.child == null) {
            root2.child = root1;
            root1.parent = root2;
            root1.next = root1;
            root1.prev = root1;
        } else {
            root1.next = root2.child;
            if (root2.child.prev == root2.child){
                root2.child.next = root1;
            }
            else{
                root2.child.prev.next = root1;
            }
            root1.prev = root2.child.prev;
            root2.child.prev = root1;

            root2.child = root1;
            root1.parent = root2;
        }

        root2.rank++;
        numOfLinks ++;
        root2.size += root1.size;

    }

    /**
     * used both for Delete(x) and for decreaseKey(x, delta)
     * toBeDeleted represents whether the intention is to delete the node
     * at the end of the process or not.
     * W.C Complexity: O(logn)
     * Amortized Complexity: O(1)
     */
    private void actualDecreaseKey(HeapNode x, int delta, boolean toBeDeleted){
        x.key -= delta;
        if ((x.key < this.min.key) && !toBeDeleted){
            this.min = x;
        }
        if (x.parent != null){  // if x is not a root:
            if (x.key < x.parent.key){
                cascadingCut(x, x.parent);
            }
        }
    }

    /**
     * Gets two HeapNodes, x as son and y parent. cuts x from y and move on to perform the cascading cuts based
     * on marks.
     * W.C Complexity: O(logn)
     * Amortized Complexity: O(1)
     */
    private void cascadingCut(HeapNode x, HeapNode y){
        cut(x, y);                     // cut x from y.
        if (y.parent != null){        // if y is not a root
            if (!y.mark){            // if y is not marked yet:
                y.mark = true;      // then mark y
                this.numOfMarks ++;
            }
            else{                 // else move up the cascadingCut.
                cascadingCut(y, y.parent);
            }
        }
    }

    /**
     * cuts HeapNode x from y
     * Complexity: O(1)
     */
    private void cut(HeapNode x, HeapNode y){   // x becomes a root
        // Cut x:
        x.parent = null;
        if (x.mark) {
            this.numOfMarks --;
        }
        x.mark = false;              // root never marked
        this.numOfTrees ++;

        // update y's pointers and fields:
        y.rank --;
        y.size -= x.size;

        if (x.next == x) {         // if x was the only child of y --> then y has no children right now.
            y.child = null;
        } else {                        // updates the connections between y's children
            x.prev.next = x.next;  // come full circle (after cut off x)
            x.next.prev = x.prev;
        }
        if (y.child == x) {          // if x is the direct child of y:
            y.child = x.next;       // the second left node becomes the child.
        }

        // place x to the left of the heap:
        x.prev = this.leftRoot.prev;
        x.next = this.leftRoot;
        this.leftRoot.prev.next = x;
        this.leftRoot.prev = x;
        this.leftRoot = x;          // update x to be leftRoot
        numOfCuts ++;
    }

    /**
     * sets h and its brothers parent as null
     * Complexity: O(k) when k is the number of brothers (thus if k is deg of parent inside heap, the complexity is O(logn)
     */
    private void setParentsAsNull(HeapNode h) {
        HeapNode stable = h;
        do {
            h.parent = null;
            h = h.next;
        }
        while (stable != h);
    }

    /**
     * public class HeapNode
     * <p>
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in another file.
     */
    public static class HeapNode {
        public int key;
        public int size;
        public int rank;
        public boolean mark;
        public HeapNode child;
        public HeapNode prev;
        public HeapNode next;
        public HeapNode parent;
        public HeapNode pointer;   // for kMin only

        public HeapNode(int key) {
            this.key = key;
            this.size = 1;
            this.rank = 0;
            this.mark = false;
            this.child = null;
            this.prev = this;
            this.next = this;
            this.parent = null;
        }
        public int getKey() {
            return this.key;
        }

        public HeapNode getChild() {
            return this.child;
        }

        public HeapNode getNext() {
            return this.next;
        }

    }

}