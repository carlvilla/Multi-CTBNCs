package es.upm.fi.cig.mctbnc.util;

import java.util.function.UnaryOperator;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

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
		// Define filter to allow only positive integers
		UnaryOperator<Change> filter = change -> {
			if (change.getControlNewText().matches("([1-9][0-9]*)?"))
				return change;
			return null;
		};
		textField.setTextFormatter(new TextFormatter<>(filter));
	}

	/**
	 * Checks that the text field only contains a positive integer that is greater
	 * than or equal to a given number.
	 * 
	 * @param textField text field
	 * @param minValue  minimum value of the integers
	 */
	public static void onlyPositiveIntegerGTE(TextField textField, int minValue) {
		// Define filter to allow only positive integers >= to a given value
		UnaryOperator<Change> filter = change -> {
			String currentText = change.getControlNewText();
			if (currentText.isEmpty()
					|| currentText.matches("([1-9][0-9]*)?") && Integer.valueOf(currentText) >= minValue)
				return change;
			return null;
		};
		textField.setTextFormatter(new TextFormatter<>(filter));
	}

	/**
	 * Checks that the text field only contains positive decimals.
	 * 
	 * @param textField text field
	 */
	public static void onlyPositiveDouble(TextField textField) {
		// Define filter to allow only positive doubles
		UnaryOperator<Change> filter = change -> {
			String changeText = change.getText();
			String currentText = change.getControlText();
			if (currentText.contains(".") && changeText.matches("[^0-9]") || changeText.matches("[^0-9.]"))
				return null;
			return change;
		};
		textField.setTextFormatter(new TextFormatter<>(filter));
	}

	/**
	 * Receives an {@code String} and tries to convert it to a {@code double}. If it
	 * is not possible, the provided default value is returned.
	 * 
	 * @param text         {@code String} from which the {@code double} is extracted
	 * @param defaultValue default value
	 * @return extracted {@code double}
	 */
	public static double extractDecimal(String text, double defaultValue) {
		try {
			return Double.valueOf(text);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Receives an {@code String} and tries to convert it to an {@code Integer}. If
	 * it is not possible, the provided default value is returned.
	 * 
	 * @param text         {@code String} from which the {@code Integer} is
	 *                     extracted
	 * @param defaultValue default value
	 * @return extracted {@code Integer}
	 */
	public static int extractInteger(String text, int defaultValue) {
		try {
			return Integer.valueOf(text);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
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
