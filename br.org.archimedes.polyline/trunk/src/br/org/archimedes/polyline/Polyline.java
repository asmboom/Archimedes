/*
 * Created on 05/06/2006
 */

package br.org.archimedes.polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import br.org.archimedes.Constant;
import br.org.archimedes.Geometrics;
import br.org.archimedes.exceptions.InvalidArgumentException;
import br.org.archimedes.exceptions.InvalidParameterException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.gui.opengl.OpenGLWrapper;
import br.org.archimedes.line.Line;
import br.org.archimedes.model.ComparablePoint;
import br.org.archimedes.model.DoubleKey;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.JoinableElement;
import br.org.archimedes.model.Layer;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.PolyLinePointKey;
import br.org.archimedes.model.Rectangle;
import br.org.archimedes.model.ReferencePoint;
import br.org.archimedes.model.Vector;
import br.org.archimedes.model.references.SquarePoint;
import br.org.archimedes.model.references.TrianglePoint;
import br.org.archimedes.model.references.XPoint;

/**
 * Belongs to package com.tarantulus.archimedes.model.
 * 
 * @author nitao
 */
public class Polyline extends Element {

    private List<Point> points;

    private Layer parentLayer;


    /**
     * Constructor.
     * 
     * @param points
     *            A list with the points that define the polyline. The polyline
     *            will use copies of those points.
     * @throws NullArgumentException
     *             Thrown if the argument is null.
     * @throws InvalidArgumentException
     *             Thrown if the argument contains less than 2 points.
     */
    public Polyline (List<Point> points) throws NullArgumentException,
            InvalidArgumentException {

        if (points == null) {
            throw new NullArgumentException();
        }
        else if (points.size() <= 1) {
            throw new InvalidArgumentException();
        }
        else {
            Point lastPoint = null;

            this.points = new ArrayList<Point>();
            for (Point point : points) {
                if ( !point.equals(lastPoint)) {
                    this.points.add(point.clone());
                    lastPoint = point;
                }
            }

            if (this.points.size() <= 1) {
                throw new InvalidArgumentException();
            }
        }
    }

