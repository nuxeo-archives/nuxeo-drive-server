/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Antoine Taillefer <ataillefer@nuxeo.com>
 */
package org.nuxeo.drive.service.adapter;

import org.nuxeo.drive.adapter.FileSystemItem;
import org.nuxeo.drive.adapter.FolderItem;
import org.nuxeo.drive.adapter.impl.DocumentBackedFolderItem;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Dummy folder implementation of a {@link FileSystemItem} for test purpose.
 *
 * @author Antoine Taillefer
 */
public class DummyFolderItem extends DocumentBackedFolderItem {

    public DummyFolderItem(String factoryName, DocumentModel doc) {
        super(factoryName, doc);
    }

    public DummyFolderItem(String factoryName, FolderItem parentItem, DocumentModel doc) {
        super(factoryName, parentItem, doc);
    }

    @Override
    public String getName() {
        return "Dummy folder with id " + docId;
    }

}
