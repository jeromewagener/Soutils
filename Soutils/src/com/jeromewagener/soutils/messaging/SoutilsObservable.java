/* The MIT License (MIT)

Copyright (c) 2012 Jerome Wagener

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.*/

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
