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
package org.openmuc.jdlms.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueHelper {
	public static <T> T waitPoll(BlockingQueue<T> queue, long timeout) throws InterruptedException {
		if (timeout > 0) {
			return queue.poll(timeout, TimeUnit.MILLISECONDS);
		}
		else {
			return queue.take();
		}
	}

}
