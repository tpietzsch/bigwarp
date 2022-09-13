/*-
 * #%L
 * BigWarp plugin for Fiji.
 * %%
 * Copyright (C) 2015 - 2021 Howard Hughes Medical Institute.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package bigwarp;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;

import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.InputActionBindings;

import bdv.BigDataViewerActions;
import bdv.KeyConfigContexts;
import bdv.KeyConfigScopes;
import bdv.gui.BigWarpViewerFrame;
import bdv.tools.ToggleDialogAction;
import bdv.viewer.SourceAndConverter;
import bigwarp.BigWarp.BigWarpData;
import bigwarp.landmarks.LandmarkGridGenerator;
import bigwarp.source.GridSource;
import bigwarp.util.BigWarpUtils;
import mpicbg.models.AbstractModel;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.realtransform.AffineTransform3D;

public class BigWarpActions extends Actions
{
	public static final CommandDescriptionProvider.Scope BIGWARP = new CommandDescriptionProvider.Scope( "bigwarp" );
	public static final String BIGWARP_CTXT = "bigwarp";

	public static final String LANDMARK_MODE_ON  = "landmark mode on";
	public static final String LANDMARK_MODE_OFF  = "landmark mode off";

	// General options
	public static final String EXPAND_CARDS = "expand and focus cards panel";
	public static final String[] EXPAND_CARDS_KEYS = new String[] { "P" };

	public static final String COLLAPSE_CARDS = "collapse cards panel";
	public static final String[] COLLAPSE_CARDS_KEYS = new String[] { "shift P", "shift ESCAPE" };

	public static final String PREFERENCES_DIALOG = "Preferences";
	public static final String[] PREFERENCES_DIALOG_KEYS= new String[] { "meta COMMA", "ctrl COMMA" };

	// Display options
	public static final String   TOGGLE_LANDMARK_MODE  = "landmark mode toggle";
	public static final String[] TOGGLE_LANDMARK_MODE_KEYS  = new String[]{ "SPACE" };

	public static final String   TOGGLE_POINTS_VISIBLE  = "toggle points visible";
	public static final String[] TOGGLE_POINTS_VISIBLE_KEYS  = new String[]{ "V" };

	public static final String TOGGLE_POINT_NAMES_VISIBLE  = "toggle point names visible";
	public static final String[] TOGGLE_POINT_NAMES_VISIBLE_KEYS  = new String[]{ "N" };

	public static final String TOGGLE_MOVING_IMAGE_DISPLAY = "toggle moving image display";
	public static final String[] TOGGLE_MOVING_IMAGE_DISPLAY_KEYS = new String[]{ "T" };

	public static final String TOGGLE_BOX_AND_TEXT_OVERLAY_VISIBLE  = "toggle box and text overlay visible";
	public static final String[] TOGGLE_BOX_AND_TEXT_OVERLAY_VISIBLE_KEYS  = new String[]{ "F8" };

	public static final String ESTIMATE_WARP = "estimate warp";
	public static final String[] ESTIMATE_WARP_KEYS = new String[] { "C" };

	public static final String PRINT_TRANSFORM = "print transform";
	public static final String[] PRINT_TRANSFORM_KEYS = new String[]{ "control shift T" };

	public static final String TOGGLE_ESTIMATE_WARP_ONDRAG = "toggle estimate warp on drag";
	public static final String[] TOGGLE_ESTIMATE_WARP_ONDRAG_KEYS = new String[]{};

//	public static final String CROP = "crop";

	public static final String SAVE_SETTINGS = "save settings";
	public static final String[] SAVE_SETTINGS_KEYS = new String[]{};

	public static final String LOAD_SETTINGS = "load settings";
	public static final String[] LOAD_SETTINGS_KEYS = new String[]{};

	public static final String BRIGHTNESS_SETTINGS = "brightness settings";
	public static final String[] BRIGHTNESS_SETTINGS_KEYS = new String[]{ "S" };

	public static final String VISIBILITY_AND_GROUPING = "visibility and grouping %s";
	public static final String VISIBILITY_AND_GROUPING_MVG = String.format( VISIBILITY_AND_GROUPING, "moving" );
	public static final String[] VISIBILITY_AND_GROUPING_MVG_KEYS = new String[]{ "F3" };

	public static final String VISIBILITY_AND_GROUPING_TGT = String.format( VISIBILITY_AND_GROUPING, "target" );
	public static final String[] VISIBILITY_AND_GROUPING_TGT_KEYS = new String[]{ "F4" };

	public static final String  SHOW_HELP = "help";
	public static final String[] SHOW_HELP_KEYS = new String[] { "F1" };

	public static final String SHOW_SOURCE_INFO = "show source info";

	// Warp visualization options
	public static final String SHOW_WARPTYPE_DIALOG = "show warp vis dialog" ;
	public static final String[] SHOW_WARPTYPE_DIALOG_KEYS = new String[]{ "U" };

	public static final String SET_WARPTYPE_VIS = "set warp vis type %s" ;

	public static final String SET_WARPTYPE_VIS_P = "p " + SET_WARPTYPE_VIS;

	public static final String SET_WARPTYPE_VIS_Q = "q " + SET_WARPTYPE_VIS;

	public static final String WARPMAG_BASE = "set warpmag base %s";
	public static final String WARPVISGRID = "set warp vis grid %s";
	public static final String WARPVISDIALOG = "warp vis dialog";

	// Navigation options
	public static final String   RESET_VIEWER = "reset active viewer";
	public static final String[] RESET_VIEWER_KEYS = new String[]{"R"};

	public static final String ALIGN_VIEW_TRANSFORMS = "align view transforms %s";
	public static final String ALIGN_OTHER_TO_ACTIVE = String.format( ALIGN_VIEW_TRANSFORMS, AlignViewerPanelAction.TYPE.OTHER_TO_ACTIVE );
	public static final String[] ALIGN_OTHER_TO_ACTIVE_KEYS = new String[] { "Q" };

	public static final String ALIGN_ACTIVE_TO_OTHER = String.format( ALIGN_VIEW_TRANSFORMS, AlignViewerPanelAction.TYPE.ACTIVE_TO_OTHER );
	public static final String[] ALIGN_ACTIVE_TO_OTHER_KEYS = new String[] { "W" };


	public static final String WARP_TO_SELECTED_POINT = "warp to selected landmark";
	public static final String[] WARP_TO_SELECTED_POINT_KEYS = new String[]{ "D" };

	public static final String WARP_TO_NEXT_POINT = "warp to next landmark";
	public static final String[] WARP_TO_NEXT_POINT_KEYS = new String[]{ "ctrl D"};

	public static final String WARP_TO_PREV_POINT = "warp to prev landmark";
	public static final String[] WARP_TO_PREV_POINT_KEYS = new String[]{ "ctrl shift D"};

	public static final String WARP_TO_NEAREST_POINT = "warp to nearest landmark";
	public static final String[] WARP_TO_NEAREST_POINT_KEYS = new String[]{ "E" };

	// landmark options
	public static final String LOAD_LANDMARKS = "load landmarks";
	public static final String[] LOAD_LANDMARKS_KEYS = new String[]{ "control O" };

	public static final String SAVE_LANDMARKS = "save landmarks";
	public static final String[] SAVE_LANDMARKS_KEYS = new String[]{ "control S" };

	public static final String QUICK_SAVE_LANDMARKS = "quick save landmarks";
	public static final String[] QUICK_SAVE_LANDMARKS_KEYS = new String[]{ "control Q" };

	public static final String SET_BOOKMARK = "set bookmark";
	public static final String[] SET_BOOKMARK_KEYS = new String[]{ "shift B" };

	public static final String GO_TO_BOOKMARK = "go to bookmark";
	public static final String[] GO_TO_BOOKMARK_KEYS = new String[]{ "B" };

	public static final String GO_TO_BOOKMARK_ROTATION = "go to bookmark rotation";
	public static final String[] GO_TO_BOOKMARK_ROTATION_KEYS = new String[]{ "control shift B" };

	public static final String UNDO = "undo";
	public static final String[] UNDO_KEYS = new String[]{ "control Z" };

	public static final String REDO = "redo";
	public static final String[] REDO_KEYS = new String[]{ "control shift Z", "control Y" };

	public static final String SELECT_TABLE_ROWS = "select table row %d";

	public static final String LANDMARK_SELECT_ALL = "select all landmarks";
	public static final String[] LANDMARK_SELECT_ALL_KEYS = new String[]{ "ctrl A"};

	public static final String LANDMARK_DESELECT_ALL = "deselect all landmarks";
	public static final String[] LANDMARK_DESELECT_ALL_KEYS = new String[]{ "ESCAPE" };

	public static final String LANDMARK_DELETE_SELECTED = "delete selected landmarks";
	public static final String[] LANDMARK_DELETE_SELECTED_KEYS = new String[]{ "DELETE"};

	public static final String LANDMARK_GRID_DIALOG = "landmark grid dialog";


	// export options
	public static final String SAVE_WARPED = "save warped";
	public static final String SAVE_WARPED_XML = "save warped xml";

	public static final String EXPORT_IP = "export imageplus";

	public static final String EXPORT_WARP = "export warp field";
	public static final String[] EXPORT_WARP_KEYS = new String[] { "ctrl W" };

	public static final String EXPORT_AFFINE = "export affine";
	public static final String[] EXPORT_AFFINE_KEYS = new String[] { "ctrl A" };


	public static final String DEBUG = "debug";
	public static final String GARBAGE_COLLECTION = "garbage collection";

	public BigWarpActions( final KeyStrokeAdder.Factory keyConfig, String name )
	{
		this( keyConfig, "bw", name );
	}

	public BigWarpActions( final KeyStrokeAdder.Factory keyConfig, String context, String name )
	{
		super( keyConfig, context, name );
	}
	
//	/*
//	 * Command descriptions for all provided commands
//	 */
//	@Plugin( type = CommandDescriptionProvider.class )
//	public static class BdvDescriptions extends CommandDescriptionProvider 
//	{
//		public BdvDescriptions()
//		{
//			super( BIGWARP, "bigwarp" );
//			bdv.BigDataViewerActions.Descriptions bdvDesc = new BigDataViewerActions.Descriptions();
//		}
//
//		@Override
//		public void getCommandDescriptions( final CommandDescriptions descriptions )
//		{
//			descriptions.add( BRIGHTNESS_SETTINGS,BRIGHTNESS_SETTINGS_KEYS, "Show the Brightness&Colors dialog." );
//			descriptions.add( VISIBILITY_AND_GROUPING, VISIBILITY_AND_GROUPING_KEYS, "Show the Visibility&Grouping dialog." );
//			descriptions.add( SHOW_HELP, SHOW_HELP_KEYS, "Show the Help dialog." );
//			descriptions.add( SAVE_SETTINGS, SAVE_SETTINGS_KEYS, "Save the BigDataViewer settings to a settings.xml file." );
//			descriptions.add( LOAD_SETTINGS, LOAD_SETTINGS_KEYS, "Load the BigDataViewer settings from a settings.xml file." );
//			descriptions.add( EXPAND_CARDS, EXPAND_CARDS_KEYS, "Expand and focus the BigDataViewer card panel" );
//			descriptions.add( COLLAPSE_CARDS, COLLAPSE_CARDS_KEYS, "Collapse the BigDataViewer card panel" );
//			descriptions.add( SET_BOOKMARK, SET_BOOKMARK_KEYS, "Set a labeled bookmark at the current location." );
//			descriptions.add( GO_TO_BOOKMARK, GO_TO_BOOKMARK_KEYS, "Retrieve a labeled bookmark location." );
//			descriptions.add( GO_TO_BOOKMARK_ROTATION, GO_TO_BOOKMARK_ROTATION_KEYS, "Retrieve a labeled bookmark, set only the orientation." );
//			descriptions.add( PREFERENCES_DIALOG, PREFERENCES_DIALOG_KEYS, "Show the Preferences dialog." );
//		}
//	}
	
	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( BIGWARP, "bigwarp" );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( TOGGLE_LANDMARK_MODE, TOGGLE_LANDMARK_MODE_KEYS, "Toggle landmark mode." );
			descriptions.add( TOGGLE_MOVING_IMAGE_DISPLAY, TOGGLE_MOVING_IMAGE_DISPLAY_KEYS, "Toggle landmark mode." );

			descriptions.add( SHOW_HELP, SHOW_HELP_KEYS, "Show the Help dialog." );
			descriptions.add( SHOW_WARPTYPE_DIALOG, SHOW_WARPTYPE_DIALOG_KEYS, "Show the BigWarp options dialog." );
			descriptions.add( PREFERENCES_DIALOG, PREFERENCES_DIALOG_KEYS, "Show the appearance and keymap dialog." );

			descriptions.add( BRIGHTNESS_SETTINGS, BRIGHTNESS_SETTINGS_KEYS, "Show the Brightness & Colors dialog." );
			descriptions.add( VISIBILITY_AND_GROUPING_MVG, VISIBILITY_AND_GROUPING_MVG_KEYS, "Show the Visibility&Grouping dialog for the moving frame." );
			descriptions.add( VISIBILITY_AND_GROUPING_TGT, VISIBILITY_AND_GROUPING_TGT_KEYS, "Show the Visibility&Grouping dialog for the fixed frame." );
			descriptions.add( SAVE_SETTINGS, SAVE_SETTINGS_KEYS, "Save the BigDataViewer settings to a settings.xml file." );
			descriptions.add( LOAD_SETTINGS, LOAD_SETTINGS_KEYS, "Load the BigDataViewer settings from a settings.xml file." );

			descriptions.add( SET_BOOKMARK, SET_BOOKMARK_KEYS, "Set a labeled bookmark at the current location." );
			descriptions.add( GO_TO_BOOKMARK, GO_TO_BOOKMARK_KEYS, "Retrieve a labeled bookmark location." );
			descriptions.add( GO_TO_BOOKMARK_ROTATION, GO_TO_BOOKMARK_ROTATION_KEYS, "Retrieve a labeled bookmark, set only the orientation." );

			descriptions.add( RESET_VIEWER, RESET_VIEWER_KEYS, "Resets the view to the view on startup." );
			descriptions.add( ALIGN_OTHER_TO_ACTIVE, ALIGN_OTHER_TO_ACTIVE_KEYS, "Sets the view of the non-active viewer to match the active viewer." );
			descriptions.add( ALIGN_ACTIVE_TO_OTHER, ALIGN_ACTIVE_TO_OTHER_KEYS, "Sets the view of the active viewer to match the non-active viewer." );
			descriptions.add( WARP_TO_SELECTED_POINT, WARP_TO_SELECTED_POINT_KEYS, "Center the viewer on the selected landmark." );
			descriptions.add( WARP_TO_NEAREST_POINT, WARP_TO_NEAREST_POINT_KEYS, "Center the viewer on the nearest landmark." );
			descriptions.add( WARP_TO_NEXT_POINT, WARP_TO_NEXT_POINT_KEYS, "Center the viewer on the next landmark." );
			descriptions.add( WARP_TO_PREV_POINT, WARP_TO_PREV_POINT_KEYS, "Center the viewer on the previous landmark." );

			// cards
			descriptions.add( EXPAND_CARDS, EXPAND_CARDS_KEYS, "Expand and focus the BigDataViewer card panel" );
			descriptions.add( COLLAPSE_CARDS, COLLAPSE_CARDS_KEYS, "Collapse the BigDataViewer card panel" );

			// export
			descriptions.add( EXPORT_WARP, EXPORT_WARP_KEYS, "Show the dialog to export the displacement field." );
			descriptions.add( EXPORT_AFFINE, EXPORT_AFFINE_KEYS, "Print the affine transformation." );

			// landmarks
			descriptions.add( LOAD_LANDMARKS, LOAD_LANDMARKS_KEYS, "Load landmark from a file." );
			descriptions.add( SAVE_LANDMARKS, SAVE_LANDMARKS_KEYS, "Save landmark from a file." );
			descriptions.add( QUICK_SAVE_LANDMARKS, QUICK_SAVE_LANDMARKS_KEYS, "Quick save landmarks.");
			descriptions.add( UNDO, UNDO_KEYS, "Undo the last landmark change." );
			descriptions.add( REDO, REDO_KEYS, "Redo the last landmark change." );

			descriptions.add( TOGGLE_POINTS_VISIBLE, TOGGLE_POINTS_VISIBLE_KEYS, "Toggle visibility of landmarks." );
			descriptions.add( TOGGLE_POINT_NAMES_VISIBLE, TOGGLE_POINT_NAMES_VISIBLE_KEYS , "Toggle visibility of landmark names." );

		}
	}
	
