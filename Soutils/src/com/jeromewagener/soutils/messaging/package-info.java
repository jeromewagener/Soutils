/** Contains all classes related to the messaging system of Soutils. In Soutils
 * everything is centered around the Observer pattern. Please give this pattern
 * a quick look if you don't know it already. (http://en.wikipedia.org/wiki/Observer_pattern) 
 * 
 * In a nutshell, all threads in charge of communicating, beaconing, filetransfering
 * are Observables (a.k.a. Subjects) to which Observers can register. If the observable
 * receives some new information, all registered observers will be notified using the
 * method provided via the SoutilsObserver interface. 
 * 
 * The messages itself are represented by the SoutilsMessage wrapper */

package com.jeromewagener.soutils.messaging;