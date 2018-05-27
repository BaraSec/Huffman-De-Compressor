import Controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.File;
import java.text.NumberFormat;

public class Main extends Application
{
	@Override
	public void start(Stage stage) throws Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource("Resources/UI/MainUI.fxml"));

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Huffman (De)Compressor");
		stage.getIcons().add((new Image(getClass().getResource("Resources/Images/compress.jpg").toString())));
		stage.show();
	}

	public static void main(String[] args)
	{
		// If the program is run from the command line
		if(args.length > 0)
		{
			MainController cont = new MainController();
			cont.setFromArgs(true);

			File inFile = new File(args[0]);

			// If the file needs to be compressed
			if(!inFile.getName().substring(inFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("huf"))
			{
				String inCompressFilePath = inFile.getAbsolutePath();
				String inCompressFileName = inFile.getName();
				String outCompressFilePath;

				if(inCompressFilePath.lastIndexOf('.') != -1)
					outCompressFilePath = inCompressFilePath.substring(0, inCompressFilePath.lastIndexOf('.')) + ".huf";
				else
					outCompressFilePath = inCompressFilePath + ".huf";

				String outCompressFileName = outCompressFilePath.substring(outCompressFilePath.lastIndexOf('\\') + 1);

				cont.setInCompressFilePath(inCompressFilePath);
				cont.setInCompressFileName(inCompressFileName);
				cont.setOutCompressFilePath(outCompressFilePath);
				cont.setOutCompressFileName(outCompressFileName);

				// Compressing the file
				cont.compress(new ActionEvent());

				// Generating an alert
				Platform.runLater(() ->
				{
					Alert alert = new Alert(Alert.AlertType.INFORMATION);

					Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
					stage.getIcons().add(new Image("Resources/Images/compress.jpg"));

					NumberFormat nf = NumberFormat.getInstance();
					nf.setMaximumFractionDigits(4);

					alert.setTitle("Complete");
					alert.setHeaderText("Success!");
					alert.setContentText("Compression is done!" + "\n\nOutput file name: " + outCompressFileName + "\nCompression Ratio = " + nf.format(cont.getRatio()) + "%");
					alert.show();
				});

			}
			// If the file needs to be decompressed
			else
			{
				String inDeCompressFilePath = inFile.getAbsolutePath();
				String outDeCompressFilePath = inDeCompressFilePath.substring(0, inDeCompressFilePath.lastIndexOf('.') + 1);

				cont.setInDeCompressFilePath(inDeCompressFilePath);
				cont.setOutDeCompressFilePath(outDeCompressFilePath);

				// Decompressing the file
				cont.deCompress(new ActionEvent());

				// Generating an alert
				Platform.runLater(() ->
				{
					Alert alert = new Alert(Alert.AlertType.INFORMATION);

					Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
					stage.getIcons().add(new Image("Resources/Images/compress.jpg"));

					alert.setTitle("Complete");
					alert.setHeaderText("Success!");
					alert.setContentText("Decompression is done!" + "\n\nOutput file name: " + cont.getOutDeCompressFilePath().substring(cont.getOutDeCompressFilePath().lastIndexOf('\\') + 1));
					alert.show();
				});
			}
		}
		else
			launch(args);
	}
}
