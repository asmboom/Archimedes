
package br.org.archimedes.dimension;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import br.org.archimedes.Constant;
import br.org.archimedes.Geometrics;
import br.org.archimedes.exceptions.InvalidArgumentException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.gui.opengl.OpenGLWrapper;
import br.org.archimedes.line.Line;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Rectangle;
import br.org.archimedes.model.ReferencePoint;
import br.org.archimedes.model.Vector;
import br.org.archimedes.model.references.SquarePoint;
import br.org.archimedes.text.Text;

/**
 * @author marivb
 */
public class Dimension extends Element {

    public static final double DIST_FROM_ELEMENT = 10.0;

    public static final double DIST_AFTER_LINE = 10.0;

    private Point initialPoint;

    private Point endingPoint;

    private Point distance;

    private Text text;

    private double fontSize;


    /**
     * @param initialPoint
     *            The initial point of the dimension
     * @param endingPoint
     *            The ending point of the dimension
     * @param distance
     *            The point that determines where the dimension will be placed
     * @throws NullArgumentException
     *             In case any point is null.
     * @throws InvalidArgumentException
     *             In case the initial and ending points are the same.
     */
    public Dimension (Point initialPoint, Point endingPoint, Point distance,
            double fontSize) throws NullArgumentException,
            InvalidArgumentException {

        if (initialPoint == null || endingPoint == null || distance == null) {
            throw new NullArgumentException();
        }
        if (initialPoint.equals(endingPoint) || initialPoint.equals(distance)
                || endingPoint.equals(distance)) {
            throw new InvalidArgumentException();
        }

        this.initialPoint = initialPoint;
        this.endingPoint = endingPoint;
        this.distance = distance.clone();
        this.fontSize = fontSize;
        remakeDistance();
        text = makeText();
    }

    /**
     * @param p1
     *            Initial point
     * @param p2
     *            Ending point
     * @param distance
     *            distance for displaying the dimension
     * @param fontSize
     *            size of text font
     * @throws NullArgumentException
     *             In case any argument is null
     * @throws InvalidArgumentException
     *             In case the initial and ending point are the same.
     */
    public Dimension (Point initialPoint, Point endingPoint, double distance,
            double fontSize) throws NullArgumentException,
            InvalidArgumentException {

        if (initialPoint == null || endingPoint == null) {
            throw new NullArgumentException();
        }
        if (initialPoint.equals(endingPoint)
                || Math.abs(distance) < Constant.EPSILON) {
            throw new InvalidArgumentException();
        }

        Vector vector = new Vector(initialPoint, endingPoint);
        vector = Geometrics.orthogonalize(vector);
        Line line = new Line(initialPoint, initialPoint.addVector(vector));
        Line offseted = (Line) line.cloneWithDistance(distance);
        this.distance = offseted.getInitialPoint();
        this.fontSize = fontSize;
        remakeDistance();
        text = makeText();
    }

    /**
     * Recalculates the distance point to be the middle of the dimension line,
     * so that it may be used as a snap point.
     */
    private void remakeDistance () {

        Line dimLine = getDimensionLine();
        Point newDistance = distance;
        try {
            newDistance = Geometrics.getMeanPoint(dimLine.getInitialPoint(),
                    dimLine.getEndingPoint());
        }
        catch (NullArgumentException e) {
            // Should never happen
            e.printStackTrace();
        }
        Vector toMove = new Vector(distance, newDistance);
        distance.move(toMove.getX(), toMove.getY());
    }

