/**
 * This file was created on 2007/03/12, 07:51:43, by nitao. It is part of
 * br.org.archimedes.arc on the br.org.archimedes.arc project.
 */

package br.org.archimedes.arc;

import br.org.archimedes.Constant;
import br.org.archimedes.Geometrics;
import br.org.archimedes.curvedshape.CurvedShape;
import br.org.archimedes.exceptions.IllegalActionException;
import br.org.archimedes.exceptions.InvalidArgumentException;
import br.org.archimedes.exceptions.InvalidParameterException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.gui.opengl.OpenGLWrapper;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Offsetable;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Rectangle;
import br.org.archimedes.model.ReferencePoint;
import br.org.archimedes.model.Vector;
import br.org.archimedes.model.references.CirclePoint;
import br.org.archimedes.model.references.SquarePoint;
import br.org.archimedes.model.references.TrianglePoint;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Belongs to package br.org.archimedes.arc.
 * 
 * @author nitao
 */
public class Arc extends CurvedShape implements Offsetable {

    private Point initialPoint;

    private Point endingPoint;

    private Point intermediatePoint;


    /**
     * Constructor. Always build the arc in the counter clockwise order.
     * 
     * @param initialPoint
     *            The initial point
     * @param intermediatePoint
     *            A point that is contained in the arc and is different from the
     *            initial and ending point
     * @param endingPoint
     *            The ending point
     * @throws NullArgumentException
     *             Thrown if any of the points is null
     * @throws InvalidArgumentException
     *             Thrown if the points are collinear
     */
    public Arc (Point initialPoint, Point intermediatePoint, Point endingPoint)
            throws NullArgumentException, InvalidArgumentException {

        if (initialPoint == null || intermediatePoint == null
                || endingPoint == null) {
            throw new NullArgumentException();
        }

        createInternalRepresentation(initialPoint.clone(), intermediatePoint
                .clone(), endingPoint.clone());
    }

    /**
     * Constructor.
     * 
     * @param initialPoint
     *            The initial point
     * @param endingPoint
     *            The ending point
     * @param centerPoint
     *            The center point
     * @param counterclock
     *            true if the points are in counter clock order, false
     *            otherwise.
     * @throws NullArgumentException
     *             Thrown if any argument is null
     * @throws InvalidArgumentException
     *             Throw if the arc is invalid.
     */
    public Arc (Point initialPoint, Point endingPoint, Point centerPoint,
            boolean counterclock) throws NullArgumentException,
            InvalidArgumentException {

        if (initialPoint == null || endingPoint == null || centerPoint == null) {
            throw new NullArgumentException();
        }
        if (initialPoint.equals(endingPoint)
                || initialPoint.equals(centerPoint)
                || endingPoint.equals(centerPoint)) {
            throw new InvalidArgumentException();
        }

        if (counterclock) {
            this.initialPoint = initialPoint.clone();
            this.endingPoint = endingPoint.clone();
        }
        else {
            this.initialPoint = endingPoint.clone();
            this.endingPoint = initialPoint.clone();
        }
        this.centerPoint = centerPoint.clone();
        this.intermediatePoint = calculateMidPoint(this.initialPoint,
                this.endingPoint, centerPoint);
    }

    /**
     * Adjusts the arc points so that they are in counter clockwise order.
     * 
     * @param initialPoint
     *            The initial point
     * @param intermediatePoint
     *            A point that is contained in the arc and is different from the
     *            initial and ending point
     * @param endingPoint
     *            The ending point
     * @throws NullArgumentException
     *             Thrown if any of the points is null
     * @throws InvalidArgumentException
     *             Thrown if the points are collinear
     */
    private void createInternalRepresentation (Point initialPoint,
            Point intermediatePoint, Point endingPoint)
            throws InvalidArgumentException, NullArgumentException {

        this.initialPoint = initialPoint;
        this.endingPoint = endingPoint;
        this.centerPoint = Geometrics.getCircumcenter(initialPoint,
                intermediatePoint, endingPoint);

        double initialAngle = Geometrics.calculateAngle(centerPoint,
                initialPoint);
        double middleAngle = Geometrics.calculateAngle(centerPoint,
                intermediatePoint);
        double endingAngle = Geometrics
                .calculateAngle(centerPoint, endingPoint);

        boolean isClockwise = (endingAngle > initialAngle && (middleAngle < initialAngle || middleAngle > endingAngle));
        isClockwise = isClockwise
                || (initialAngle > endingAngle && (endingAngle < middleAngle && middleAngle < initialAngle));

        if (isClockwise) {
            Point tempPoint = this.initialPoint;
            this.initialPoint = this.endingPoint;
            this.endingPoint = tempPoint;
        }
        this.intermediatePoint = calculateMidPoint(this.initialPoint,
                this.endingPoint, this.centerPoint);
    }

