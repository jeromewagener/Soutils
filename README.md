Soutils
=======

**NOTE: Currently under development. Do not use yet!**

A miniature Java socket utilitiy framework for Desktops and Android based devices.

This project might interest you in if you want to quickly prototype:
* TCP client-server communications?
* UDP beaconing?
* File transfers between different devices?

using little effort in order to:

```Java
// establish a local server
CommunicationManager communicationManager = new CommunicationManager(4242)
communication.start()

// connect to a remote server
Communication communication = new Communication(192.168.1.42, 4242)
communication.start()

// send or receive a message
communication.sendMessage("Hello World");

// or beacons...
BeaconSender beaconSender = new BeaconSender("Hello Beacon", 192.168.178.255);
beaconSender.start();

```

In this case, Soutils provides all this *(and more)* using three simple and easy to use modules:
* Soutils-Common: The common parameters and code
* Soutils-Desktop: The desktop implementation
* Soutils-Android: The android implementation

**NOTE: Currently under development. Do not use yet!**
