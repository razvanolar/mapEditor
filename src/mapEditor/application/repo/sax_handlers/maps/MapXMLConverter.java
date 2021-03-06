package mapEditor.application.repo.sax_handlers.maps;

import javafx.scene.paint.Color;
import mapEditor.application.main_part.app_utils.AppParameters;
import mapEditor.application.main_part.app_utils.data_types.CustomMap;
import mapEditor.application.main_part.app_utils.models.*;
import mapEditor.application.repo.SystemParameters;
import mapEditor.application.repo.types.MapType;

import java.util.*;

/**
 *
 * Created by razvanolar on 27.02.2016.
 */
public class MapXMLConverter {

  private StringBuilder auxBuilder = new StringBuilder();

  public String convertMapToXML(MapDetail map, String projectPath) throws Exception {
    if (map == null)
      throw new Exception("MapXMLConverter - convertMapToXML - Map instance is NULL");

    projectPath = projectPath == null ? "" : projectPath;

    StringBuilder builder = new StringBuilder();
    builder.append(SystemParameters.XML_HEADER).append("\n\n");

    builder.append("<map name=\"").append(map.getName()).
            append("\" path=\"").append(map.getRelativePath()).
            append("\" rows=\"").append(map.getRows()).
            append("\" columns=\"").append(map.getColumns()).
            append("\" x=\"").append(map.getX()).
            append("\" y=\"").append(map.getY()).
            append("\" zoom=\"").append(map.getZoomStatus()).append("\">\n");

    /** convert colors */
    convertColorToXml(builder, "bg_color", map.getBackgroundColor());
    convertColorToXml(builder, "grid_color", map.getGridColor());
    convertColorToXml(builder, "square_color", map.getSquareColor());

    /** convert tiles */
    CustomMap<ImageModel, Integer> indexedTiles = computeTilesIndex(getDistinctTiles(map));
    convertIndexedTiles(builder, indexedTiles, projectPath);

    /** convert layers */
    List<LayerModel> layers = map.getLayers();
    if (layers != null && !layers.isEmpty()) {
      builder.append("\n\t<layers>\n");
      for (LayerModel layer : layers) {
        boolean addedTileFlag = false;
        builder.append("\t\t").append("<layer name=\"").append(layer.getName()).
                append("\" type=\"").append(layer.getType().name()).
                append("\" isSelected=\"").append(layer.isSelected()).
                append("\" isChecked=\"").append(layer.isChecked()).append("\"");

        if (map.getMapTilesInfo() != null) {
          CustomMap<LayerModel, CustomMap<ImageModel, List<CellModel>>> layersTilesMap = map.getMapTilesInfo().getLayersTilesMap();
          if (indexedTiles != null && layersTilesMap != null && !indexedTiles.isEmpty()) {
            CustomMap<ImageModel, List<CellModel>> tilesMap = layersTilesMap.get(layer);
            if (tilesMap != null && !tilesMap.isEmpty()) {
              for (ImageModel tile : tilesMap.keys()) {
                Integer index = indexedTiles.get(tile);
                if (index == null)
                  continue;
                String tileResult = convertTileWithCells(index, tilesMap.get(tile));
                if (tileResult != null) {
                  if (!addedTileFlag) {
                    builder.append(">\n");
                    addedTileFlag = true;
                  }
                  builder.append(tileResult);
                }
              }
            }
          }
        }

        if (addedTileFlag)
          builder.append("\t\t</layer>\n");
        else
          builder.append(" />\n");
      }
      builder.append("\t</layers>\n");
    }

    return builder.append("</map>").toString();
  }

  public String convertMapToTMX(MapDetail map, String tilesetName, String tilesetPath) throws Exception {
    if (map == null)
      throw new Exception("MapXMLConverter - convertMapToXML - Map instance is NULL");

    int cellSize = AppParameters.CURRENT_PROJECT.getCellSize();

    StringBuilder builder = new StringBuilder();
    int spacing = 0;
    int margin = 0;

    builder.append(SystemParameters.XML_HEADER).append("\n\n");

    builder.append("<map orientation=\"").append(MapType.ORTHOGONAL.getOrientation()).append("\" ").
            append("width=\"").append(map.getColumns()).append("\" ").
            append("height=\"").append(map.getRows()).append("\" ").
            append("tilewidth=\"").append(cellSize).append("\" ").
            append("tileheight=\"").append(cellSize).append("\" >\n");

    builder.append("\t<tileset firstgid=\"1\" ").append("name=\"").append(tilesetName).append("\" ").
            append("tilewidth=\"").append(cellSize).append("\" ").
            append("tileheight=\"").append(cellSize).append("\" ").
            append("spacing=\"").append(spacing).append("\" ").
            append("margin=\"").append(margin).append("\" >\n");
    builder.append("\t\t<image source=\"").append(tilesetPath).append("\" />\n");
    builder.append("\t</tileset>\n");

    CustomMap<LayerModel, CustomMap<ImageModel, List<CellModel>>> layersTilesMap = map.getMapTilesInfo().getLayersTilesMap();
    for (LayerModel layer : layersTilesMap.keys()) {
      convertLayerDetailsToTMXFormat(builder, layer, layersTilesMap.get(layer));
    }

    builder.append("</map>");

    return builder.toString();
  }

