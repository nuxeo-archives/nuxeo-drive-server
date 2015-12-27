/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.drive.service;

import java.security.Principal;

import org.nuxeo.drive.adapter.FolderItem;

/**
 * Interface for the classes contributed to the {@code topLevelFolderItemFactory} extension point of the
 * {@link FileSystemItemAdapterService}.
 * <p>
 * Allows to get a virtual {@link FolderItem} for a given user.
 *
 * @author Antoine Taillefer
 * @see XXX
 */
public interface VirtualFolderItemFactory extends FileSystemItemFactory {

    /**
     * Returns the (unique) virtual {@link FolderItem}.
     */
    FolderItem getVirtualFolderItem(Principal principal);

    String getFolderName();

    void setFolderName(String folderName);

}