    /**
     * Constructor.
     * 
     * @param initialPoint
     *            The initial point
     * @param endingPoint
     *            The ending point
     * @param centerPoint
     *            The center point
     * @param direction
     *            The direction to create the arc
     * @throws NullArgumentException
     *             Thrown if any of the points is null
     * @throws InvalidArgumentException
     *             Thrown if the points are collinear
     */
    public Arc (Point initialPoint, Point endingPoint, Point centerPoint,
            Point direction) throws NullArgumentException,
            InvalidArgumentException {

        if (initialPoint == null || centerPoint == null || endingPoint == null
                || direction == null) {
            throw new NullArgumentException();
        }

        if (Math.abs(Geometrics.calculateDeterminant(initialPoint, centerPoint,
                endingPoint)) <= Constant.EPSILON) {
            double initialToCenter = Geometrics.calculateDistance(initialPoint,
                    centerPoint);
            double endingToCenter = Geometrics.calculateDistance(endingPoint,
                    centerPoint);

            if (Math.abs(initialToCenter - endingToCenter) > Constant.EPSILON) {
                throw new InvalidArgumentException();
            }
        }

        this.centerPoint = centerPoint.clone();

        double arcAngle = Geometrics.calculateRelativeAngle(centerPoint,
                initialPoint, endingPoint);
        double dirAngle = Geometrics.calculateRelativeAngle(centerPoint,
                initialPoint, direction);

        boolean isClockwise = (dirAngle > arcAngle);

        this.initialPoint = initialPoint.clone();
        this.endingPoint = endingPoint.clone();

        if (isClockwise) {
            Point tempPoint = this.initialPoint;
            this.initialPoint = this.endingPoint;
            this.endingPoint = tempPoint;
        }

        this.intermediatePoint = calculateMidPoint(this.initialPoint,
                this.endingPoint, this.centerPoint);
    }

    public Arc clone () {

        Arc arc = null;

        try {
            arc = new Arc(initialPoint.clone(), intermediatePoint.clone(),
                    endingPoint.clone());
            arc.setLayer(parentLayer);
        }
        catch (NullArgumentException e) {
            // Should never reach this block
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should never reach this block
            e.printStackTrace();
        }

        return arc;
    }

    public boolean equals (Object object) {

        boolean result = false;

        if (object != null) {
            try {
                Arc arc = (Arc) object;
                result = this.equals(arc);
            }
            catch (ClassCastException e) {
                // The elements are not equal
            }
        }

        return result;
    }

