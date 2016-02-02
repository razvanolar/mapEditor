package mapEditor.application.main_part.manage_maps.primary_map;

import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import mapEditor.MapEditorController;
import mapEditor.application.main_part.manage_maps.MapCanvas;

/**
 *
 * Created by razvanolar on 21.01.2016.
 */
public class PrimaryMapView extends MapCanvas {

  public PrimaryMapView() {
    super();
    this.setWidth(100);
    this.setHeight(100);
    this.prefHeight(100);
    this.prefWidth(100);
    initializeComponents();
  }

  private void initializeComponents() {
    fillColor = new Color(0.5, 0.5, 0.5, 0.5);
    gridColor = Color.DARKCYAN;
    squareColor = Color.YELLOW;
    this.setCache(true);
    this.setCacheHint(CacheHint.SPEED);

    canvasX = 0;
    canvasY = 0;
  }

  public void onMousePressedEvent(MouseEvent event) {
    super.handleMousePressEvent(event);
    if (event.isControlDown())
      return;

    drawImagesOnMouseActions(event);
  }

  public void onMouseReleasedEvent(MouseEvent event, boolean paintMap) {
    if (dragDetected) {
      dragDetected = false;

      canvasX = getDragHorizontalValue(event);
      canvasY = getDragVerticalValue(event);
      lastValidCanvasX = canvasX;
      lastValidCanvasY = canvasY;

      /* this 'if' block is same as in the MiniMapCanvas method; be careful when you modified it, probably the one from
       * the child class method needs to be modified also */
      if(paintMap) {
        paint();
        MapEditorController.getInstance().setSceneCursor(Cursor.DEFAULT);
        notifyPositionChange();
      }
    }

  }

  public void onMouseMovedEvent(MouseEvent event) {
    /* the cartesian coordinates of the hovered cell */
    int x = (int) event.getX() + 1;
    int y = (int) event.getY() + 1;

    if (!checkMouseBorders(x, y))
      return;

    int hoveredCellX = (int) ((event.getX() - canvasX) / CELL_WIDTH) * CELL_WIDTH;
    int hoveredCellY = (int) ((event.getY() - canvasY) / CELL_HEIGHT) * CELL_HEIGHT;

    if (lastMoveX == hoveredCellX && lastMoveY == hoveredCellY)
      return;

    drawGridLines(hoveredCellX, hoveredCellY);

    lastMoveX = hoveredCellX;
    lastMoveY = hoveredCellY;
  }

  public void onMouseDraggedEvent(MouseEvent event) {
    if (event.isControlDown()) {
      super.handleMouseDragEvent(event);
      return;
    }
    if (!event.isPrimaryButtonDown())
      return;

    drawImagesOnMouseActions(event);
  }

  public void onScrollEvent(ScrollEvent event) {
        /* If the mouse position is not situated in the map borders, zoom actions are not applied */
    if(!checkMouseBorders((int) event.getX(), (int) event.getY()))
      return;

    /* the cartesian coordinates of the hovered cell relative to the matrix, not to the origin of the canvas */
    int hoveredCellX = (int) ((event.getX() - canvasX) / CELL_WIDTH) * CELL_WIDTH;
    int hoveredCellY = (int) ((event.getY() - canvasY) / CELL_HEIGHT) * CELL_HEIGHT;

    /* the matrix coordinates of the hovered cell */
    int hoveredCellColumn = hoveredCellX / CELL_WIDTH;
    int hoveredCellRow = hoveredCellY / CELL_HEIGHT;

    //System.out.println("row=" + hoveredCellRow + " col=" + hoveredCellColumn);

    /* if deltaY < 0 -> ZOOM OUT else ZOOM IN */
    zoomAction(event.getDeltaY() < 0 ? -1 : 1, hoveredCellRow, hoveredCellColumn);
  }