//	@Plugin( type = CommandDescriptionProvider.class )
//	public static class TableDescriptions extends CommandDescriptionProvider
//	{
//		public TableDescriptions()
//		{
//			super( BIGWARP, "bw-table" );
////			super( BDV, "bdv" );
//		}
//
//		@Override
//		public void getCommandDescriptions( final CommandDescriptions descriptions )
//		{
//			descriptions.add( TOGGLE_LANDMARK_MODE, TOGGLE_LANDMARK_MODE_KEYS, "Toggle landmark mode." );
//
//			descriptions.add( BRIGHTNESS_SETTINGS,BRIGHTNESS_SETTINGS_KEYS, "Show the Brightness&Colors dialog." );
//			descriptions.add( VISIBILITY_AND_GROUPING_MVG, VISIBILITY_AND_GROUPING_MVG_KEYS, "Show the Visibility&Grouping dialog for the moving frame." );
//			descriptions.add( VISIBILITY_AND_GROUPING_TGT, VISIBILITY_AND_GROUPING_TGT_KEYS, "Show the Visibility&Grouping dialog for the fixed frame." );
//			descriptions.add( SHOW_HELP, SHOW_HELP_KEYS, "Show the Help dialog." );
//			descriptions.add( SAVE_SETTINGS, SAVE_SETTINGS_KEYS, "Save the BigDataViewer settings to a settings.xml file." );
//			descriptions.add( LOAD_SETTINGS, LOAD_SETTINGS_KEYS, "Load the BigDataViewer settings from a settings.xml file." );
//
//			descriptions.add( SET_BOOKMARK, SET_BOOKMARK_KEYS, "Set a labeled bookmark at the current location." );
//			descriptions.add( GO_TO_BOOKMARK, GO_TO_BOOKMARK_KEYS, "Retrieve a labeled bookmark location." );
//			descriptions.add( GO_TO_BOOKMARK_ROTATION, GO_TO_BOOKMARK_ROTATION_KEYS, "Retrieve a labeled bookmark, set only the orientation." );
//
//			descriptions.add( PREFERENCES_DIALOG, PREFERENCES_DIALOG_KEYS, "Show the Preferences dialog." );
//		}
//	}

	public static void installViewerActions(
			Actions actions,
			final BigWarpViewerFrame bwFrame,
			final BigWarp< ? > bw )
	{

		final InputActionBindings inputActionBindings = bwFrame.getKeybindings();
		System.out.println( "install viewer actions" );
		actions.install( inputActionBindings, "bw" );

		actions.runnableAction( () -> { bw.getBwTransform().transformToString(); }, PRINT_TRANSFORM, PRINT_TRANSFORM_KEYS);
		actions.runnableAction( bw::toggleInLandmarkMode, TOGGLE_LANDMARK_MODE, TOGGLE_LANDMARK_MODE_KEYS);
		actions.runnableAction( bw::toggleMovingImageDisplay, TOGGLE_MOVING_IMAGE_DISPLAY, TOGGLE_MOVING_IMAGE_DISPLAY_KEYS );

		// navigation
		actions.runnableAction( bw::resetView, RESET_VIEWER, RESET_VIEWER_KEYS);
		actions.runnableAction( bw::matchOtherViewerPanelToActive, ALIGN_OTHER_TO_ACTIVE, ALIGN_OTHER_TO_ACTIVE_KEYS );
		actions.runnableAction( bw::matchActiveViewerPanelToOther, ALIGN_ACTIVE_TO_OTHER, ALIGN_ACTIVE_TO_OTHER_KEYS );
		actions.runnableAction( bw::warpToSelectedLandmark, WARP_TO_SELECTED_POINT, WARP_TO_SELECTED_POINT_KEYS );
		actions.runnableAction( bw::warpToNearestLandmark, WARP_TO_NEAREST_POINT, WARP_TO_NEAREST_POINT_KEYS );
		actions.runnableAction( bw::warpToNextLandmark, WARP_TO_NEXT_POINT, WARP_TO_NEXT_POINT_KEYS );
		actions.runnableAction( bw::warpToPrevLandmark, WARP_TO_PREV_POINT, WARP_TO_PREV_POINT_KEYS );

		// bookmarks
		actions.runnableAction( bw::goToBookmark, GO_TO_BOOKMARK, GO_TO_BOOKMARK_KEYS );
		actions.runnableAction( bw::goToBookmarkRotation, GO_TO_BOOKMARK_ROTATION, GO_TO_BOOKMARK_ROTATION_KEYS );
		actions.runnableAction( bw::setBookmark, SET_BOOKMARK, SET_BOOKMARK_KEYS );

		// cards
		actions.runnableAction( bwFrame::expandAndFocusCardPanel, EXPAND_CARDS, EXPAND_CARDS_KEYS );
		actions.runnableAction( bwFrame::collapseCardPanel, COLLAPSE_CARDS, COLLAPSE_CARDS_KEYS );

		// export 
		actions.runnableAction( bw::exportWarpField, EXPORT_WARP, EXPORT_WARP_KEYS );
		actions.runnableAction( () -> { bw.getBwTransform().printAffine(); }, EXPORT_AFFINE, EXPORT_AFFINE_KEYS );
		
		// dialogs
		actions.namedAction( new ToggleDialogAction( SHOW_HELP, bw.helpDialog ), SHOW_HELP_KEYS );
		actions.namedAction( new ToggleDialogAction( VISIBILITY_AND_GROUPING_MVG, bw.activeSourcesDialogP ), VISIBILITY_AND_GROUPING_MVG_KEYS );
		actions.namedAction( new ToggleDialogAction( VISIBILITY_AND_GROUPING_TGT, bw.activeSourcesDialogQ ), VISIBILITY_AND_GROUPING_TGT_KEYS );
		actions.namedAction( new ToggleDialogAction( SHOW_WARPTYPE_DIALOG, bw.warpVisDialog ), SHOW_WARPTYPE_DIALOG_KEYS );
		actions.namedAction( new ToggleDialogAction( PREFERENCES_DIALOG, bw.preferencesDialog ), PREFERENCES_DIALOG_KEYS );

		// landmarks 
		actions.runnableAction( bw::loadLandmarks, LOAD_LANDMARKS, LOAD_LANDMARKS_KEYS );
		actions.runnableAction( bw::saveLandmarks, SAVE_LANDMARKS, SAVE_LANDMARKS_KEYS );
		actions.runnableAction( bw::quickSaveLandmarks, QUICK_SAVE_LANDMARKS, QUICK_SAVE_LANDMARKS_KEYS );

		actions.namedAction( new UndoRedoAction( UNDO, bw ), UNDO_KEYS );
		actions.namedAction( new UndoRedoAction( REDO, bw ), REDO_KEYS );

	}

	public static void installTableActions(
			Actions actions,
			final InputActionBindings inputActionBindings,
			final BigWarp< ? > bw )
	{
		actions.install( inputActionBindings, "bw" );

		// landmarks 
		actions.runnableAction( bw::loadLandmarks, LOAD_LANDMARKS, LOAD_LANDMARKS_KEYS );
		actions.runnableAction( bw::saveLandmarks, SAVE_LANDMARKS, SAVE_LANDMARKS_KEYS );
		actions.runnableAction( bw::quickSaveLandmarks, QUICK_SAVE_LANDMARKS, QUICK_SAVE_LANDMARKS_KEYS );

		actions.runnableAction( () -> { bw.getLandmarkPanel().getJTable().selectAll(); }, LANDMARK_SELECT_ALL, LANDMARK_SELECT_ALL_KEYS );
		actions.runnableAction( () -> { bw.getLandmarkPanel().getJTable().clearSelection(); }, LANDMARK_DESELECT_ALL, LANDMARK_DESELECT_ALL_KEYS );

		actions.namedAction( bw.landmarkPopupMenu.deleteSelectedHandler, LANDMARK_DELETE_SELECTED_KEYS );

	}

	/**
	 * Create BigWarp actions and install them in the specified
	* {@link InputActionBindings}.
	 *
	 * @param inputActionBindings
	 *            {@link InputMap} and {@link ActionMap} are installed here.
	 * @param bw
	 *            Actions are targeted at this {@link BigWarp}.
	 * @param keyProperties
	 *            user-defined key-bindings.
	 */
	public static void installActionBindings(
			final InputActionBindings inputActionBindings,
			final BigWarp< ? > bw,
			final KeyStrokeAdder.Factory keyProperties )
	{
		inputActionBindings.addActionMap( "bw", createActionMap( bw ) );
		inputActionBindings.addInputMap( "bw", createInputMap( keyProperties ) );

		inputActionBindings.addActionMap( "bwV", createActionMapViewer( bw ) );
		inputActionBindings.addInputMap( "bwv", createInputMapViewer( keyProperties ) );
	}
	
	public static void installLandmarkPanelActionBindings(
			final InputActionBindings inputActionBindings,
			final BigWarp< ? > bw,
			final JTable landmarkTable,
			final KeyStrokeAdder.Factory keyProperties )
	{
		inputActionBindings.addActionMap( "bw", createActionMap( bw ) );
		inputActionBindings.addInputMap( "bw", createInputMap( keyProperties ) );
		
		TableCellEditor celled = landmarkTable.getCellEditor( 0, 1 );
		Component c = celled.getTableCellEditorComponent(landmarkTable, Boolean.TRUE, true, 0, 1 );

		InputMap parentInputMap = ((JCheckBox)c).getInputMap().getParent();
		parentInputMap.clear();
		KeyStroke enterDownKS = KeyStroke.getKeyStroke("pressed ENTER" );
		KeyStroke enterUpKS = KeyStroke.getKeyStroke("released ENTER" );

		parentInputMap.put( enterDownKS, "pressed" );
		parentInputMap.put(   enterUpKS, "released" );
	}

	public static InputMap createInputMapViewer( final KeyStrokeAdder.Factory keyProperties )
	{
		final InputMap inputMap = new InputMap();
		final KeyStrokeAdder map = keyProperties.keyStrokeAdder( inputMap );

		map.put(RESET_VIEWER, "R");
		
		map.put( String.format( VISIBILITY_AND_GROUPING, "moving" ), "F3" );
		map.put( String.format( VISIBILITY_AND_GROUPING, "target" ), "F4" );
		map.put( "transform type", "F2" );
		
		map.put( String.format( ALIGN_VIEW_TRANSFORMS, AlignViewerPanelAction.TYPE.OTHER_TO_ACTIVE ), "Q" );
		map.put( String.format( ALIGN_VIEW_TRANSFORMS, AlignViewerPanelAction.TYPE.ACTIVE_TO_OTHER ), "W" );

		map.put( TOGGLE_MOVING_IMAGE_DISPLAY, "T" );

		map.put( WARP_TO_SELECTED_POINT, "D" );
		map.put( String.format( WARP_TO_NEXT_POINT, true), "ctrl D" );
		map.put( String.format( WARP_TO_NEXT_POINT, false), "ctrl shift D" );
		map.put( WARP_TO_NEAREST_POINT, "E" );

		map.put( EXPORT_WARP, "ctrl W" );
		map.put( EXPORT_AFFINE, "ctrl A" );

		map.put( GO_TO_BOOKMARK, "B" );
		map.put( GO_TO_BOOKMARK_ROTATION, "O" );
		map.put( SET_BOOKMARK, "shift B" );

		return inputMap;
	}

	public static ActionMap createActionMapViewer( final BigWarp< ? > bw )
	{
		final ActionMap actionMap = new ActionMap();

		new ToggleDialogAction( String.format( VISIBILITY_AND_GROUPING, "moving" ), bw.activeSourcesDialogP ).put( actionMap );
		new ToggleDialogAction( String.format( VISIBILITY_AND_GROUPING, "target" ), bw.activeSourcesDialogQ ).put( actionMap );
		new ToggleDialogAction( "transform type", bw.transformSelector ).put( actionMap );

		for( final BigWarp.WarpVisType t: BigWarp.WarpVisType.values())
		{
			new SetWarpVisTypeAction( t, bw ).put( actionMap );
			new SetWarpVisTypeAction( t, bw, bw.getViewerFrameP() ).put( actionMap );
			new SetWarpVisTypeAction( t, bw, bw.getViewerFrameQ() ).put( actionMap );
		}

		new ResetActiveViewerAction( bw ).put( actionMap );
		new AlignViewerPanelAction( bw, AlignViewerPanelAction.TYPE.ACTIVE_TO_OTHER ).put( actionMap );
		new AlignViewerPanelAction( bw, AlignViewerPanelAction.TYPE.OTHER_TO_ACTIVE ).put( actionMap );
		new WarpToSelectedAction( bw ).put( actionMap );
		new WarpToNextAction( bw, true ).put( actionMap );
		new WarpToNextAction( bw, false ).put( actionMap );
		new WarpToNearest( bw ).put( actionMap );

		for( final GridSource.GRID_TYPE t : GridSource.GRID_TYPE.values())
			new SetWarpVisGridTypeAction( String.format( WARPVISGRID, t.name()), bw, t ).put( actionMap );

		new SetBookmarkAction( bw ).put( actionMap );
		new GoToBookmarkAction( bw ).put( actionMap );
		new GoToBookmarkRotationAction( bw ).put( actionMap );

		new SaveSettingsAction( bw ).put( actionMap );
		new LoadSettingsAction( bw ).put( actionMap );

		return actionMap;
	}

	public static InputMap createInputMap( final KeyStrokeAdder.Factory keyProperties )
	{
		final InputMap inputMap = new InputMap();
		final KeyStrokeAdder map = keyProperties.keyStrokeAdder( inputMap );

		map.put( SHOW_WARPTYPE_DIALOG, "U" );
		map.put( TOGGLE_LANDMARK_MODE, "SPACE" );

		map.put( BRIGHTNESS_SETTINGS, "S" );
		map.put( SHOW_HELP, "F1", "H" );

		map.put( TOGGLE_POINTS_VISIBLE, "V" );
		map.put( TOGGLE_POINT_NAMES_VISIBLE, "N" );
		map.put( ESTIMATE_WARP, "C" );

		map.put( UNDO, "control Z" );
		map.put( REDO, "control Y" );
		map.put( REDO, "control shift Z" );

		map.put( SAVE_LANDMARKS, "control S" );
		map.put( QUICK_SAVE_LANDMARKS, "control Q" );
		map.put( LOAD_LANDMARKS, "control O" );

		map.put( EXPORT_IP, "control E" );
//		map.put( SAVE_WARPED, "control alt shift E" );
		map.put( SAVE_WARPED_XML, "control shift E" );

//		map.put( LandmarkPointMenu.CLEAR_SELECTED_MOVING, "BACK_SPACE" );
//		map.put( LandmarkPointMenu.CLEAR_SELECTED_FIXED, "control BACK_SPACE" );
//		map.put( LandmarkPointMenu.DELETE_SELECTED, "DELETE" );

		map.put(  String.format( SELECT_TABLE_ROWS, -1 ), "shift ESCAPE" );

		map.put( TOGGLE_BOX_AND_TEXT_OVERLAY_VISIBLE, "F8" );
		map.put( GARBAGE_COLLECTION, "F9" );
		map.put( PRINT_TRANSFORM, "control shift T" );
		map.put( DEBUG, "F11" );

		return inputMap;
	}

	public static ActionMap createActionMap( final BigWarp< ? > bw )
	{
		final ActionMap actionMap = new ActionMap();

		/*
		 * The below two lines with ui-behavior-1.6.- or so
		 */
//		new LandmarkModeAction( LANDMARK_MODE_ON, bw, true ).put( actionMap );
//		new LandmarkModeAction( LANDMARK_MODE_OFF, bw, false ).put( actionMap );

//		new ToggleLandmarkModeAction( LANDMARK_MODE_ON, bw ).put( actionMap );
//		new ToggleLandmarkModeAction( LANDMARK_MODE_OFF, bw ).put( actionMap );


//		bw.landmarkPopupMenu.deleteSelectedHandler.put( actionMap );
//		bw.landmarkPopupMenu.activateAllHandler.put( actionMap );
//		bw.landmarkPopupMenu.deactivateAllHandler.put( actionMap );
//
//		bw.landmarkPopupMenu.clearAllMoving.put( actionMap );
//		bw.landmarkPopupMenu.clearAllFixed.put( actionMap );

		new ToggleLandmarkModeAction( TOGGLE_LANDMARK_MODE, bw ).put( actionMap );

		new ToggleDialogAction( SHOW_WARPTYPE_DIALOG, bw.warpVisDialog ).put( actionMap );

		new ToggleDialogAction( BRIGHTNESS_SETTINGS, bw.brightnessDialog ).put( actionMap );
		new ToggleDialogAction( SHOW_HELP, bw.helpDialog ).put( actionMap );
		new ToggleDialogAction( SHOW_SOURCE_INFO, bw.sourceInfoDialog ).put( actionMap );

		new SaveWarpedAction( bw ).put( actionMap );
		new SaveWarpedXmlAction( bw ).put( actionMap );
		new ExportImagePlusAction( bw ).put( actionMap );
		new ExportWarpAction( bw ).put( actionMap );
		new ExportAffineAction( bw ).put( actionMap );

		new LoadLandmarksAction( bw ).put( actionMap );
		new SaveLandmarksAction( bw ).put( actionMap );
		new QuickSaveLandmarksAction( bw ).put( actionMap );

		new LandmarkGridDialogAction( bw ).put( actionMap );

		new TogglePointsVisibleAction( TOGGLE_POINTS_VISIBLE, bw ).put( actionMap );
		new TogglePointNameVisibleAction( TOGGLE_POINT_NAMES_VISIBLE, bw ).put( actionMap );
		new ToggleBoxAndTexOverlayVisibility( TOGGLE_BOX_AND_TEXT_OVERLAY_VISIBLE, bw ).put( actionMap );
		new ToggleMovingImageDisplayAction( TOGGLE_MOVING_IMAGE_DISPLAY, bw ).put( actionMap );
		new EstimateWarpAction( ESTIMATE_WARP, bw ).put( actionMap );

		for( int i = 0; i < bw.baseXfmList.length; i++ ){
			final AbstractModel<?> xfm = bw.baseXfmList[ i ];
			new SetWarpMagBaseAction( String.format( WARPMAG_BASE, xfm.getClass().getName()), bw, i ).put( actionMap );
		}

		new UndoRedoAction( UNDO, bw ).put( actionMap );
		new UndoRedoAction( REDO, bw ).put( actionMap );

		new TableSelectionAction( String.format( SELECT_TABLE_ROWS, -1 ), bw.getLandmarkPanel().getJTable(), -1 ).put( actionMap );

		new GarbageCollectionAction( GARBAGE_COLLECTION ).put( actionMap );
		new DebugAction( DEBUG, bw ).put( actionMap );
		new PrintTransformAction( PRINT_TRANSFORM, bw ).put( actionMap );

		return actionMap;
	}

	public static class UndoRedoAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -5413579107763110117L;

		private BigWarp< ? > bw;
		private boolean isRedo;

		public UndoRedoAction( final String name, BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
			
			isRedo = false;

			if ( name.equals( REDO ) )
				isRedo = true;

		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( bw.isInLandmarkMode() )
			{
				bw.message.showMessage( "Undo/Redo not allowed in landmark mode" );
				return;
			}

			// I would love for this check to work instead of using a try-catch
			// bug it doesn't seem to be consistent
//			if( isRedo && manager.canRedo() ){
			try { 

				if( isRedo )
				{
					bw.getLandmarkPanel().getTableModel().getUndoManager().redo();
					bw.message.showMessage( "Redo" );
				}
				else
				{
					//			} else if( manager.canUndo() ) {
//					bw.getLandmarkPanel().getTableModel().getUndoManager().
					bw.getLandmarkPanel().getTableModel().getUndoManager().undo();
					bw.message.showMessage( "Undo" );
				}

				/*
				 * Keep the stuff below in the try-catch block to avoid unnecessary calls
				 * if there is nothing to undo/redo
				 */
				if( this.bw.updateWarpOnPtChange )
					this.bw.restimateTransformation();

				// repaint
				this.bw.getLandmarkPanel().repaint();
			}
			catch( Exception ex )
			{
				if( isRedo )
				{
					bw.message.showMessage("Can't redo");
				}
				else
				{
					bw.message.showMessage("Can't undo");
				}
				//System.err.println( " Undo / redo error, or nothing to do " );
				//ex.printStackTrace();
			}
		}
	}

	public static class LandmarkModeAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 4079013525930019558L;

		private BigWarp< ? > bw;

		private final boolean isOn;

		public LandmarkModeAction( final String name, final BigWarp< ? > bw, final boolean on )
		{
			super( name );
			this.bw = bw;
			this.isOn = on;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
//			System.out.println( "LM MODE : " + isOn );
			bw.setInLandmarkMode( isOn );
		}
	}

	public static class ToggleLandmarkModeAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 234323425930019L;

		private BigWarp< ? > bw;

		public ToggleLandmarkModeAction( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
//			System.out.println( "TOGGLE LM MODE" );
			bw.setInLandmarkMode( !bw.inLandmarkMode );
		}
	}

	public static class ToggleAlwaysEstimateTransformAction extends AbstractNamedAction 
	{
		private static final long serialVersionUID = 2909830484701853577L;

		private BigWarpViewerFrame bwvp;

		public ToggleAlwaysEstimateTransformAction( final String name, final BigWarpViewerFrame bwvp )
		{
			super( name );
			this.bwvp = bwvp;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bwvp.getViewerPanel().toggleUpdateOnDrag();
		}
	}

	public static class GarbageCollectionAction extends AbstractNamedAction 
	{
		private static final long serialVersionUID = -4487441057212703143L;

		public GarbageCollectionAction( final String name )
		{
			super( name );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			System.out.println( "GARBAGE COLLECTION" );
			System.gc();
		}
	}
	
	public static class PrintTransformAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 6065343788485350279L;

		private BigWarp< ? > bw;
 
		public PrintTransformAction( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.getBwTransform().transformToString();
		}
	}
	public static class DebugAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 7408679512565343805L;

		private BigWarp< ? > bw;

		public DebugAction( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			System.out.println( "Debug" );

//			System.out.println( "viewerP is Transformed: " + bw.isMovingDisplayTransformed() );
//			LandmarkTableModel ltm = this.bw.getLandmarkPanel().getTableModel();
//			 ltm.printState();
			// ltm.validateTransformPoints();

			// System.out.println( ltm.getChangedSinceWarp() );
//			 System.out.println( ltm.getWarpedPoints() );
//			ltm.printWarpedPoints();
			
			AffineTransform3D xfm = new AffineTransform3D();
			bw.viewerP.state().getViewerTransform( xfm );
			System.out.println( "mvg xfm " + xfm  + "   DET = " + BigWarpUtils.det( xfm ));

			bw.viewerQ.state().getViewerTransform( xfm );
			System.out.println( "tgt xfm " + xfm + "   DET = " + BigWarpUtils.det( xfm ));

			BigWarpData< ? > data = bw.getData();
			for( int mi : data.movingSourceIndices )
			{
				((SourceAndConverter<?>)data.sources.get( mi )).getSpimSource().getSourceTransform( 0, 0, xfm );
				System.out.println( "mvg src xfm " + xfm  );
			}

			for( int ti : data.targetSourceIndices )
			{
				((SourceAndConverter<?>)data.sources.get( ti )).getSpimSource().getSourceTransform( 0, 0, xfm );
				System.out.println( "tgt src xfm " + xfm  );
			}


			System.out.println( " " );
		}
	}
	
	public static class EstimateWarpAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -210012348709096037L;

		private BigWarp< ? > bw;

		public EstimateWarpAction( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.restimateTransformation();
		}
	}
	
	public static class ToggleMovingImageDisplayAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 6495981071796613953L;
		
		private BigWarp< ? > bw;
		
		public ToggleMovingImageDisplayAction( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.toggleMovingImageDisplay();
		}
	}
	
	public static class TogglePointNameVisibleAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 2639535533224809586L;

		private BigWarp< ? > bw;

		public TogglePointNameVisibleAction( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.toggleNameVisibility();	
		}
	}

	public static class ToggleBoxAndTexOverlayVisibility extends AbstractNamedAction
	{
		private static final long serialVersionUID = -900781969157241037L;

		private BigWarp< ? > bw;

		public ToggleBoxAndTexOverlayVisibility( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.getViewerFrameP().getViewerPanel().toggleBoxOverlayVisible();
			bw.getViewerFrameQ().getViewerPanel().toggleBoxOverlayVisible();
			bw.getViewerFrameP().getViewerPanel().toggleTextOverlayVisible();
			bw.getViewerFrameQ().getViewerPanel().toggleTextOverlayVisible();
			bw.getViewerFrameP().repaint();
			bw.getViewerFrameQ().repaint();
		}
	}

	public static class TogglePointsVisibleAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 8747830204501341125L;
		private BigWarp< ? > bw;
		
		public TogglePointsVisibleAction( final String name, final BigWarp< ? > bw )
		{
			super( name );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.togglePointVisibility();	
		}
	}
	
	public static class ResetActiveViewerAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -130575800163574517L;
		
		private BigWarp< ? > bw;
		
		public ResetActiveViewerAction( final BigWarp< ? > bw )
		{
			super( String.format( RESET_VIEWER ) );
			this.bw = bw;
		}
		
		public void actionPerformed( ActionEvent e )
		{
			bw.resetView();
		}
	}
	
	public static class AlignViewerPanelAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -7023242695323421450L;
		
		public enum TYPE { ACTIVE_TO_OTHER, OTHER_TO_ACTIVE };
		
		private BigWarp< ? >bw;
		private TYPE type;
		
		public AlignViewerPanelAction( final BigWarp< ? > bw, TYPE type )
		{
			super( String.format( ALIGN_VIEW_TRANSFORMS, type ) );
			this.bw = bw;
			this.type = type;
		}
		
		public void actionPerformed( ActionEvent e )
		{
			if( type == TYPE.ACTIVE_TO_OTHER )
				bw.matchActiveViewerPanelToOther();
			else
				bw.matchOtherViewerPanelToActive();
		}
	}

	public static class SetWarpMagBaseAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 7370813069619338918L;
		
		private BigWarp< ? > bw;
		private int i;
		
		public SetWarpMagBaseAction( final String name, final BigWarp< ? > bw, int i )
		{
			super( name );
			this.bw = bw;
			this.i = i;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.setWarpMagBaselineIndex( i );
		}
	}
	
	public static class SetWarpVisGridTypeAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 7370813069619338918L;
		
		private final BigWarp< ? > bw;
		private final GridSource.GRID_TYPE type;
		
		public SetWarpVisGridTypeAction( final String name, final BigWarp< ? > bw, final GridSource.GRID_TYPE type )
		{
			super( name );
			this.bw = bw;
			this.type = type;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.setWarpVisGridType( type );
		}
	}
	
	public static class SetWarpVisTypeAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 7370813069619338918L;
		
		private BigWarp< ? > bw;
		private BigWarpViewerFrame p;
		private BigWarp.WarpVisType type;
		
		public SetWarpVisTypeAction( final BigWarp.WarpVisType type, final BigWarp< ? > bw )
		{
			this( type, bw, null );
		}
		
		public SetWarpVisTypeAction( final BigWarp.WarpVisType type, final BigWarp< ? > bw, BigWarpViewerFrame p )
		{
			super( getName( type, p ));
			this.bw = bw;
			this.p = p;
			this.type = type;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( p == null )
				bw.setWarpVisMode( type, p, true );
			else
				bw.setWarpVisMode( type, p, false );
		}
		
		public static String getName( final BigWarp.WarpVisType type, BigWarpViewerFrame p )
		{
			if( p == null )
				return String.format( SET_WARPTYPE_VIS, type.name() );
			else if( p.isMoving() )
				return String.format( SET_WARPTYPE_VIS_P, type.name() );
			else
				return String.format( SET_WARPTYPE_VIS_Q, type.name() );
		}
	}
	
	public static class TableSelectionAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -4647679094757721276L;

		private final JTable table;
		private final int selection;

		public TableSelectionAction( final String name, JTable table, int selection )
		{
			super( name );
			this.table = table;
			this.selection = selection;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if ( selection < 0 || selection >= table.getRowCount() )
				table.removeRowSelectionInterval( 0, table.getRowCount() - 1 );
			else
				table.setRowSelectionInterval( selection, selection );
		}
	}

	public static class SetBookmarkAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -4060308986781809606L;
		BigWarp< ? > bw;

		public SetBookmarkAction( final BigWarp< ? > bw )
		{
			super( SET_BOOKMARK );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( bw.getViewerFrameP().isActive() )
				bw.bookmarkEditorP.initSetBookmark();
			else if ( bw.getViewerFrameQ().isActive() )
				bw.bookmarkEditorQ.initSetBookmark();
		}

	}

	public static class GoToBookmarkAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 8777199828772379323L;
		BigWarp< ? > bw;

		public GoToBookmarkAction( final BigWarp< ? > bw )
		{
			super( GO_TO_BOOKMARK );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			bw.goToBookmark();
		}
	}

	public static class GoToBookmarkRotationAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -6169895035295179820L;
		BigWarp< ? > bw;

		public GoToBookmarkRotationAction( final BigWarp< ? > bw )
		{
			super( GO_TO_BOOKMARK_ROTATION );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( bw.getViewerFrameP().isActive() )
				bw.bookmarkEditorP.initGoToBookmarkRotation();
			else if ( bw.getViewerFrameP().isActive() )
				bw.bookmarkEditorQ.initGoToBookmarkRotation();
		}
	}

	public static class SaveSettingsAction extends AbstractNamedAction
	{
		BigWarp< ? > bw;
		public SaveSettingsAction( final BigWarp< ? > bw )
		{
			super( SAVE_SETTINGS );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			bw.saveSettings();
		}

		private static final long serialVersionUID = 1L;
	}

	public static class LoadSettingsAction extends AbstractNamedAction
	{
		BigWarp< ? > bw;
		public LoadSettingsAction( final BigWarp< ? > bw )
		{
			super( LOAD_SETTINGS );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			bw.loadSettings();
		}

		private static final long serialVersionUID = 1L;
	}

	public static class WarpToSelectedAction extends AbstractNamedAction
	{
		final BigWarp< ? > bw;

		public WarpToSelectedAction( final BigWarp< ? > bw )
		{
			super( WARP_TO_SELECTED_POINT );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			int[] selectedRows =  bw.getLandmarkPanel().getJTable().getSelectedRows();

			int row = 0;
			if( selectedRows.length > 0 )
				row = selectedRows[ 0 ];

			if( bw.getViewerFrameP().isActive() )
				bw.warpToLandmark( row, bw.getViewerFrameP().getViewerPanel() );
			else
				bw.warpToLandmark( row, bw.getViewerFrameQ().getViewerPanel() );
		}

		private static final long serialVersionUID = 5233843444920094805L;
	}

	public static class WarpToNearest extends AbstractNamedAction
	{
		final BigWarp< ? > bw;
		public WarpToNearest( final BigWarp< ? > bw )
		{
			super( WARP_TO_NEAREST_POINT );
			this.bw = bw;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( bw.getViewerFrameP().isActive() )
				bw.warpToNearest( bw.getViewerFrameP().getViewerPanel() );
			else
				bw.warpToNearest( bw.getViewerFrameQ().getViewerPanel() );
		}
		private static final long serialVersionUID = 3244181492305479433L;
	}

	public static class WarpToNextAction extends AbstractNamedAction
	{
		final BigWarp< ? > bw;
		final int inc;

		public WarpToNextAction( final BigWarp< ? > bw, boolean fwd )
		{
			super( String.format( WARP_TO_NEXT_POINT, fwd) );
			this.bw = bw;
			if( fwd )
				inc = 1;
			else
				inc = -1;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			int[] selectedRows =  bw.getLandmarkPanel().getJTable().getSelectedRows();

			int row = 0;
			if( selectedRows.length > 0 )
				row = selectedRows[ selectedRows.length - 1 ];

			row = row + inc; // increment to get the *next* row

			// wrap to start if necessary
			if( row >= bw.getLandmarkPanel().getTableModel().getRowCount() )
				row = 0;
			else if( row < 0 )
				row = bw.getLandmarkPanel().getTableModel().getRowCount() - 1;

			// select new row
			bw.getLandmarkPanel().getJTable().setRowSelectionInterval( row, row );

			if( bw.getViewerFrameP().isActive() )
			{
				bw.warpToLandmark( row, bw.getViewerFrameP().getViewerPanel() );
			}
			else
			{
				bw.warpToLandmark( row, bw.getViewerFrameQ().getViewerPanel() );
			}
		}
		private static final long serialVersionUID = 8515568118251877405L;
	}

	public static class LoadLandmarksAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -5405137757290988030L;
		BigWarp< ? > bw;
		public LoadLandmarksAction( final BigWarp< ? > bw )
		{
			super( LOAD_LANDMARKS );
			this.bw = bw;
		}
		@Override
		public void actionPerformed( ActionEvent e )
		{
			System.out.println("load landmarks");
			bw.loadLandmarks();
		}
	}

	public static class QuickSaveLandmarksAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -4761309639234262643L;
		BigWarp< ? > bw;
		public QuickSaveLandmarksAction( final BigWarp< ? > bw )
		{
			super( QUICK_SAVE_LANDMARKS );
			this.bw = bw;
		}
		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.quickSaveLandmarks();
		}
	}

	public static class SaveLandmarksAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 7897687176745034315L;
		BigWarp< ? > bw;
		public SaveLandmarksAction( final BigWarp< ? > bw )
		{
			super( SAVE_LANDMARKS );
			this.bw = bw;
		}
		@Override
		public void actionPerformed( ActionEvent e )
		{
			bw.saveLandmarks();
		}
	}

	public static class ExportImagePlusAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -8109832912959931917L;
		BigWarp< ? > bw;
		public ExportImagePlusAction( final BigWarp< ? > bw )
		{
			super( EXPORT_IP );
			this.bw = bw;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			bw.exportAsImagePlus( false );
		}
	}
	
	public static class ExportWarpAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 4626378501415886468L;
		BigWarp< ? > bw;
		public ExportWarpAction( final BigWarp< ? > bw )
		{
			super( EXPORT_WARP );
			this.bw = bw;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			bw.exportWarpField();
		}
	}

	public static class ExportAffineAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 9190515918045510236L;
		BigWarp< ? > bw;
		public ExportAffineAction( final BigWarp< ? > bw )
		{
			super( EXPORT_AFFINE );
			this.bw = bw;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			bw.getBwTransform().printAffine();
		}
	}

	@Deprecated
	public static class SaveWarpedAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 4965249994677649713L;

		BigWarp< ? > bw;
		public SaveWarpedAction( final BigWarp< ? > bw )
		{
			super( SAVE_WARPED );
			this.bw = bw;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			bw.saveMovingImageToFile();
		}
	}

	public static class SaveWarpedXmlAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = -5437508072904256758L;

		BigWarp< ? > bw;
		public SaveWarpedXmlAction( final BigWarp< ? > bw )
		{
			super( SAVE_WARPED_XML );
			this.bw = bw;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			bw.saveMovingImageXml();
		}
	}

	public static class LandmarkGridDialogAction extends AbstractNamedAction
	{
		private static final long serialVersionUID = 1L;
		BigWarp< ? > bw;

		public LandmarkGridDialogAction( final BigWarp< ? > bw )
		{
			super( LANDMARK_GRID_DIALOG );
			this.bw = bw;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			System.out.println( "LandmarkGridGenerator.fillFromDialog( bw )" );
			LandmarkGridGenerator.fillFromDialog( bw );
		}
	}
	
//	public synchronized void discoverCommandDescriptions()
//	{
//		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
//		final Context context = new Context( PluginService.class );
//		context.inject( builder );
//		builder.discoverProviders( KeyConfigScopes.BIGDATAVIEWER );
//		builder.discoverProviders( "bw" );
//		context.dispose();
//		setCommandDescriptions( builder.build() );
//	}
}
