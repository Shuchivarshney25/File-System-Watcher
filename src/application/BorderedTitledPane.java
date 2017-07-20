package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class BorderedTitledPane extends StackPane 
{

	public BorderedTitledPane(String titleString, Node content) 
	{
		Label title = new Label(" " + titleString + " ");
		title.getStyleClass().add("bordered-titled-title");
		StackPane.setMargin(title, new Insets(0, 700, 0, 0));
		StackPane.setAlignment(title, Pos.TOP_RIGHT);
		StackPane contentPane = new StackPane();
		contentPane.getChildren().add(content);
		getStyleClass().add("bordered-titled-border");
		getChildren().addAll(contentPane, title);
	}
	
}
