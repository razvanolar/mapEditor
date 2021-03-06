package mapEditor;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mapEditor.application.create_project_part.CreateProjectController;
import mapEditor.application.main_part.app_utils.AppParameters;
import mapEditor.application.main_part.app_utils.models.MapDetail;
import mapEditor.application.main_part.app_utils.views.dialogs.Dialog;
import mapEditor.application.main_part.main_app_toolbars.project_tree_toolbar.ProjectVerticalToolbarController;
import mapEditor.application.main_part.main_app_toolbars.project_tree_toolbar.ProjectVerticalToolbarView;
import mapEditor.application.main_part.manage_images.manage_tile_sets.ManageTileSetsController;
import mapEditor.application.main_part.manage_images.manage_tile_sets.ManageTileSetsView;
import mapEditor.application.main_part.manage_maps.ManageMapsController;
import mapEditor.application.main_part.manage_maps.ManageMapsView;
import mapEditor.application.main_part.main_app_toolbars.main_toolbar.MapEditorToolbarController;
import mapEditor.application.main_part.main_app_toolbars.main_toolbar.MapEditorToolbarView;
import mapEditor.application.main_part.manage_maps.MapCanvas;
import mapEditor.application.main_part.manage_maps.manage_characters.ManageCharactersController;
import mapEditor.application.main_part.manage_maps.manage_tiles.ManageTilesController;
import mapEditor.application.main_part.menu_bar.MapEditorMenuBarController;
import mapEditor.application.main_part.menu_bar.MapEditorMenuBarView;
import mapEditor.application.main_part.project_tree.ProjectTreeController;
import mapEditor.application.main_part.project_tree.ProjectTreeView;
import mapEditor.application.main_part.status_bar.StatusBarController;
import mapEditor.application.main_part.status_bar.StatusBarView;
import mapEditor.application.repo.RepoController;
import mapEditor.application.repo.models.ProjectModel;

import java.io.File;

/**
 *
 * Created by razvanolar on 21.01.2016.
 */
public class MapEditorController {

  private static MapEditorController INSTANCE;

  private MapEditorView mapEditorView;
  private Scene scene;
  private BorderPane mainContainer;
  private SplitPane centerSplitPane;

  private RepoController repoController;
  private MapEditorToolbarController toolbarController;
  private ManageMapsController manageMapsController;
  private ManageTileSetsController manageTileSetsController;
  private ProjectTreeController projectTreeController;
  private ProjectVerticalToolbarController projectVerticalToolbarController;

  private double lastProjectTreeDividerPosition = -1;

  public static MapEditorController getInstance() {
    if (INSTANCE == null)
      INSTANCE = new MapEditorController();
    return INSTANCE;
  }

  public void initPrimaryView() {
    ProjectModel currentProject = AppParameters.CURRENT_PROJECT;
    lastProjectTreeDividerPosition = currentProject.getProjectTreeDividerPosition();
    /* init menu bar */
    MapEditorMenuBarController.IMapEditorMenuBarView menuBarView = new MapEditorMenuBarView();
    MapEditorMenuBarController menuBarController = new MapEditorMenuBarController(menuBarView);
    menuBarController.bind();

    /* init main toolbar */
    MapEditorToolbarController.IMapEditorToolbarView toolbarView = new MapEditorToolbarView();
    toolbarController = new MapEditorToolbarController(toolbarView,
            currentProject.is2DVisibilitySelected(),
            currentProject.isGridVisibilitySelected(),
            currentProject.isDeleteEntity(),
            currentProject.isFillArea(),
            currentProject.isShowGrid());
    toolbarController.bind();
    MapCanvas.FILL_AREA = currentProject.isFillArea();
    MapCanvas.DELETE_ENTITY = currentProject.isDeleteEntity();

    /* init status bar */
    StatusBarController.IStatusBarView statusBarView = new StatusBarView();
    StatusBarController statusBarController = new StatusBarController(statusBarView);
    statusBarController.bind();

    addMapEditorToolBarsAndStatusBars(menuBarView.asNode(), toolbarView.asNode(), statusBarView.asNode());

    /* init left side toolbar : project toolbar */
    ProjectVerticalToolbarController.IProjectVerticalToolbarView projectVerticalToolbarView = new ProjectVerticalToolbarView();
    projectVerticalToolbarController = new ProjectVerticalToolbarController(projectVerticalToolbarView, currentProject.isShowProjectTree());
    projectVerticalToolbarController.bind();
    mainContainer.setLeft(projectVerticalToolbarView.asNode());

    // use it when construct the project tree view; now it's only for testing
    setProjectTreeView(currentProject.isShowProjectTree());

    changeMainView();
    if (currentProject.isTilesTabsSelected())
      changeToTilesView();
    else
      changeToCharactersView();

    addListeners();
  }

