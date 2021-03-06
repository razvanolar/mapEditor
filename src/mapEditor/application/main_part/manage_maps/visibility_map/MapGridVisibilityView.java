package mapEditor.application.main_part.manage_maps.visibility_map;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import mapEditor.application.main_part.app_utils.constants.CssConstants;

/**
 *
 * Created by razvanolar on 19.03.2016.
 */
public class MapGridVisibilityView implements MapGridVisibilityController.IMapGridVisibilityView {

  private ScrollPane scrollPane;

  public MapGridVisibilityView() {
    initGUI();
  }

  private void initGUI() {
    scrollPane = new ScrollPane();
    scrollPane.getStyleClass().add(CssConstants.CANVAS_CONTAINER_LIGHT_BG);
  }

  public ScrollPane getScrollPane() {
    return scrollPane;
  }

  @Override
  public Region asNode() {
    return scrollPane;
  }
}