  private void convertLayerDetailsToTMXFormat(StringBuilder builder, LayerModel layer,
                                              CustomMap<ImageModel, List<CellModel>> imageModelMap) {
    builder.append("\t<layer name=\"").append(layer.getName()).append("\" type=\"").
            append(layer.getType()).append("\" >\n");
    builder.append("\t\t<data>\n");

    for (ImageModel imageModel : imageModelMap.keys()) {
      List<CellModel> cellModels = imageModelMap.get(imageModel);
      int gid = imageModel.getId();
      for (CellModel cell : cellModels) {
        builder.append("\t\t\t<tile gid=\"").append(gid).append("\" row=\"").append(cell.getY()).append("\" ").
                append(" col=\"").append(cell.getX()).append("\" />\n");
      }
    }

    builder.append("\t\t</data>\n");
    builder.append("\t</layer>\n");
  }

  private String convertColorToXml(StringBuilder builder, String tag, Color color) {
    builder.append("\t<").append(tag).append(" red=\"").append(color.getRed()).
            append("\" green=\"").append(color.getGreen()).
            append("\" blue=\"").append(color.getBlue()).
            append("\" opacity=\"").append(color.getOpacity()).append("\" />\n");
    return builder.toString();
  }

  private void convertIndexedTiles(StringBuilder builder, CustomMap<ImageModel, Integer> indexedTiles, String projectPath) {
    builder.append("\n\t<images>");
    if (indexedTiles == null || indexedTiles.isEmpty()) {
      builder.append("</images>\n");
      return;
    }

    builder.append("\n");
    for (ImageModel tile : indexedTiles.keys()) {
      builder.append("\t\t<image index=\"").append(indexedTiles.get(tile)).append("\" path=\"").
              append(tile.getFile().getAbsolutePath().replace(projectPath, "")).append("\" />\n");
    }
    builder.append("\t</images>\n");
  }

  private String convertTileWithCells(int tileIndex, List<CellModel> cells) {
    if (cells == null || cells.isEmpty())
      return null;
    auxBuilder.setLength(0);
    auxBuilder.append("\t\t\t<tile index=\"").append(tileIndex).append("\">\n");
    for (CellModel cell : cells)
      auxBuilder.append("\t\t\t\t<cell x=\"").append(cell.getX()).append("\"").
              append(" y=\"").append(cell.getY()).append("\" />\n");
    auxBuilder.append("\t\t\t</tile>\n");
    return auxBuilder.toString();
  }


  /**
   * Build a set of all distinct tiles that constructs the map.
   * @param mapDetail
   * MapDetail
   * @return a set off all distinct ImageModels
   */
  private List<ImageModel> getDistinctTiles(MapDetail mapDetail) {
    if (mapDetail == null || mapDetail.getLayers() == null || mapDetail.getLayers().isEmpty())
      return null;
    MapTilesInfo mapTilesInfo = mapDetail.getMapTilesInfo();
    if (mapTilesInfo == null)
      return null;
    CustomMap<LayerModel, CustomMap<ImageModel, List<CellModel>>> layersTilesMap = mapTilesInfo.getLayersTilesMap();
    if (layersTilesMap == null || layersTilesMap.isEmpty())
      return null;

    List<ImageModel> tilesSet = new ArrayList<>();
    for (LayerModel layer : mapDetail.getLayers()) {
      CustomMap<ImageModel, List<CellModel>> tilesMap = layersTilesMap.get(layer);
      if (tilesMap == null || tilesMap.isEmpty())
        continue;
      tilesMap.keys().stream().filter(key -> !tilesSet.contains(key)).forEach(tilesSet::add);
    }

    return tilesSet;
  }

  /**
   * Assign an index to every tile present in the set.
   * @param set
   * Set
   * @return an indexed tiles map
   */
  private CustomMap<ImageModel, Integer> computeTilesIndex(List<ImageModel> set) {
    if (set == null || set.isEmpty())
      return null;

    int index = 0;
    CustomMap<ImageModel, Integer> result = new CustomMap<>();
    for (ImageModel tile : set)
      result.put(tile, index++);

    return result;
  }
}
