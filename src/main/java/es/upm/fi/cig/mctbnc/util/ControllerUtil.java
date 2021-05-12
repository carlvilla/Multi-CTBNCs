package es.upm.fi.cig.mctbnc.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;

/**
 * Utility class with methods related to controlling the UI behaviour.
 * 
 * @author Carlos Villa Blanco
 *
 */
public final class ControllerUtil {

	private ControllerUtil() {
	}

	/**
	 * Checks that the text field only contains positive integers.
	 * 
	 * @param textField text field
	 */
	public static void onlyPositiveInteger(TextField textField) {
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*"))
					textField.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
	}

	/**
	 * Checks that the text field only contains positive integers greater than a
	 * given number.
	 * 
	 * @param textField text field
	 * @param minValue  minimum value of the integers
	 */
	public static void onlyPositiveIntegerGreaterThan(TextField textField, int minValue) {
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d+"))
					textField.setText(newValue.replaceAll("[^\\d+]", ""));
				if (newValue.matches("\\d+") && Integer.valueOf(newValue) < minValue)
					textField.setText(newValue.replaceAll("[^\\d+]", ""));
			}
		});
	}

	/**
	 * Changes the visibility of a node.
	 * 
	 * @param node node whose status is changed
	 * @param show true to show the node, otherwise false
	 */
	public static void showNode(Node node, boolean show) {
		if (node != null) {
			node.setVisible(show);
			node.setManaged(show);
		}
	}

}
