package mapEditor.application.main_part.app_utils.views.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import mapEditor.application.main_part.app_utils.inputs.StringValidator;

/**
 *
 * Created by razvanolar on 13.02.2016.
 */
public class Dialog {

  public enum DialogButtonType {
    YES, NO
  }

  public static void showWarningDialog(String title, String message) {
    showWarningDialog(title, message, null);
  }

  public static void showWarningDialog(String title, String message, Window owner) {
    title = StringValidator.isNullOrEmpty(title) ? "Warning" : title;
    Alert alert = createAlertDialog(Alert.AlertType.WARNING, title, message, owner);
    alert.showAndWait();
  }

  public static boolean showConfirmDialog(String title, String message) {
    return showConfirmDialog(title, message, null);
  }

  public static boolean showConfirmDialog(String title, String message, Window owner) {
    title = StringValidator.isNullOrEmpty(title) ? "Confirm" : title;
    Alert alert = createAlertDialog(Alert.AlertType.CONFIRMATION, title, message, owner);
    alert.showAndWait();
    return alert.getResult() == ButtonType.OK;
  }

  public static void showInformDialog(String title, String message) {
    showInformDialog(title, message, null);
  }

  public static void showInformDialog(String title, String message, Window owner) {
    title = StringValidator.isNullOrEmpty(title) ? "Info" : title;
    Alert alert = createAlertDialog(Alert.AlertType.INFORMATION, title, message, owner);
    alert.showAndWait();
  }

  public static void showErrorDialog(String title, String message) {
    showErrorDialog(title, message, null);
  }

  public static void showErrorDialog(String title, String message, Window owner) {
    title = StringValidator.isNullOrEmpty(title) ? "Error" : title;
    Alert alert = createAlertDialog(Alert.AlertType.ERROR, title, message, owner);
    alert.showAndWait();
  }

  public static boolean showYesNoDialog(String title, String message) {
    title = StringValidator.isNullOrEmpty(title) ? "Info" : title;
    return YesNoDialog.getInstance().showAndWait(title, message);
  }

  private static Alert createAlertDialog(Alert.AlertType type, String title, String message, Window owner) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setContentText(message);
    if (owner != null)
      alert.initOwner(owner);
    return alert;
  }
}
