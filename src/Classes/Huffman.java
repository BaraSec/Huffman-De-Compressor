package Classes;

import Data_Structures.Node;

import java.util.PriorityQueue;

public class Huffman
{
	public static final int ALPH_SIZE = (1 << 8); // or 256

	// A method to build the huffman tree
	public static Node buildTree(int[] charRatio)
	{
		PriorityQueue<Node> HuffmanTree = new PriorityQueue();

		for(int i = 0; i < ALPH_SIZE; i++)
			if(charRatio[i] != 0)
				HuffmanTree.add(new Node((char)i, charRatio[i]));

		while(HuffmanTree.size() > 1)
		{
			Node sub1 = HuffmanTree.poll();
			Node sub2 = HuffmanTree.poll();
			HuffmanTree.add(new Node(-1, sub1.getFreq()+sub2.getFreq(), sub1, sub2));
		}

		return HuffmanTree.poll();
	}

	// A method to extract the huffman codes from the tree
	public static void extractCodes(Node current, String path, String[] codes)
	{
		if(current.isLeaf())
		{
			codes[current.getCh()] = path;
			return;
		}

		extractCodes(current.getLeft(), path + 0, codes);
		extractCodes(current.getRight(), path + 1, codes);
	}
}
