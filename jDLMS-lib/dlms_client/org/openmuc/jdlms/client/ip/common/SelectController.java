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
package org.openmuc.jdlms.client.ip.common;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SelectController extends Thread {

	private static SelectController instance;

	private final List<ISelectableChannel> registeredChannel = new LinkedList<ISelectableChannel>();
	private final List<ISelectableChannel> toRegister = new LinkedList<ISelectableChannel>();

	private SelectController() {
		setDaemon(true);
		setName("IP listening Thread");
		// TODO create TcpServerListener as ISelectableChannel and register an
		// instance here to support incoming connection for server side code
	}

	public static SelectController getInstance() {
		if (instance == null) {
			instance = new SelectController();
			instance.start();
		}
		return instance;
	}

	public void registerChannel(ISelectableChannel channel) {
		toRegister.add(channel);
	}

	public void removeChannel(ISelectableChannel channel) {
		toRegister.remove(channel);
		registeredChannel.remove(channel);
	}

	@Override
	public void run() {
		Selector select;

		try {
			select = Selector.open();
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
			instance = null;
			return;
		}

		while (Thread.interrupted() == false) {
			try {
				// Register channels added after last select
				for (ISelectableChannel channel : toRegister) {
					channel.registerSelector(select);
					registeredChannel.add(channel);
				}
				toRegister.clear();

				select.select(1000);
				Set<SelectionKey> keys = select.selectedKeys();

				Iterator<ISelectableChannel> channelIter = registeredChannel.iterator();

				// Iterate until all channels have been checked or the set of
				// selected keys is empty
				while (channelIter.hasNext() && keys.isEmpty() == false) {
					ISelectableChannel channel = channelIter.next();
					Iterator<SelectionKey> iter = keys.iterator();

					while (iter.hasNext()) {
						SelectionKey key = iter.next();
						if (channel.isRightKey(key)) {
							iter.remove();
							channel.processSelection(key);
						}
					}
				}
			} catch (IOException e) {
				//TODO logger.debug("Error on select");
				//TODO LoggingHelper.logStackTrace(e, logger);
			}
		}
	}
}
