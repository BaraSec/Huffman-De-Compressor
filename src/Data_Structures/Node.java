package Data_Structures;

public class Node implements Comparable<Node>
{
	// Data fields
	private int ch;
	private int freq;
	private Node left, right;

	// Constructors
	public Node(int ch, int freq) {
		this.ch = ch;
		this.freq = freq;
	}

	public Node(int ch, int freq, Node left, Node right) {
		this.ch = ch;
		this.freq = freq;
		this.left = left;
		this.right = right;
	}

	// Getters
	public int getCh()
	{
		return ch;
	}

	public int getFreq()
	{
		return freq;
	}

	public Node getLeft()
	{
		return left;
	}

	public Node getRight()
	{
		return right;
	}

	// is the node a leaf node?
	public boolean isLeaf()
	{
		return (left == null) && (right == null);
	}

	// compare, based on frequency
	public int compareTo(Node that) {
		return this.freq - that.freq;
	}
}