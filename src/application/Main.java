package application;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	File file;
	Thread watchDirectoryThread;
	String selected="Watch Directory";
	boolean isMonitoringStarted;
	Stage primaryStage;
	boolean isFile;
	@Override
	public void start(Stage primaryStage) 
	{
		try
		{
			this.primaryStage = primaryStage;
			primaryStage.setTitle("File System Change Notifier");
			ToggleGroup group = new ToggleGroup();
			RadioButton radioButton1 = new RadioButton("Watch Directory");
			radioButton1.setUserData("1");
			radioButton1.setToggleGroup(group);
			radioButton1.setSelected(true);
			RadioButton radioButton2 = new RadioButton("Watch File");
			radioButton2.setToggleGroup(group);
			radioButton1.setUserData("2");
			group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			    public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) 
			    {
			    	RadioButton chk = (RadioButton)new_toggle.getToggleGroup().getSelectedToggle(); 
			    	selected = chk.getText();
			     } 
			});
			Button btn = new Button("BROWSE");
			Button btn1= new Button("START MONITORING");
			Button btn2= new Button("STOP MONITORING");
			btn.getStyleClass().add("btn-css");
			btn1.getStyleClass().add("btn1-css");
			btn2.getStyleClass().add("btn2-css");
			TextArea textArea = new TextArea();
			textArea.getStyleClass().add("hbox-css");
			textArea.setPrefWidth(50);
			textArea.setWrapText(true);
			textArea.setText("");
			textArea.setEditable(false);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("File System Change Notifier");
			alert.setHeaderText(null);
			
			btn1.setOnAction(new EventHandler<ActionEvent>() 
			{
				public void handle(ActionEvent event) 
				{
					if(file!=null){
					textArea.appendText("STARTED MONITORING  :"+file.getAbsolutePath()+"\n");
					try {
						watchDirectoryThread = new Thread(new WatchDirectoryService(file.getAbsolutePath(),textArea,isFile));
					} catch (IOException e) {
						
						e.printStackTrace();
					}
					watchDirectoryThread.start();
					isMonitoringStarted=true;
					}
					else{
						alert.setContentText("BROWSE THE FILE OR FOLDER");
						alert.showAndWait();
					}
			       
				}
		});
			btn2.setOnAction(new EventHandler<ActionEvent>() 
			{
				public void handle(ActionEvent event) 
				{
					if(isMonitoringStarted){
					textArea.clear();
					textArea.appendText("STOPPED MONITORING :"+file.getAbsolutePath()+"\n");
					watchDirectoryThread.stop();
					}
					else{
						alert.setContentText("CLICK ON START MONITORING");
						alert.showAndWait();
					}
				
				}
			});
			
			
			
			
			final TextField textfield = new TextField();
			textfield.setPrefWidth(300);
			textfield.setDisable(true);
			btn.setOnAction(new EventHandler<ActionEvent>() 
			{
				public void handle(ActionEvent event) 
				{
					if(selected.equals("Watch Directory"))
					{
						isFile= false;
				     	DirectoryChooser chooser = new DirectoryChooser();
				    	chooser.setTitle("Open Resource File");
				    	File defaultDirectory = new File("D:");
				    	chooser.setInitialDirectory(defaultDirectory);
				    	file = chooser.showDialog(primaryStage);
					}
					if(selected.equals("Watch File"))
					{
						isFile= true;
				     	FileChooser fileChooser = new FileChooser();
				    	fileChooser.setTitle("Open Resource File");
				     	//fileChooser.showOpenDialog(primaryStage);
					    fileChooser.getExtensionFilters().addAll(
							new FileChooser.ExtensionFilter("All Images", "*.*"),
							new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif","*.bmp","*.jpeg"),
							new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
							new FileChooser.ExtensionFilter("Text Files", "*.txt"),
							new FileChooser.ExtensionFilter("Audio Files and video", "*.wav", "*.mp3", "*.aac",".avi"),
							new FileChooser.ExtensionFilter("Web pages", "*.tpl", "*.html", "*.htm"),
							new FileChooser.ExtensionFilter("Document Files", "*.doc","* .docx"),
							new FileChooser.ExtensionFilter("Zipped Files", "*.zip"),
							new FileChooser.ExtensionFilter("Graphics Interchange files", "*.gif"),
							new FileChooser.ExtensionFilter("Powerpoint files", "*.ppt","*.pptx")
							);
					file = fileChooser.showOpenDialog(primaryStage);
					}
					textfield.setText(file.getAbsolutePath());
				}
			});
			primaryStage.setOnCloseRequest(confirmCloseEventHandler);
			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setContent(textArea);
			scrollPane.setFitToWidth(true);
			scrollPane.setPrefWidth(400);
			scrollPane.setPrefHeight(180);
			HBox hbox1 = new HBox();
			hbox1.getChildren().addAll(textfield,btn);
		    hbox1.setSpacing(15);
		    HBox hbox2 = new HBox();
			hbox2.getChildren().addAll(btn1,btn2);
		    hbox2.setSpacing(20);
			VBox hbox = new VBox();
			hbox.getChildren().addAll(new Label("MODE"),radioButton1,radioButton2,hbox1,hbox2,new Label("CHANGE NOTIFICATION"),textArea);
			hbox.setSpacing(15);
			hbox.setPadding(new Insets(10, 20, 20, 20));
			Scene scene = new Scene(hbox, 450, 450);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
          
	}

	    private EventHandler<WindowEvent> confirmCloseEventHandler = event -> {
	        Alert closeConfirmation = new Alert(
	                Alert.AlertType.CONFIRMATION,
	                "Are you sure you want to exit?"
	        );
	        Button exitButton = (Button) closeConfirmation.getDialogPane().lookupButton(
	                ButtonType.OK
	        );
	        exitButton.setText("Exit");
	        closeConfirmation.setHeaderText("Confirm Exit");
	        closeConfirmation.initModality(Modality.APPLICATION_MODAL);
	        closeConfirmation.initOwner(primaryStage);
	        closeConfirmation.setX(primaryStage.getX()+primaryStage.getWidth()/8);
	        closeConfirmation.setY(primaryStage.getY() + primaryStage.getHeight()/4);

	        Optional<ButtonType> closeResponse = closeConfirmation.showAndWait();
	        if (!ButtonType.OK.equals(closeResponse.get())) {
	            event.consume();
	        }
	    };

	public static void main(String[] args)
	{
		launch(args);
	}
	public void startWatch(TextArea textArea){}

}


