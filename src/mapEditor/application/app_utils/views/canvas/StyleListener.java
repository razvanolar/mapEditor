package mapEditor.application.app_utils.views.canvas;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;

/**
 * Used to communicate between ImageCanvas and a controller that can modify canvas styles.
 *
 * Created by razvanolar on 31.01.2016.
 */
public interface StyleListener {
  ColorAdjust getColorAdjust();
  Color getBackgroundColor();
  Color getSquareBorderColor();
  Color getSquareFillColor();
  void setBackgroundColor(Color color);
  void setSquareBorderColor(Color color);
  void setSquareFillColor(Color color);
  void paintContent();
}
