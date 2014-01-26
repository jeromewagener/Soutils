/** Contains all classes necessary for communicating between different devices. In order to establish
 * a communication, a CommunicationManager (server) must be up and running. One or more Communications (clients)
 * are then able to connect and sent messages to this server. The server is able to sent messages to
 * a single specific client, or all client alltogether. A message is represented by SoutilsMessage
 * @see com.jeromewagener.soutils.messaging.SoutilsMessage */

package com.jeromewagener.soutils.communication;