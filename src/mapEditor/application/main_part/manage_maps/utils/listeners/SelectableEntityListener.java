package mapEditor.application.main_part.manage_maps.utils.listeners;

import mapEditor.application.main_part.manage_maps.utils.SelectableBrushView;
import mapEditor.application.main_part.manage_maps.utils.SelectableCharacterView;
import mapEditor.application.main_part.manage_maps.utils.SelectableObjectView;
import mapEditor.application.main_part.manage_maps.utils.SelectableTileView;

/**
 *
 * Created by razvanolar on 01.03.2016.
 */
public interface SelectableEntityListener {

  void selectedTileChanged(SelectableTileView selectedView);
  void selectedBrushChanged(SelectableBrushView brushView);
  void selectedObjectChanged(SelectableObjectView objectView);
  void selectedCharacterChanged(SelectableCharacterView characterView);
}
