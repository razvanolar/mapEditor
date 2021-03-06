package mapEditor.application.main_part.manage_images.manage_tile_sets.cropped_tiles.detailed_view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import mapEditor.application.main_part.app_utils.inputs.FileExtensionUtil;
import mapEditor.application.main_part.app_utils.inputs.StringValidator;
import mapEditor.application.main_part.app_utils.models.ImageModel;
import mapEditor.application.main_part.app_utils.models.KnownFileExtensions;
import mapEditor.application.main_part.app_utils.views.dialogs.OkCancelDialog;
import mapEditor.application.main_part.app_utils.views.others.SystemFilesView;
import mapEditor.application.main_part.manage_images.manage_tile_sets.utils.listeners.ManageImagesListener;
import mapEditor.application.main_part.types.Controller;
import mapEditor.application.main_part.types.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by razvanolar on 14.02.2016.
 */
public class CroppedTilesDetailedController implements Controller {

  public interface ICroppedTileDetailedView extends View {
    TextField getNameTextField();
    TextField getPathTextField();
    Button getPathButton();
    Button getSaveButton();
    Button getDropButton();
    ImageModel getImage();
  }

  private List<ICroppedTileDetailedView> views;
  private ManageImagesListener listener;
  private KnownFileExtensions extension;
  private File rootFile;

  private static EventHandler<ActionEvent> fileSystemHandler;

  public CroppedTilesDetailedController(File rootFile, ManageImagesListener listener) {
    this.rootFile = rootFile;
    this.listener = listener;
  }

  @Override
  public void bind() {
    views = new ArrayList<>();
    extension = KnownFileExtensions.PNG;
  }

  private void addListeners(ICroppedTileDetailedView view) {
    view.getNameTextField().textProperty().addListener((observable1, oldValue1, newValue) -> {
      view.getSaveButton().setDisable(!isValidSelection(newValue, view.getPathTextField().getText()));
    });

    view.getPathTextField().textProperty().addListener((observable, oldValue, newValue) -> {
      view.getSaveButton().setDisable(!isValidSelection(view.getNameTextField().getText(), newValue));
      if (view.getPathTextField().getTooltip() != null)
        view.getPathTextField().getTooltip().setText(newValue);
    });

    view.getPathButton().setOnAction(getFileSystemHandler());

    view.getSaveButton().setOnAction(event1 -> listener.saveCroppedImage(view));

    view.getDropButton().setOnAction(event -> {
      views.remove(view);
      listener.dropCroppedTileView(view);
    });
  }

  public void addView(ICroppedTileDetailedView view) {
    views.add(view);
    addListeners(view);

    ImageModel imageModel = view.getImage();
    String name = imageModel.getName();
    String path = imageModel.getPath();
    name = StringValidator.isNullOrEmpty(name) ? "*" + extension.getExtension() : name;
    path =StringValidator.isNullOrEmpty(path) ? rootFile.getAbsolutePath() : path;

    view.getNameTextField().setText(name);
    view.getPathTextField().setText(path);
    view.getPathTextField().setTooltip(new Tooltip(path));

  }

  private EventHandler<ActionEvent> getFileSystemHandler() {
    if (fileSystemHandler == null)
      fileSystemHandler = event -> {
        OkCancelDialog dialog = new OkCancelDialog("Choose Tile Path", StageStyle.UTILITY, Modality.APPLICATION_MODAL, true);
        SystemFilesView filesView = new SystemFilesView(dialog.getOkButton(), rootFile, true, null);
        dialog.setContent(filesView.asNode());
        dialog.show();
      };
    return fileSystemHandler;
  }

  private boolean isValidSelection(String name, String path) {
    return isValidName(name) && isValidPath(path);
  }

  private boolean isValidName(String name) {
    return name != null && (name.matches("^[a-zA-Z0-9[-_]]+") || (name.matches("[a-zA-Z0-9[-_]]+\\..*") && FileExtensionUtil.isPngFile(name)));
  }

  private boolean isValidPath(String path) {
    return path != null && path.contains(rootFile.getAbsolutePath());
  }

  public List<ImageModel> getImages() {
    List<ImageModel> result = new ArrayList<>();
    for (ICroppedTileDetailedView view : views) {
      ImageModel image = view.getImage();
      String name = view.getNameTextField().getText();
      String path = view.getPathTextField().getText();
      image.setName(isValidName(name) ? name : "");
      image.setPath(isValidPath(path) ? path : "");
      result.add(image);
    }
    return result;
  }

  public void deleteAllImages() {
    views.clear();
  }
}