  private void zoomAction(int zoomType, int hoveredCellRow, int hoveredCellColumn) {
    if (zoomType != 1 && zoomType != -1)
      return;

    zoomStatus = zoomType == 1 ? zoomStatus + ZOOM_RATIO : zoomStatus - ZOOM_RATIO;
    /* if zoom status runs out of the bound we restore it and end the function call */
    if (zoomStatus < -10 || zoomStatus > 15) {
      zoomStatus = zoomType == 1 ? zoomStatus - ZOOM_RATIO : zoomStatus + ZOOM_RATIO;
      return;
    }

    /* determine the offset of the hovered cell before the cell dimensions are changed */
    int offsetCellX = (hoveredCellColumn * CELL_WIDTH) + canvasX;
    int offsetCellY = (hoveredCellRow * CELL_HEIGHT) + canvasY;

    //System.out.println("offX=" + offsetCellX + " offY=" + offsetCellY);

    CELL_WIDTH = DEFAULT_CELL_WIDTH + zoomStatus;
    CELL_HEIGHT = DEFAULT_CELL_HEIGHT + zoomStatus;

    canvasX = offsetCellX - (hoveredCellColumn * CELL_WIDTH);
    canvasY = offsetCellY - (hoveredCellRow * CELL_HEIGHT);

    //System.out.println("canvasX=" + canvasX + " canvasY=" + canvasY);

    if(isHorizontalDraggable()) {
      if(canvasX > CELL_WIDTH)
        canvasX = CELL_WIDTH;
      else if(canvasX < 0 && ((canvasX + getMapWidth()) - getWidth()) < -CELL_WIDTH) {
        canvasX = (int) getWidth() - getMapWidth() - CELL_WIDTH;
      }
    } else
      canvasX = ((int) getWidth() - getMapWidth()) / 2;

    if(isVerticalDraggable()) {
      if(canvasY > CELL_HEIGHT)
        canvasY = CELL_HEIGHT;
      else if(canvasY < 0 && ((canvasY + getMapHeight()) - getHeight()) < -CELL_HEIGHT) {
        canvasY = (int) getHeight() - getMapHeight() - CELL_HEIGHT;
      }
    } else
      canvasY = ((int) getHeight() - getMapHeight()) / 2;

    lastValidCanvasX = canvasX;
    lastValidCanvasY = canvasY;
    paint();
    notifyPositionChange();
  }

  private void drawGridLines(int hoveredCellX, int hoveredCellY) {
     /* the matrix coordinates of the hovered cell */
    int hoveredCellColumn = hoveredCellX / CELL_WIDTH;
    int hoveredCellRow = hoveredCellY / CELL_HEIGHT;

    //System.out.println("hX=" + hoveredCellColumn + " hY=" + hoveredCellRow);

    int cellX = hoveredCellColumn * CELL_WIDTH + canvasX;
    int cellY = hoveredCellRow * CELL_HEIGHT + canvasY;
    drawGrid(getGraphicsContext2D(), cellX, cellY,
            mapInfo[START_X],
            mapInfo[START_Y],
            mapInfo[STOP_X],
            mapInfo[STOP_Y]);
  }

  private void drawImagesOnMouseActions(MouseEvent event) {
    /* the cartesian coordinates of the hovered cell */
    int x = (int) event.getX() + 1;
    int y = (int) event.getY() + 1;

//    if(!checkMouseBorders(x, y) || SelectedTilesUtil.primaryTile == null || SelectedTilesUtil.primaryTile.image == null)
//      return;

    /** matrix cells coordinates */
    int cellX = (int) ((event.getX() - canvasX) / CELL_WIDTH);
    int cellY = (int) ((event.getY() - canvasY) / CELL_HEIGHT);

    /** map coordinates */
    int hoveredCellX = (int) ((event.getX() - canvasX) / CELL_WIDTH) * CELL_WIDTH + canvasX;
    int hoveredCellY = (int) ((event.getY() - canvasY) / CELL_HEIGHT) * CELL_HEIGHT + canvasY;

    /* we need to draw the selected pattern */
//    controller.addImage(SelectedTilesUtil.primaryTile.image, cellX, cellY);
//    GraphicsContext g = getGraphicsContext2D();
//    LinkedList<DrawAreaPaneRepository.TilesMatrix> layers = controller.getLayers();
//    for (DrawAreaPaneRepository.TilesMatrix tilesMatrix : layers) {
//      Image image = tilesMatrix.getMatrix()[cellY][cellX];
//      if (image != null)
//        g.drawImage(image, hoveredCellX, hoveredCellY, CELL_WIDTH, CELL_HEIGHT);
//    }
//
//    List<SelectedTilesUtil.TileDetails> list = SelectedTilesUtil.secondaryTyles;
//    if(list != null && !list.isEmpty()) {
//      for(SelectedTilesUtil.TileDetails tileDetail : list)
//        if(tileDetail.image != null) {
//          controller.addImage(tileDetail.image, cellX + tileDetail.X, cellY + tileDetail.Y);
//          for (DrawAreaPaneRepository.TilesMatrix tilesMatrix : layers) {
//            Image image = tilesMatrix.getMatrix()[cellY + tileDetail.Y][cellX + tileDetail.X];
//            if (image != null)
//              g.drawImage(image,
//                      hoveredCellX + (tileDetail.X * CELL_WIDTH),
//                      hoveredCellY + (tileDetail.Y * CELL_HEIGHT),
//                      CELL_WIDTH,
//                      CELL_HEIGHT);
//          }
//        }
//    }

    drawGridLines(hoveredCellX - canvasX, hoveredCellY - canvasY);

//    controller.handleChangePosition(MapNotificationTypes.PRIMARY_MAP);
  }
}