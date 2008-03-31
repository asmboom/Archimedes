/*
 * Created on 27/03/2006
 */

package br.org.archimedes.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.org.archimedes.Utils;
import br.org.archimedes.exceptions.IllegalActionException;
import br.org.archimedes.exceptions.NoActiveDrawingException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.factories.CommandFactory;
import br.org.archimedes.factories.QuickMoveFactory;
import br.org.archimedes.gui.model.Workspace;
import br.org.archimedes.interfaces.Command;
import br.org.archimedes.model.Drawing;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Layer;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Rectangle;
import br.org.archimedes.model.ReferencePoint;
import br.org.archimedes.model.Selection;

/**
 * Belongs to package br.org.archimedes.controller.
 */
public class Controller {

	private static Controller instance;

	private Drawing activeDrawing;

	private int defaultDrawingNumber = 1;

	public static Controller getInstance() {

		if (instance == null) {
			instance = new Controller();
		}
		return instance;
	}

	private Controller() {

		// Empty constructor
	}

	/**
	 * Opens the given drawing.
	 * 
	 * @param drawing
	 */
	public void openDrawing(Drawing drawing) {

		setActiveDrawing(drawing);
	}

	/**
	 * Checks whether there is an active drawing.
	 * 
	 * @return true if there is an active drawing, false otherwise.
	 */
	public boolean isThereActiveDrawing() {

		return (activeDrawing != null);
	}

	/**
	 * @return The current drawing being edited
	 * @throws NoActiveDrawingException
	 *             In case there is no active drawing.
	 */
	public Drawing getActiveDrawing() throws NoActiveDrawingException {

		if (activeDrawing == null) {
			throw new NoActiveDrawingException();
		}
		return activeDrawing;
	}

