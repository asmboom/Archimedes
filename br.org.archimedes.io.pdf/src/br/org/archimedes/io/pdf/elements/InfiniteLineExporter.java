/**
 * Copyright (c) 2008, 2009 Hugo Corbucci and others.<br>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html<br>
 * <br>
 * Contributors:<br>
 * Hugo Corbucci - initial API and implementation<br>
 * <br>
 * This file was created on 2008/06/23, 10:01:23, by Hugo Corbucci.<br>
 * It is part of package br.org.archimedes.io.pdf.elements on the br.org.archimedes.io.pdf project.<br>
 */

package br.org.archimedes.io.pdf.elements;

import java.io.IOException;
import java.util.List;

import br.org.archimedes.exceptions.NotSupportedException;
import br.org.archimedes.infiniteline.InfiniteLine;
import br.org.archimedes.interfaces.ElementExporter;
import br.org.archimedes.io.pdf.PDFWriterHelper;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Rectangle;

/**
 * Belongs to package br.org.archimedes.io.pdf.
 * 
 * @author night
 */
public class InfiniteLineExporter implements ElementExporter<InfiniteLine> {

    /*
     * (non-Javadoc)
     * @see br.org.archimedes.interfaces.ElementExporter#exportElement(br.org.archimedes
     * .model.Element, java.lang.Object)
     */
    public void exportElement (InfiniteLine infiniteLine, Object outputObject) throws IOException {

        PDFWriterHelper helper = (PDFWriterHelper) outputObject;
        Rectangle viewArea = helper.getModelArea();

        List<Point> crossing = infiniteLine.getPointsCrossing(viewArea);

        LinePointsExporter exporter = new LinePointsExporter(helper);
        exporter.exportLine(crossing);
    }

    public void exportElement (InfiniteLine element, Object outputObject, Rectangle boundingBox)
            throws IOException, NotSupportedException {

        throw new NotSupportedException();
    }
}
