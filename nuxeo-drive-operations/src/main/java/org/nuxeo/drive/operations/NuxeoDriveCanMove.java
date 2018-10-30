/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Antoine Taillefer <ataillefer@nuxeo.com>
 */
package org.nuxeo.drive.operations;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.drive.adapter.FileSystemItem;
import org.nuxeo.drive.adapter.RootlessItemException;
import org.nuxeo.drive.service.FileSystemItemManager;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.runtime.api.Framework;

/**
 * Checks if the document backing the {@link FileSystemItem} with the given source id can be moved to the document
 * backing the {@link FileSystemItem} with the given destination id.
 *
 * @author Antoine Taillefer
 */
@Operation(id = NuxeoDriveCanMove.ID, category = Constants.CAT_SERVICES, label = "Nuxeo Drive: Can move", description = "Check if the document backing the file system item with the given source id can be moved to the document backing the file system item with the given destination id." //
        + " Return the result as a JSON blob.")
public class NuxeoDriveCanMove {

    private static final Log log = LogFactory.getLog(NuxeoDriveCanMove.class);

    public static final String ID = "NuxeoDrive.CanMove";

    @Context
    protected OperationContext ctx;

    @Param(name = "srcId", description = "Id of the source file system item.")
    protected String srcId;

    @Param(name = "destId", description = "Id of the destination file system item.")
    protected String destId;

    @OperationMethod
    public Blob run() throws IOException {
        boolean canMove = false;
        try {
            FileSystemItemManager fileSystemItemManager = Framework.getLocalService(FileSystemItemManager.class);
            canMove = fileSystemItemManager.canMove(srcId, destId, ctx.getPrincipal());
        } catch (RootlessItemException e) {
            // can happen if srcId or destId no longer match a document under an
            // active sync root: just return false in that case.
            if (log.isDebugEnabled()) {
                log.debug(String.format("Cannot move %s to %s: %s", srcId, destId, e.getMessage()), e);
            }
        }
        return NuxeoDriveOperationHelper.asJSONBlob(canMove);
    }

}
