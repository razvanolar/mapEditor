package mapEditor.application.repo;

import mapEditor.application.main_part.app_utils.models.MessageKey;
import mapEditor.application.repo.models.LWProjectModel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by razvanolar on 05.02.2016.
 */
public class SystemParameters {

  public static final String CONFIG_FILE_PATH = "med_config\\";
  public static final String KNOWN_PROJECTS_FILE_PATH = CONFIG_FILE_PATH + "projects\\";
  public static final String PROJECT_FILE_EXT = ".med";
  public static final String TILE_GROUPS_FOLDER_PATH = "tile_groups\\";
  public static final String TILES_FOLDER_PATH = TILE_GROUPS_FOLDER_PATH + "tiles\\";
  public static final String TILE_SETS_FOLDER_PATH = TILE_GROUPS_FOLDER_PATH + "tile_sets\\";
  public static final String CHARACTERS_FOLDER_PATH = "characters\\";
  public static final String MAPS_FOLDER_PATH = "maps\\";

  public static List<LWProjectModel> PROJECTS;

  public static final MessageKey MESSAGE_KEY = new MessageKey();

  public static List<Thread> watchers = new ArrayList<>();
  public static Thread appListenerThread;
}
