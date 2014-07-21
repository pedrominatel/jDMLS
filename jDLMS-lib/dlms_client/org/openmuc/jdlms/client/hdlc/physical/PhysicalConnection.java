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
package org.openmuc.jdlms.client.hdlc.physical;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.UUID;

/**
 * Wrapper class around the actual SerialPort object, abstracting sending and
 * receiving of data.
 * 
 * @author Karsten Mueller-Bier
 */
public class PhysicalConnection implements IPhysicalConnection {

	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	protected static final int MESSAGE_READ_OK = 2;
	String tag = "debugging";
	private String deviceMAC = "";

	BluetoothAdapter btAdapter;
	BluetoothDevice btdevice;

	ConnectThread connectBt;
	ConnectedThread connectedThread;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Log.i(tag, "in handler");
			super.handleMessage(msg);
			switch (msg.what) {
			case SUCCESS_CONNECT:
				isClosed = false;
				connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
				connectedThread.start(); // start the read thread
				Log.i(tag, "connected"); // TODO Use R.string.xxx
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				serialEvent(readBuf);
				break;
			case MESSAGE_READ_OK:

				break;
			}
		}
	};

	private IPhysicalConnectionListener listener = null;

	private boolean isClosed;

	private final byte[] buffer = new byte[1024];

	public PhysicalConnection(String deviceMAC)
			throws TooManyListenersException {
		this.deviceMAC = deviceMAC;
		btdevice = btAdapter.getRemoteDevice(deviceMAC);
		connectBt = new ConnectThread(btdevice);
		isClosed = false;
	}

	@Override
	public void send(byte[] data) throws IOException {
		connectedThread.write(data);
	}

	@Override
	public void close() {
		if (isClosed == false) {
			connectedThread.cancel();
			connectBt.cancel();
			isClosed = true;
		}

	}

//	@Override
//	public void setSerialParams(int baud, int databits, int stopbits, int parity) {
//		// port.setSerialPortParams(baud, databits, stopbits, parity);
//		// port.enableReceiveTimeout(5);
//	}

	@Override
	public void registerListener(IPhysicalConnectionListener listener)
			throws TooManyListenersException {
		if (this.listener != null) {
			throw new TooManyListenersException();
		}
		this.listener = listener;
	}

	@Override
	public void removeListener() {
		listener = null;
	}

	/**
	 * Callback method when data is received from the wrapped SerialPort object
	 */
	public void serialEvent(byte[] buffer) {
		int dataLen = buffer.length;
		listener.dataReceived(buffer, dataLen);
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}

	private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;
			Log.i(tag, "construct");
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.i(tag, "get socket failed");

			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			btAdapter.cancelDiscovery();
			Log.i(tag, "connect - run");
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				Log.i(tag, "connect - succeeded");
			} catch (IOException connectException) {
				Log.i(tag, "connect failed");
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)

			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

}
