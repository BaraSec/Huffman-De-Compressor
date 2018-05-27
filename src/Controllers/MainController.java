package Controllers;

import Classes.Huffman;
import Data_Structures.Node;
import IO.BitInputStream;
import IO.BitOutputStream;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MainController
{
	@FXML
	private TextField stat;

	@FXML
	private JFXTextArea charsCodes;

	@FXML
	private JFXTextField inFile;

	@FXML
	private JFXTextField outFile;

	@FXML
	private JFXTextField compRatio;

	@FXML
	private JFXTextArea header;

	// Data fields
	private String inCompressFilePath, inCompressFileName, outCompressFilePath, outCompressFileName, inDeCompressFilePath, outDeCompressFilePath;
	private static final int BITS_PER_WORD = 8;
	private static final int BITS_PER_INT = 32;
	private static final int ALPH_SIZE = (1 << BITS_PER_WORD); // or 256
	private double ratio;
	private boolean isFromArgs;

	// Getters and Setters
	public boolean isFromArgs()
	{
		return isFromArgs;
	}

	public void setFromArgs(boolean fromArgs)
	{
		isFromArgs = fromArgs;
	}

	public String getInCompressFilePath()
	{
		return inCompressFilePath;
	}

	public void setInCompressFilePath(String inCompressFilePath)
	{
		this.inCompressFilePath = inCompressFilePath;
	}

	public String getInCompressFileName()
	{
		return inCompressFileName;
	}

	public void setInCompressFileName(String inCompressFileName)
	{
		this.inCompressFileName = inCompressFileName;
	}

	public String getOutCompressFilePath()
	{
		return outCompressFilePath;
	}

	public void setOutCompressFilePath(String outCompressFilePath)
	{
		this.outCompressFilePath = outCompressFilePath;
	}

	public String getOutCompressFileName()
	{
		return outCompressFileName;
	}

	public void setOutCompressFileName(String outCompressFileName)
	{
		this.outCompressFileName = outCompressFileName;
	}

	public String getInDeCompressFilePath()
	{
		return inDeCompressFilePath;
	}

	public void setInDeCompressFilePath(String inDeCompressFilePath)
	{
		this.inDeCompressFilePath = inDeCompressFilePath;
	}

	public String getOutDeCompressFilePath()
	{
		return outDeCompressFilePath;
	}

	public void setOutDeCompressFilePath(String outDeCompressFilePath)
	{
		this.outDeCompressFilePath = outDeCompressFilePath;
	}

	public double getRatio()
	{
		return ratio;
	}

	// Openning file for compression
	private boolean openFileForCompress()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a file to compress");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Any File", "*");
		fileChooser.getExtensionFilters().add(extFilter);
		File file1 = fileChooser.showOpenDialog(charsCodes.getScene().getWindow());

		if(file1 != null)
		{
			inCompressFilePath = file1.getAbsolutePath();
			inCompressFileName = file1.getName();

			if(inCompressFilePath.lastIndexOf('.') != -1)
				outCompressFilePath = inCompressFilePath.substring(0, inCompressFilePath.lastIndexOf('.')) + ".huf";
			else
				outCompressFilePath = inCompressFilePath + ".huf";
			outCompressFileName = outCompressFilePath.substring(outCompressFilePath.lastIndexOf('\\') + 1);

			if(inCompressFileName.substring(inCompressFileName.lastIndexOf('.') + 1).equalsIgnoreCase("huf"))
			{
				stat.setText("File is already compressed!");
				return false;
			}

			inFile.setText(inCompressFileName);
			outFile.setText(outCompressFileName);

			return true;
		}

		return false;
	}

	// The compression method
	@FXML
	public void compress(ActionEvent event)
	{
		reset();
		if(!isFromArgs && !openFileForCompress())
			return;

		BitInputStream in = new BitInputStream(inCompressFilePath);

		int[] charRatio = new int[ALPH_SIZE];
		long numOfCharInFile = 0;
		int character = 0;

		// calculate chars ratio, and number of chars in file
		while(true)
		{
			character = in.readBits(BITS_PER_WORD);
			if(character == -1)
				break;

			charRatio[character]++;
			numOfCharInFile++;
		}

		in.close();

		in = new BitInputStream(inCompressFilePath);

		// build huffman's tree
		Node root = Huffman.buildTree(charRatio);

		String[] codes = new String[ALPH_SIZE];/////////////////////////////////////////////////////////
		BitOutputStream out = new BitOutputStream(outCompressFilePath);
		String ext = "";

		if(inCompressFileName.lastIndexOf('.') != -1)
			ext = inCompressFileName.substring(inCompressFileName.lastIndexOf('.') + 1);

		// write the extension on the .huf file
		for(int i = 0; i < ext.length(); i++)
			out.writeBits(BITS_PER_WORD, ext.charAt(i));
		out.writeBits(BITS_PER_WORD, '\n');

		String stat = "";
		StringBuilder head = new StringBuilder();
		int headerLength = ext.length() + 1;
		head.append(ext + "\n");

		// write the header on the file
		headerLength += writeHeader(root, out, head) / 8;

		long outDataSize = 0;
		String code = "";

		// extract huffman codes
		Huffman.extractCodes(root, "", codes);

		// write data on .huf file
		while(true)
		{
			character = in.readBits(BITS_PER_WORD);
			if(character == -1)
				break;

			code = codes[character];
			out.writeBits(code.length(), Integer.parseInt(code, 2));
			outDataSize += code.length();
		}
		out.flush();
		outDataSize /= 8;

		// calculate the compression ratio
		ratio = (double)(numOfCharInFile - (headerLength + outDataSize)) * 100 / numOfCharInFile;

		// if not run from command line
		if(!isFromArgs)
		{
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);

			// write info to the GUI
			stat += "Input file size: " + numOfCharInFile + " byte(s)\n";
			stat += "Header size: " + headerLength + " byte(s)\nOutput data size:  " + outDataSize + " byte(s)\nCompressed file size: " + (headerLength + outDataSize) + " byte(s)" + "\n-----------\n";

			for (int i = 0; i < codes.length ; i++)
				stat += (char) i + "\t: " + "Repetition: " + charRatio[i] + ", Code: " + codes[i] + "\n";

			compRatio.setText(nf.format(ratio) + "%");
			charsCodes.setText(stat);
			header.setText(String.valueOf(head));
			this.stat.setAlignment(Pos.CENTER);
			this.stat.setText("File compressed successfully!");
		}

		// close streams
		in.close();
		out.close();

		// Generating an alert
		if(!isFromArgs)
		{
			Platform.runLater(() ->
			{
				Alert alert = new Alert(Alert.AlertType.INFORMATION);

				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				stage.getIcons().add(new Image("Resources/Images/compress.jpg"));

				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(4);

				alert.setTitle("Complete");
				alert.setHeaderText("Success!");
				alert.setContentText("Compression is done!" + "\n\nOutput file name: " + outCompressFileName + "\nCompression Ratio = " + nf.format(ratio) + "%");
				alert.show();
			});
		}
	}

	// Openning file for decompression
	private boolean openFileForDeCompress()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a file to decompress");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Huffman File", "*.huf");
		fileChooser.getExtensionFilters().add(extFilter);
		File file1 = fileChooser.showOpenDialog(charsCodes.getScene().getWindow());

		if(file1 != null)
		{
			inDeCompressFilePath = file1.getAbsolutePath();
			outDeCompressFilePath = inDeCompressFilePath.substring(0, inDeCompressFilePath.lastIndexOf('.') + 1);

			return true;
		}

		return false;
	}

	// The decompression method
	@FXML
	public void deCompress(ActionEvent event)
	{
		reset();
		if(!isFromArgs && !openFileForDeCompress())
			return;

		BitInputStream in = new BitInputStream(inDeCompressFilePath);

		int input = in.readBits(8);
		ArrayList<Character> ext = new ArrayList();

		// Read the extension
		while(input != '\n')
		{
			ext.add((char)input);
			input = in.readBits(8);
		}

		char nExt[] = new char[ext.size()];
		for (int i = 0; i < nExt.length; i++)
			nExt[i] = ext.get(i);
		String extension = new String(nExt);
		outDeCompressFilePath += extension;

		BitOutputStream out = new BitOutputStream(outDeCompressFilePath);

		// Read the head and reconstruct the tree
		Node root = readHeader(in);

		// parse body of compressed file
		Node current = root;
		while(true)
		{
			int bit = in.readBits(1);

			if(bit == -1)
				break;

			if(bit == 1)
				current = current.getRight();
			else
				current = current.getLeft();

			if(current.isLeaf())
			{
				out.writeBits(BITS_PER_WORD, current.getCh());
				current = root;
			}
		}

		// flush the buffer
		out.flush();

		// close streams
		in.close();
		out.close();

		// Generating an alert
		if(!isFromArgs)
		{
			stat.setAlignment(Pos.BASELINE_LEFT);
			stat.setText("File decompressed to: " + outDeCompressFilePath);

			Platform.runLater(() ->
			{
				Alert alert = new Alert(Alert.AlertType.INFORMATION);

				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				stage.getIcons().add(new Image("Resources/Images/compress.jpg"));

				alert.setTitle("Complete");
				alert.setHeaderText("Success!");
				alert.setContentText("Decompression is done!" + "\n\nOutput file name: " + outDeCompressFilePath.substring(outDeCompressFilePath.lastIndexOf('\\') + 1));
				alert.show();
			});
		}
	}

	// To write the header to the file
	private int writeHeader(Node current, BitOutputStream out, StringBuilder head)
	{
		if(current.isLeaf())
		{
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD, current.getCh());

			head.append("1" + (char)current.getCh());

			return 1 + BITS_PER_WORD;
		}

		out.writeBits(1, 0);

		head.append("0");

		return writeHeader(current.getLeft(), out, head) + writeHeader(current.getRight(), out, head) + 1;
	}

	// To read the header from the file and reconstruct the huffman tree
	private Node readHeader(BitInputStream in)
	{
		if(in.readBits(1) == 0)
		{
			Node left = readHeader(in);
			Node right = readHeader(in);

			return new Node(-1, 0, left, right);
		}
		else
			return new Node(in.readBits(BITS_PER_WORD), 0);
	}

	// To reset data fields
	private void reset()
	{
		if(!isFromArgs)
		{
			inCompressFilePath = inCompressFileName = outCompressFileName = outCompressFilePath = outDeCompressFilePath = inDeCompressFilePath = null;

			stat.clear();
			stat.setAlignment(Pos.CENTER);
			charsCodes.clear();
			header.clear();
			inFile.clear();
			outFile.clear();
			compRatio.clear();
		}
	}

	// closes the application
	@FXML
	void close(ActionEvent event)
	{
		charsCodes.getScene().getWindow().hide();
	}
}
