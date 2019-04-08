/*
 * (C) Copyright 2015-2019 Nuxeo (http://nuxeo.com/) and contributors.
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
 *     Delbosc Benoit
 */
package org.nuxeo.drive.bench

import io.gatling.core.Predef._
import io.gatling.core.config.GatlingFiles
import io.gatling.http.Predef._

import scala.io.Source

object Setup {

  def run = (userCount: Integer) => {

    feed(Feeders.admins)
      .exec(Actions.createGroupIfNotExists(Constants.GAT_GROUP_NAME)).exitHereIfFailed
      .exec(Actions.createDocumentIfNotExistsAsAdmin(Constants.ROOT_WORKSPACE_PATH, Constants.GAT_WS_NAME, "Workspace")).exitHereIfFailed
      .exec(Actions.grantReadWritePermission(Constants.GAT_WS_PATH, Constants.GAT_GROUP_NAME)).exitHereIfFailed
      .exec(Actions.createDocumentIfNotExistsAsAdmin(Constants.GAT_WS_PATH, Constants.GAT_FOLDER_NAME, "Folder")).exitHereIfFailed
      .repeat(userCount.intValue(), "count") {
      feed(Feeders.usersQueue)
        .feed(Feeders.deviceId)
        .exec(Actions.createUserIfNotExists(Constants.GAT_GROUP_NAME))
        .exec(Actions.createDocumentIfNotExists(Constants.GAT_WS_PATH, Constants.GAT_USER_FOLDER_NAME, "Folder"))
        .exec(Actions.synchronyzeFolder(Constants.GAT_WS_PATH + "/" + Constants.GAT_USER_FOLDER_NAME))
        .exec(Actions.synchronyzeFolder(Constants.GAT_WS_PATH + "/" + Constants.GAT_FOLDER_NAME))
        .exec(Actions.getDriveToken())
    }
  }
}

object SetupRemoteScan {

  def run = (nbThreads: Integer, nbNodes: Integer, userCount: Integer) => {
    scenario("SetupRemoteScan")
      .feed(Feeders.admins)
      .exec(Actions.createGroupIfNotExists(Constants.GAT_GROUP_NAME)).exitHereIfFailed
      .exec(Actions.createDocumentIfNotExistsAsAdmin(Constants.ROOT_WORKSPACE_PATH, Constants.GAT_WS_NAME, "Workspace")).exitHereIfFailed
      .exec(Actions.grantReadWritePermission(Constants.GAT_WS_PATH, Constants.GAT_GROUP_NAME)).exitHereIfFailed
      .exec(NuxeoImporter.massImport(nbThreads, nbNodes))
      .exec(NuxeoImporter.waitForAsyncJobsAndESIndexation())
      .repeat(userCount.intValue(), "count") {
      feed(Feeders.usersQueue)
        .feed(Feeders.deviceId)
        .exec(Actions.createUserIfNotExists(Constants.GAT_GROUP_NAME))
        .exec(Actions.synchronyzeFolder(Constants.GAT_WS_PATH))
        // Let's imitate Drive server binding
        .exec(Actions.getDriveToken())
        .exec(Actions.fetchAutomationAPI())
        .exec(Actions.getClientUpdateInfo())
    }
  }
}

class Sim00SetupPolling extends Simulation {

  val httpProtocol = http
    .baseUrl(Parameters.getBaseUrl())
    .disableWarmUp
    .acceptEncodingHeader("gzip, deflate")
    .connectionHeader("keep-alive")

  val userCount = Source.fromFile(GatlingFiles.resourcesDirectory + "/data/users.csv").getLines.size - 1
  val scn = scenario("Setup").exec(Setup.run(userCount))

  Feeders.clearTokens()
  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))
}

class Sim30SetupRemoteScan extends Simulation {

  val httpProtocol = http
    .baseUrl(Parameters.getBaseUrl())
    .disableWarmUp
    .acceptEncodingHeader("gzip, deflate")
    .connectionHeader("keep-alive")
    .disableCaching // disabling Etag cache since If-None-Modified on GetClientUpdateInfo fails

  val userCount = Source.fromFile(GatlingFiles.resourcesDirectory + "/data/users.csv").getLines.size - 1
  val scn = SetupRemoteScan.run(Parameters.getNbThreads(12), Parameters.getNbNodes(100000), userCount)

  Feeders.clearTokens()
  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))
}