  private void addListeners() {
    if (scene != null) {
      scene.setOnKeyPressed(event -> {
        boolean isCtrlDown = event.isControlDown();
        if (isCtrlDown) {
          if (event.getCode() == KeyCode.N)
            toolbarController.showCreateMapDialog();
          if (event.getCode() == KeyCode.S)
            saveSelectedMap();
        }
      });
    }
  }

  public void initCreateProjectView(CreateProjectController.ICreateProjectView createProjectView) {
    CreateProjectController createProjectController = new CreateProjectController(createProjectView);
    createProjectController.bind();
  }

  public void convertCurrentMapToTMX() {
    manageMapsController.exportCurrentMapToTMX();
  }

  public void changeVisibilityState(boolean is2DVisibilitySelected, boolean isGridVisibilitySelected) {
    AppParameters.CURRENT_PROJECT.setIs2DVisibilitySelected(is2DVisibilitySelected);
    AppParameters.CURRENT_PROJECT.setIsGridVisibilitySelected(isGridVisibilitySelected);
    manageMapsController.change2DVisibilityState(is2DVisibilitySelected, isGridVisibilitySelected, null);
  }

  public void setDeleteEntityValue(boolean value) {
    AppParameters.CURRENT_PROJECT.setDeleteEntity(value);
    MapCanvas.DELETE_ENTITY = value;
  }

  public void setFillAreaValue(boolean value) {
    AppParameters.CURRENT_PROJECT.setFillArea(value);
    MapCanvas.FILL_AREA = value;
  }

  public void showMapGrid(boolean showMapGrid) {
    AppParameters.CURRENT_PROJECT.setShowGrid(showMapGrid);
    manageMapsController.showGridValueChanged(showMapGrid);
  }

  /**
   * Change primary content view. It is switching between map view and image view.
   * It is called when the view selection of the toolbar is changed.
   */
  public void changeMainView() {
    if (toolbarController.isMapViewSelected())
      setMapView();
    else
      setImageView();
  }

  private void setMapView() {
    if (manageMapsController == null) {
      ManageMapsController.IMangeMapsView mapContentView = new ManageMapsView();
      manageMapsController = new ManageMapsController(mapContentView, AppParameters.CURRENT_PROJECT.getMapViewDividerPosition());
      manageMapsController.bind();
      projectTreeController.setManageMapsController(manageMapsController);
    }
    setContentView(manageMapsController.getView().asNode());
  }

  private void setImageView() {
    if (manageTileSetsController == null) {
      ManageTileSetsController.IManageTileSetsView imagesView = new ManageTileSetsView();
      manageTileSetsController = new ManageTileSetsController(imagesView);
      manageTileSetsController.bind();
      projectTreeController.setManageTileSetsController(manageTileSetsController);
    }
    setContentView(manageTileSetsController.getView().asNode());
  }

  public void setProjectTreeView(boolean showTreeView) {
    if (projectTreeController == null) {
      ProjectTreeController.IProjectTreeView projectTreeView = new ProjectTreeView();
      projectTreeController = new ProjectTreeController(projectTreeView);
      projectTreeController.bind();
    }
    Region node = projectTreeController.getViewNode();
    SplitPane.setResizableWithParent(node, false);

    if (showTreeView) {
      centerSplitPane.getItems().add(0, node);
      if (lastProjectTreeDividerPosition < 0)
        centerSplitPane.setDividerPositions(0.4);
      else
        centerSplitPane.setDividerPositions(lastProjectTreeDividerPosition);
    } else {
      double[] dividers = centerSplitPane.getDividerPositions();
      if (dividers != null && dividers.length > 0)
        lastProjectTreeDividerPosition = dividers[0];
      if (!centerSplitPane.getItems().isEmpty())
        centerSplitPane.getItems().remove(0);
    }
    AppParameters.CURRENT_PROJECT.setShowProjectTree(showTreeView);
    AppParameters.CURRENT_PROJECT.setProjectTreeDividerPosition(lastProjectTreeDividerPosition);
  }

  private void setContentView(Region contentView) {
    double[] dividerPositions = centerSplitPane.getDividerPositions();
    if (centerSplitPane.getItems().size() == 2 && projectVerticalToolbarController.isShowProject()) {
      centerSplitPane.getItems().remove(1);
      centerSplitPane.getItems().add(contentView);
    } else if (centerSplitPane.getItems().size() == 1 && projectVerticalToolbarController.isShowProject()) {
      centerSplitPane.getItems().add(1, contentView);
    } else {
      centerSplitPane.getItems().clear();
      centerSplitPane.getItems().add(contentView);
    }

    if (dividerPositions.length > 0)
      centerSplitPane.setDividerPositions(dividerPositions);
  }

  private void addMapEditorToolBarsAndStatusBars(Region menuBar, Region toolbar, Region statusBar) {
    VBox pane = new VBox();
    pane.getChildren().addAll(menuBar, toolbar);
    mainContainer.setTop(pane);
    mainContainer.setBottom(statusBar);
  }

