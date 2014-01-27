Soutils
=======
Soutils is a miniature Java socket networking utility framework for Java enabled devices *(Android included)*. This project might interest you in case you want to quickly prototype applications that require non-blocking / threaded:
* TCP client-server communications
* UDP beaconing / broadcasting
* File transfers between different devices

How to use Soutils?
--------
One of the best resources to learn about Soutils is its Javadoc documentation. This API documentation
along with the code snippets from below should suffice to get you started.

Furthermore, a sample desktop and Android demo application can be found in the repository. These demo
applications make use of almost all of the main Soutils features.

By design, Soutils is kept small and simple to allow for easier prototyping and extension.

Code Snippets
--------
This section provides some working Soutils snippets. They should give you an indication on how straight forward it is to use Soutils in order to:

```Java
// establish a communication server...
CommunicationManager communicationManager = new CommunicationManager(4242, observer)
communication.start()

// connect from any Java enabled device to this remote communication server...
Communication communication = new Communication("192.168.1.42", 4242, observer)
communication.start()

// sending or receiving a messages from the client...
communication.sendMessage("Hello World");

// or from the server...
communicationManager.sendMessage("192.168.1.21", "Hello to you too!");
communicationManager.sendMessageToAllConnectedPeers("Hello everybody!");

// broadcasting beacons to all devices within the network...
BeaconSender beaconSender = new BeaconSender("Hello Beacon", "192.168.178.255", 2121, observer);
beaconSender.start();

// or offering to download files...
FileTransferServer fileTransferServer = new FileTransferServer(pathToMyFile, 8484, observer);
fileTransferServer.start();

// that can be downloaded by file transfer clients...
FileTransferClient fileTransferClient = new FileTransferClient(downloadLocation, "192.168.1.42", 8484, observer);
fileTransferClient.start();

// creating universal communication, beaconing or file transfer observers...
public class MyBeaconListener implements SoutilsObserver {
   @Override
   public void handleSoutilsMessage(SoutilsMessage soutilsMessage) {
       System.out.println("New beacon received: " + soutilsMessage.getContent());
   }
}

// and registering them with different observables
BeaconReceiver beaconReceiver = new BeaconReceiver(2121, new MyBeaconListener());

```
Good to know
--------
It might help to know that classes implementing `SoutilsObservable` are wrapped Java threads to which one or more `SoutilsObservers` can register.
For example: The `BeaconReceiver` extends `SoutilsObservable` which wraps a thread that continously listens for incomming beacons (UDP messages). Every registered `SoutilsObserver` will be notified as soon as a beacon has been detected. 

Implementing the `SoutilsObserver` interface, and by registering to a `SoutilsObservable` is really all you need to do apart from starting the `SoutilsObserver` thread.

More observers can be added to the same observable using the register method defined within the abstract `SoutilsObservable` class.

License 
--------
Soutils is open-source and is distributed under the MIT license. If you decide to use Soutils in your projects, I would greatly appreciate any kind of feedback.

How can I contribute?
--------
If you want to contribute, just fork the repository, add your modifications and give me a pull request. I will do everything possible to include every kind of improvements.
