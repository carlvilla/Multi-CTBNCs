package com.cig.mctbnc.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public final class ControllerUtil {

	private ControllerUtil() {
	}

	public static void onlyPositiveInteger(TextField textField) {
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					textField.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

	public static void onlyPositiveIntegerGreaterThan(TextField textField, int minValue) {
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					textField.setText(newValue.replaceAll("[^\\d]", ""));
				}

				if (newValue.matches("\\d*") && Integer.valueOf(newValue) < minValue) {
					textField.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

}
