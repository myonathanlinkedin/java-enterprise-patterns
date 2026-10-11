package rbtree;

class RedBlackTree {
    private RBNode root;

    public RedBlackTree() {
        this.root = null;
    }

    // Public insert method
    public void insert(int key, int value) {
        RBNode z = new RBNode(key, value, Color.RED);
        RBNode y = null;
        RBNode x = this.root;

        // Standard BST insertion
        while (x != null) {
            y = x;
            if (z.key < x.key) {
                x = x.left;
            } else if (z.key > x.key) {
                x = x.right;
            } else {
                // Duplicate keys: replace value and exit
                x.value = value;
                return;
            }
        }

        z.parent = y;
        if (y == null) {
            this.root = z; // Tree was empty
        } else if (z.key < y.key) {
            y.left = z;
        } else {
            y.right = z;
        }

        // Fix red‑black properties
        insertFixup(z);
    }

    // Search for a key; returns null if not found
    public RBNode search(int key) {
        RBNode x = this.root;
        while (x != null) {
            if (key == x.key) {
                return x;
            } else if (key < x.key) {
                x = x.left;
            } else {
                x = x.right;
            }
        }
        return null;
    }

    public boolean contains(int key) {
        return search(key) != null;
    }

    public int get(int key) {
        RBNode node = search(key);
        if (node == null) {
            throw new IllegalArgumentException("Key not found: " + key);
        }
        return node.value;
    }

    // Left rotation around node x
    private void leftRotate(RBNode x) {
        RBNode y = x.right;
        if (y == null) {
            throw new IllegalStateException("Cannot left-rotate a node with null right child");
        }

        x.right = y.left;
        if (y.left != null) {
            y.left.parent = x;
        }

        y.parent = x.parent;
        if (x.parent == null) {
            this.root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    // Right rotation around node y
    private void rightRotate(RBNode y) {
        RBNode x = y.left;
        if (x == null) {
            throw new IllegalStateException("Cannot right-rotate a node with null left child");
        }

        y.left = x.right;
        if (x.right != null) {
            x.right.parent = y;
        }

        x.parent = y.parent;
        if (y.parent == null) {
            this.root = x;
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }

        x.right = y;
        y.parent = x;
    }

    // Fixup to restore red‑black properties after insertion
    private void insertFixup(RBNode z) {
        while (z.parent != null && z.parent.color == Color.RED) {
            if (z.parent == z.parent.parent.left) {
                RBNode y = z.parent.parent.right; // Uncle
                if (y != null && y.color == Color.RED) {
                    // Case 1: recolor
                    z.parent.color = Color.BLACK;
                    y.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        // Case 2: left-rotate
                        z = z.parent;
                        leftRotate(z);
                    }
                    // Case 3: right-rotate
                    z.parent.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    rightRotate(z.parent.parent);
                }
            } else {
                // Mirror of above with "right" and "left" exchanged
                RBNode y = z.parent.parent.left; // Uncle
                if (y != null && y.color == Color.RED) {
                    // Case 1
                    z.parent.color = Color.BLACK;
                    y.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        // Case 2
                        z = z.parent;
                        rightRotate(z);
                    }
                    // Case 3
                    z.parent.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    leftRotate(z.parent.parent);
                }
            }
        }
        this.root.color = Color.BLACK;
    }

    // Public validation method; throws AssertionError if any property is violated
    public void validate() {
        // Property 1: root is black
        assert this.root == null || this.root.color == Color.BLACK : "Root must be black";

        // Property 2: every leaf (null) is black – implicit, no check needed

        // Property 3: red node's children are black
        validateRedProperty(this.root);

        // Property 4: all paths have same black-height
        int blackHeight = computeBlackHeight(this.root);
        assert blackHeight != -1 : "Black-height mismatch detected";
    }

    // Helper to validate red property recursively
    private void validateRedProperty(RBNode node) {
        if (node == null) {
            return;
        }
        if (node.color == Color.RED) {
            assert (node.left == null || node.left.color == Color.BLACK) :
                "Red node has red left child (key=" + node.key + ")";
            assert (node.right == null || node.right.color == Color.BLACK) :
                "Red node has red right child (key=" + node.key + ")";
        }
        validateRedProperty(node.left);
        validateRedProperty(node.right);
    }

    // Returns black-height if uniform, otherwise -1
    private int computeBlackHeight(RBNode node) {
        if (node == null) {
            return 1; // Null leaves count as black
        }
        int leftBH = computeBlackHeight(node.left);
        int rightBH = computeBlackHeight(node.right);
        if (leftBH == -1 || rightBH == -1 || leftBH != rightBH) {
            return -1; // Mismatch
        }
        return leftBH + (node.color == Color.BLACK ? 1 : 0);
    }

    // In‑order traversal for debugging (not used in tests)
    public String inorder() {
        StringBuilder sb = new StringBuilder();
        inorderHelper(this.root, sb);
        return sb.toString();
    }

    private void inorderHelper(RBNode node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        inorderHelper(node.left, sb);
        sb.append('(').append(node.key).append(',').append(node.color).append(')');
        inorderHelper(node.right, sb);
    }
}
