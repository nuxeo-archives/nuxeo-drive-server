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
package org.nuxeo.drive.hierarchy.permission.factory;

import java.security.Principal;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.drive.adapter.FileSystemItem;
import org.nuxeo.drive.adapter.FolderItem;
import org.nuxeo.drive.hierarchy.permission.adapter.UserSyncRootParentFolderItem;
import org.nuxeo.drive.hierarchy.userworkspace.adapter.UserWorkspaceHelper;
import org.nuxeo.drive.service.FileSystemItemFactory;
import org.nuxeo.drive.service.FileSystemItemManager;
import org.nuxeo.drive.service.VirtualFolderItemFactory;
import org.nuxeo.drive.service.impl.AbstractFileSystemItemFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * User workspace based implementation of {@link FileSystemItemFactory} for the parent {@link FolderItem} of the user's
 * synchronization roots.
 *
 * @author Antoine Taillefer
 */
public class UserSyncRootParentFactory extends AbstractFileSystemItemFactory implements VirtualFolderItemFactory {

    private static final Log log = LogFactory.getLog(UserSyncRootParentFactory.class);

    protected static final String FOLDER_NAME_PARAM = "folderName";

    protected String folderName;

    /*------------------- AbstractFileSystemItemFactory ------------------- */
    @Override
    public void handleParameters(Map<String, String> parameters) {
        // Look for the "folderName" parameter
        String folderNameParam = parameters.get(FOLDER_NAME_PARAM);
        if (StringUtils.isEmpty(folderNameParam)) {
            throw new NuxeoException(String.format("Factory %s has no %s parameter, please provide one.", getName(),
                    FOLDER_NAME_PARAM));
        }
        folderName = folderNameParam;
    }

    @Override
    public boolean isFileSystemItem(DocumentModel doc, boolean includeDeleted, boolean relaxSyncRootConstraint) {
        // Check user workspace
        boolean isUserWorkspace = UserWorkspaceHelper.isUserWorkspace(doc);
        if (!isUserWorkspace) {
            if (log.isTraceEnabled()) {
                log.trace(String.format(
                        "Document %s is not a user workspace, it cannot be adapted as a FileSystemItem.", doc.getId()));
            }
            return false;
        }
        // Check "deleted" life cycle state
        if (!includeDeleted && LifeCycleConstants.DELETED_STATE.equals(doc.getCurrentLifeCycleState())) {
            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "Document %s is in the '%s' life cycle state, it cannot be adapted as a FileSystemItem.",
                        doc.getId(), LifeCycleConstants.DELETED_STATE));
            }
            return false;
        }
        return true;
    }

    @Override
    protected FileSystemItem adaptDocument(DocumentModel doc, boolean forceParentItem, FolderItem parentItem,
            boolean relaxSyncRootConstraint, boolean getLockInfo) {
        return new UserSyncRootParentFolderItem(getName(), doc, parentItem, folderName, relaxSyncRootConstraint,
                getLockInfo);
    }

    /*------------------- FileSystemItemFactory ------------------- */
    /**
     * Force parent item using {@link #getTopLevelFolderItem(Principal)}.
     */
    @Override
    public FileSystemItem getFileSystemItem(DocumentModel doc, boolean includeDeleted) {
        Principal principal = doc.getCoreSession().getPrincipal();
        return getFileSystemItem(doc, getTopLevelFolderItem(principal), includeDeleted);
    }

    @Override
    public FileSystemItem getFileSystemItem(DocumentModel doc, boolean includeDeleted, boolean relaxSyncRootConstraint) {
        Principal principal = doc.getCoreSession().getPrincipal();
        return getFileSystemItem(doc, getTopLevelFolderItem(principal), includeDeleted, relaxSyncRootConstraint);
    }

    @Override
    public FileSystemItem getFileSystemItem(DocumentModel doc, boolean includeDeleted, boolean relaxSyncRootConstraint,
            boolean getLockInfo) {
        Principal principal = doc.getCoreSession().getPrincipal();
        return getFileSystemItem(doc, getTopLevelFolderItem(principal), includeDeleted, relaxSyncRootConstraint,
                getLockInfo);
    }

    /*------------------- VirtualFolderItemFactory ------------------- */
    @Override
    public FolderItem getVirtualFolderItem(Principal principal) {
        RepositoryManager repositoryManager = Framework.getLocalService(RepositoryManager.class);
        // TODO: handle multiple repositories
        try (CoreSession session = CoreInstance.openCoreSession(repositoryManager.getDefaultRepositoryName(), principal)) {
            UserWorkspaceService userWorkspaceService = Framework.getLocalService(UserWorkspaceService.class);
            DocumentModel userWorkspace = userWorkspaceService.getCurrentUserPersonalWorkspace(session, null);
            if (userWorkspace == null) {
                throw new NuxeoException(String.format("No personal workspace found for user %s.", principal.getName()));
            }
            return (FolderItem) getFileSystemItem(userWorkspace);
        }
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    /*------------------- Protected ------------------- */
    protected FolderItem getTopLevelFolderItem(Principal principal) {
        FolderItem topLevelFolder = Framework.getLocalService(FileSystemItemManager.class).getTopLevelFolder(principal);
        if (topLevelFolder == null) {
            throw new NuxeoException("Found no top level folder item. Please check your "
                    + "contribution to the following extension point:"
                    + " <extension target=\"org.nuxeo.drive.service.FileSystemItemAdapterService\""
                    + " point=\"topLevelFolderItemFactory\">.");
        }
        return topLevelFolder;
    }

}
