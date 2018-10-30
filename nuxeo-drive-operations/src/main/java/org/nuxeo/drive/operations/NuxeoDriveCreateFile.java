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

import javax.mail.internet.ParseException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.drive.adapter.FileItem;
import org.nuxeo.drive.adapter.FileSystemItem;
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
 * Creates a document from the input blob in the container backing the {@link FileSystemItem} with the given id.
 *
 * @author Antoine Taillefer
 */
@Operation(id = NuxeoDriveCreateFile.ID, category = Constants.CAT_SERVICES, label = "Nuxeo Drive: Create file", description = "Create a document from the input blob in the container backing the file system item with the given id." //
        + " Return the file system item backed by the created document as a JSON blob.")
public class NuxeoDriveCreateFile {

    public static final String ID = "NuxeoDrive.CreateFile";

    @Context
    protected OperationContext ctx;

    @Param(name = "parentId", description = "Id of the file system item backed by the parent container.")
    protected String parentId;

    @Param(name = "overwrite", required = false, description = "Optional, whether to overwrite an existing document with the same title.", values = "false")
    protected boolean overwrite;

    /**
     * @deprecated
     * @see https://jira.nuxeo.com/browse/NXP-12173
     */
    @Deprecated
    @Param(name = "name", required = false)
    protected String name;

    @OperationMethod
    public Blob run(Blob blob) throws ParseException, IOException {
        FileSystemItemManager fileSystemItemManager = Framework.getLocalService(FileSystemItemManager.class);
        // The filename transfered by the multipart encoding is not preserved
        // correctly if there is non ascii characters in it.
        if (StringUtils.isNotBlank(name)) {
            blob.setFilename(name);
        }
        NuxeoDriveOperationHelper.normalizeMimeTypeAndEncoding(blob);
        FileItem fileItem = fileSystemItemManager.createFile(parentId, blob, ctx.getPrincipal(), overwrite);
        return NuxeoDriveOperationHelper.asJSONBlob(fileItem);
    }

}
