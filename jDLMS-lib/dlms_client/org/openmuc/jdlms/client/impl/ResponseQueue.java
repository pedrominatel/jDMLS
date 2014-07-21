/*
 * Copyright 2012-13 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jdlms.client.impl;

import java.util.concurrent.LinkedBlockingDeque;

import org.openmuc.jdlms.util.QueueHelper;

public class ResponseQueue<E> {

	private final LinkedBlockingDeque<Entry<E>> queue = new LinkedBlockingDeque<Entry<E>>();

	public void put(int invokeId, E data) throws InterruptedException {
		queue.putFirst(new Entry<E>(invokeId, data));
	}

	public E poll(int invokeId, long timeout) throws InterruptedException {
		E result = null;

		while (result == null) {
			synchronized (queue) {
				Entry<E> tmp = QueueHelper.waitPoll(queue, timeout);
				if (tmp == null) {
					return null;
				}
				if (tmp.invokeId == invokeId) {
					result = tmp.data;
				}
				else {
					queue.putLast(tmp);
					Thread.sleep(10);
				}
			}
		}

		return result;
	}

	private class Entry<T> {
		private final int invokeId;
		private final T data;

		public Entry(int invokeId, T pdu) {
			this.invokeId = invokeId;
			this.data = pdu;
		}
	}
}