	/**
	 * Sets the current drawing. Also sets the Workspace.getInstance() viewport
	 * to this drawing's viewport.
	 * 
	 * @param drawing
	 *            The drawing to be set active.
	 */
	public void setActiveDrawing(Drawing drawing) {

		activeDrawing = drawing;
		if (drawing != null) {
			try {
				Workspace.getInstance().setViewport(
						drawing.getViewportPosition(), drawing.getZoom());
			} catch (NullArgumentException e) {
				System.err.println("NullArgumentException caught."); //$NON-NLS-1$
				e.printStackTrace();
			} catch (NoActiveDrawingException e) {
				// Should not reach this code
				// (since I'm setting the active drawing to not null)
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return The number of the next drawing number to be used.
	 */
	public int fetchAndAddDrawingNumber() {

		return defaultDrawingNumber++;
	}

	/**
	 * Executes all the commands in the current drawing
	 * 
	 * @param commands
	 *            A list of commands to be executed
	 * @throws NoActiveDrawingException
	 *             Thrown if there is no active drawing.
	 * @throws IllegalActionException
	 *             Thrown if the action is not legal
	 */
	public void execute(List<Command> commands)
			throws NoActiveDrawingException, IllegalActionException {

		Drawing drawing = getActiveDrawing();
		drawing.execute(commands);
		// TODO Atualizar o desenho.
	}

	/**
	 * Copy the contents of selection to the clipboard.
	 * 
	 * @param selection
	 *            the selection
	 */
	public void copyToClipboard(Set<Element> selection) {

		Workspace.getInstance().getClipboard().clear();

		for (Element element : selection) {
			// For now, elements that go to the clipboard have no layer.
			Element cloned = element.clone();
			Layer layerClone = element.getLayer().clone();
			cloned.setLayer(layerClone);
			Workspace.getInstance().getClipboard().add(cloned);
		}
	}

	/**
	 * @param point
	 *            The point used to select the element
	 * @param name
	 *            The class or interface the element must be
	 * @return The selected element or null if the selection was invalid
	 * @throws NoActiveDrawingException
	 *             In case there is no active drawing
	 */
	public Element getElementUnder(Point point, Class<?> name)
			throws NoActiveDrawingException {

		Collection<Element> elementsUnder = getElementsUnder(point, name);
		Element element = null;
		if (!elementsUnder.isEmpty()) {
			element = elementsUnder.iterator().next();
		}
		return element;

	}

	/**
	 * @param point
	 *            The point used to select the element
	 * @param name
	 *            The class or interface the element must be
	 * @return The selected elements or an empty list if the selection was
	 *         invalid
	 * @throws NoActiveDrawingException
	 *             In case there is no active drawing
	 */
	public Collection<Element> getElementsUnder(Point point, Class<?> name)
			throws NoActiveDrawingException {

		Collection<Element> elements = new ArrayList<Element>();
		Drawing drawing = getActiveDrawing();
		double delta = Workspace.getInstance().getSelectionSize() / 2.0;
		delta = Workspace.getInstance().screenToModel(delta);
		Point a = new Point(point.getX() - delta, point.getY() - delta);
		Point b = new Point(point.getX() + delta, point.getY() + delta);

		Rectangle rect = new Rectangle(a.getX(), a.getY(), b.getX(), b.getY());

		Set<Element> selected = null;
		try {
			selected = drawing.getSelectionIntersection(rect);
		} catch (NullArgumentException e) {
			// Should not happen because I just created this rectangle
			e.printStackTrace();
		}

		if (selected != null && !selected.isEmpty()) {
			for (Element element : selected) {
				if ( Utils.isSubclassOf(element, name) || Utils.isInterfaceOf(element, name)) {
					elements.add(element);
				}
			}
		}

		return elements;
	}

	/**
	 * Selects the elements by point (adds them to the current selection).
	 * 
	 * @param point
	 *            The point to find the intersected elements.
	 * @param invertSelection
	 *            If true, the selection isn't cleared, but inverted.
	 * @return False if no elements were selected; true otherwise.
	 * @throws NullArgumentException
	 *             In case somethig goes very wrong.
	 * @throws NoActiveDrawingException
	 *             In case there's no active drawing
	 */
	public boolean select(Point point, boolean invertSelection)
			throws NullArgumentException, NoActiveDrawingException {

		double delta = Workspace.getInstance().getSelectionSize() / 2.0;
		delta = Workspace.getInstance().screenToModel(delta);
		Point a = new Point(point.getX() - delta, point.getY() - delta);
		Point b = new Point(point.getX() + delta, point.getY() + delta);

		Rectangle rect = new Rectangle(a.getX(), a.getY(), b.getX(), b.getY());

		int selected = getCurrentSelectedElements().size();
		select(rect.getUpperRight(), rect.getLowerLeft(), invertSelection);

		return (selected != getCurrentSelectedElements().size());
	}

	/**
	 * Selects the elements by area.
	 * 
	 * @param p1
	 *            Starting rectangle point.
	 * @param p2
	 *            Ending rectangle point.
	 * @param invertSet
	 *            If true, the selection isn't cleared, but inverted.
	 * @return True always.
	 * @throws NullArgumentException
	 *             In case something goes very wrong.
	 * @throws NoActiveDrawingException
	 *             In case there's no active drawing
	 */
	public boolean select(Point p1, Point p2, boolean invertSelection)
			throws NullArgumentException, NoActiveDrawingException {

		if (p1 == null || p2 == null) {
			throw new NullArgumentException();
		}

		Rectangle rect = new Rectangle(p1.getX(), p1.getY(), p2.getX(), p2
				.getY());
		Set<Element> selected;
		Selection selection = new Selection(rect, invertSelection);

		Drawing drawing = getActiveDrawing();
		if (p1.getX() < p2.getX()) {
			selected = drawing.getSelectionInside(rect);
		} else {
			selected = drawing.getSelectionIntersection(rect);
		}

		changeSelection(selection, selected, invertSelection);

		return true;
	}

	/**
	 * Deselects all elements in the current drawing, if there is one.
	 */
	public void deselectAll() {

		try {
			getActiveDrawing().setSelection(new Selection());
		} catch (NoActiveDrawingException e) {
			// Do nothing
		}
	}

	/**
	 * @param mousePosition
	 *            The click position
	 * @return true if there is at least one point to be moved, false otherwise.
	 * @throws NoActiveDrawingException
	 *             In case there's no active drawing
	 */
	protected boolean movePoint(Point mousePosition)
			throws NoActiveDrawingException {

		double delta = Workspace.getInstance().getMouseSize() / 2.0;
		delta = Workspace.getInstance().screenToModel(delta);
		Rectangle selectionArea = Utils
				.getSquareFromDelta(mousePosition, delta);
		Map<Element, Collection<Point>> pointsToMove = new HashMap<Element, Collection<Point>>();
		Set<Element> selectedElements = getCurrentSelectedElements();
		for (Element element : selectedElements) {
			Collection<Point> pointsFromElement = getPointsToMove(element,
					selectionArea);
			if (pointsFromElement != null && !pointsFromElement.isEmpty()) {
				pointsToMove.put(element, pointsFromElement);
			}
		}

		boolean shouldMove = !pointsToMove.isEmpty();
		if (shouldMove) {
			try {
				CommandFactory quickMoveFactory = new QuickMoveFactory(
						pointsToMove, mousePosition);
				InputController.getInstance().setCurrentFactory(quickMoveFactory);
			} catch (Exception e) {
				// Should not happen
				e.printStackTrace();
			}
		}

		return shouldMove;
	}

	/**
	 * @param element
	 *            The selected element whose points might be moved.
	 * @param selectionArea
	 *            The area in which the points must be to be moved.
	 * @return The collection of points that should be moved.
	 */
	private Collection<Point> getPointsToMove(Element element,
			Rectangle selectionArea) {

		Rectangle modelDrawingArea = Workspace.getInstance()
				.getCurrentViewportArea();

		Collection<Point> points = new ArrayList<Point>();
		for (ReferencePoint referencePoint : element
				.getReferencePoints(modelDrawingArea)) {
			if (referencePoint.isInside(selectionArea)) {
				points.addAll(referencePoint.getPointsToMove());
				break;
			}
		}

		return points;
	}

	/**
	 * Auxiliar method. Changes the current selection to the recieved parameter,
	 * according to the invertSelection parameter.
	 * 
	 * @param selection
	 *            The selection that was just made. It will only be used if the
	 *            current selection is empty.
	 * @param selectedElements
	 *            The elements to be considered.
	 * @param invertSelection
	 *            If true, the selection is inverted. If false, it is cleared.
	 * @throws NullArgumentException
	 *             In case selection is null.
	 * @throws NoActiveDrawingException
	 *             In case there's no active drawing
	 */
	private void changeSelection(Selection selection,
			Set<Element> selectedElements, boolean invertSelection)
			throws NullArgumentException, NoActiveDrawingException {

		if (selectedElements == null) {
			throw new NullArgumentException();
		}
		Selection currentSelection = getCurrentSelection();
		if (currentSelection.isEmpty()) {
			currentSelection = selection;
			currentSelection.addAll(selectedElements);
			getActiveDrawing().setSelection(currentSelection);
		} else {
			if (!invertSelection) {
				currentSelection.addAll(selectedElements);
			} else {
				for (Element element : selectedElements) {
					if (!currentSelection.remove(element)) {
						currentSelection.add(element);
					}
				}
			}
			currentSelection.setRectangle(null);
		}
	}

	/**
	 * @return The current selection with information about what was done.
	 * @throws NoActiveDrawingException
	 *             In case there's no active drawing
	 */
	public Selection getCurrentSelection() throws NoActiveDrawingException {

		return getActiveDrawing().getSelection();
	}

	/**
	 * @return The current selected elements..
	 * @throws NoActiveDrawingException
	 *             In case there's no active drawing
	 */
	public Set<Element> getCurrentSelectedElements()
			throws NoActiveDrawingException {

		return getActiveDrawing().getSelection().getSelectedElements();
	}
}