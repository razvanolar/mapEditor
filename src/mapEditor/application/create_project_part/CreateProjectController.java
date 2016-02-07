package mapEditor.application.create_project_part;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mapEditor.MapEditorController;
import mapEditor.application.main_part.app_utils.AppParameters;
import mapEditor.application.main_part.app_utils.inputs.StringValidator;
import mapEditor.application.main_part.app_utils.views.dialogs.OkCancelDialog;
import mapEditor.application.main_part.app_utils.views.others.SystemFilesView;
import mapEditor.application.main_part.types.Controller;
import mapEditor.application.main_part.types.View;
import mapEditor.application.repo.RepoController;
import mapEditor.application.repo.models.ProjectModel;
import mapEditor.application.repo.types.CreateProjectStatus;

/**
 *
 * Created by razvanolar on 02.02.2016.
 */
public class CreateProjectController implements Controller {

  public enum ICreateProjectViewState {
    CREATE, NONE
  }

  public interface ICreateProjectView extends View {
    void setState(ICreateProjectViewState state);
    TextField getProjectNameTextField();
    TextField getProjectPathTextField();
    Button getPathButton();
    Button getCancelProjectButton();
    Button getCreateProjectButton();
  }

  private ICreateProjectView view;
  private Stage stage;

  public CreateProjectController(ICreateProjectView view) {
    this.view = view;
  }

  @Override
  public void bind() {
    addListeners();
    view.getProjectNameTextField().setText(AppParameters.DEFAULT_PROJECT_NAME);
    view.getProjectPathTextField().setText(AppParameters.SYSTEM_FILES_VIEW_PATH);
  }

  private void addListeners() {
    view.getPathButton().setOnAction(event -> {
      OkCancelDialog dialog = new OkCancelDialog("Choose Project Path", null, null, true);
      SystemFilesView systemFilesView = new SystemFilesView(dialog.getOkButton());
      dialog.setContent(systemFilesView.asNode());
      dialog.getOkButton().setOnAction(event1 -> {
        if (!StringValidator.isNullOrEmpty(systemFilesView.getSelectedPath()) && systemFilesView.isFolderSelected()) {
          view.getProjectPathTextField().setText(systemFilesView.getSelectedPath());
          dialog.close();
        }
      });
      dialog.show();
    });

    ChangeListener<String> textFieldsListener = (observable, oldValue, newValue) -> view.getCreateProjectButton().setDisable(!checkTextFieldsValidity());
    view.getProjectNameTextField().textProperty().addListener(textFieldsListener);
    view.getProjectPathTextField().textProperty().addListener(textFieldsListener);

    view.getCreateProjectButton().setOnAction(event -> doOnCreateProjectSelection());
  }

  private void doOnCreateProjectSelection() {
    String name = view.getProjectNameTextField().getText() + ".med";
    String path = view.getProjectPathTextField().getText();

    CreateProjectStatus status = RepoController.getInstance().checkIfProjectFieldsAreValid(name, path);

    if (status == CreateProjectStatus.NOT_DIRECTORY) {
      showWarningDialog(null, "You can't use the selected path because it's not a directory.");
      return;
    }

    if (status == CreateProjectStatus.ANOTHER_CREATED) {
      showWarningDialog(null, "There is another .med project created. Please choose another file");
      return;
    }

    if (status == CreateProjectStatus.NAME_EXISTS) {
      showWarningDialog(null, "There is another file with the same name. Please rename the project.");
      return;
    }

    if (status == CreateProjectStatus.PATH_MISSING) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Info");
      alert.setContentText("Specified path does not exist. Press OK if you want to create it.");
      alert.showAndWait();
      if (alert.getResult() == ButtonType.OK && RepoController.getInstance().createPathIfNotExist(path))
        createProjectFiles(name, path);
      return;
    }

    createProjectFiles(name, path);
  }

  private void createProjectFiles(String name, String path) {
    ProjectModel project = RepoController.getInstance().createProject(name, path);

    if (project == null) {
      showWarningDialog(null, "Failed to create project files.");
      return;
    }

    MapEditorController.getInstance().loadProject(project, true);
  }

  private boolean checkTextFieldsValidity() {
    return !StringValidator.isNullOrEmpty(view.getProjectNameTextField().getText()) &&
            !StringValidator.isNullOrEmpty(view.getProjectPathTextField().getText()) &&
            StringValidator.isValidFileName(view.getProjectNameTextField().getText());
  }

  private void showWarningDialog(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title == null ? "Warning" : title);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
