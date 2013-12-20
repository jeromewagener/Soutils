package com.jeromewagener.soutils.messaging;

import java.util.ArrayList;
import java.util.List;

public abstract class SoutilsObservable extends Thread {
	/** A list of all Soutils observers that are observing the current thread. There will always be at least one observer per thread! */
	private List<SoutilsObserver> soutilsObservers = new ArrayList<SoutilsObserver>();
	
	/**
	 * Registers a message reception observer which is notified as soon as a message is received. The message can
	 * be fetched from the communications message queue or via the observer update.
	 * @param soutilsObserver the observer that should be registered
	 */
	public void registerSoutilsObserver(SoutilsObserver soutilsObserver) {
		soutilsObservers.add(soutilsObserver);
	}

	/**
	 * Remove a message reception observer
	 * @param the message reception observer to be removed
	 */
	public void removeSoutilsObserver(SoutilsObserver soutilsObserver) {
		soutilsObservers.remove(soutilsObserver);
	}
	
	public List<SoutilsObserver> getSoutilsObservers() {
		return soutilsObservers;
	}
	
	public void notifyAllObservers(SoutilsMessage soutilsMessage) {
		for (SoutilsObserver messageReceptionObserver : soutilsObservers) {
			messageReceptionObserver.handleSoutilsMessage(soutilsMessage);
		}
	}
}
