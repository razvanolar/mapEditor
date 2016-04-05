package mapEditor.application.main_part.manage_images.manage_tile_sets.create_brush;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import mapEditor.application.main_part.app_utils.AppParameters;
import mapEditor.application.main_part.app_utils.constants.CssConstants;
import mapEditor.application.main_part.app_utils.inputs.StringValidator;
import mapEditor.application.main_part.app_utils.models.brush.LWBrushModel;
import mapEditor.application.main_part.app_utils.views.canvas.BrushCanvas;
import mapEditor.application.main_part.app_utils.views.dialogs.Dialog;
import mapEditor.application.main_part.app_utils.views.dialogs.OkCancelDialog;
import mapEditor.application.main_part.app_utils.views.others.SystemFilesView;
import mapEditor.application.main_part.manage_images.manage_tile_sets.utils.CreateBrushListener;
import mapEditor.application.main_part.manage_images.manage_tile_sets.utils.create_views.SelectableCreateBrushView;
import mapEditor.application.main_part.types.Controller;
import mapEditor.application.main_part.types.View;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Created by razvanolar on 03.04.2016.
 */
public class CreateBrushController implements Controller, CreateBrushListener {

  private BrushCanvas brushCanvas;

  public interface ICreateBrushView extends View {
    Button getAddBrushButton();
    TextField getPathTextField();
    Button getPathButton();
    void addBrushView(Region region);
    void removeBrushView(Region region);
    void addCanvasContainer(Region region);
  }

  private ICreateBrushView view;
  private Image image;
  private Button completeSelectionNode;
  private Window parent;

  private List<SelectableCreateBrushView> brushViews;

  public CreateBrushController(ICreateBrushView view, Image image, Button completeSelectionNode, Window parent) {
    this.view = view;
    this.image = image;
    this.completeSelectionNode = completeSelectionNode;
    this.parent = parent;
  }

  @Override
  public void bind() {
    brushViews = new ArrayList<>();
    brushCanvas = new BrushCanvas(image);
    ScrollPane scrollPane = new ScrollPane(brushCanvas);

    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.getStyleClass().add(CssConstants.CANVAS_CONTAINER_LIGHT_BG);

    ChangeListener<Number> listener = (observable, oldValue, newValue) -> brushCanvas.paint();
    brushCanvas.widthProperty().bind(scrollPane.widthProperty());
    brushCanvas.heightProperty().bind(scrollPane.heightProperty());
    scrollPane.widthProperty().addListener(listener);
    scrollPane.heightProperty().addListener(listener);

    view.addCanvasContainer(scrollPane);
    view.getPathTextField().setText(AppParameters.CURRENT_PROJECT.getBrushesFile().getAbsolutePath());

    completeSelectionNode.setDisable(true);
    addListeners();
  }

  private void addListeners() {
    view.getAddBrushButton().setOnAction(event -> {
      if (!brushCanvas.isValidSelection()) {
        Dialog.showWarningDialog(null, "You didn't select a brush content!", parent);
        return;
      }

      LWBrushModel brushModel = getCroppedImagesModel();
      SelectableCreateBrushView selectableCreateBrushView = new SelectableCreateBrushView(brushModel, this);
      view.addBrushView(selectableCreateBrushView.asNode());
      brushViews.add(selectableCreateBrushView);
      completeSelectionNode.setDisable(true);
    });

    view.getPathTextField().textProperty().addListener((observable, oldValue, newValue) -> {
      completeSelectionNode.setDisable(!isValidSelection());
    });

    view.getPathButton().setOnAction(event -> {
      OkCancelDialog dialog = new OkCancelDialog("Select Brushes Path", StageStyle.UTILITY, Modality.APPLICATION_MODAL, true, true, parent);

      SystemFilesView filesView = new SystemFilesView(dialog.getOkButton(), AppParameters.CURRENT_PROJECT.getBrushesFile(), true, null);
      dialog.getOkButton().setOnAction(event1 -> {
        String selectedPath = filesView.getSelectedPath();
        if (StringValidator.isValidBrushesPath(selectedPath)) {
          view.getPathTextField().setText(selectedPath);
          completeSelectionNode.setDisable(!isValidSelection());
        }
        dialog.close();
      });

      dialog.setContent(filesView.asNode());
      dialog.show();
    });
  }

  private LWBrushModel getCroppedImagesModel() {
    int primaryX = brushCanvas.getSquareCellX();
    int primaryY = brushCanvas.getSquareCellY();
    int secondaryX = brushCanvas.getCompleteSelectionCellX();
    int secondaryY = brushCanvas.getCompleteSelectionCellY();

    Image[][] primaryImages = new Image[3][3];
    Image[][] secondaryImages = new Image[2][2];

    primaryImages[0][0] = brushCanvas.cropCell(primaryX-1, primaryY-1);
    primaryImages[0][1] = brushCanvas.cropCell(primaryX, primaryY-1);
    primaryImages[0][2] = brushCanvas.cropCell(primaryX+1, primaryY-1);
    primaryImages[1][0] = brushCanvas.cropCell(primaryX-1, primaryY);
    primaryImages[1][1] = brushCanvas.cropCell(primaryX, primaryY);
    primaryImages[1][2] = brushCanvas.cropCell(primaryX+1, primaryY);
    primaryImages[2][0] = brushCanvas.cropCell(primaryX-1, primaryY+1);
    primaryImages[2][1] = brushCanvas.cropCell(primaryX, primaryY+1);
    primaryImages[2][2] = brushCanvas.cropCell(primaryX+1, primaryY+1);

    secondaryImages[0][0] = brushCanvas.cropCell(secondaryX, secondaryY);
    secondaryImages[0][1] = brushCanvas.cropCell(secondaryX+1, secondaryY);
    secondaryImages[1][0] = brushCanvas.cropCell(secondaryX, secondaryY+1);
    secondaryImages[1][1] = brushCanvas.cropCell(secondaryX+1, secondaryY+1);

    Image previewImage = brushCanvas.cropBrushPreviewImage();

    return new LWBrushModel(primaryImages, secondaryImages, previewImage, 1, 1);
  }

  private boolean brushNamesAreValid() {
    for (SelectableCreateBrushView view : brushViews) {
      LWBrushModel brushModel = view.getBrushModel();
      if (!(brushModel != null && (StringValidator.isValidFileName(brushModel.getName()))))
        return false;
    }
    return true;
  }

  public boolean isValidSelection() {
    return StringValidator.isValidBrushesPath(view.getPathTextField().getText()) && brushNamesAreValid();
  }

  public String getSelectedPath() {
    return view.getPathTextField().getText();
  }

  public List<LWBrushModel> getBrushModels() {
    return brushViews.stream().map(SelectableCreateBrushView::getBrushModel).collect(Collectors.toList());
  }

  @Override
  public void removeBrushField(SelectableCreateBrushView selectableCreateBrushView) {
    brushViews.remove(selectableCreateBrushView);
    view.removeBrushView(selectableCreateBrushView.asNode());
  }

  @Override
  public void brushNameChanged(SelectableCreateBrushView selectableCreateBrushView) {
    completeSelectionNode.setDisable(!isValidSelection());
  }
}