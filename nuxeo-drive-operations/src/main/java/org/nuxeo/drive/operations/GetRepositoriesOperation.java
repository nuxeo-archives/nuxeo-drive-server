/**
 *
 */

package org.nuxeo.drive.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;

/**
 * Returns the list of repository names.
 */
@Operation(id = GetRepositoriesOperation.ID, category = Constants.CAT_FETCH, label = "List repository names on the server", description = "Return the list of repository names.")
public class GetRepositoriesOperation {

    public static final String ID = "GetRepositories";

    @Context
    protected RepositoryManager repositoryManager;

    @OperationMethod
    public List<String> run() {
        List<String> repositoryNames = new ArrayList<String>(repositoryManager.getRepositoryNames());
        // Make order deterministic to make it simpler to write tests.
        Collections.sort(repositoryNames);
        return repositoryNames;
    }

}
