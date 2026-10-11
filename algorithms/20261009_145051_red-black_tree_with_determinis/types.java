package rbtree;

enum Color {
    RED,
    BLACK
}

class RBNode {
    int key;
    int value;
    Color color;
    RBNode left;
    RBNode right;
    RBNode parent;

    RBNode(int key, int value, Color color) {
        this.key = key;
        this.value = value;
        this.color = color;
        this.left = null;
        this.right = null;
        this.parent = null;
    }
}
