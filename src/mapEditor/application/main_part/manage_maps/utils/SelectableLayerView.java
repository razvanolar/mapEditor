package mapEditor.application.main_part.manage_maps.utils;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import mapEditor.application.main_part.app_utils.AppParameters;
import mapEditor.application.main_part.app_utils.models.LayerType;

/**
 *
 * Created by razvanolar on 23.02.2016.
 */
public class SelectableLayerView extends StackPane {

  private static final Background TRANSPARENT_BG = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));
  private static EventHandler<MouseEvent> onMouseEnteredListener;
  private static EventHandler<MouseEvent> onMouseExitedListener;
  private static EventHandler<MouseEvent> onMouseClickedListener;

  private CheckBox checkBox;
  private Text text;
  private HBox container;

  private SelectableLayerListener listener;
  private LayerType type;
  private String name;
  private boolean isSelected;

  public SelectableLayerView(LayerType type, String name, SelectableLayerListener listener) {
    this.type = type;
    this.name = name;
    this.listener = listener;
    initGUI();
    addListeners();
  }

  private void initGUI() {
    checkBox = new CheckBox();
    text = new Text(name);
    container = new HBox(5, checkBox, text);

    checkBox.setSelected(true);
    checkBox.setPadding(new Insets(3, 0, 5, 5));
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPrefWidth(25);
    getChildren().add(container);
  }

  private void addListeners() {
    this.setOnMouseEntered(getOnMouseEnteredListener());
    this.setOnMouseExited(getOnMouseExitedListener());
    this.setOnMouseClicked(getOnMouseClickedListener());
  }

  public void select() {
    isSelected = true;
    container.setBackground(AppParameters.SELECTED_LAYER_BG);
    listener.selectedLayerChanged(this);
  }

  public void unselect() {
    isSelected = false;
    container.setBackground(TRANSPARENT_BG);
  }

  private void onMouseEntered() {
    if (!isSelected)
      container.setBackground(AppParameters.HOVERED_LAYER_BG);
  }

  private void onMouseExited() {
    if (!isSelected)
      container.setBackground(TRANSPARENT_BG);
  }

  private static EventHandler<MouseEvent> getOnMouseEnteredListener() {
    if (onMouseEnteredListener == null) {
      onMouseEnteredListener = event -> {
        SelectableLayerView source = (SelectableLayerView) event.getSource();
        source.onMouseEntered();
      };
    }
    return onMouseEnteredListener;
  }

  private static EventHandler<MouseEvent> getOnMouseExitedListener() {
    if (onMouseExitedListener == null) {
      onMouseExitedListener = event -> {
        SelectableLayerView source = (SelectableLayerView) event.getSource();
        source.onMouseExited();
      };
    }
    return onMouseExitedListener;
  }

  public static EventHandler<MouseEvent> getOnMouseClickedListener() {
    if (onMouseClickedListener == null) {
      onMouseClickedListener = event -> {
        SelectableLayerView source = (SelectableLayerView) event.getSource();
        source.select();
      };
    }
    return onMouseClickedListener;
  }
}