    public boolean equals (Arc arc) {

        boolean result = true;

        if (arc == null) {
            result = false;
        }
        else if ( !this.centerPoint.equals(arc.centerPoint)) {
            result = false;
        }
        else if ( !this.initialPoint.equals(arc.initialPoint)
                && !this.initialPoint.equals(arc.endingPoint)) {
            result = false;
        }
        else if ( !this.endingPoint.equals(arc.initialPoint)
                && !this.endingPoint.equals(arc.endingPoint)) {
            result = false;
        }
        else {
            try {
                double intermediatePointSign1 = Geometrics
                        .calculateDeterminant(initialPoint, endingPoint,
                                intermediatePoint);
                double intermediatePointSign2 = Geometrics
                        .calculateDeterminant(initialPoint, endingPoint,
                                arc.intermediatePoint);

                if ((intermediatePointSign1 * intermediatePointSign2) < 0.0) {
                    result = false;
                }
            }
            catch (NullArgumentException e) {
                // Should never happen
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * @return The radius of the arc
     */
    public double getRadius () {

        double radius = 0.0;

        try {
            radius = Geometrics.calculateDistance(centerPoint, initialPoint);
        }
        catch (NullArgumentException e) {
            // Should not reach this block
            e.printStackTrace();
        }

        return radius;
    }

    /*
     * (non-Javadoc)
     * @see com.tarantulus.archimedes.model.Element#getBoundaryRectangle()
     */
    public Rectangle getBoundaryRectangle () {

        double maxX = Math.max(initialPoint.getX(), endingPoint.getX());
        double minX = Math.min(initialPoint.getX(), endingPoint.getX());
        double maxY = Math.max(initialPoint.getY(), endingPoint.getY());
        double minY = Math.min(initialPoint.getY(), endingPoint.getY());

        double rightSide = centerPoint.getX() + getRadius();
        if ( !contains(rightSide, centerPoint.getY())) {
            rightSide = maxX;
        }

        double leftSide = centerPoint.getX() - getRadius();
        if ( !contains(leftSide, centerPoint.getY())) {
            leftSide = minX;
        }

        double topSide = centerPoint.getY() + getRadius();
        if ( !contains(centerPoint.getX(), topSide)) {
            topSide = maxY;
        }

        double bottomSide = centerPoint.getY() - getRadius();
        if ( !contains(centerPoint.getX(), bottomSide)) {
            bottomSide = minY;
        }

        return new Rectangle(leftSide, topSide, rightSide, bottomSide);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tarantulus.archimedes.model.Element#getReferencePoints(com.tarantulus
     * .archimedes.model.Rectangle)
     */
    public Collection<ReferencePoint> getReferencePoints (Rectangle area) {

        Collection<ReferencePoint> references = new LinkedList<ReferencePoint>();
        List<Point> allPoints = getPoints();
        if (area != null) {
            try {
                if (initialPoint.isInside(area)) {
                    references.add(new SquarePoint(initialPoint, initialPoint));
                }
                if (endingPoint.isInside(area)) {
                    references.add(new SquarePoint(endingPoint, endingPoint));
                }
                if (centerPoint.isInside(area)) {
                    references.add(new CirclePoint(centerPoint, allPoints));
                }
                if (intermediatePoint.isInside(area)) {
                    references.add(new TrianglePoint(intermediatePoint,
                            intermediatePoint));
                }
            }
            catch (NullArgumentException e) {
                // Should never reach this block
                e.printStackTrace();
            }
        }

        return references;
    }

    /**
     * @param initialPoint
     *            The initial point of the arc
     * @param endingPoint
     *            The ending point of the arc
     * @param centerPoint
     *            The center point of the arc
     * @return The point corresponding to the mid point of this arc.
     */
    private Point calculateMidPoint (Point initialPoint, Point endingPoint,
            Point centerPoint) {

        Point midPoint;
        double angle = Geometrics.calculateRelativeAngle(centerPoint,
                initialPoint, endingPoint);
        angle /= 2;
        midPoint = initialPoint.clone();
        try {
            midPoint.rotate(centerPoint, angle);
        }
        catch (NullArgumentException e) {
            // Should not happen
            e.printStackTrace();
        }
        return midPoint;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tarantulus.archimedes.model.Element#getProjectionOf(com.tarantulus
     * .archimedes.model.Point)
     */
    public Point getProjectionOf (Point point) throws NullArgumentException {

        if (point == null) {
            throw new NullArgumentException();
        }

//        Point closer = null, farther = null;
        // FIXME Implementar a projecao
        // try {
        // Line line = new Line(centerPoint, point);
        // Collection<Point> intersectionWithLine =
        // getIntersectionWithLine(line);
        // double closestDist = Double.MAX_VALUE;
        // for (Point intersection : intersectionWithLine) {
        // double dist = Geometrics.calculateDistance(point, intersection);
        // if (dist < closestDist) {
        // if (closer != null) {
        // farther = closer;
        // }
        // closer = intersection;
        // closestDist = dist;
        // }
        // else {
        // farther = intersection;
        // }
        // }
        // }
        // catch (InvalidArgumentException e) {
        // // May happen
        // e.printStackTrace();
        // }
        //
        // Point projection = null;
        // if (contains(closer) || !contains(farther)) {
        // projection = closer;
        // }
        // else {
        // projection = farther;
        // }
        // return projection;
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tarantulus.archimedes.model.Element#contains(com.tarantulus.archimedes
     * .model.Point)
     */
    public boolean contains (Point point) throws NullArgumentException {

        boolean result = false;

        double distance = Geometrics.calculateDistance(point, centerPoint);
        double radius = Geometrics.calculateDistance(initialPoint, centerPoint);

        if (Math.abs(distance - radius) <= Constant.EPSILON) {
            double intermediateSign = Geometrics.calculateDeterminant(
                    initialPoint, endingPoint, intermediatePoint);
            double pointSign = Geometrics.calculateDeterminant(initialPoint,
                    endingPoint, point);

            result = ((intermediateSign * pointSign) >= 0.0);
        }

        return result;
    }

    public boolean contains (Arc arc) throws NullArgumentException {

        if (arc == null) {
            throw new NullArgumentException();
        }

        if (this.contains(arc.getInitialPoint())
                && this.contains(arc.getIntermediatePoint())
                && this.contains(arc.getEndingPoint())) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if the arc contains a point determined by x and y.
     * 
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     * @return true if the arc contains the point, false otherwise.
     */
    private boolean contains (double x, double y) {

        boolean result = false;
        try {
            result = contains(new Point(x, y));
        }
        catch (NullArgumentException e) {
            // Should not reach this code
            e.printStackTrace();
        }
        return result;
    }

    public boolean isPositiveDirection (Point point) {

        boolean isOutside = false;

        try {
            if (Geometrics.calculateDistance(this.getCenter(), point) > Geometrics
                    .calculateDistance(this.getCenter(), this.getInitialPoint())) {
                isOutside = true;
            }
        }
        catch (NullArgumentException e) {
            // Should not reach this block
            e.printStackTrace();
        }
        return isOutside;
    }

    public Element cloneWithDistance (double distance)
            throws InvalidParameterException {

        if (distance < 0) {
            if (Math.abs(getRadius() - distance) <= Constant.EPSILON
                    || Math.abs(distance) > getRadius()) {
                throw new InvalidParameterException();
            }
        }

        Vector startVector = new Vector(centerPoint, initialPoint);
        startVector = Geometrics.normalize(startVector);
        startVector = startVector.multiply(getRadius() + distance);

        Point p1 = centerPoint.addVector(startVector);

        Vector midVector = new Vector(centerPoint, intermediatePoint);
        midVector = Geometrics.normalize(midVector);
        midVector = midVector.multiply(getRadius() + distance);

        Point p2 = centerPoint.addVector(midVector);

        Vector endVector = new Vector(centerPoint, endingPoint);
        endVector = Geometrics.normalize(endVector);
        endVector = endVector.multiply(getRadius() + distance);

        Point p3 = centerPoint.addVector(endVector);

        Arc arc = null;
        try {
            arc = new Arc(p1, p2, p3);
            arc.setLayer(parentLayer);
        }
        catch (NullArgumentException e) {
            // Should not reach this code
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should not reach this code
            e.printStackTrace();
        }

        return arc;
    }

    /**
     * @see br.org.archimedes.model.Element#draw(br.org.archimedes.gui.opengl.OpenGLWrapper)
     */
    @Override
    public void draw (OpenGLWrapper wrapper) {

        Point center = this.getCenter();

        double initialAngle = 0.0;
        double endingAngle = 0.0;
        try {
            initialAngle = Geometrics.calculateAngle(center, this
                    .getInitialPoint());
            endingAngle = Geometrics.calculateAngle(center, this
                    .getEndingPoint());
        }
        catch (NullArgumentException e) {
            // Should never reach this block
            e.printStackTrace();
        }

        if (initialAngle > endingAngle) {
            endingAngle += 2.0 * Math.PI;
        }
        this.drawCurvedShape(wrapper, center, initialAngle, endingAngle);
    }

    /**
     * @return Returns the endingPoint.
     */
    public Point getEndingPoint () {

        return endingPoint;
    }

    /**
     * @return Returns the initialPoint.
     */
    public Point getInitialPoint () {

        return initialPoint;
    }

    /**
     * @return Returns the intermediatePoint.
     */
    public Point getIntermediatePoint () {

        return intermediatePoint;
    }

    /*
     * (non-Javadoc)
     * @see com.tarantulus.archimedes.model.elements.Element#getPoints()
     */
    @Override
    public List<Point> getPoints () {

        List<Point> points = new LinkedList<Point>();
        points.add(initialPoint);
        points.add(endingPoint);
        points.add(intermediatePoint);
        points.add(centerPoint);
        return points;
    }

    public String toString () {

        return centerPoint.toString() + " with radius " + getRadius() //$NON-NLS-1$
                + " from " + initialPoint.toString() + " to " //$NON-NLS-1$ //$NON-NLS-2$
                + endingPoint.toString();
    }

    /*
     * (non-Javadoc)
     * @see com.tarantulus.archimedes.model.Element#move(Collection<Point>,
     * Vector)
     */
    public void move (Collection<Point> points, Vector vector)
            throws NullArgumentException {

        super.move(points, vector);
        try {
            createInternalRepresentation(initialPoint, intermediatePoint,
                    endingPoint);
        }
        catch (InvalidArgumentException e) {
            // If the arc is invalid, undoes the move
            super.move(points, vector.multiply( -1.0));
        }
        catch (NullArgumentException e) {
            // Should never happen
            e.printStackTrace();
        }
    }

    /**
     * Este m�todo foi reescrito pois, apesar de o mirror ter sido feito, o arco
     * ainda guardava as refer�ncias inalteradas para o ending point e
     * initialPoint. Logo, para que o mirror funcionasse corretamente, havia a
     * necessidade de trocar o initialPoint com o endingPoint.
     * 
     * @author Victor
     * @author Eduardo
     */
    @Override
    public void mirror (Point p1, Point p2) throws NullArgumentException,
            IllegalActionException {

        super.mirror(p1, p2);

        Point auxiliarPoint = initialPoint;

        initialPoint = endingPoint;
        endingPoint = auxiliarPoint;
    }
}
