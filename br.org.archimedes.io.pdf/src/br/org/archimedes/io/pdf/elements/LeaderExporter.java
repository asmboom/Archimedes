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

import br.org.archimedes.Constant;
import br.org.archimedes.exceptions.NotSupportedException;
import br.org.archimedes.interfaces.ElementExporter;
import br.org.archimedes.io.pdf.PDFWriterHelper;
import br.org.archimedes.leader.Leader;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Rectangle;

import com.lowagie.text.pdf.PdfContentByte;

import java.io.IOException;

/**
 * Belongs to package br.org.archimedes.io.pdf.
 * 
 * @author Hugo Corbucci
 */
public class LeaderExporter implements ElementExporter<Leader> {

    /* (non-Javadoc)
     * @see br.org.archimedes.interfaces.ElementExporter#exportElement(br.org.archimedes.model.Element, java.lang.Object)
     */
    public void exportElement (Leader leader, Object outputObject) throws IOException,
            NotSupportedException {

        PDFWriterHelper helper = (PDFWriterHelper) outputObject;
        PdfContentByte cb = helper.getPdfContentByte();

        Point tip = leader.getPointer().getInitialPoint();
        LineExporter exporter = new LineExporter();
        exporter.exportElement(leader.getPointer(), outputObject);
        exporter.exportElement(leader.getTextBase(), outputObject);

        Point center = helper.modelToDocument(tip);
        float centerX = (float) center.getX();
        float centerY = (float) center.getY();
        float radius = (float) Constant.LEADER_RADIUS;
        cb.circle(centerX, centerY, radius);

        cb.closePathFillStroke();
    }

    /* (non-Javadoc)
     * @see br.org.archimedes.interfaces.ElementExporter#exportElement(br.org.archimedes.model.Element, java.lang.Object, br.org.archimedes.model.Rectangle)
     */
    public void exportElement (Leader element, Object outputObject, Rectangle boundingBox)
            throws IOException, NotSupportedException {

        throw new NotSupportedException();
    }
}