    /**
     * Makes the text for this dimension.
     * 
     * @return The created text.
     */
    private Text makeText () {

        Line lineToMeasure = getDimensionLine();
        Point initial = lineToMeasure.getInitialPoint();
        Point ending = lineToMeasure.getEndingPoint();
        Text text = null;
        try {
            double length = Geometrics.calculateDistance(initial, ending);
            DecimalFormat df = new DecimalFormat();
            String lengthStr = df.format(length);

            Point mean = Geometrics.getMeanPoint(initial, ending);
            text = new Text(lengthStr, mean, fontSize);
            double width = text.getWidth();

            if (isDimLineHorizontal()) {
                text.move( -width / 2.0, DIST_FROM_ELEMENT);
            }
            else {
                text.rotate(mean.clone(), Math.PI / 2.0);
                text.move( -DIST_FROM_ELEMENT, -width / 2.0);
            }
        }
        catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }

        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#clone()
     */
    @Override
    public Element clone () {

        Dimension clone = null;
        try {
            clone = new Dimension(initialPoint.clone(), endingPoint.clone(),
                    distance.clone(), fontSize);
        }
        catch (Exception e) {
            // Should never happen
            e.printStackTrace();
        }
        return clone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#contains(br.org.archimedes.model.Point)
     */
    @Override
    public boolean contains (Point point) throws NullArgumentException {

        // Dimension will not be used for trim, extend, fillet or intersection
        // snap points.
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object object) {

        boolean result = (object == this);
        if ( !result && object != null && object.getClass() == getClass()) {
            Dimension other = (Dimension) object;
            Line line = getDimensionLine();
            Line otherLine = other.getDimensionLine();
            result = line.equals(otherLine);
            if (result) {
                Point otherInitial = other.getInitialPoint();
                Point otherEnding = other.getEndingPoint();
                if (otherInitial.equals(getInitialPoint())) {
                    result = otherEnding.equals(getEndingPoint());
                }
                else if (otherInitial.equals(getEndingPoint())) {
                    result = otherInitial.equals(getEndingPoint());
                }
                else {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * @return The line that should be measured.
     */
    private Line getDimensionLine () {

        Point first, second;
        if (isDimLineHorizontal()) {
            first = new Point(initialPoint.getX(), distance.getY());
            second = new Point(endingPoint.getX(), distance.getY());
        }
        else {
            first = new Point(distance.getX(), initialPoint.getY());
            second = new Point(distance.getX(), endingPoint.getY());
        }

        Line dimLine = null;
        try {
            dimLine = new Line(first, second);
        }
        catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }

        return dimLine;
    }

    /**
     * @return true if the dimension line is horizontal, false if it is vertical
     */
    private boolean isDimLineHorizontal () {

        Point point1 = new Point(initialPoint.getX(), endingPoint.getY());
        Point point2 = new Point(endingPoint.getX(), initialPoint.getY());

        double originalDeterminant = 0;
        double otherDeterminant = 0;
        try {
            originalDeterminant = Geometrics.calculateDeterminant(initialPoint,
                    endingPoint, distance);
            otherDeterminant = Geometrics.calculateDeterminant(point1, point2,
                    distance);
        }
        catch (NullArgumentException e) {
            // Should not happen
            e.printStackTrace();
        }
        boolean horizontal = originalDeterminant * otherDeterminant >= 0;
        return horizontal;
    }

    /**
     * @return Returns the endingPoint.
     */
    public Point getEndingPoint () {

        return this.endingPoint;
    }

    /**
     * @param endingPoint
     *            The endingPoint to set.
     */
    public void setEndingPoint (Point endingPoint) {

        this.endingPoint = endingPoint;
    }

    /**
     * @return Returns the initialPoint.
     */
    public Point getInitialPoint () {

        return this.initialPoint;
    }

    /**
     * @param initialPoint
     *            The initialPoint to set.
     */
    public void setInitialPoint (Point initialPoint) {

        this.initialPoint = initialPoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#getBoundaryRectangle()
     */
    @Override
    public Rectangle getBoundaryRectangle () {

        Rectangle boundary = null;

        Collection<Line> linesToDraw = getLinesToDraw();
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Line line : linesToDraw) {
            boundary = line.getBoundaryRectangle();
            Point lowerLeft = boundary.getLowerLeft();
            minX = Math.min(minX, lowerLeft.getX());
            minY = Math.min(minY, lowerLeft.getY());
            Point upperRight = boundary.getUpperRight();
            maxX = Math.max(maxX, upperRight.getX());
            maxY = Math.max(maxY, upperRight.getY());
        }

        return new Rectangle(minX, minY, maxX, maxY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#getNearestExtremePoint(br.org.archimedes.model.Point)
     */
    @Override
    public Point getNearestExtremePoint (Point point)
            throws NullArgumentException {

        // Dimension will not be used for trim, extend, fillet or intersection
        // snap points.
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#getPoints()
     */
    @Override
    public List<Point> getPoints () {

        List<Point> points = new ArrayList<Point>();
        points.add(initialPoint);
        points.add(endingPoint);
        points.add(distance);
        points.add(text.getLowerLeft());
        return points;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#getProjectionOf(br.org.archimedes.model.Point)
     */
    @Override
    public Point getProjectionOf (Point point) throws NullArgumentException {

        // Dimension will not be used for perpendicular snap points.
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#getReferencePoints(br.org.archimedes.model.Rectangle)
     */
    @Override
    public Collection<ReferencePoint> getReferencePoints (Rectangle area) {

        ArrayList<ReferencePoint> references = new ArrayList<ReferencePoint>();
        try {
            references.add(new SquarePoint(initialPoint, initialPoint));
            references.add(new SquarePoint(endingPoint, endingPoint));
            references.add(new SquarePoint(distance, distance));
            references.add(new SquarePoint(text.getLowerLeft(), text
                    .getLowerLeft()));
        }
        catch (NullArgumentException e) {
            // Should not happen
            e.printStackTrace();
        }
        return references;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#move(java.util.Collection,
     *      br.org.archimedes.model.Vector)
     */
    public void move (Collection<Point> pointsToMove, Vector vector)
            throws NullArgumentException {

        super.move(pointsToMove, vector);
        remakeDistance();
        if ( !pointsToMove.contains(text.getLowerLeft())) {
            text = makeText();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#intersects(br.org.archimedes.model.Rectangle)
     */
    public boolean intersects (Rectangle rectangle)
            throws NullArgumentException {

        boolean inter = false;
        Collection<Line> lines = getLinesToDraw();
        for (Line line : lines) {
            inter = inter || line.intersects(rectangle);
        }
        return inter;
    }

    /**
     * @return The lines to be drawn for this dimension
     */
    public Collection<Line> getLinesToDraw () {

        double sign = 0;
        Line dimLine = null, initialLine = null, endingLine = null;
        Collection<Line> lines = new ArrayList<Line>();
        try {
            double initialX = initialPoint.getX();
            double initialY = initialPoint.getY();
            if (isDimLineHorizontal()) {
                sign = Math.signum(initialX - endingPoint.getX());

                double dimInitial = initialX + (sign * DIST_AFTER_LINE);
                double dimEnding = endingPoint.getX()
                        - (sign * DIST_AFTER_LINE);

                dimLine = new Line(dimInitial, distance.getY(), dimEnding,
                        distance.getY());

                sign = Math.signum(distance.getY() - initialY);

                double initialInitial = (initialY + sign * DIST_FROM_ELEMENT);
                double initialEnding = distance.getY() + sign * DIST_AFTER_LINE;

                if (Math.abs(initialInitial - initialEnding) > Constant.EPSILON) {
                    initialLine = new Line(initialX, initialInitial, initialX,
                            initialEnding);
                }

                double endingInitial = (endingPoint.getY() + sign
                        * DIST_FROM_ELEMENT);
                double endingEnding = distance.getY() + sign * DIST_AFTER_LINE;

                if (Math.abs(endingInitial - endingEnding) > Constant.EPSILON) {
                    endingLine = new Line(endingPoint.getX(), endingInitial,
                            endingPoint.getX(), endingEnding);
                }
            }
            else {
                sign = Math.signum(initialY - endingPoint.getY());

                double dimInitial = initialY + (sign * DIST_AFTER_LINE);
                double dimEnding = endingPoint.getY()
                        - (sign * DIST_AFTER_LINE);

                dimLine = new Line(distance.getX(), dimInitial,
                        distance.getX(), dimEnding);

                sign = Math.signum(distance.getX() - initialX);

                double initialInitial = (initialX + sign * DIST_FROM_ELEMENT);
                double initialEnding = distance.getX() + sign * DIST_AFTER_LINE;

                if (Math.abs(initialInitial - initialEnding) > Constant.EPSILON) {
                    initialLine = new Line(initialInitial, initialY,
                            initialEnding, initialY);
                }

                double endingInitial = (endingPoint.getX() + sign
                        * DIST_FROM_ELEMENT);
                double endingEnding = distance.getX() + sign * DIST_AFTER_LINE;

                if (Math.abs(endingInitial - endingEnding) > Constant.EPSILON) {
                    endingLine = new Line(endingInitial, endingPoint.getY(),
                            endingEnding, endingPoint.getY());
                }
            }

            lines.add(dimLine);
            if (initialLine != null) {
                lines.add(initialLine);
            }
            if (endingLine != null) {
                lines.add(endingLine);
            }
        }
        catch (InvalidArgumentException e) {
            // Should not happen
            e.printStackTrace();
        }

        return lines;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#isCollinearWith(br.org.archimedes.model.Element)
     */
    @Override
    public boolean isCollinearWith (Element element) {

        // Dimension will not be used for trim, extend, fillet or intersection
        // snap points.
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#isParallelTo(br.org.archimedes.model.Element)
     */
    @Override
    public boolean isParallelTo (Element element) {

        // Dimension will not be used for trim, extend, fillet or intersection
        // snap points.
        return false;
    }

    /**
     * @return The distance Point (not yet very useful)
     */
    public Point getDistancePoint () {

        return distance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString () {

        return "Dimension: measures " + initialPoint.toString() + " and " //$NON-NLS-1$ //$NON-NLS-2$
                + endingPoint.toString() + " at distance " //$NON-NLS-1$
                + distance.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#getIntersection(br.org.archimedes.model.Element)
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public Collection<Point> getIntersection (Element element)
            throws NullArgumentException {

        // TODO Auto-generated method stub
        return Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.org.archimedes.model.Element#draw(br.org.archimedes.gui.opengl.OpenGLWrapper)
     */
    @Override
    public void draw (OpenGLWrapper wrapper) {

        Collection<Line> linesToDraw = getLinesToDraw();
        text.draw(wrapper);
        for (Line line : linesToDraw) {
            try {
                wrapper.drawFromModel(new ArrayList<Point>(line
                        .getExtremePoints()));
            }
            catch (NullArgumentException e) {
                // Won't happen, but anyway...
                e.printStackTrace();
            }
        }
    }

    /**
     * @return the text
     */
    public Text getText () {

        return this.text;
    }
}