  /**
   * Load the project.
   * @param project - project
   * @param loadFromCreateProjectView true if the call it's made from CreateProjectView
   */
  public void loadProject(ProjectModel project, boolean loadFromCreateProjectView) {
    AppParameters.CURRENT_PROJECT = project;

    mapEditorView.showPrimaryStage();

    if (loadFromCreateProjectView)
      mapEditorView.clearCreateProjectView();
  }

  /**
   * Create a new map instance based on the provided model.
   * If the map name already exist, a new one will be provided with a higher order number.
   * @param mapDetail
   * MapDetail
   */
  public void createNewMap(MapDetail mapDetail) {
    try {
      maskView();

      String name = repoController.saveMap(AppParameters.CURRENT_PROJECT.getHomePath(), mapDetail, null, false);
      mapDetail.setName(name);
      manageMapsController.addNewMap(mapDetail);

      unmaskView();
    } catch (Exception ex) {
      unmaskView();
      Dialog.showErrorDialog(null, "MapEditorController: Unexpected error while creating the map.");
    }
  }

  public void saveCurrentProjectState() {
    manageMapsController.updateMapModelsForExistingTabs();
    AppParameters.CURRENT_PROJECT.setIs2DVisibilitySelected(is2DVisibilitySelected());
    AppParameters.CURRENT_PROJECT.setProjectTreeDividerPosition(lastProjectTreeDividerPosition);
    AppParameters.CURRENT_PROJECT.setMapViewDividerPosition(manageMapsController.getDividerPosition());
    repoController.saveProject(AppParameters.CURRENT_PROJECT);
  }

  public void saveSelectedMap() {
    if (manageMapsController == null)
      return;
    MapDetail mapDetail = manageMapsController.getSelectedMapDetail();
    if (mapDetail == null) {
      Dialog.showWarningDialog(null, "Detail model for the selected map is null!");
      return;
    }
    Platform.runLater(() -> {
      try {
        repoController.saveMap(AppParameters.CURRENT_PROJECT.getHomePath(), mapDetail, null, true);
        System.out.println("MapEditorController - saveSelectedMap - " + mapDetail.getName() + " was saved");
      } catch (Exception ex) {
        System.out.println("MapEditorController - saveSelectedMap - Unable to save map: " + mapDetail.getName() + " Error message: " + ex.getMessage());
      }
    });
  }

  public boolean deleteMap(File file) {
    try {
      if (!repoController.deleteFile(file)) {
        Dialog.showWarningDialog(null, "Unable to delete map file: " + file);
      } else {
        return true;
      }
    } catch (Exception ex) {
      Dialog.showErrorDialog(null, "Error occurred while trying to delete the file: " + file);
    }
    return false;
  }

  public void dividerPositionChanged() {
    double[] dividerPositions = centerSplitPane.getDividerPositions();
    if (dividerPositions != null && dividerPositions.length > 0)
      lastProjectTreeDividerPosition = dividerPositions[0];
  }

  public void changeToMapView() {
    toolbarController.changeToMapView();
  }

  public void changeToImageEditorView() {
    toolbarController.changeToImageEditorView();
  }

  public void changeToTilesView() {
    manageMapsController.switchTileView(true);
  }

  public void changeToCharactersView() {
    manageMapsController.switchTileView(false);
  }

  public void maskView() {
    mapEditorView.mask();
  }

  public void unmaskView() {
    mapEditorView.unmask();
  }

  public void closeApp() {
    mapEditorView.close();
  }



  /* getters and setters */

  public void setSceneCursor(Cursor cursor) {
    scene.setCursor(cursor);
  }

  public void setSceneAndMainContainer(Scene scene, BorderPane mainContainer, SplitPane centerSplitPane) {
    this.scene = scene;
    this.mainContainer = mainContainer;
    this.centerSplitPane = centerSplitPane;
  }

  public void setMapEditorView(MapEditorView mapEditorView) {
    this.mapEditorView = mapEditorView;
  }

  public void setRepoController(RepoController repoController) {
    this.repoController = repoController;
  }

  public RepoController getRepoController() {
    return repoController;
  }

  public final Scene getScene() {
    return scene;
  }

  public boolean isImageEditorView() {
    return !toolbarController.isMapViewSelected();
  }

  public boolean isMapView() {
    return toolbarController.isMapViewSelected();
  }

  public boolean is2DVisibilitySelected() {
    return toolbarController.is2DVisibilitySelected();
  }

  public boolean isGridVisibilitySelected() {
    return toolbarController.isGridVisibilitySelected();
  }

  public ManageTilesController getManageTilesController() {
    return manageMapsController != null ? manageMapsController.getManageTilesController() : null;
  }

  public ManageCharactersController getManageCharactersController() {
    return manageMapsController != null ? manageMapsController.getManageCharactersController() : null;
  }
}