    /**
     * @return A collection with the lines that compose this polyline.
     */
    public List<Line> getLines () {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            try {
                lines.add(new Line(p1, p2));
            }
            catch (Exception e) {
                // Should never reach this code
                e.printStackTrace();
            }
        }
        return lines;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getIntersection(com.tarantulus.archimedes.model.Element)
     */
    public Collection<Point> getIntersection (Element element)
            throws NullArgumentException {

        Collection<Point> intersections = new LinkedList<Point>();
        for (Line line : getLines()) {
            intersections.addAll(element.getIntersection(line));
        }

        return intersections;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#move(double, double)
     */
    public void move (double deltaX, double deltaY) {

        for (Point point : points) {
            point.setX(point.getX() + deltaX);
            point.setY(point.getY() + deltaY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getBoundaryRectangle()
     */
    public Rectangle getBoundaryRectangle () {

        double minx = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;

        for (Point point : points) {
            minx = Math.min(minx, point.getX());
            maxx = Math.max(maxx, point.getX());
            miny = Math.min(miny, point.getY());
            maxy = Math.max(maxy, point.getY());
        }

        Rectangle answer = new Rectangle(minx, miny, maxx, maxy);
        return answer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getSegment()
     */
    public Line getSegment () {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getProjectionOf(com.tarantulus.archimedes.model.Point)
     */
    /**
     * @return null if the polyline is closed and the projection is not
     *         contained. The result specified on Element otherwise.
     */
    public Point getProjectionOf (Point point) throws NullArgumentException {

        if (point == null) {
            throw new NullArgumentException();
        }

        Collection<Line> lines = getLines();
        Point closestPoint = null;
        double closestDist = Double.MAX_VALUE;
        for (Line line : lines) {
            Point projection = line.getProjectionOf(point);
            if (projection != null && ( !isClosed() || contains(projection))) {
                double dist = Geometrics.calculateDistance(point, projection);
                if (dist < closestDist) {
                    closestPoint = projection;
                    closestDist = dist;
                }
            }
        }
        return closestPoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#contains(com.tarantulus.archimedes.model.Point)
     */
    public boolean contains (Point point) throws NullArgumentException {

        boolean contain = false;
        Collection<Line> lines = getLines();
        for (Line line : lines) {
            contain = line.contains(point);
            if (contain) {
                break;
            }
        }

        return contain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#clone()
     */
    public Element clone () {

        List<Point> points = new ArrayList<Point>();
        for (Point point : this.points) {
            points.add(point.clone());
        }
        Polyline clone = null;
        try {
            clone = new Polyline(points);
            clone.setLayer(parentLayer);
        }
        catch (NullArgumentException e) {
            // Will never happen (I create it).
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // The arguments should not be invalid (otherwise I'm invalid).
            e.printStackTrace();
        }
        return clone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getIntersectionWithPolyline(com.tarantulus.archimedes.model.PolyLine)
     */
    public Collection<Point> getIntersectionWithPolyline (Polyline element)
            throws NullArgumentException {

        return getIntersection(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getIntersectionWithLine(com.tarantulus.archimedes.model.Element)
     */
    public Collection<Point> getIntersectionWithLine (Element element)
            throws NullArgumentException {

        return getIntersection(element);
    }

    public boolean equals (Object object) {

        boolean equal = (object == this);
        if (object != null && !equal && object.getClass() == this.getClass()) {
            Polyline polyline = (Polyline) object;
            equal = points.equals(polyline.getPoints());
            if ( !equal) {
                List<Point> inverseOrder = new ArrayList<Point>(points.size());
                for (Point point : points) {
                    inverseOrder.add(0, point);
                }
                equal = inverseOrder.equals(polyline.getPoints());
            }
        }
        return equal;
    }

    /**
     * @return The list of points that represent the polyline.
     */
    public List<Point> getPoints () {

        return points;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getReferencePoints(com.tarantulus.archimedes.model.Rectangle)
     */
    public Collection<ReferencePoint> getReferencePoints (Rectangle area) {

        Collection<ReferencePoint> references = new ArrayList<ReferencePoint>();
        try {
            Point first = points.get(0);
            Point last = points.get(points.size() - 1);
            references.add(new SquarePoint(first, first));
            references.add(new SquarePoint(last, last));
        }
        catch (NullArgumentException e) {
            // Should never reach this block
            e.printStackTrace();
        }
        for (int i = 1; i < points.size() - 1; i++) {
            Point point = points.get(i);
            if (point != null) {
                try {
                    references.add(new XPoint(point, point));
                }
                catch (NullArgumentException e) {
                    // Should never reach this block
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < points.size() - 1; i++) {
            if (points.get(i) != null) {
                try {
                    Collection<Point> segmentPoints = new ArrayList<Point>();

                    segmentPoints.add(points.get(i));
                    segmentPoints.add(points.get(i + 1));

                    Point midPoint = Geometrics.getMeanPoint(segmentPoints);

                    references.add(new TrianglePoint(midPoint, points));
                }
                catch (NullArgumentException e) {
                    // Should never reach this block
                    e.printStackTrace();
                }
            }
        }

        return references;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Offsetable#isPositiveDirection(com.tarantulus.archimedes.model.Point)
     */
    public boolean isPositiveDirection (Point point) {

        boolean result = false;
        try {
            int leftSide = 0;
            List<Line> lines = getLines();
            for (Line segment : lines) {
                Vector orthogonal = new Vector(segment.getInitialPoint(),
                        segment.getEndingPoint());
                orthogonal = orthogonal.getOrthogonalVector();
                orthogonal = Geometrics.normalize(orthogonal);

                Point meanPoint = Geometrics.getMeanPoint(segment
                        .getInitialPoint(), segment.getEndingPoint());
                Point helper = meanPoint.addVector(orthogonal);

                Line ray = new Line(point, helper);
                Collection<Point> crossings = this.getIntersectionWithLine(ray);

                int numberOfCrossings = 0;
                for (Point crossing : crossings) {
                    if (this.contains(crossing) && ray.contains(crossing)) {
                        numberOfCrossings++;
                    }
                }

                if (numberOfCrossings % 2 == 0) {
                    leftSide++;
                }
            }

            if (leftSide > lines.size() / 2) {
                result = true;
            }
        }
        catch (Exception e) {
            // Should not catch any exception
            e.printStackTrace();
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Offsetable#cloneWithDistance(double)
     */
    public Element cloneWithDistance (double distance)
            throws InvalidParameterException {

        Element result = null;
        List<Point> polyLine = new ArrayList<Point>();
        List<Line> bissectors;

        List<Line> segments = getLines();
        if (segments.size() == 1) {
            Line line = segments.get(0);
            Line copy = (Line) line.cloneWithDistance(distance);
            polyLine.add(copy.getInitialPoint());
            polyLine.add(copy.getEndingPoint());
        }
        else {
            bissectors = getBissectors();

            Line segment = (Line) segments.get(0).cloneWithDistance(distance);

            Collection<Point> intersections;
            try {
                intersections = segment.getIntersection(bissectors.get(0));
                Point lastPoint = intersections.iterator().next();
                polyLine.add(lastPoint);

                for (int i = 0; i < segments.size(); i++) {
                    Line currentSegment = segments.get(i);
                    currentSegment = (Line) currentSegment
                            .cloneWithDistance(distance);
                    // currentSegment = getCorrectOffset(lastPoint, distance,
                    // currentSegment);

                    intersections = currentSegment.getIntersection(bissectors
                            .get(i + 1));

                    Point newPoint = intersections.iterator().next();

                    Vector originalDirection = new Vector(currentSegment
                            .getInitialPoint(), currentSegment.getEndingPoint());
                    Vector newDirection = new Vector(lastPoint, newPoint);

                    double dotProduct = originalDirection
                            .dotProduct(newDirection);

                    if (dotProduct > 0.0) {

                        lastPoint = newPoint;
                        polyLine.add(lastPoint);
                    }
                    else {
                        throw new InvalidParameterException();
                    }
                }
            }
            catch (NullArgumentException e) {
                // Should never happen
                e.printStackTrace();
            }
        }

        try {
            result = new Polyline(polyLine);
        }
        catch (NullArgumentException e) {
            // Should never happen
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should not happen
            e.printStackTrace();
        }

        return result;
    }

    /**
     * @return The list of bissectors (and orthogonal lines) of the
     *         intersections of the polyline.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private List<Line> getBissectors () {

        // TODO Implementar
        return Collections.EMPTY_LIST;
    }

    /**
     * @return True if the polyline is closed, false otherwise
     */
    public boolean isClosed () {

        boolean closed = false;

        Point firstPoint = points.get(0);
        Point lastPoint = points.get(points.size() - 1);

        if (firstPoint.equals(lastPoint)) {
            closed = true;
        }

        return closed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Trimmable#trim(java.util.Collection,
     *      com.tarantulus.archimedes.model.Point)
     */
    public Collection<Element> trim (Collection<Element> references, Point click) {

        Collection<Element> trimResult = new ArrayList<Element>();
        Collection<Point> intersectionPoints = getIntersectionPoints(references);
        Point point = getPoints().get(0);
        SortedSet<ComparablePoint> sortedPointSet = getSortedPointSet(point,
                intersectionPoints);

        int clickSegment = getNearestSegment(click);
        Line line = getLines().get(clickSegment);
        Vector direction = new Vector(line.getInitialPoint(), line
                .getEndingPoint());
        Vector clickVector = new Vector(line.getInitialPoint(), click);
        PolyLinePointKey key = new PolyLinePointKey(clickSegment, direction
                .dotProduct(clickVector));

        ComparablePoint zero = null;
        ComparablePoint clickPoint = null;
        try {
            clickPoint = new ComparablePoint(click, key);
            zero = new ComparablePoint(point, new DoubleKey(0));
        }
        catch (NullArgumentException e) {
            // Should never reach
            e.printStackTrace();
        }

        sortedPointSet = sortedPointSet.tailSet(zero);
        SortedSet<ComparablePoint> negativeIntersections = sortedPointSet
                .headSet(clickPoint);
        SortedSet<ComparablePoint> positiveIntersections = sortedPointSet
                .tailSet(clickPoint);

        Point firstCut = null;
        Point secondCut = null;
        if (negativeIntersections.size() == 0
                && positiveIntersections.size() > 0) {
            firstCut = positiveIntersections.first().getPoint();
            secondCut = positiveIntersections.last().getPoint();
        }
        else if (positiveIntersections.size() == 0
                && negativeIntersections.size() > 0) {
            firstCut = negativeIntersections.first().getPoint();
            secondCut = negativeIntersections.last().getPoint();
        }
        else if (negativeIntersections.size() > 0
                && positiveIntersections.size() > 0) {
            firstCut = positiveIntersections.first().getPoint();
            secondCut = negativeIntersections.last().getPoint();
        }

        Collection<Polyline> polyLines = this.split(firstCut, secondCut);
        for (Polyline polyLine : polyLines) {
            boolean clicked = false;
            try {
                clicked = polyLine.contains(click);
            }
            catch (NullArgumentException e) {
                // Should not happen
                e.printStackTrace();
            }
            if ( !clicked) {
                polyLine.setLayer(getLayer());
                trimResult.add(polyLine);
            }
        }

        if (trimResult.size() == 2) {
            Iterator<Element> iterator = trimResult.iterator();
            Polyline poly1 = (Polyline) iterator.next();
            Polyline poly2 = (Polyline) iterator.next();
            List<Point> poly1Points = poly1.getPoints();
            List<Point> poly2Points = poly2.getPoints();
            Point firstPoly2 = poly2Points.get(0);
            Point lastPoly1 = poly1Points.get(poly1Points.size() - 1);
            if (lastPoly1.equals(firstPoly2)) {
                trimResult.clear();
                List<Point> points = poly1Points;
                points.remove(points.size() - 1);
                Point last = points.get(points.size() - 1);
                try {
                    double determinant = Geometrics.calculateDeterminant(last,
                            firstPoly2, poly2Points.get(1));
                    if (Math.abs(determinant) < Constant.EPSILON) {
                        poly2Points.remove(0);
                    }
                    points.addAll(poly2Points);
                    trimResult.add(new Polyline(points));
                }
                catch (NullArgumentException e) {
                    // Should not happen
                    e.printStackTrace();
                }
                catch (InvalidArgumentException e) {
                    // Ignores it
                }
            }
        }

        return trimResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.PointSortable#getSortedPointSet(com.tarantulus.archimedes.model.Point,
     *      java.util.Collection)
     */
    public SortedSet<ComparablePoint> getSortedPointSet (Point referencePoint,
            Collection<Point> intersectionPoints) {

        SortedSet<ComparablePoint> sortedPointSet = new TreeSet<ComparablePoint>();
        List<Line> lines = getLines();

        Point firstPoint = points.get(0);
        boolean invertOrder = !firstPoint.equals(referencePoint);

        for (Point intersection : intersectionPoints) {
            try {
                int i = getIntersectionIndex(invertOrder, intersection);

                if (i < lines.size()) {
                    ComparablePoint point = generateComparablePoint(
                            intersection, invertOrder, i);
                    sortedPointSet.add(point);
                }
            }
            catch (NullArgumentException e) {
                // Should never happen
                e.printStackTrace();
            }
        }

        return sortedPointSet;
    }

    /**
     * Looks for a segment that contains the intersection, if none is found
     * tries to discover an extension that contains it.
     * 
     * @param invertOrder
     *            true if the order is to be inversed, false otherwise.
     * @param intersection
     *            The intersection point
     * @return The index of the segment that contains the intersection. If no
     *         segment contains the intersection three things can happen:
     *         <LI> return 0 if the intersection is in the extension of the
     *         first segment
     *         <LI> return the index of the last segment if the intersection is
     *         in the extension of the last segment
     *         <LI> the number of segments (which is an invalid index).
     * @throws NullArgumentException
     *             Thrown if something is null.
     */
    private int getIntersectionIndex (boolean invertOrder, Point intersection)
            throws NullArgumentException {

        List<Line> lines = getLines();
        int i;
        for (i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (line.contains(intersection)) {
                break;
            }
        }
        if (i >= lines.size()) {
            Line line = null;
            int extendableSegment = 0;
            if ( !invertOrder) {
                try {
                    line = new Line(points.get(1), points.get(0));
                }
                catch (InvalidArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else {
                Point beforeLast = points.get(points.size() - 2);
                Point last = points.get(points.size() - 1);
                try {
                    line = new Line(beforeLast, last);
                }
                catch (InvalidArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                extendableSegment = lines.size() - 1;
            }

            if (line.contains(intersection)) {
                i = extendableSegment;
            }
        }
        return i;
    }

    /**
     * Generates a comparable point with a polyline key to this point.
     * 
     * @param point
     *            The point to be used to generate the Comparable one.
     * @param invertOrder
     *            true if the order is to be inversed, false otherwise.
     * @param i
     *            The segment number that contains the point.
     * @throws NullArgumentException
     *             Thrown if something is null.
     */
    private ComparablePoint generateComparablePoint (Point point,
            boolean invertOrder, int i) throws NullArgumentException {

        List<Line> lines = getLines();
        Line line = lines.get(i);
        Point initialPoint = line.getInitialPoint();
        Point endingPoint = line.getEndingPoint();
        int segmentNumber = i;
        if (invertOrder) {
            Point temp = initialPoint;
            initialPoint = endingPoint;
            endingPoint = temp;
            segmentNumber = (lines.size() - 1) - segmentNumber;
        }
        Vector direction = new Vector(initialPoint, endingPoint);

        ComparablePoint element = null;
        try {
            Vector pointVector = new Vector(initialPoint, point);
            PolyLinePointKey key = new PolyLinePointKey(segmentNumber,
                    direction.dotProduct(pointVector));
            element = new ComparablePoint(point, key);
        }
        catch (NullArgumentException e) {
            // Should not reach this block
            e.printStackTrace();
        }
        return element;
    }

    /**
     * Gets all the proper intersections of the collection of references with
     * this element. The initial point and the ending point are not considered
     * intersections.
     * 
     * @param references
     *            A collection of references
     * @return A collection of proper intersections points
     */
    private Collection<Point> getIntersectionPoints (
            Collection<Element> references) {

        Collection<Point> intersectionPoints = new ArrayList<Point>();

        for (Element element : references) {
            try {
                if (element != this) {
                    Collection<Point> inter = element.getIntersection(this);
                    for (Point point : inter) {
                        if (this.contains(point)
                                && element.contains(point)
                                && !this.points.get(0).equals(point)
                                && !this.points.get(points.size() - 1).equals(
                                        point)) {
                            intersectionPoints.add(point);
                        }
                    }
                }
            }
            catch (NullArgumentException e) {
                // Should never catch this exception
                e.printStackTrace();
            }
        }

        return intersectionPoints;
    }

    /**
     * @param point
     *            The point
     * @return The index of the nearest segment to the point or -1 if the point
     *         is null.
     */
    public int getNearestSegment (Point point) {

        int result = -1;
        double minDist = Double.POSITIVE_INFINITY;

        List<Line> lines = getLines();

        for (int i = 0; i < lines.size(); i++) {
            Line segment = lines.get(i);
            try {
                double dist;
                Point projection = segment.getProjectionOf(point);

                if (projection == null) {
                    double distToInitial = Geometrics.calculateDistance(point,
                            segment.getInitialPoint());
                    double distToEnding = Geometrics.calculateDistance(point,
                            segment.getEndingPoint());

                    dist = Math.min(distToInitial, distToEnding);
                }
                else {
                    dist = Geometrics.calculateDistance(point, projection);
                }

                if (dist < minDist) {
                    result = i;
                    minDist = dist;
                }
            }
            catch (NullArgumentException e) {
                // Should not happen
                e.printStackTrace();
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.FilletableElement#fillet(com.tarantulus.archimedes.model.Point,
     *      com.tarantulus.archimedes.model.Point)
     */
    public Element fillet (Point intersection, Point direction) {

        int intersectionSegment = getPointSegment(intersection);
        int directionSegment = getNearestSegment(direction);
        List<Point> newPoints = new ArrayList<Point>();

        if (intersectionSegment >= 0) {
            boolean toEnd = false;
            if (intersectionSegment == directionSegment) {
                Vector toEndVector = new Vector(intersection, points
                        .get(intersectionSegment + 1));
                Vector directionVector = new Vector(intersection, direction);

                if (toEndVector.dotProduct(directionVector) > 0) {
                    toEnd = true;
                }
            }
            else if (intersectionSegment < directionSegment) {
                toEnd = true;
            }

            newPoints.add(intersection);
            if (toEnd) {
                for (int i = intersectionSegment + 1; i < points.size(); i++) {
                    newPoints.add(points.get(i));
                }
            }
            else {
                for (int i = intersectionSegment; i >= 0; i--) {
                    newPoints.add(points.get(i));
                }
            }
        }
        else {
            try {
                boolean extendFirstSegment = Math.abs(Geometrics
                        .calculateDeterminant(points.get(0), points.get(1),
                                intersection)) < Constant.EPSILON;
                boolean extendLastSegment = Math.abs(Geometrics
                        .calculateDeterminant(points.get(points.size() - 2),
                                points.get(points.size() - 1), intersection)) < Constant.EPSILON;
                if (extendFirstSegment && extendLastSegment) {
                    double distToFirst = Geometrics.calculateDistance(
                            direction, points.get(0));
                    double distToLast = Geometrics.calculateDistance(direction,
                            points.get(points.size() - 1));
                    if (distToFirst <= distToLast) {
                        extendLastSegment = false;
                    }
                    else {
                        extendFirstSegment = false;
                    }
                }
                if (extendFirstSegment) {
                    Vector firstSegmentVector = new Vector(points.get(1),
                            points.get(0));
                    Vector intersectionVector = new Vector(points.get(1),
                            intersection);
                    if (firstSegmentVector.dotProduct(intersectionVector) > 0) {
                        newPoints.add(intersection);
                        for (int i = 1; i < points.size(); i++) {
                            newPoints.add(points.get(i));
                        }
                    }
                }
                else if (extendLastSegment) {
                    Vector lastSegmentVector = new Vector(points.get(points
                            .size() - 2), points.get(points.size() - 1));
                    Vector intersectionVector = new Vector(points.get(points
                            .size() - 2), intersection);
                    if (lastSegmentVector.dotProduct(intersectionVector) > 0) {
                        newPoints.add(intersection);
                        for (int i = points.size() - 2; i >= 0; i--) {
                            newPoints.add(points.get(i));
                        }
                    }
                }
            }
            catch (NullArgumentException e) {
                // Should not reach this block
                e.printStackTrace();
            }
        }

        Polyline newPolyLine = null;
        if (newPoints.size() > 1) {
            try {
                newPolyLine = new Polyline(newPoints);
                newPolyLine.setLayer(parentLayer);
            }
            catch (NullArgumentException e) {
                // Should never reach this block
                e.printStackTrace();
            }
            catch (InvalidArgumentException e) {
                // Should never reach this block
                e.printStackTrace();
            }
        }
        return newPolyLine;
    }

    /**
     * @param point
     * @return Return the index of the segment that contains the point or -1 if
     *         the polyline does not contain the point.
     */
    public int getPointSegment (Point point) {

        int index = -1;

        List<Line> lines = getLines();
        for (int i = 0; i < lines.size(); i++) {
            try {
                if (lines.get(i).contains(point)) {
                    index = i;
                }
            }
            catch (NullArgumentException e) {
                e.printStackTrace();
            }
        }

        return index;
    }

    public String toString () {

        String string = "PolyLine: "; //$NON-NLS-1$
        for (Point point : points) {
            string += " " + point.toString(); //$NON-NLS-1$
        }
        return string;
    }

    public Element join (Element element) {

        return this;
    }

    public Element joinWithLine (Line line) {

        List<Point> newPoints = new ArrayList<Point>(points);
        List<Line> lines = getLines();
        if (points.get(0).equals(line.getInitialPoint())) {
            newPoints.add(0, line.getEndingPoint());
        }
        else if (points.get(0).equals(line.getEndingPoint())) {
            newPoints.add(0, line.getInitialPoint());
        }
        else if (points.get(points.size() - 1).equals(line.getInitialPoint())) {
            newPoints.add(line.getEndingPoint());
        }
        else if (points.get(points.size() - 1).equals(line.getEndingPoint())) {
            newPoints.add(line.getInitialPoint());
        }
        else if (line.isCollinearWith(lines.get(0))) {
            try {
                double distToStart = Geometrics.calculateDistance(
                        points.get(0), line.getInitialPoint());
                double distToEnd = Geometrics.calculateDistance(points.get(0),
                        line.getEndingPoint());

                Point point = points.get(0);
                if (distToStart < distToEnd) {
                    point.setX(line.getEndingPoint().getX());
                    point.setY(line.getEndingPoint().getY());
                }
                else {
                    point.setX(line.getInitialPoint().getX());
                    point.setY(line.getInitialPoint().getY());
                }
            }
            catch (NullArgumentException e) {
                // Should not reach this block
                e.printStackTrace();
            }
        }
        else if (line.isCollinearWith(lines.get(lines.size() - 1))) {
            try {
                double distToStart = Geometrics.calculateDistance(points
                        .get(points.size() - 1), line.getInitialPoint());
                double distToEnd = Geometrics.calculateDistance(points
                        .get(points.size() - 1), line.getEndingPoint());

                Point point = points.get(points.size() - 1);
                if (distToStart < distToEnd) {
                    point.setX(line.getEndingPoint().getX());
                    point.setY(line.getEndingPoint().getY());
                }
                else {
                    point.setX(line.getInitialPoint().getX());
                    point.setY(line.getInitialPoint().getY());
                }
            }
            catch (NullArgumentException e) {
                // Should not reach this block
                e.printStackTrace();
            }
        }

        Polyline result = null;
        try {
            result = new Polyline(newPoints);
        }
        catch (NullArgumentException e) {
            // Should not reach this block
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should nor reach this block
            e.printStackTrace();
        }

        return result;
    }

    public boolean isJoinableWith (Element element) {

        JoinableElement joinableElement = (JoinableElement) element;
        return joinableElement.isJoinableWith(this);
    }

    public Element joinWithPolyLine (Polyline polyLine) {

        List<Point> newPoints = new ArrayList<Point>();
        List<Point> otherPoints = polyLine.getPoints();

        List<Line> lines = getLines();
        List<Line> otherLines = polyLine.getLines();

        if (lines.get(0).isCollinearWith(otherLines.get(0))) {
            for (int i = otherPoints.size() - 1; i > 0; i--) {
                newPoints.add(otherPoints.get(i));
            }
            for (int i = 1; i < points.size(); i++) {
                newPoints.add(points.get(i));
            }
        }
        else if (lines.get(0).isCollinearWith(
                otherLines.get(otherLines.size() - 1))) {
            for (int i = 0; i < otherPoints.size() - 1; i++) {
                newPoints.add(otherPoints.get(i));
            }
            for (int i = 1; i < points.size(); i++) {
                newPoints.add(points.get(i));
            }
        }
        else if (lines.get(lines.size() - 1).isCollinearWith(otherLines.get(0))) {
            for (int i = 0; i < points.size() - 1; i++) {
                newPoints.add(points.get(i));
            }
            for (int i = 1; i < otherPoints.size(); i++) {
                newPoints.add(otherPoints.get(i));
            }
        }
        else if (lines.get(lines.size() - 1).isCollinearWith(
                otherLines.get(otherLines.size() - 1))) {
            for (int i = 0; i < points.size() - 1; i++) {
                newPoints.add(points.get(i));
            }
            for (int i = otherPoints.size() - 2; i >= 0; i--) {
                newPoints.add(otherPoints.get(i));
            }
        }
        else if (points.get(0).equals(otherPoints.get(0))) {
            for (int i = otherPoints.size() - 1; i > 0; i--) {
                newPoints.add(otherPoints.get(i));
            }
            for (int i = 0; i < points.size(); i++) {
                newPoints.add(points.get(i));
            }
        }
        else if (points.get(0).equals(otherPoints.get(otherPoints.size() - 1))) {
            for (int i = 0; i < otherPoints.size() - 1; i++) {
                newPoints.add(otherPoints.get(i));
            }
            for (int i = 0; i < points.size(); i++) {
                newPoints.add(points.get(i));
            }
        }
        else if (points.get(points.size() - 1).equals(otherPoints.get(0))) {
            for (int i = 0; i < points.size() - 1; i++) {
                newPoints.add(points.get(i));
            }
            for (int i = 0; i < otherPoints.size(); i++) {
                newPoints.add(otherPoints.get(i));
            }
        }
        else if (points.get(points.size() - 1).equals(
                otherPoints.get(otherPoints.size() - 1))) {
            for (int i = 0; i < points.size(); i++) {
                newPoints.add(points.get(i));
            }
            for (int i = otherPoints.size() - 1; i >= 0; i--) {
                newPoints.add(otherPoints.get(i));
            }
        }

        Polyline result = null;
        try {
            result = new Polyline(newPoints);
        }
        catch (NullArgumentException e) {
            // Should not reach this block
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should nor reach this block
            e.printStackTrace();
        }

        return result;
    }

    public boolean isCollinearWith (Element element) {

        // TODO Implementar
        return false;
    }

    public boolean isCollinearWithLine (Line line) {

        boolean isCollinear = false;

        Line segment1 = getLines().get(0);
        Line segment2 = getLines().get(getLines().size() - 1);
        Line segment3 = line;

        if (segment1 != null && segment2 != null && segment3 != null) {
            Line line1 = null;
            Line line2 = null;
            try {
                line1 = new Line(points.get(1), points.get(0));
                line2 = new Line(points.get(points.size() - 2), points
                        .get(points.size() - 1));

                if (line1.contains(line.getInitialPoint())
                        && line1.contains(line.getEndingPoint())) {
                    isCollinear = true;
                }
                else if (line2.contains(line.getInitialPoint())
                        && line2.contains(line.getEndingPoint())) {
                    isCollinear = true;
                }
            }
            catch (NullArgumentException e) {
                // Should nor reach this block
                e.printStackTrace();
            }
            catch (InvalidArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return isCollinear;

    }

    public boolean isCollinearWithPolyLine (Polyline polyLine) {

        boolean isCollinear = false;

        isCollinear = polyLine.isCollinearWithLine(getLines().get(0))
                || polyLine.isCollinearWith(getLines().get(
                        getLines().size() - 1));

        return isCollinear;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.elements.Element#isParallelTo(com.tarantulus.archimedes.model.elements.Element)
     */
    public boolean isParallelTo (Element element) {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.elements.Element#getNearestExtremePoint(com.tarantulus.archimedes.model.Point)
     */
    @Override
    public Point getNearestExtremePoint (Point point)
            throws NullArgumentException {

        int nearestSegmentIndex = getNearestSegment(point);
        List<Line> lines = getLines();
        Line nearestSegment = lines.get(nearestSegmentIndex);
        Point projection = nearestSegment.getProjectionOf(point);
        List<Point> perimetersPoints = new ArrayList<Point>();
        for (int i = 0; i <= nearestSegmentIndex; i++) {
            perimetersPoints.add(lines.get(i).getInitialPoint());
        }
        perimetersPoints.add(projection);

        List<Point> secondPerimetersPoints = new ArrayList<Point>();
        secondPerimetersPoints.add(projection);
        for (int i = nearestSegmentIndex; i < lines.size(); i++) {
            secondPerimetersPoints.add(lines.get(i).getEndingPoint());
        }
        double firstPerimeter = -1;
        double secondPerimeter = -1;
        Point initialPoint = points.get(0);
        Point endingPoint = points.get(points.size() - 1);
        try {
            firstPerimeter = Geometrics.calculatePerimeter(perimetersPoints);
        }
        catch (Exception e) {
            firstPerimeter = Geometrics.calculateDistance(initialPoint,
                    projection);
        }
        try {
            secondPerimeter = Geometrics
                    .calculatePerimeter(secondPerimetersPoints);
        }
        catch (Exception e) {
            secondPerimeter = Geometrics.calculateDistance(endingPoint,
                    projection);
        }

        Point nearestPoint = initialPoint;
        if (firstPerimeter > secondPerimeter) {
            nearestPoint = endingPoint;
        }

        return nearestPoint;
    }

    /**
     * Cuts the polyline in the specified points. Returns the resulting
     * polylines ordered from the initial point to the ending point.
     * 
     * @param firstCut
     *            The point of the first cut
     * @param secondCut
     *            The point of the second cut
     * @return The resulting polylines ordered from the initial point to the
     *         ending point.
     */
    public Collection<Polyline> split (Point firstCut, Point secondCut) {

        Collection<Polyline> polyLines = new ArrayList<Polyline>();

        int firstSegment = getNearestSegment(firstCut);
        int lastSegment = getNearestSegment(secondCut);

        double distToSecond = 0;
        double distToFirst = 0;
        if (firstSegment == lastSegment) {
            Point tmpPoint = points.get(firstSegment);
            try {
                distToFirst = Geometrics.calculateDistance(tmpPoint, firstCut);
                distToSecond = Geometrics
                        .calculateDistance(tmpPoint, secondCut);
            }
            catch (NullArgumentException e) {
                // Should not happen
                e.printStackTrace();
            }
        }

        if (firstSegment > lastSegment || distToFirst > distToSecond) {
            Point tmpPoint = firstCut;
            firstCut = secondCut;
            secondCut = tmpPoint;

            int tmpInt = firstSegment;
            firstSegment = lastSegment;
            lastSegment = tmpInt;
        }

        Polyline firstLine = null;
        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i <= firstSegment; i++) {
            points.add(getPoints().get(i));
        }
        points.add(firstCut);
        try {
            firstLine = new Polyline(points);
        }
        catch (Exception e) {
            // Can happen if the first cut is the initialPoint.
        }

        Polyline middleLine = null;
        points = new ArrayList<Point>();
        points.add(firstCut);
        for (int i = firstSegment + 1; i <= lastSegment; i++) {
            points.add(getPoints().get(i));
        }
        points.add(secondCut);
        try {
            middleLine = new Polyline(points);
        }
        catch (Exception e) {
            // Will happen if the first and the second cut are the same.
        }

        Polyline lastLine = null;
        points = new ArrayList<Point>();
        points.add(secondCut);
        for (int i = lastSegment + 1; i < getPoints().size(); i++) {
            points.add(getPoints().get(i));
        }
        try {
            lastLine = new Polyline(points);
        }
        catch (Exception e) {
            // Can happen if the second cut is the final point.
        }

        if (isClosed() && firstLine != null && lastLine != null) {
            points = lastLine.getPoints();
            points.addAll(firstLine.getPoints());

            try {
                Polyline polyLine = new Polyline(points);
                polyLines.add(polyLine);
            }
            catch (Exception e) {
                // Should never happen
                e.printStackTrace();
            }
            if (middleLine != null) {
                polyLines.add(middleLine);
            }
        }
        else {
            if (firstLine != null) {
                polyLines.add(firstLine);
            }
            if (middleLine != null) {
                polyLines.add(middleLine);
            }
            if (lastLine != null) {
                polyLines.add(lastLine);
            }
        }

        return polyLines;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.FilletableElement#getFilletSegment(com.tarantulus.archimedes.model.Point,
     *      com.tarantulus.archimedes.model.Point)
     */
    public Line getFilletSegment (Point intersection, Point click) {

        Line segment = null;

        try {
            Point firstPoint = points.get(0);
            Point secondPoint = points.get(1);

            Point beforeLast = points.get(points.size() - 2);
            Point lastPoint = points.get(points.size() - 1);

            boolean startCollinear = (Math.abs(Geometrics.calculateDeterminant(
                    firstPoint, secondPoint, intersection)) < Constant.EPSILON);
            boolean endCollinear = (Math.abs(Geometrics.calculateDeterminant(
                    beforeLast, lastPoint, intersection)) < Constant.EPSILON);

            if (this.contains(intersection)) {
                int intersectionSegment = getPointSegment(intersection);
                int clickSegment = getNearestSegment(click);

                boolean toStart = true;

                if (intersectionSegment == clickSegment) {
                    Vector segmentDirection = new Vector(points
                            .get(intersectionSegment), points
                            .get(intersectionSegment + 1));
                    Vector filletDirection = new Vector(intersection, click);

                    double dotProduct = segmentDirection
                            .dotProduct(filletDirection);

                    if (dotProduct > 0) {
                        toStart = false;
                    }
                }
                else {
                    if (intersectionSegment < clickSegment) {
                        toStart = false;
                    }
                }

                if (toStart) {
                    if ( !intersection.equals(points.get(intersectionSegment))) {
                        segment = new Line(intersection, points
                                .get(intersectionSegment));
                    }
                }
                else {
                    if ( !intersection.equals(points
                            .get(intersectionSegment + 1))) {
                        segment = new Line(intersection, points
                                .get(intersectionSegment + 1));
                    }
                }
            }
            else {
                if (startCollinear) {
                    segment = new Line(firstPoint, intersection);
                }
                else if (endCollinear) {
                    segment = new Line(lastPoint, intersection);
                }
            }
        }
        catch (NullArgumentException e) {
            // Should never happen
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should never happen
            e.printStackTrace();
        }

        return segment;
    }

    /**
     * @see br.org.archimedes.model.Element#draw(br.org.archimedes.gui.opengl.OpenGLWrapper)
     */
    @Override
    public void draw (OpenGLWrapper wrapper) {

        for (Line line : getLines()) {
            line.draw(wrapper);
        }
    }

    /**
     * @see br.org.archimedes.model.Element#intersects(br.org.archimedes.model.Rectangle)
     */
    @Override
    public boolean intersects (Rectangle rectangle)
            throws NullArgumentException {

        for (Line line : getLines()) {
            if (line.intersects(rectangle)) {
                return true;
            }
        }
        return false;
    }
}